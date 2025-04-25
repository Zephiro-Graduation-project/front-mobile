package com.example.frontzephiro.activities

import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
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
import com.example.frontzephiro.models.StoreProduct
import com.example.frontzephiro.network.RetrofitClient
import kotlinx.coroutines.*

@Composable
fun StoreItemDetailDialog(
    storeProduct: StoreProduct,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val titulosFont = FontFamily(Font(R.font.titulos))
    val normalFont = FontFamily(Font(R.font.normal))

    val imageResId = context.resources.getIdentifier(
        storeProduct.imageName.replace(".png", ""), // quita extensión si la tiene
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
                    text = storeProduct.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = titulosFont
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$${storeProduct.price}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontFamily = titulosFont
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = storeProduct.description,
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

                                    if(storeProduct.kind == "Plant"){
                                        // Paso 1: Obtener la flor por ID
                                        val flower = service.getFlowerById(storeProduct.id)

                                        // Paso 2: Hacer la compra
                                        val response = service.buyFlower(userId, flower)
                                        Log.d("CompraDebug", "Flor comprada correctamente: $response")

                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "¡Has comprado ${flower.name}!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            launchGardenActivity(context, storeProduct)
                                            onDismiss()
                                        }
                                    } else if (storeProduct.kind == "Background"){
                                        // Paso 1: Obtener la flor por ID
                                        val background = service.getBackgroundById(storeProduct.id)

                                        // Paso 2: Hacer la compra
                                        val response = service.buyBackground(userId, background)
                                        Log.d("CompraDebug", "Fondo comprado correctamente: $response")

                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "¡Has comprado ${background.title}!",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            launchGardenActivity(context, storeProduct)
                                            onDismiss()
                                        }
                                    }

                                } catch (e: retrofit2.HttpException) {
                                    val errorCode = e.code()
                                    val errorBody = e.response()?.errorBody()?.string()

                                    Log.e("CompraError", "HTTP $errorCode\nCuerpo del error: $errorBody", e)

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
                    }
                ) {
                    Text(
                        "Comprar",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontFamily = titulosFont
                        )
                    )
                }


            }
        }
    }
}

private fun launchGardenActivity(context: Context, storeProduct: StoreProduct) {
    val intent = Intent(context, GardenMain::class.java).apply {
        putExtra("PLANTA_RES_ID", storeProduct.imageName)
        putExtra("PLANTA_NOMBRE", storeProduct.name)
    }
    context.startActivity(intent)
}
