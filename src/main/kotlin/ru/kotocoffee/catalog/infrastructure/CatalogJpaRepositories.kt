package ru.kotocoffee.catalog.infrastructure

import org.springframework.data.jpa.repository.JpaRepository

interface IngredientJpaRepository : JpaRepository<IngredientEntity, Long>

interface ModifierJpaRepository : JpaRepository<ModifierEntity, Long> {
    fun findAllByOrderById(): List<ModifierEntity>
}

interface BaseDrinkJpaRepository : JpaRepository<BaseDrinkEntity, Long> {
    fun findAllByOrderById(): List<BaseDrinkEntity>
}
