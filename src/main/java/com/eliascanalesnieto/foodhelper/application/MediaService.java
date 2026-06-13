package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.MediaRepository;
import com.eliascanalesnieto.foodhelper.domain.MediaUpload;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Base64;
import java.util.Iterator;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.MemoryCacheImageOutputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class MediaService {
    private static final String OUTPUT_CONTENT_TYPE = "image/jpeg";
    private static final float[] JPEG_QUALITIES = {0.82f, 0.72f, 0.62f, 0.52f, 0.42f, 0.35f};

    private final MediaRepository mediaRepository;

    @Value("${app.media.max-bytes:153600}")
    private int maxBytes;

    @Value("${app.media.max-dimension:1280}")
    private int maxDimension;

    public Media createOptimized(MediaUpload upload) {
        if (upload == null) {
            return null;
        }
        byte[] originalBytes = decode(upload.getBase64Data());
        BufferedImage originalImage = readImage(originalBytes);
        BufferedImage scaledImage = downscale(normalize(originalImage), maxDimension);
        byte[] optimizedBytes = compress(scaledImage);
        BufferedImage optimizedImage = readImage(optimizedBytes);

        return mediaRepository.create(Media.builder()
                .fileName(normalizeFileName(upload.getFileName()))
                .contentType(OUTPUT_CONTENT_TYPE)
                .sizeBytes(optimizedBytes.length)
                .width(optimizedImage.getWidth())
                .height(optimizedImage.getHeight())
                .data(optimizedBytes)
                .build());
    }

    public Media findById(Long id) {
        return mediaRepository.findById(id);
    }

    public void delete(Long id) {
        if (id != null) {
            mediaRepository.delete(id);
        }
    }

    private byte[] decode(String base64Data) {
        if (!StringUtils.hasText(base64Data)) {
            throw new IllegalArgumentException("Photo data is required");
        }
        try {
            return Base64.getDecoder().decode(base64Data);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Photo must be valid base64 data");
        }
    }

    private BufferedImage readImage(byte[] bytes) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(bytes));
            if (image == null) {
                throw new IllegalArgumentException("Unsupported image format");
            }
            return image;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid image data");
        }
    }

    private BufferedImage normalize(BufferedImage source) {
        if (source.getType() == BufferedImage.TYPE_INT_RGB) {
            return source;
        }
        BufferedImage normalized = new BufferedImage(source.getWidth(), source.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = normalized.createGraphics();
        graphics.setComposite(AlphaComposite.SrcOver);
        graphics.setColor(Color.WHITE);
        graphics.fillRect(0, 0, normalized.getWidth(), normalized.getHeight());
        graphics.drawImage(source, 0, 0, null);
        graphics.dispose();
        return normalized;
    }

    private BufferedImage downscale(BufferedImage image, int maxTargetDimension) {
        if (image.getWidth() <= maxTargetDimension && image.getHeight() <= maxTargetDimension) {
            return image;
        }
        double ratio = Math.min((double) maxTargetDimension / image.getWidth(), (double) maxTargetDimension / image.getHeight());
        int width = Math.max(1, (int) Math.round(image.getWidth() * ratio));
        int height = Math.max(1, (int) Math.round(image.getHeight() * ratio));
        return resize(image, width, height);
    }

    private byte[] compress(BufferedImage source) {
        BufferedImage current = source;
        byte[] best = null;

        for (int resizeAttempt = 0; resizeAttempt < 6; resizeAttempt++) {
            for (float quality : JPEG_QUALITIES) {
                byte[] candidate = writeJpeg(current, quality);
                if (best == null || candidate.length < best.length) {
                    best = candidate;
                }
                if (candidate.length <= maxBytes) {
                    return candidate;
                }
            }
            current = resize(
                    current,
                    Math.max(1, (int) Math.round(current.getWidth() * 0.85d)),
                    Math.max(1, (int) Math.round(current.getHeight() * 0.85d))
            );
        }
        return best;
    }

    private byte[] writeJpeg(BufferedImage image, float quality) {
        Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpeg");
        if (!writers.hasNext()) {
            throw new IllegalStateException("JPEG writer is not available");
        }
        ImageWriter writer = writers.next();
        try (ByteArrayOutputStream output = new ByteArrayOutputStream();
             MemoryCacheImageOutputStream imageOutput = new MemoryCacheImageOutputStream(output)) {
            writer.setOutput(imageOutput);
            ImageWriteParam params = writer.getDefaultWriteParam();
            params.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
            params.setCompressionQuality(quality);
            writer.write(null, new IIOImage(image, null, null), params);
            writer.dispose();
            return output.toByteArray();
        } catch (Exception ex) {
            writer.dispose();
            throw new IllegalStateException("Unable to compress image", ex);
        }
    }

    private BufferedImage resize(BufferedImage source, int width, int height) {
        BufferedImage resized = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.drawImage(source, 0, 0, width, height, null);
        graphics.dispose();
        return resized;
    }

    private String normalizeFileName(String fileName) {
        String baseName = StringUtils.hasText(fileName) ? fileName.trim() : "photo";
        int extensionIndex = baseName.lastIndexOf('.');
        if (extensionIndex > 0) {
            baseName = baseName.substring(0, extensionIndex);
        }
        String sanitized = baseName.replaceAll("[^A-Za-z0-9-_]+", "-").replaceAll("(^-+|-+$)", "");
        if (!StringUtils.hasText(sanitized)) {
            sanitized = "photo";
        }
        return sanitized + ".jpg";
    }
}
