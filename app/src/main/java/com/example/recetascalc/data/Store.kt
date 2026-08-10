package com.example.recetascalc.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class Store(context: Context) {

    private val prefs = context.applicationContext.getSharedPreferences("data", Context.MODE_PRIVATE)

    var recipes: MutableList<Recipe> = mutableListOf()
    var pantry: MutableList<PantryItem> = mutableListOf()

    var currency: String
        get() = prefs.getString("currency", "PEN") ?: "PEN"
        set(v) = prefs.edit().putString("currency", v).apply()

    init {
        recipes = parseRecipes(prefs.getString("recipes", null)).toMutableList()
        pantry = parsePantry(prefs.getString("pantry", null)).toMutableList()
    }

    fun saveRecipes() = prefs.edit().putString(
        "recipes", JSONObject().put("recipes", JSONArray(recipes.map { it.toJson() })).toString()
    ).apply()

    fun savePantry() = prefs.edit().putString(
        "pantry", JSONObject().put("pantry", JSONArray(pantry.map { it.toJson() })).toString()
    ).apply()

    fun priceFor(name: String): Double? =
        pantry.firstOrNull { it.name.equals(name, ignoreCase = true) }?.price

    /** Exporta recetas + despensa en un solo JSON */
    fun exportAll(): String = JSONObject()
        .put("recipes", JSONArray(recipes.map { it.toJson() }))
        .put("pantry", JSONArray(pantry.map { it.toJson() }))
        .toString(2)

    /** Importa recetas + despensa (reemplaza todo) */
    fun importAll(text: String) {
        recipes = parseRecipes(text).toMutableList()
        pantry = parsePantry(text).toMutableList()
        saveRecipes(); savePantry()
    }

    private fun parseRecipes(text: String?): List<Recipe> {
        if (text.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONObject(text).optJSONArray("recipes") ?: return emptyList()
            (0 until arr.length()).map { i ->
                val r = arr.getJSONObject(i)
                Recipe(
                    id = r.optLong("id", System.currentTimeMillis()),
                    name = r.optString("name", ""),
                    sections = (r.optJSONArray("sections")?.let { s ->
                        (0 until s.length()).map { j ->
                            val so = s.getJSONObject(j)
                            Section(
                                name = so.optString("name", "MASA"),
                                ingredients = (so.optJSONArray("ingredients")?.let { a ->
                                    (0 until a.length()).map { k ->
                                        val io = a.getJSONObject(k)
                                        Ingredient(io.optString("name", ""), io.optDouble("percent", 0.0))
                                    }
                                } ?: emptyList()).toMutableList()
                            )
                        }
                    } ?: emptyList()).toMutableList()
                )
            }
        } catch (_: Exception) { emptyList() }
    }

    private fun parsePantry(text: String?): List<PantryItem> {
        if (text.isNullOrBlank()) return emptyList()
        return try {
            val arr = JSONObject(text).optJSONArray("pantry") ?: return emptyList()
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                PantryItem(o.optString("name", ""), o.optDouble("price", 0.0))
            }
        } catch (_: Exception) { emptyList() }
    }
}
