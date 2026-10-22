package ru.kotocoffee.catalog.api

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.kotocoffee.catalog.application.CatalogService

@RestController
@RequestMapping("/api/menu")
@Tag(name = "Меню", description = "Базовые напитки и их настройки")
class CatalogController(private val catalogService: CatalogService) {

    @GetMapping
    @Operation(
        summary = "Меню",
        description = "Базовые напитки с размерами, температурами, лимитами модификаторов по категориям и самими модификаторами.",
    )
    fun getMenu(): List<MenuDrinkResponse> = catalogService.getMenu().map { MenuDrinkResponse.from(it) }

    @GetMapping("/drinks/{drinkId}")
    @Operation(summary = "Настройки напитка", description = "Доступные размеры, температуры, лимиты и модификаторы. 404 — напитка нет")
    fun getDrink(@PathVariable drinkId: Long): MenuDrinkResponse = MenuDrinkResponse.from(catalogService.getMenuItem(drinkId))
}
