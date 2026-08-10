package com.example.recetascalc.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetascalc.data.Ingredient
import com.example.recetascalc.data.Recipe
import com.example.recetascalc.data.Section
import com.example.recetascalc.data.Store

@Composable
fun RecipeEditScreen(store: Store, recipeId: Long?, onBack: () -> Unit) {
    val recipe = remember {
        store.recipes.firstOrNull { it.id == recipeId }
            ?: Recipe(name = "", sections = mutableListOf(Section()))
    }
    val isNew = store.recipes.none { it.id == recipeId }
    var name by remember { mutableStateOf(recipe.name) }
    var weightText by remember { mutableStateOf("") }
    var tick by remember { mutableStateOf(0) }
    val _t = tick

    val weight = parseDouble(weightText)
    val totalPct = recipe.totalPercent()

    fun save() {
        recipe.name = name
        if (isNew && !store.recipes.contains(recipe)) store.recipes.add(recipe)
        store.saveRecipes()
        onBack()
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = Accent) }
                Spacer(Modifier.weight(1f))
                IconButton(onClick = ::save) { Icon(Icons.Default.Save, "Guardar", tint = Accent) }
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState())) {

            TextField(
                value = name,
                onValueChange = { name = it },
                placeholder = { Text("Nombre de tu receta", color = TextSec) },
                colors = transparentField(),
                textStyle = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))
            Text("INGREDIENTES", color = TextSec, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text("Cada ingrediente necesita un nombre y un % (porcentaje de panadero).", color = TextSec, fontSize = 13.sp)
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(color = Card, shape = RoundedCornerShape(12.dp)) {
                    TextField(
                        value = weightText,
                        onValueChange = { weightText = it },
                        label = { Text("Peso total (g)", color = TextSec) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = transparentField(),
                        singleLine = true,
                        modifier = Modifier.width(170.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                Surface(color = Field, shape = RoundedCornerShape(20.dp)) {
                    Text(
                        "Σ ${fmt(totalPct)}%",
                        color = Accent,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                    )
                }
            }

            recipe.sections.forEach { section ->
                Surface(color = Card, shape = RoundedCornerShape(18.dp), modifier = Modifier.padding(vertical = 8.dp)) {
                    Column(Modifier.padding(14.dp)) {

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Label, null, tint = Accent)
                            TextField(
                                value = section.name,
                                onValueChange = { section.name = it },
                                colors = transparentField(),
                                textStyle = TextStyle(color = Accent, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                                singleLine = true,
                                modifier = Modifier.weight(1f)
                            )
                            if (recipe.sections.size > 1) {
                                IconButton(onClick = { recipe.sections.remove(section); tick++ }) {
                                    Icon(Icons.Default.Delete, "Eliminar sección", tint = TextSec)
                                }
                            }
                        }

                        section.ingredients.forEach { ing ->
                            key(ing) {
                                IngredientRow(store, ing, weight, totalPct) {
                                    section.ingredients.remove(ing); tick++
                                }
                            }
                        }

                        TextButton(onClick = { section.ingredients.add(Ingredient()); tick++ }) {
                            Icon(Icons.Default.Add, null, tint = Accent)
                            Spacer(Modifier.width(6.dp))
                            Text("Ingrediente", color = Accent)
                        }
                    }
                }
            }

            TextButton(onClick = { recipe.sections.add(Section()); tick++ }) {
                Icon(Icons.Default.Add, null, tint = Accent)
                Spacer(Modifier.width(6.dp))
                Text("Sección", color = Accent)
            }

            if (weight > 0 && totalPct > 0) {
                val cost = recipe.sections.flatMap { it.ingredients }.sumOf { ing ->
                    val p = store.priceFor(ing.name) ?: return@sumOf 0.0
                    weight * ing.percent / totalPct / 1000 * p
                }
                Spacer(Modifier.height(12.dp))
                Surface(color = Card, shape = RoundedCornerShape(16.dp)) {
                    Row(Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Coste estimado", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Spacer(Modifier.weight(1f))
                        Text("${fmt(cost)} ${store.currency}", color = Accent, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
fun IngredientRow(
    store: Store,
    ing: Ingredient,
    totalWeight: Double,
    totalPct: Double,
    onDelete: () -> Unit
) {
    var pctText by remember(ing) { mutableStateOf(if (ing.percent == 0.0) "" else fmt(ing.percent)) }
    var nameText by remember(ing) { mutableStateOf(ing.name) }

    val grams = if (totalWeight > 0 && totalPct > 0) totalWeight * ing.percent / totalPct else null
    val price = store.priceFor(ing.name)

    Row(Modifier.padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Surface(color = Field, shape = RoundedCornerShape(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextField(
                    value = pctText,
                    onValueChange = { pctText = it; ing.percent = parseDouble(it) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = transparentField(),
                    singleLine = true,
                    textStyle = TextStyle(color = Accent, fontWeight = FontWeight.Bold, fontSize = 16.sp),
                    modifier = Modifier.width(72.dp)
                )
                Text("%", color = Accent, modifier = Modifier.padding(end = 10.dp))
            }
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            TextField(
                value = nameText,
                onValueChange = { nameText = it; ing.name = it },
                placeholder = { Text("Nombre del ingrediente", color = TextSec) },
                colors = transparentField(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            if (grams != null) {
                Row {
                    Text("${fmt(grams)} g", color = TextSec, fontSize = 13.sp)
                    if (price != null) {
                        Text("  ·  ${fmt(grams / 1000 * price)} ${store.currency}", color = Accent, fontSize = 13.sp)
                    }
                }
            }
        }
        IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, "Eliminar", tint = TextSec) }
    }
}
