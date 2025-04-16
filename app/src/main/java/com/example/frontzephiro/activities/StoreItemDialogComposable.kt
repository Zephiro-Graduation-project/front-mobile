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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.frontzephiro.models.Store_Item

@Composable
fun StoreItemDetailDialog(
    storeItem: Store_Item,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

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
                    painter = painterResource(id = storeItem.imageResId),
                    contentDescription = null,
                    modifier = Modifier
                        .height(160.dp)
                        .fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = storeItem.name,
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "$${storeItem.price}",
                    style = MaterialTheme.typography.bodyLarge
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = storeItem.description,
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        launchGardenActivity(context, storeItem)
                        onDismiss()
                    }
                ) {
                    Text("Comprar")
                }
            }
        }
    }
}

private fun launchGardenActivity(context: Context, storeItem: Store_Item) {
    val intent = Intent(context, GardenMain::class.java).apply {
        putExtra("PLANTA_RES_ID", storeItem.imageResId)
        putExtra("PLANTA_NOMBRE", storeItem.name)
    }
    context.startActivity(intent)
}
