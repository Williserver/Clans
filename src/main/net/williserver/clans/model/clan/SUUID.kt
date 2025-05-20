package net.williserver.clans.model.clan

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.util.UUID

/**
 * A typealias for a UUID with a custom serializer for KotlinX serialization.
 * If deserializing an invalid UUID, will throw exception.
 */
typealias SUUID = @Serializable(with = UUIDSerializer::class) UUID

/**
 * Custom serializer for a non-nullable UUID.
 * Credit: https://github.com/perracodex/Kotlinx-UUID-Serializer
 *
 * @author Willmo3
 */
object UUIDSerializer : KSerializer<UUID> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("UUID", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: UUID) = encoder.encodeString(value.toString())

    override fun deserialize(decoder: Decoder): UUID = UUID.fromString(decoder.decodeString())
}