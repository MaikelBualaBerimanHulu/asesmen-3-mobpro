package com.maikelhulu.asesmen3app.util

private val catNames = listOf(
    "Milo",
    "Luna",
    "Coco",
    "Oyen",
    "Mochi",
    "Nala",
    "Leo",
    "Mimi",
    "Simba",
    "Bella",
    "Chiko",
    "Molly",
    "Tama",
    "Kiko",
    "Lilo",
    "Neko",
    "Bimo",
    "Piko",
    "Snowy",
    "Miko"
)

fun catNameFor(id: String): String {
    if (id.isBlank()) return "Milo"
    val index = id.fold(0) { total, char -> total + char.code }.mod(catNames.size)
    return catNames[index]
}
