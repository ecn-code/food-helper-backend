package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.MediaRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcMediaRepository implements MediaRepository {
    private final MediaCrudRepository mediaCrudRepository;

    @Override
    public Media create(Media media) {
        return toDomain(mediaCrudRepository.save(toEntity(media)));
    }

    @Override
    public Media findById(Long id) {
        return mediaCrudRepository.findById(id)
                .map(this::toDomain)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found"));
    }

    @Override
    public void delete(Long id) {
        mediaCrudRepository.deleteById(id);
    }

    private MediaEntity toEntity(Media media) {
        return new MediaEntity(
                media.getId(),
                media.getFileName(),
                media.getContentType(),
                media.getSizeBytes(),
                media.getWidth(),
                media.getHeight(),
                media.getData()
        );
    }

    private Media toDomain(MediaEntity entity) {
        return Media.builder()
                .id(entity.id())
                .fileName(entity.fileName())
                .contentType(entity.contentType())
                .sizeBytes(entity.sizeBytes())
                .width(entity.width())
                .height(entity.height())
                .data(entity.data())
                .build();
    }
}
