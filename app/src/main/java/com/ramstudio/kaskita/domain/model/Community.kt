package com.ramstudio.kaskita.domain.model

import androidx.compose.ui.graphics.Color
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class Community(
    val id: String? = null,
    val name: String,
    val description: String,
    val code: String,
    @SerialName("created_by")
    val createdBy: String? = null,
    val balance: Double,
    val membersCount: Int = 0,
    @Serializable(with = ColorSerializer::class)
    val themeColor: Color = PrimaryGreen
)

@Serializable
data class JoinResponse(
    val success: Boolean,
    val message: String
)

enum class CommunityTab(val title: String) {
    TRANSACTIONS("Transactions"),
    MEMBERS("Members")
}

object ColorSerializer : KSerializer<Color> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("Color", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Color) {

        encoder.encodeString(value.value.toString(16).uppercase())
    }

    override fun deserialize(decoder: Decoder): Color {
        // Mengubah String Hex dari JSON kembali menjadi Color Jetpack Compose
        val hexString = decoder.decodeString()
        return Color(hexString.toULong(16))
    }
}