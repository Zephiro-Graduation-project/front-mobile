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
import com.example.frontzephiro.models.StoreProduct

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
                        launchGardenActivity(context, storeProduct)
                        onDismiss()
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
