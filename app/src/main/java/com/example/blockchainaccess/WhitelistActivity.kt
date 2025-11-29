package com.example.blockchainaccess

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField // Correct import for OutlinedTextField
import androidx.compose.material3.Divider // Correct import for Divider
import androidx.compose.runtime.Composable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.ui.unit.dp
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import kotlinx.coroutines.launch
import com.example.blockchainaccess.ui.theme.BlockchainAccessTheme
import androidx.compose.foundation.lazy.LazyColumn // Correct import for LazyColumn
import androidx.compose.foundation.lazy.items // Correct import for LazyColumn items
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource

class WhitelistActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockchainAccessTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    WhitelistScreen(
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun WhitelistScreen(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // Collect the whitelist Flow as state. The UI will automatically
    // recompose whenever the whitelist changes.
    val whitelist by SessionManager.getWhitelist(context).collectAsState(initial = emptySet())
    val port by SessionManager.getPort(context).collectAsState(initial = "")

    // State for the text field
    var newId by remember { mutableStateOf("") }

            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Input section to add new addresses
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newId,
                        onValueChange = { newId = it },
                        label = { Text("New ID") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        if (newId.isNotBlank()) {
                            scope.launch {
                                SessionManager.addToWhitelist(context, newId, port)
                                newId = "" // Clear the text field
                            }
                        }
                    }) {
                        Text("Add")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // List of whitelisted ids
                if (whitelist.isEmpty()) {
                    Text("Whitelist is empty.")
                } else {
                    LazyColumn(modifier = Modifier.fillMaxWidth()) {
                        items(whitelist.toList()) { id ->
                            WhitelistItem(
                                id = id,
                                onRemove = {
                                    scope.launch {
                                        SessionManager.removeFromWhitelist(context, id, port)
                                    }
                                }
                            )
                            Divider()
                        }
                    }
                }
            }
}

@Composable
fun WhitelistItem(id: String, onRemove: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = id,
            modifier = Modifier.weight(1f)
        )
        IconButton(onClick = onRemove) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Remove ID",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}