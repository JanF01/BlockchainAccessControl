package com.example.blockchainaccess

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
import kotlinx.coroutines.launch
import com.example.blockchainaccess.ui.theme.BlockchainAccessTheme
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.foundation.clickable



object NfcDataHolder {
    var userId: String = "unknown-user"
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

@Composable
fun UserScreen(name: String, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    var userId by remember { mutableStateOf("Loading...") }
    var username by remember { mutableStateOf("Loading...") }
    var clicked by remember { mutableStateOf(false) }
    // For now, we don’t store username — so it’s a placeholder

    LaunchedEffect(Unit) {
        val id = SessionManager.getUserId(context)
        userId = id ?: "Unknown"

        val name = SessionManager.getUsername(context)
        username = name ?: "Unknown User"
    }
    val gradient = if (clicked) {
        // Brighter gradient when clicked
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.9f)
            )
        )
    } else {
        // Default shaded blue gradient
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                MaterialTheme.colorScheme.secondary.copy(alpha = 0.5f)
            )
        )
    }

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
                .padding(24.dp)
        ) {

            Spacer(modifier = Modifier.height(10.dp))
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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp)) // clip the shape so background doesn't overflow
                        .background(gradient)
                        .clickable { clicked = !clicked
                                     saveUserIdForNfc(context, userId)
                                    val intent = Intent(context, MyHostApduService::class.java).apply {
                                        putExtra("NFC_USER_ID", userId)
                                    }
                                    context.startService(intent)
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
                            ),
                        contentAlignment = Alignment.Center
                    ){
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
                                context.startActivity(
                                    Intent(context, WhitelistActivity::class.java)
                                )
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
        }
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