package com.example.dtaquito.chat
import Beans.chat.ChatMessage
import com.google.gson.*
import java.lang.reflect.Type
import java.util.Calendar
import java.util.TimeZone
import java.text.SimpleDateFormat
import java.util.Locale


class ChatMessageDeserializer : JsonDeserializer<ChatMessage> {
    override fun deserialize(
        json: JsonElement,
        typeOfT: Type,
        context: JsonDeserializationContext
    ): ChatMessage {
        if (!json.isJsonObject) {
            throw JsonParseException("No es un objeto JSON válido: $json")
        }
        val jsonObject = json.asJsonObject

        val createdAtElement = jsonObject["createdAt"]
        val createdAt = if (createdAtElement != null && createdAtElement.isJsonArray) {
            val dateArray = createdAtElement.asJsonArray.map { it.asInt }
            val cal = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            cal.set(dateArray[0], dateArray[1] - 1, dateArray[2], dateArray[3], dateArray[4], dateArray[5])
            cal.set(Calendar.MILLISECOND, dateArray[6] / 1_000_000)
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            sdf.format(cal.time)
        } else {
            createdAtElement?.asString ?: ""
        }

        // Manejar usuario anidado
        var userId = -1
        var userName: String? = null
        if (jsonObject.has("user")) {
            val userObj = jsonObject["user"].asJsonObject
            userId = userObj["id"].asInt
            userName = userObj["name"].asString
        } else {
            userId = jsonObject["userId"]?.asInt ?: -1
            userName = jsonObject["userName"]?.asString
        }

        val roomId = jsonObject["roomId"]?.asInt
            ?: throw JsonParseException("Falta campo roomId en el mensaje WS")

        return ChatMessage(
            content = jsonObject["content"].asString,
            userId = userId,
            userName = userName.toString(),
            createdAt = createdAt,
            roomId = roomId
        )
    }
}