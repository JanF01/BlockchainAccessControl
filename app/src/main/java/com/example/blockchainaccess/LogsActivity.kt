package com.example.blockchainaccess


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material3.Scaffold
import java.text.SimpleDateFormat
import java.util.Locale
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
import com.google.gson.Gson
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

    val rawJson = """
    [
        {
            "type": "add_user_event",
            "block_height": 1,
            "room": "RoomA",
            "timestamp": "2025-08-13T13:07:37Z",
            "previous_block_hash": "GENESIS",
            "votes": [
                {
                    "node_id": "UE1",
                    "vote": "yes",
                    "signature": "3046022100a33497f200af45b013ae2ec9f8d313817fcd2a0f0bd76d8f9e437443fe1d83f8022100bff19866ad894d6492b9312eec2e7c6c694f2d131493bd7989b4c52f39b3bfad"
                }
            ],
            "new_user_id": "UE1",
            "voting_summary": {
                "total_nodes": 1,
                "yes_votes": 1,
                "no_votes": 0
            },
            "added_to_chain": true,
            "aggregator_signature": "304402204d1a70ad5b72684613baa6a4c33b7169f8a68776ddadb7b939534d7b8ca884230220750ed2792e76adf961d2d2002d0f8d789d314f96fa4fe33d41bf8f688128b9d3",
            "block_hash": "56de21c8818b955dc6bfff66a8b6add54667cc41974ff349e8ae9c70471091"
        },
        {
            "type": "add_user_event",
            "block_height": 2,
            "room": "RoomA",
            "timestamp": "2025-08-13T13:07:43Z",
            "previous_block_hash": "56de21c8818b955dc6bfff66a8b6add54667cc41974ff349e8ae9c70471091",
            "votes": [
                {
                    "node_id": "UE1",
                    "vote": "yes",
                    "signature": "30440220638fb7c358b9fa1084cd91b292d8b7a7b427e47ecca8fca30b70b07b59203937022021baa189a40afd07a222a16195832da86474f676c1699a6a2d7b8a7e50c2a93b"
                }
            ],
            "new_user_id": "UE2",
            "voting_summary": {
                "total_nodes": 1,
                "yes_votes": 1,
                "no_votes": 0
            },
            "added_to_chain": true,
            "aggregator_signature": "304502210087b3ab92c507a77d589ccb430310355b6b40ab44119c38bbd06530d8032836bd0220722c48f28320bb9510362d729786fa2afc6f582806345f6e8b3dc004f4c5c1f6",
            "block_hash": "c1405e224e8e2b925a916d6a3ba772ce642ba06137640b05c87692355d8bc"
        },
        {
            "type": "access_event",
            "block_height": 3,
            "room": "RoomA",
            "timestamp": "2025-08-13T13:07:43Z",
            "previous_block_hash": "c1405e224e8e2b925a916d6a3ba772ce642ba06137640b05c87692355d8bc",
            "votes": [
                {
                    "node_id": "UE1",
                    "vote": "yes",
                    "signature": "30440220638fb7c358b9fa1084cd91b292d8b7a7b427e47ecca8fca30b70b07b59203937022021baa189a40afd07a222a16195832da86474f676c1699a6a2d7b8a7e50c2a93b"
                }
            ],
            "user_id": "UE2",
            "voting_summary": {
                "total_nodes": 1,
                "yes_votes": 1,
                "no_votes": 0
            },
            "added_to_chain": true,
            "aggregator_signature": "3045022100a74e571590431d42a84f5afcae9e14f3bfeed89a5be3ba32ec36f9be8e9dde7402201635462c149a3a727f0bd42cef80170754fb5e854172ba25f2c1c0675d92bb5a",
            "block_hash": "16b81df576b5678ae533754fa6311aa6242f794a0742a9bbe6c21e5f4c05bd5"
        },
        {
            "type": "add_pubkey_event",
            "block_height": 4,
            "user_id": "UE2",
            "public_key": "-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEA3fvbH5favbreRw04xYSEd9yNFQU\nHhJq+ffQArGk7yjAkzWxfaBmdoYXhXVZML7uGvQcskTPIlkc1FH5v72jAA==\n-----END PUBLIC KEY-----",
            "timestamp": "2025-08-13T13:07:43Z",
            "previous_block_hash": "16b81df576b5678ae533754fa6311aa6242f794a0742a9bbe6c21e5f4c05bd5",
            "aggregator_signature": "30450221009b5b652a99ee714ac99e0157ed1956fabf0a3f296e69f3195213a6259c124d4202200a6365fa455e8c6b45e51605b573ad6a98e74d8e2f92ba740fb2c59ca7ee8da1",
            "block_hash": "8a564133cb53336e777784ec6f201ff6de98d6b93b2ac44bccf581d5d6e"
        },
        {
            "type": "add_user_event",
            "block_height": 5,
            "room": "RoomA",
            "timestamp": "2025-08-13T13:07:49Z",
            "previous_block_hash": "8a564133cb53336e777784ec6f201ff6de98d6b93b2ac44bccf581d5d6e",
            "votes": [
                {
                    "node_id": "UE1",
                    "vote": "yes",
                    "signature": "3045022100f9c5589530ee626888af90605034980d02bce692f158a264241a7913e5de3e22022077ea43f8acab04fa4e7ca104de0ebd6fc58a34992d3d9964c9c7de344c462195"
                },
                {
                    "room": "RoomA",
                    "candidate": "UE3",
                    "voter_id": "UE2",
                    "vote": "yes",
                    "signature": "304502204175c62797bd66a5bc4fb601d2d10cdeb5a89c6039bb812c8f3487c3b5bcdb3d022100dd2cc64c63249ea7d5a152f042c9fc5b8bf0ae42111c66d1c1d3a17e090cd64d"
                }
            ],
            "new_user_id": "UE3",
            "voting_summary": {
                "total_nodes": 2,
                "yes_votes": 2,
                "no_votes": 0
            },
            "added_to_chain": true,
            "aggregator_signature": "3045022100da606b86f5fb21e35e86f79ec48aad46192c5ad435b74d8aa7a301d98e30501402204ad264a03ff6b553b3aff95222739c3c22f814939984650175b29f3c6ac2ca2c",
            "block_hash": "83dfc6fe536afe6f7cb9332c8e5a6aeee3a38d8babeadaf0b1823211982067"
        },
        {
            "type": "access_event",
            "block_height": 6,
            "room": "RoomA",
            "timestamp": "2025-08-13T13:07:49Z",
            "previous_block_hash": "83dfc6fe536afe6f7cb9332c8e5a6aeee3a38d8babeadaf0b1823211982067",
            "votes": [
                {
                    "node_id": "UE1",
                    "vote": "yes",
                    "signature": "3045022100f9c5589530ee626888af90605034980d02bce692f158a264241a7913e5de3e22022077ea43f8acab04fa4e7ca104de0ebd6fc58a34992d3d9964c9c7de344c462195"
                },
                {
                    "room": "RoomA",
                    "candidate": "UE3",
                    "voter_id": "UE2",
                    "vote": "yes",
                    "signature": "304502204175c62797bd66a5bc4fb601d2d10cdeb5a89c6039bb812c8f3487c3b5bcdb3d022100dd2cc64c63249ea7d5a152f042c9fc5b8bf0ae42111c66d1c1d3a17e090cd64d"
                }
            ],
            "user_id": "UE3",
            "voting_summary": {
                "total_nodes": 2,
                "yes_votes": 2,
                "no_votes": 0
            },
            "added_to_chain": true,
            "aggregator_signature": "3045022100ce563ecada3975ae584c0d3c367c5c2eaeb27967d8d35f522d35404efe98b211022057581b3263c15037021cbc38d0bb26d0c5a4a339bd616bd63685500ae8b6d20f",
            "block_hash": "babbe447882e2c8da67bb61e6455ca98d884b3a1291b58069d9c6e0b389d826"
        },
        {
            "type": "add_pubkey_event",
            "block_height": 7,
            "user_id": "UE3",
            "public_key": "-----BEGIN PUBLIC KEY-----\nMFkwEwYHKoZIzj0CAQYIKoZIzj0DAQcDQgAEWuWqcpuP6o1l/LgceAKHPDmQBtKg\najIyi8Ulf0B/oLnpTolY6jxeK5ZB4VkEW6uKhb/LO+Cxp4nAtC95NL+K+g==\n-----END PUBLIC KEY-----",
            "timestamp": "2025-08-13T13:07:49Z",
            "previous_block_hash": "babbe447882e2c8da67bb61e6455ca98d884b3a1291b58069d9c6e0b389d826",
            "aggregator_signature": "3045022030d2a2ce973ae94e475947b55e1f77803fb42c12961e995b49f14975ef614fbe022100ca567409d824ca1eb8c0e3ef7e84a18d61f3b359bd0839f10cd0a4f276e8cbae",
            "block_hash": "c1cdd61638678a4ea462e8ed551d97cc971f27c299fc16c845a6a099d17e1b"
        },
        {
            "type": "access_event",
            "block_height": 8,
            "room": "RoomA",
            "timestamp": "2025-08-13T13:08:27Z",
            "previous_block_hash": "c1cdd61638678a4ea462e8ed551d97cc971f27c299fc16c845a6a099d17e1b",
            "votes": [
                {
                    "node_id": "UE1",
                    "vote": "yes",
                    "signature": "3045022100fc197fe3888b2c04bd07c109350423617ddce3e645b7d643dac528554fdcf74602207b0efffa50d4e45263f3f4c663a3d6a7576d9803f8916b0732b64592f6ace01c"
                }
            ],
            "user_id": "UE3",
            "voting_summary": {
                "total_nodes": 1,
                "yes_votes": 1,
                "no_votes": 0
            },
            "added_to_chain": true,
            "aggregator_signature": "304502205e94bb18f2c553cac5b9aea3b17fbbe4f45677549f972a2a6a9267093cfed2a4022100d0c0e2a5b8c249446292bc3d6766aa77a4986cc2aee1913a380aca17a1c29100",
            "block_hash": "83b9a14528f28852da324167f1591df589c13e526251565f4aa9fa12b47b2667"
        },
        {
            "type": "access_event",
            "block_height": 9,
            "room": "RoomA",
            "timestamp": "2025-08-13T13:45:36Z",
            "previous_block_hash": "83b9a14528f28852da324167f1591df589c13e526251565f4aa9fa12b47b2667",
            "votes": [
                {
                    "node_id": "UE1",
                    "vote": "yes",
                    "signature": "3045022030b00d06f17dd2ac89e19b2a40c9b6f45ec0379e28ec3eb6cedfeeae25d83050022100eb870f71512d8857270c3fb4238a07f8daa37fbfff912e7acff86465c356ab59"
                }
            ],
            "user_id": "UE2",
            "voting_summary": {
                "total_nodes": 1,
                "yes_votes": 1,
                "no_votes": 0
            },
            "added_to_chain": true,
            "aggregator_signature": "30440220078ef6b8b9b152096e48505c423607bc3ffcad23ecdaf4efd52f2ecd40246fae02200bf2de9adc0a187359df4ed2110dca848b4fb1ad2919fbd6829886f9ef7e03a1",
            "block_hash": "a9b0e998fab5a9342fe141369c6b78a03edc12cdf582a5b66712934ad3d77e2"
        }
    ]
    """.trimIndent()

    // Use remember to parse the data only once
    val logs: List<BlockchainEvent> = remember {
        parseBlockchainEvents(rawJson)
    }

    val navController = rememberNavController()
    val gson: Gson = GsonBuilder()
        .registerTypeAdapter(BlockchainEvent::class.java, BlockchainEventTypeAdapter())
        .create()
    NavHost(
        navController = navController,
        startDestination = "logs_list",
        modifier = modifier
    ) {
        composable("logs_list") {
            LogsScreen(
                logs = logs,
                onLogClick = { log ->
                    // Use the global gson instance to serialize
                    val jsonLog = gson.toJson(log)
                    navController.navigate("log_details/${URLEncoder.encode(jsonLog, StandardCharsets.UTF_8.toString())}")
                }
            )
        }
        composable(
            "log_details/{log_data}",
            arguments = listOf(navArgument("log_data") { type = NavType.StringType })
        ) { backStackEntry ->
            val logData = backStackEntry.arguments?.getString("log_data")
            // Use the global gson instance to deserialize
            val log = gson.fromJson(logData, BlockchainEvent::class.java)
            DetailsScreen(log)
        }
    }
}

@Composable
fun LogsScreen(modifier: Modifier = Modifier, logs: List<BlockchainEvent>, onLogClick: (BlockchainEvent) -> Unit) {
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

private fun parseBlockchainEvents(jsonString: String): List<BlockchainEvent> {
    val gson = GsonBuilder()
        .registerTypeAdapter(BlockchainEvent::class.java, BlockchainEventTypeAdapter())
        .create()

    val type = object : TypeToken<List<BlockchainEvent>>() {}.type
    return gson.fromJson(jsonString, type)
}


@Composable
fun LogItem(log: BlockchainEvent, onLogClick: (BlockchainEvent) -> Unit) {
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
                text = "Block #${log.blockHeight}",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
            Text(text = "Timestamp: $formattedTime", color = Color.Gray)
            Spacer(modifier = Modifier.height(8.dp))

            when (log) {
                is AddUserEvent -> {
                    Text(text = "Event Type: New User Added", fontWeight = FontWeight.SemiBold)
                    Text(text = "New User: ${log.newUserId}")
                    Text(text = "Room: ${log.room}")
                    Text(text = "Yes Votes: ${log.votingSummary.yesVotes}")
                    Text(text = "No Votes: ${log.votingSummary.noVotes}")
                }
                is AccessEvent -> {
                    Text(text = "Event Type: Access Granted", fontWeight = FontWeight.SemiBold)
                    Text(text = "User ID: ${log.userId}")
                    Text(text = "Room: ${log.room}")
                    Text(text = "Yes Votes: ${log.votingSummary.yesVotes}")
                    Text(text = "No Votes: ${log.votingSummary.noVotes}")
                }
                is AddPubkeyEvent -> {
                    Text(text = "Event Type: Public Key Added", fontWeight = FontWeight.SemiBold)
                    Text(text = "User ID: ${log.userId}")
                    // You might not want to display the full key
                    Text(text = "Public Key Added for user ${log.userId}")
                }
            }
        }
    }
}

@Composable
fun DetailsScreen(log: BlockchainEvent) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Display basic event info
        Text("Block Hash: ${log.blockHash}")
        Text("Previous Block Hash: ${log.previousBlockHash}")
        Text("Aggregator Signature: ${log.aggregatorSignature}")
        Spacer(Modifier.height(16.dp))

        val votes = when (log) {
            is AddUserEvent -> log.votes
            is AccessEvent -> log.votes
            else -> null
        }

        if (votes != null && votes.isNotEmpty()) {
            Text(
                text = "Votes:",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyColumn {
                items(votes) { vote ->
                    VoteItem(vote)
                }
            }
        }
    }
}

@Composable
fun VoteItem(vote: Vote) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(text = "Node ID: ${vote.nodeId}")
            Text(text = "Vote: ${vote.vote.uppercase(Locale.getDefault())}", fontWeight = FontWeight.SemiBold)
            Text(text = "Signature: ${vote.signature}", fontSize = 12.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
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

