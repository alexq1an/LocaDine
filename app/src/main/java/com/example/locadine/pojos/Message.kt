package com.example.locadine.pojos

data class Message(val message: String, val sender: SenderType)

enum class SenderType(val type: String, val number: Int) {
    USER("USER", 0),
    BOT("BOT", 1);

    companion object {
        fun fromNumber(number: Int): SenderType? {
            return SenderType.values().firstOrNull { it.number == number }
        }

        fun fromText(text: String): SenderType? {
            return SenderType.values().firstOrNull { it.type == text }
        }
    }
}