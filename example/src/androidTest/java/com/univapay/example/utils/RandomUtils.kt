package com.univapay.example.utils

import com.univapay.adapters.KonbiniAdapter
import java.util.*

val testCards: List<String> = listOf(
        "4485652694641345",
        "4485322190017495",
        "4024007151322855",
        "5407777867969728",
        "5495371574537334"
)

fun generateCardNumber(): String {
    return testCards.shuffled().first()
}

fun getRandomKonbini(): KonbiniAdapter {
    return KonbiniAdapter.values().toList().shuffled().first()
}

fun getRandomKonbiniPosition(): Int {
    return KonbiniAdapter.values().indexOf(getRandomKonbini())
}

fun getRandomCustomerID(): UUID {
    return UUID.randomUUID()
}
