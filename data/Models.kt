package com.example.recetascalc.data

import org.json.JSONArray
import org.json.JSONObject

class Ingredient(var name: String = "", var percent: Double = 0.0) {
    fun toJson() = JSONObject().put("name", name).put("percent", percent)
}

class Section(var name: String = "MASA", var ingredients: MutableList<Ingredient> = mutableListOf()) {
    fun toJson() = JSONObject()
        .put("name", name)
        .put("ingredients", JSONArray(ingredients.map { it.toJson() }))
}

class Recipe(
    var id: Long = System.currentTimeMillis(),
    var name: String = "",
    var sections: MutableList<Section> = mutableListOf(Section())
) {
    fun toJson() = JSONObject()
        .put("id", id)
        .put("name", name)
        .put("sections", JSONArray(sections.map { it.toJson() }))

    fun totalPercent() = sections.sumOf { s -> s.ingredients.sumOf { it.percent } }
}

class PantryItem(var name: String = "", var price: Double = 0.0) {
    fun toJson() = JSONObject().put("name", name).put("price", price)
}
