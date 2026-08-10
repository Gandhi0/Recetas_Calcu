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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.recetascalc.data.PantryItem
import com.example.recetascalc.data.Store

@Composable
fun PantryScreen(store: Store, onBack: () -> Unit) {
    var tick by remember { mutableStateOf(0) }
    val _t = tick
    var adding by remember { mutableStateOf(false) }
    var editing by remember { mutableStateOf<PantryItem?>(null) }
    var currency by remember { mutableStateOf(store.currency) }

    Scaffold(
        containerColor = Background,
        topBar = {
            Row(Modifier.padding(horizontal = 8.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Volver", tint = Accent) }
                Text("Mi despensa", fontSize = 22.sp, fontWeight = FontWeight.Bold)
            }
        }
    ) { pad ->
        Column(Modifier.padding(pad).padding(16.dp).verticalScroll(rememberScrollState())) {

            Text(
                "Guarda los precios de tus ingredientes para ver el coste de las recetas.",
                color = TextSec
            )

            Spacer(Modifier.height(14.dp))

            Surface(color = Card, shape = RoundedCornerShape(14.dp)) {
                Row(
                    Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Moneda", color = TextSec, modifier = Modifier.weight(1f))
                    TextField(
                        value = currency,
                        onValueChange = { currency = it; store.currency = it },
                        colors = transparentField(),
                        singleLine = true,
                        textStyle = TextStyle(color = Accent, fontWeight = FontWeight.Bold, textAlign = androidx.compose.ui.text.style.TextAlign.End),
                        modifier = Modifier.width(110.dp)
                    )
                }
            }

            Spacer(Modifier.height(14.dp))

            store.pantry.forEach { item ->
                key(item) {
                    Surface(color = Card, shape = RoundedCornerShape(16.dp), modifier = Modifier.padding(vertical = 6.dp)) {
                        Row(
                            Modifier.padding(horizontal = 18.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(item.name, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                                Text("${currency}${fmt(item.price)} · 1 kg", color = TextSec)
                            }
                            IconButton(onClick = { editing = item }) { Icon(Icons.Default.Edit, "Editar", tint = Accent) }
                            IconButton(onClick = { store.pantry.remove(item); store.savePantry(); tick++ }) {
                                Icon(Icons.Default.Delete, "Eliminar", tint = TextSec)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            TextButton(onClick = { adding = true }) {
                Icon(Icons.Default.Add, null, tint = Accent)
                Spacer(Modifier.width(6.dp))
                Text("Ingrediente", color = Accent)
            }
        }
    }

    if (adding || editing != null) {
        PantryDialog(
            initial = editing,
            onDismiss = { adding = false; editing = null },
            onSave = { n, p ->
                val e = editing
                if (e != null) { e.name = n; e.price = p } else store.pantry.add(PantryItem(n, p))
                store.savePantry()
                adding = false; editing = null; tick++
            }
        )
    }
}

@Composable
fun PantryDialog(initial: PantryItem?, onDismiss: () -> Unit, onSave: (String, Double) -> Unit) {
    var name by remember { mutableStateOf(initial?.name ?: "") }
    var price by remember { mutableStateOf(initial?.let { if (it.price == 0.0) "" else fmt(it.price) } ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Nuevo ingrediente" else "Editar ingrediente") },
        text = {
            Column {
                TextField(name, { name = it }, label = { Text("Nombre") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                TextField(
                    price, { price = it },
                    label = { Text("Precio por 1 kg") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = { TextButton(onClick = { onSave(name.trim(), parseDouble(price)) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancelar") } },
        containerColor = Card
    )
}
