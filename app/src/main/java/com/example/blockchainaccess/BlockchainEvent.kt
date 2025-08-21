package com.example.blockchainaccess

import com.google.gson.annotations.SerializedName
import com.google.gson.*
import java.lang.reflect.Type

sealed class BlockchainEvent {
    abstract val type: String
    abstract val blockHeight: Int
    abstract val timestamp: String
    abstract val previousBlockHash: String

    abstract val aggregatorSignature: String
    abstract val blockHash: String
}

data class AddUserEvent(
    @SerializedName("type") override val type: String,
    @SerializedName("block_height") override val blockHeight: Int,
    @SerializedName("room") val room: String,
    @SerializedName("timestamp") override val timestamp: String,
    @SerializedName("previous_block_hash") override val previousBlockHash: String,
    @SerializedName("votes") val votes: List<Vote>,
    @SerializedName("new_user_id") val newUserId: String,
    @SerializedName("voting_summary") val votingSummary: VotingSummary,
    @SerializedName("added_to_chain") val addedToChain: Boolean,
    @SerializedName("aggregator_signature") override val aggregatorSignature: String,
    @SerializedName("block_hash") override val blockHash: String
) : BlockchainEvent()

data class AccessEvent(
    @SerializedName("type") override val type: String,
    @SerializedName("block_height") override val blockHeight: Int,
    @SerializedName("room") val room: String,
    @SerializedName("timestamp") override val timestamp: String,
    @SerializedName("previous_block_hash") override val previousBlockHash: String,
    @SerializedName("votes") val votes: List<Vote>,
    @SerializedName("user_id") val userId: String,
    @SerializedName("voting_summary") val votingSummary: VotingSummary,
    @SerializedName("added_to_chain") val addedToChain: Boolean,
    @SerializedName("aggregator_signature") override val aggregatorSignature: String,
    @SerializedName("block_hash") override val blockHash: String
) : BlockchainEvent()

data class AddPubkeyEvent(
    @SerializedName("type") override val type: String,
    @SerializedName("block_height") override val blockHeight: Int,
    @SerializedName("user_id") val userId: String,
    @SerializedName("public_key") val publicKey: String,
    @SerializedName("timestamp") override val timestamp: String,
    @SerializedName("previous_block_hash") override val previousBlockHash: String,
    @SerializedName("aggregator_signature") override val aggregatorSignature: String,
    @SerializedName("block_hash") override val blockHash: String
) : BlockchainEvent()

data class Vote(
    @SerializedName("node_id") val nodeId: String,
    @SerializedName("vote") val vote: String,
    @SerializedName("signature") val signature: String
)

data class VotingSummary(
    @SerializedName("total_nodes") val totalNodes: Int,
    @SerializedName("yes_votes") val yesVotes: Int,
    @SerializedName("no_votes") val noVotes: Int
)

/**
 * A custom TypeAdapter for deserializing BlockchainEvent objects.
 * This adapter inspects the "type" field to determine the correct subclass
 * to instantiate.
 */
class BlockchainEventTypeAdapter : JsonDeserializer<BlockchainEvent> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type?,
        context: JsonDeserializationContext
    ): BlockchainEvent {
        val jsonObject = json.asJsonObject
        val eventType = jsonObject.get("type").asString

        return when (eventType) {
            "add_user_event" -> context.deserialize(jsonObject, AddUserEvent::class.java)
            "access_event" -> context.deserialize(jsonObject, AccessEvent::class.java)
            "add_pubkey_event" -> context.deserialize(jsonObject, AddPubkeyEvent::class.java)
            else -> throw IllegalArgumentException("Unknown event type: $eventType")
        }
    }
}