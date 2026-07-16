package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(
        name = "MenuItemImportDestination",
        description = "Destination where an imported purchased item is applied",
        allowableValues = {"MENU_STOCK", "MONEY_BOX", "GLOBAL_STOCK"}
)
public enum MenuItemImportDestination {
    MENU_STOCK,
    MONEY_BOX,
    GLOBAL_STOCK
}
