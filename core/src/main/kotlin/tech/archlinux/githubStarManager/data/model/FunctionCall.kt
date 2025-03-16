package tech.archlinux.githubStarManager.data.model

import kotlinx.serialization.Serializable

@Serializable
data class FunctionCall(
    val type: String = "function",
    val function: LLMFunction
)

@Serializable
data class LLMFunction(
    val name: String,
    val description: String,
    val parameters: Parameters
)

@Serializable
data class Parameters(
    val type: String = "object",
    val properties: Map<String, Parameter>
)

@Serializable
data class Parameter(
    val type: String,
    val description: String
)

inline fun functionCall(init: LLMFunctionDsl.() -> Unit): LLMFunction {
    val dsl = LLMFunctionDsl()
    dsl.init()

    return LLMFunction(
        name = dsl.name,
        description = dsl.description,
        parameters = Parameters(
            type = "object",
            properties = dsl.properties,
        )
    )
}

class LLMFunctionDsl {
    var name: String = ""
    var description: String = ""
    var properties: Map<String, Parameter> = emptyMap()

    fun parameter(name: Pair<String, String>, init: ParametersDsl.() -> Unit) {
        val dsl = ParametersDsl()
        dsl.init()
        properties += (name.first to Parameter(
            type = name.second,
            description = dsl.description
        ))
    }

    class ParametersDsl {
        var description: String = ""
    }
}
