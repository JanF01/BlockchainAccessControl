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
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.lifecycle.lifecycleScope
import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.launch
import com.example.blockchainaccess.ui.theme.BlockchainAccessTheme
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.foundation.clickable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Locale

// Sealed class to manage the UI state of the access check
sealed class AccessCheckState {
    object Idle : AccessCheckState()
    object Checking : AccessCheckState()
    data class Granted(val room: String, val timestamp: String) : AccessCheckState()
    object Denied : AccessCheckState()
}

class UserActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Check session validity first
        lifecycleScope.launch {
            val isLoggedIn = SessionManager.isLoggedIn(this@UserActivity)
            if (!isLoggedIn) {
                startActivity(Intent(this@UserActivity, CreateProfileActivity::class.java))
                finish()
            }
        }

        setContent {
            BlockchainAccessTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    UserScreen(
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
fun UserScreen(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var userId by remember { mutableStateOf("Loading...") }
    var username by remember { mutableStateOf("Loading...") }
    var clicked by remember { mutableStateOf(false) }

    // State to track the access result
    var accessState by remember { mutableStateOf<AccessCheckState>(AccessCheckState.Idle) }

    val port by SessionManager.getPort(context).collectAsState(initial = "Loading...")

    LaunchedEffect(Unit) {
        val id = SessionManager.getUserId(context)
        userId = id ?: "Unknown"

        val name = SessionManager.getUsername(context)
        username = name ?: "Unknown User"
    }

    val gradient = if (clicked) {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
    }

    Box(
        modifier = modifier.fillMaxSize()
    ) {
        // Background Image
        Image(
            painter = painterResource(id = R.drawable.wavy),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.5f)
        )
        // Dark Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // User Info Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.secondary
                            )
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Column {
                    Text(
                        text = "Welcome, $username",
                        fontSize = 22.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Your ID: $userId",
                        fontSize = 16.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Main Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // NFC Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(gradient)
                        .clickable {
                            clicked = !clicked
                            accessState = AccessCheckState.Checking

                            // 1. Start NFC Service
                            saveUserIdForNfc(context, userId)
                            val intent = Intent(context, MyHostApduService::class.java).apply {
                                putExtra("NFC_USER_ID", userId)
                            }
                            context.startService(intent)

                            // 2. Launch Coroutine logic locally
                            coroutineScope.launch(Dispatchers.IO) {
                                // Attempt 1: Wait 4 seconds
                                delay(4000)
                                var result = performChainCheck(context, userId, port)

                                if (result is AccessCheckState.Granted) {
                                    withContext(Dispatchers.Main) { accessState = result }
                                    return@launch
                                }

                                // Attempt 2: Wait 2 more seconds (Total 6s)
                                delay(2000)
                                result = performChainCheck(context, userId, port)

                                withContext(Dispatchers.Main) {
                                    accessState = if (result is AccessCheckState.Granted) {
                                        result
                                    } else {
                                        AccessCheckState.Denied
                                    }
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.nfc),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(0.7f)
                            .graphicsLayer(alpha = 0.5f),
                        colorFilter = ColorFilter.tint(Color.White, BlendMode.SrcIn)
                    )
                }

                // Logs and Authority List Column
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                context.startActivity(
                                    Intent(context, LogsActivity::class.java)
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Logs",
                            fontSize = 18.sp,
                            color = Color.White,
                        )
                    }
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.secondary
                                    )
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val result = NetworkClient.requestForWhitelist(userId, port)
                                    withContext(Dispatchers.Main) {
                                        result.fold(
                                            onSuccess = { whitelist ->
                                                Toast.makeText(context, "Whitelist retrieved!", Toast.LENGTH_SHORT).show()
                                                SessionManager.saveWhitelist(context, whitelist)
                                                val intent = Intent(context, WhitelistActivity::class.java)
                                                context.startActivity(intent)
                                            },
                                            onFailure = { error ->
                                                Toast.makeText(context, error.message ?: "Unknown error", Toast.LENGTH_SHORT).show()
                                            }
                                        )
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Authority List",
                            fontSize = 18.sp,
                            color = Color.White,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Access Status Block
            when (val state = accessState) {
                is AccessCheckState.Granted -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFF4CAF50).copy(alpha = 0.8f), // Green
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Text(
                                text = "Access Granted",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Room: ${state.room}",
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Time: ${state.timestamp}",
                                fontSize = 14.sp,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }
                }
                is AccessCheckState.Denied -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = Color(0xFFE53935).copy(alpha = 0.8f), // Red
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Access Denied",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
                is AccessCheckState.Checking -> {
                    // Optional loading state text if desired
                }
                is AccessCheckState.Idle -> {
                    // No display
                }
            }
        }

        // Logout Button
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Button(onClick = { coroutineScope.launch {
                SessionManager.clearSession(context)
                val intent = Intent(context, CreateProfileActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                context.startActivity(intent)
            }  }) {
                Text(text = "Logout")
            }
        }
    }
}

/**
 * Standalone suspend function to check the chain.
 * Returns Granted or Denied/Idle based on the check.
 */
private suspend fun performChainCheck(
    context: Context,
    userId: String,
    port: String
): AccessCheckState {
    try {
        // 1. Request Chain Update
        NetworkClient.getChain(userId, port, context)

        // 2. Read the latest chain from SessionManager
        val chain = SessionManager.logsFlow(context).first()

        if (chain.isNotEmpty()) {
            val lastBlock = chain.last()

            // 3. Check if last block is AccessEvent AND belongs to current user
            if (lastBlock is NetworkClient.AccessEvent && lastBlock.user_id == userId) {
                val formattedTime = try {
                    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                    val outputFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    val date = inputFormat.parse(lastBlock.timestamp)
                    outputFormat?.format(date) ?: lastBlock.timestamp
                } catch (e: Exception) {
                    lastBlock.timestamp
                }

                return AccessCheckState.Granted(
                    room = lastBlock.room,
                    timestamp = formattedTime
                )
            }
        }
        return AccessCheckState.Idle // Not found yet
    } catch (e: Exception) {
        e.printStackTrace()
        return AccessCheckState.Idle // Error is treated as not found
    }
}

fun saveUserIdForNfc(context: Context, userId: String) {
    val prefs = context.getSharedPreferences("nfc_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("userId", userId).apply()
}

@Preview(showBackground = true)
@Composable
fun UserScreenPreview() {
    BlockchainAccessTheme {
        UserScreen("Android")
    }
}