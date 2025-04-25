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
import com.example.frontzephiro.api.GamificationApiService
import com.example.frontzephiro.models.Flower
import com.example.frontzephiro.models.InventoryProduct
import com.example.frontzephiro.network.RetrofitClient
import kotlinx.coroutines.*
import android.util.Log
import android.widget.Toast
import com.example.frontzephiro.models.Background
import java.text.Normalizer

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

                        val sharedPreferences = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                        val userId = sharedPreferences.getString("USER_ID", null)

                        if (userId != null) {
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    val service = RetrofitClient.getAuthenticatedGamificationClient(context)
                                        .create(GamificationApiService::class.java)

                                    if (inventoryProduct.kind == "Plant") {
                                        val flower = service.getFlowerById(inventoryProduct.id)
                                        launchGardenActivity(context, flower)
                                    } else if (inventoryProduct.kind == "Background") {
                                        val background = service.getBackgroundById(inventoryProduct.id)
                                        val backTitleOk = normalizarTexto(background.title)
                                        guardarFondoSeleccionado(context, backTitleOk)
                                        val intent = Intent(context, GardenMain::class.java)
                                        context.startActivity(intent)
                                    }

                                } catch (e: retrofit2.HttpException) {
                                    val errorCode = e.code()
                                    val errorBody = e.response()?.errorBody()?.string()

                                    Log.e("Inv_a_Jardin_Error", "HTTP $errorCode\nCuerpo del error: $errorBody", e)

                                    val mensajeError = when {
                                        errorCode == 404 -> "Fondo no encontrado. Verifica el ID y la URL del backend."
                                        errorBody?.contains("not have enough coins") == true -> "No tienes suficientes monedas."
                                        errorBody?.contains("already in the inventory") == true -> "Ya tienes este fondo."
                                        else -> "Error al realizar la compra. Código $errorCode"
                                    }

                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, mensajeError, Toast.LENGTH_LONG).show()
                                    }

                                } catch (e: Exception) {
                                    Log.e("CompraError", "Error inesperado al comprar", e)
                                    withContext(Dispatchers.Main) {
                                        Toast.makeText(context, "Error inesperado al comprar", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        } else {
                        Toast.makeText(context, "Usuario no autenticado", Toast.LENGTH_SHORT).show()
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

private fun launchGardenActivity(context: Context, flower: Flower) {
    val intent = Intent(context, GardenMain::class.java).apply {
        putExtra("FLOWER", flower) // Requiere que Flower sea Parcelable
    }
    context.startActivity(intent)
}


fun guardarFondoSeleccionado(context: Context, fondoNombre: String) {
    val prefs = context.getSharedPreferences("zephiro_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("fondo_jardin", fondoNombre).apply()
}

private fun normalizarTexto(texto: String): String {
    return Normalizer.normalize(texto, Normalizer.Form.NFD)
        .replace(Regex("\\p{InCombiningDiacriticalMarks}+"), "") // Quita tildes
        .replace('ñ', 'n') // Reemplaza ñ minúscula
        .replace('Ñ', 'n') // Reemplaza Ñ mayúscula también por minúscula n
        .lowercase() // Convierte a minúsculas
}

