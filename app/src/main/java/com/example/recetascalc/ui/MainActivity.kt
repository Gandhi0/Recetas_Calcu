package com.example.recetascalc

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.recetascalc.data.Store
import com.example.recetascalc.ui.AppTheme
import com.example.recetascalc.ui.PantryScreen
import com.example.recetascalc.ui.RecipeEditScreen
import com.example.recetascalc.ui.RecipeListScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val store = Store(this)
        setContent { AppTheme { AppNav(store) } }
    }
}

sealed interface Nav
object NavList : Nav
data class NavEdit(val id: Long?) : Nav
object NavPantry : Nav

@Composable
fun AppNav(store: Store) {
    var nav by remember { mutableStateOf<Nav>(NavList) }
    val current = nav
    when (current) {
        is NavList -> RecipeListScreen(
            store,
            onNew = { nav = NavEdit(null) },
            onOpen = { nav = NavEdit(it) },
            onPantry = { nav = NavPantry }
        )
        is NavEdit -> RecipeEditScreen(store, current.id) { nav = NavList }
        is NavPantry -> PantryScreen(store) { nav = NavList }
    }
}
