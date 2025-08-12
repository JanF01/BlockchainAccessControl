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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.blockchainaccess.ui.theme.BlockchainAccessTheme
import kotlinx.coroutines.*
import android.widget.Toast

class CreateProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockchainAccessTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    CreateProfileScreen(
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
fun CreateProfileScreen(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repeatPassword by remember { mutableStateOf("") }
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
                    .background(Color.Black.copy(alpha = 0.6f)) // Adjust alpha to control darkness
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
                    text = "Create your profile, provided you are authorized by the system administrator.",
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
                TextField(
                    value = repeatPassword,
                    onValueChange = { repeatPassword = it },
                    label = { Text("Repeat Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth(0.88f)
                        .padding(vertical = 6.dp)
                )
                Spacer(modifier = Modifier.height(34.dp))
                Button(
                    onClick = { register(context, username,password,repeatPassword,coroutineScope) },
                    shape = RoundedCornerShape(50),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp),
                    modifier = Modifier
                        .fillMaxWidth(0.5f)
                        .padding(vertical = 8.dp)
                ) {
                    Text("Create Profile", color = Color.White)
                }
            }
        }
    }
}

@Preview
@Composable
fun PreviewCreateProfileScreen() {
    BlockchainAccessTheme {
        CreateProfileScreen("Android")
    }
}

fun register(
    context: Context,
    username: String,
    password: String,
    repeatPassword: String,
    coroutineScope: CoroutineScope
) {
    if (username.isBlank() || password.isBlank() || repeatPassword.isBlank()) {
        Toast.makeText(context, "All fields must be filled.", Toast.LENGTH_SHORT).show()
        return
    }

    if (password != repeatPassword) {
        Toast.makeText(context, "Passwords do not match.", Toast.LENGTH_SHORT).show()
        return
    }

    // Launch a coroutine to handle the asynchronous network request
    coroutineScope.launch(Dispatchers.IO) {
        // The network request is made here, on a background thread.
        val result = NetworkClient.createAndRegisterProfile(username, password)

        // Switch back to the main thread to update UI or navigate
        withContext(Dispatchers.Main) {
            result.fold(
                onSuccess = { message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()

                    // Here's where the network call has already completed successfully.
                    // Now, we can safely save the session and navigate.
                    // Note: The message from the network client contains the ID.
                    val id = message.substringAfter("ID: ")

                    SessionManager.saveSession(context, id, username)

                    val intent = Intent(context, UserActivity::class.java)
                    context.startActivity(intent)
                },
                onFailure = { error ->
                    // This block runs if the network call failed for any reason.
                    Toast.makeText(context, error.message ?: "Unknown error", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
}