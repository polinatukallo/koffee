package ru.kotocoffee

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class KotoCoffeeApplication

fun main(args: Array<String>) {
    runApplication<KotoCoffeeApplication>(*args)
}
