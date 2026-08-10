package com.example.recetascalc.ui

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.FileDownload
import androidx.compose.material.icons.outlined.FileUpload
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetascalc.data.Recipe
import com.example.recetascalc.data.Store

@Composable
fun RecipeListScreen(
    store: Store,
    onNew: () -> Unit,
    onOpen: (Long) -> Unit,
    onPantry: () -> Unit
) {
    var tick by remember { mutableStateOf(0) }
    val _t = tick // lectura para forzar recomposición
    var query by remember { mutableStateOf("") }
    var toDelete by remember { mutableStateOf<Recipe?>(null) }
    val context = LocalContext.current

    val exportLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri != null) runCatching {
            context.contentResolver.openOutputStream(uri)
                ?.use { it.write(store.exportAll().toByteArray()) }
            Toast.makeText(context, "Recetas exportadas ✔", Toast.LENGTH_SHORT).show()
        }.onFailure { Toast.makeText(context, "Error al exportar", Toast.LENGTH_SHORT).show() }
    }

    val importLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) runCatching {
            val text = context.contentResolver.openInputStream(uri)
                ?.use { String(it.readBytes()) } ?: ""
            store.importAll(text)
            tick++
            Toast.makeText(context, "Recetas importadas ✔", Toast.LENGTH_SHORT).show()
        }.onFailure { Toast.makeText(context, "JSON no válido", Toast.LENGTH_SHORT).show() }
    }

    Scaffold(
        containerColor = Background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNew,
                containerColor = Accent,
                contentColor = Color.White,
                shape = RoundedCornerShape(28.dp)
            ) { Icon(Icons.Default.Add, null, Modifier.size(28.dp)) }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).fillMaxSize()) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Calculadora", fontSize = 30.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onPantry) { Icon(Icons.Outlined.Inventory2, "Mi despensa", tint = Accent) }
                IconButton(onClick = { exportLauncher.launch("recetas.json") }) { Icon(Icons.Outlined.FileDownload, "Exportar", tint = Accent) }
                IconButton(onClick = { importLauncher.launch(arrayOf("*/*")) }) { Icon(Icons.Outlined.FileUpload, "Importar", tint = Accent) }
            }

            Spacer(Modifier.height(12.dp))

            Surface(color = Card, shape = RoundedCornerShape(14.dp)) {
                Row(Modifier.padding(start = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Search, null, tint = TextSec)
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Buscar receta", color = TextSec) },
                        colors = transparentField(),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("${store.recipes.size} recetas", color = TextSec)
            Spacer(Modifier.height(8.dp))

            val list = store.recipes.filter { it.name.contains(query, ignoreCase = true) }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(list, key = { it.id }) { r ->
                    Surface(color = Card, shape = RoundedCornerShape(16.dp)) {
                        Row(
                            Modifier.padding(horizontal = 18.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                r.name.ifBlank { "(sin nombre)" },
                                fontSize = 18.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onOpen(r.id) }) { Icon(Icons.Default.Edit, "Editar", tint = Accent) }
                            IconButton(onClick = { toDelete = r }) { Icon(Icons.Default.Delete, "Eliminar", tint = TextSec) }
                        }
                    }
                }
            }
        }

        if (toDelete != null) {
            AlertDialog(
                onDismissRequest = { toDelete = null },
                confirmButton = {
                    TextButton(onClick = {
                        store.recipes.removeAll { it.id == toDelete!!.id }
                        store.saveRecipes()
                        toDelete = null
                        tick++
                    }) { Text("Eliminar", color = Danger) }
                },
                dismissButton = { TextButton(onClick = { toDelete = null }) { Text("Cancelar") } },
                title = { Text("Eliminar receta") },
                text = { Text("¿Eliminar \"${toDelete!!.name}\"?") },
                containerColor = Card
            )
        }
    }
}
