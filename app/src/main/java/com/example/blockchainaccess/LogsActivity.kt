package com.example.blockchainaccess


import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Scaffold
import java.text.SimpleDateFormat
import java.util.Locale
import android.util.Log
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.Card
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.NavType
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.runtime.*
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import androidx.compose.foundation.clickable
import com.example.blockchainaccess.ui.theme.BlockchainAccessTheme
import androidx.compose.foundation.lazy.LazyColumn // Correct import for LazyColumn
import androidx.compose.foundation.lazy.items // Correct import for LazyColumn items
import androidx.compose.ui.graphics.Color
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.ui.text.style.TextOverflow
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.net.URLDecoder

class LogsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockchainAccessTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) { innerPadding ->
                    AppNavigation(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun AppNavigation(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val navController = rememberNavController()

    // Ensure the chain is fetched at app startup
    LaunchedEffect(Unit) {
        val userId = SessionManager.getUserId(context) ?: return@LaunchedEffect
        val port = SessionManager.getPort(context).first()
        NetworkClient.getChain(userId, port, context)
    }

    val json = remember { Json { ignoreUnknownKeys = true } }

    NavHost(
        navController = navController,
        startDestination = "logs_list",
        modifier = modifier
    ) {
        composable("logs_list") {
            LogsScreen(
                context = context,
                onLogClick = { log ->
                    val jsonLog = json.encodeToString(NetworkClient.ChainBlock.serializer(), log)
                    navController.navigate(
                        "log_details/${URLEncoder.encode(jsonLog, StandardCharsets.UTF_8.toString())}"
                    )
                }
            )
        }

        composable(
            "log_details/{log_data}",
            arguments = listOf(navArgument("log_data") { type = NavType.StringType })
        ) { backStackEntry ->
            val logData = backStackEntry.arguments?.getString("log_data") ?: return@composable
            val log = json.decodeFromString(
                NetworkClient.ChainBlock.serializer(),
                URLDecoder.decode(logData, StandardCharsets.UTF_8.toString())
            )
            DetailsScreen(log)
        }
    }
}

@Composable
fun LogsScreen(
    context: Context,
    modifier: Modifier = Modifier,
    onLogClick: (NetworkClient.ChainBlock) -> Unit
) {
    // Collect the logs from DataStore as a state
    val logs by SessionManager.logsFlow(context).collectAsState(initial = emptyList())

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Blockchain Access Logs",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyColumn {
            items(logs) { log ->
                LogItem(log, onLogClick)
            }
        }
    }
}


@Composable
fun LogItem(log: NetworkClient.ChainBlock, onLogClick: (NetworkClient.ChainBlock) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onLogClick(log) }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            val formattedTime = try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
                val outputFormat = SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault())
                val date = inputFormat.parse(log.timestamp)
                outputFormat.format(date)
            } catch (e: Exception) {
                log.timestamp
            }

            Text(
                text = "Block #${log.block_height}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(text = "Timestamp: $formattedTime", color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))


            when (log) {
                is NetworkClient.AddUserEvent -> {
                    Text(text = "Event Type: User Added", fontWeight = FontWeight.SemiBold)
                    Text(text = "New User: ${log.new_user_id}")
                    Text(text = "Room: ${log.room}")
                    Text(text = "Yes Votes: ${log.voting_summary.yes_votes}")
                    Text(text = "No Votes: ${log.voting_summary.no_votes}")
                }
                is NetworkClient.RemoveUserEvent -> {
                    Text(text = "Event Type: User Removed", fontWeight = FontWeight.SemiBold)
                    Text(text = "Removed User: ${log.removed_user_id}")
                    Text(text = "Room: ${log.room}")
                    Text(text = "Yes Votes: ${log.voting_summary.yes_votes}")
                    Text(text = "No Votes: ${log.voting_summary.no_votes}")
                }
                is NetworkClient.AccessEvent -> {
                    Text(text = "Event Type: Access", fontWeight = FontWeight.SemiBold)
                    Text(text = "User ID: ${log.user_id}")
                    Text(text = "Room: ${log.room}")
                    Text(text = "Yes Votes: ${log.voting_summary.yes_votes}")
                    Text(text = "No Votes: ${log.voting_summary.no_votes}")
                }
                is NetworkClient.AddPubKeyEvent -> {
                    Text(text = "Event Type: Public Key Submission", fontWeight = FontWeight.SemiBold)
                    Text(text = "User ID: ${log.user_id}")
                    Text(text = "Public Key Added")
                }
            }
        }
    }
}

@Composable
fun DetailsScreen(log: NetworkClient.ChainBlock) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display abstract properties
        Text("Block Hash: ${log.block_hash ?: "N/A"}")
        Text("Previous Block Hash: ${log.previous_block_hash ?: "N/A"}")
        Spacer(Modifier.height(16.dp))

        when (log) {
            is NetworkClient.AddUserEvent -> {
                Text("Event Type: New User Added")
                Text("New User: ${log.new_user_id}")
                Text("Room: ${log.room}")
                Text("Yes Votes: ${log.voting_summary.yes_votes}")
                Text("No Votes: ${log.voting_summary.no_votes}")
                DisplayVotes(log.votes)
            }
            is NetworkClient.RemoveUserEvent -> {
                Text("Event Type: User Removed")
                Text("Removed User: ${log.removed_user_id}")
                Text("Room: ${log.room}")
                Text("Yes Votes: ${log.voting_summary.yes_votes}")
                Text("No Votes: ${log.voting_summary.no_votes}")
                DisplayVotes(log.votes)
            }
            is NetworkClient.AccessEvent -> {
                Text("Event Type: Access Granted")
                Text("User ID: ${log.user_id}")
                Text("Room: ${log.room}")
                Text("Yes Votes: ${log.voting_summary.yes_votes}")
                Text("No Votes: ${log.voting_summary.no_votes}")
                DisplayVotes(log.votes)
            }
            is NetworkClient.AddPubKeyEvent -> {
                Text("Event Type: Public Key Added")
                Text("User ID: ${log.user_id}")
                Text("Public Key: ${log.public_key}")
            }
        }
    }
}

@Composable
fun DisplayVotes(votes: List<NetworkClient.Vote>) {
    if (votes.isEmpty()) return
    Text(
        text = "Votes:",
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        modifier = Modifier.padding(vertical = 8.dp)
    )
    LazyColumn {
        items(votes) { vote ->
            VoteItem(vote)
        }
    }
}

@Composable
fun VoteItem(vote: NetworkClient.Vote) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            vote.node_id?.let { Text("Node ID: $it") }
            vote.voter_id?.let { Text("Voter ID: $it") }
            vote.candidate?.let { Text("Candidate: $it") }
            vote.room?.let { Text("Room: $it") }
            Text("Vote: ${vote.vote.uppercase(Locale.getDefault())}", fontWeight = FontWeight.SemiBold)
            Text("Signature: ${vote.signature}", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun LogsScreenPreview() {
    BlockchainAccessTheme {
        AppNavigation()
    }
}

