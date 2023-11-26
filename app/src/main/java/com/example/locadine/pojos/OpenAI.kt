package com.example.locadine.pojos

data class OpenAIRequest(
    val model: String,
    val messages: List<OpenAIMessage>,
    val temperature: Double
)

data class OpenAIMessage(
    val role: String,
    val content: String
)

data class OpenAIResponse(
    val id: String,
    val model: String,
    val choices: List<Choice>
)

data class Choice(
    val message: OpenAIMessage
)

enum class OpenAIRoleType(val type: String) {
    USER("user"),
    ASSISTANT("assistant")
}