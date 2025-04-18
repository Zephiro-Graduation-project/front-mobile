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

@Composable
fun InventoryItemDialog(
    imageResId: Int,
    name: String,
    description: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    val titulosFont = FontFamily(Font(R.font.titulos))
    val normalFont = FontFamily(Font(R.font.normal))

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
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = titulosFont
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    fontFamily = normalFont
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        launchGardenActivity(context, imageResId, name)
                        onDismiss()
                    }
                ) {
                    Text("Sembrar",
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
