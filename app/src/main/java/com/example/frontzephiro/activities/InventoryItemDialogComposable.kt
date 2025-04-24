package com.example.frontzephiro.activities

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.frontzephiro.R
import com.example.frontzephiro.activities.GardenMain
import com.example.frontzephiro.models.InventoryProduct

@Composable
fun InventoryItemDialog(
    inventoryProduct: InventoryProduct,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val titulosFont = FontFamily(Font(R.font.titulos))
    val normalFont = FontFamily(Font(R.font.normal))

    val imageResId = context.resources.getIdentifier(
        inventoryProduct.imageName.replace(".png", ""), // quita extensión si la tiene
        "drawable",
        context.packageName
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = MaterialTheme.shapes.medium
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Image(
                    painter = painterResource(id = if (imageResId != 0) imageResId else R.drawable.logo_multimedia),
                    contentDescription = null,
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = inventoryProduct.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = titulosFont
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = inventoryProduct.description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = normalFont
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (inventoryProduct.kind == "Plant") {
                            launchGardenActivity(context, imageResId, inventoryProduct.name)
                        } else if (inventoryProduct.kind == "Background") {
                            guardarFondoSeleccionado(context, inventoryProduct.name)
                            val intent = Intent(context, GardenMain::class.java)
                            context.startActivity(intent)
                        }
                        onDismiss()
                    }
                ){
                    Text(
                        text = if (inventoryProduct.kind == "Plant") "Sembrar" else "Usar fondo",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = titulosFont
                        )
                    )
                }
            }
        }
    }
}

private fun launchGardenActivity(context: Context, imageResId: Int, name: String) {
    val intent = Intent(context, GardenMain::class.java).apply {
        putExtra("PLANTA_RES_ID", imageResId)
        putExtra("PLANTA_NOMBRE", name)
    }
    context.startActivity(intent)
}

fun guardarFondoSeleccionado(context: Context, fondoNombre: String) {
    val prefs = context.getSharedPreferences("zephiro_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("fondo_jardin", fondoNombre).apply()
}
