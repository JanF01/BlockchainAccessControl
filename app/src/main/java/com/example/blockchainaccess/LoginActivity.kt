package com.example.blockchainaccess

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.background
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.content.Context
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.blockchainaccess.ui.theme.BlockchainAccessTheme
import kotlinx.coroutines.*
import android.widget.Toast

class LoginActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockchainAccessTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LoginScreen(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var username by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var selectedPort by rememberSaveable { mutableStateOf("8081") }
    var showPortDropdown by remember { mutableStateOf(false) }
    Scaffold {
        Box(
            modifier = modifier
                .fillMaxSize()
        ) {
            Image(
                painter = painterResource(id = R.drawable.wavy),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(alpha = 0.5f)
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 28.dp, start = 0.dp, bottom = 100.dp, end = 0.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.Start // Align text to the left
                ) {
                    Text(
                        text = "Welcome!",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.padding(28.dp, 0.dp, 0.dp, 0.dp)
                    )
                    Text(
                        text = "Sign in to your profile, provided you are authorized by the system administrator.",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 14.sp,
                        textAlign = TextAlign.Left,
                        modifier = Modifier.padding(28.dp, 8.dp, 10.dp, 50.dp)
                    )
                }
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(vertical = 6.dp)
                )

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(vertical = 6.dp)
                )
                Spacer(modifier = Modifier.height(34.dp))
                Button(
                    onClick = {  login(context, username,password,selectedPort, coroutineScope) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(vertical = 8.dp)
                ) {
                    Text("Sign in", color = Color.White)
                }
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Button(onClick = { showPortDropdown = true }) {
                    Text(text = "Port: $selectedPort")
                }
                DropdownMenu(
                    expanded = showPortDropdown,
                    onDismissRequest = { showPortDropdown = false }
                ) {
                    val ports = listOf("8081", "8082", "8083", "8084")
                    ports.forEach { port ->
                        DropdownMenuItem(
                            text = { Text(port) },
                            onClick = {
                                selectedPort = port
                                showPortDropdown = false
                                // Reset session when port changes
                                coroutineScope.launch {
                                    SessionManager.clearSession(context)
                                }
                                Toast.makeText(context, "Session reset. Please log in again.", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewLoginScreen() {
    BlockchainAccessTheme {
        LoginScreen("Android")
    }
}

fun login(
    context: Context,
    username: String,
    password: String,
    port: String,
    coroutineScope: CoroutineScope
) {
    if (username.isBlank() || password.isBlank()) {
        Toast.makeText(context, "All fields must be filled.", Toast.LENGTH_SHORT).show()
        return
    }


    coroutineScope.launch(Dispatchers.IO) {

        val result = NetworkClient.loginToProfile(username, password, port)


        withContext(Dispatchers.Main) {
            result.fold(
                onSuccess = { (id, whitelist) ->
                    Toast.makeText(context, "Logged in successfully!", Toast.LENGTH_SHORT).show()

                    SessionManager.saveSession(context, id, username, port)

                    SessionManager.saveWhitelist(context, whitelist)

                    val intent = Intent(context, UserActivity::class.java)
                    context.startActivity(intent)
                },
                onFailure = { error ->
                    Toast.makeText(context, error.message ?: "Unknown error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}