package tech.archlinux.githubStarManager.data.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

class LLMFunctionDsl {
    var name: String = ""
    var description: String = ""
    var properties: MutableMap<String, Parameter> = mutableMapOf()
    var functionBody: (Map<String, Any?>) -> Any? = { null }
    var paramDefinitions: MutableList<ParamDefinition<*>> = mutableListOf()

    inline fun <reified R> function(noinline block: FunctionContext.() -> R): FunctionExecutor<R> {
        val context = FunctionContext()
        context.block() // This collects parameter definitions but doesn't execute the actual function logic

        // Store the function body for later execution
        val capturedParams = context.capturedParams
        functionBody = { params ->
            // Create a new context with the actual parameter values
            val execContext = FunctionContext()
            execContext.paramValues = params
            execContext.capturedParams = capturedParams
            execContext.block() // Now execute with real values
        }

        return FunctionExecutor(functionBody as (Map<String, Any?>) -> R, paramDefinitions)
    }

    inner class FunctionContext {
        var paramValues: Map<String, Any?> = emptyMap()
        var capturedParams = mutableListOf<ParamDefinition<*>>()

        @OptIn(ExperimentalStdlibApi::class)
        inline fun <reified T> param(noinline init: ParamDsl.() -> Unit): T {
            val paramDsl = ParamDsl().apply(init)

            val typeString = when (typeOf<T>().javaType.typeName) {
                "java.lang.String" -> "string"
                "java.lang.Integer", "int" -> "integer"
                "java.lang.Boolean", "boolean" -> "boolean"
                "java.lang.Double", "double" -> "number"
                else -> "object"
            }

            properties[paramDsl.name] = Parameter(typeString, paramDsl.description)

            // Create and store parameter definition
            val paramDef = ParamDefinition<T>(paramDsl.name, paramDsl.description, typeOf<T>())
            capturedParams.add(paramDef)
            paramDefinitions.add(paramDef)

            // Return actual value if available, otherwise default
            return if (paramValues.containsKey(paramDsl.name)) {
                paramValues[paramDsl.name] as T
            } else {
                when (typeOf<T>().javaType.typeName) {
                    "java.lang.String" -> "" as T
                    "java.lang.Integer", "int" -> 0 as T
                    "java.lang.Boolean", "boolean" -> false as T
                    "java.lang.Double", "double" -> 0.0 as T
                    else -> null as T
                }
            }
        }
    }

    class ParamDsl {
        var name: String = ""
        var description: String = ""
    }
}

class ParamDefinition<T>(
    val name: String,
    val description: String,
    val type: kotlin.reflect.KType
)

class FunctionExecutor<R>(
    private val executor: (Map<String, Any?>) -> R,
    val parameters: List<ParamDefinition<*>>
) {
    fun execute(params: Map<String, Any?> = emptyMap()): R {
        return executor(params)
    }
}



@Serializable
data class FunctionCall(
    val type: String = "function",
    val function: LLMFunction,
    @Transient val functionBody: (Map<String, Any?>) -> Any? = { null }
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

fun functionCall(init: LLMFunctionDsl.() -> Unit): FunctionCall {
    val dsl = LLMFunctionDsl().apply(init)

    return FunctionCall(
        type = "function",
        function = LLMFunction(
            name = dsl.name,
            description = dsl.description,
            parameters = Parameters(
                type = "object",
                properties = dsl.properties
            ),
        ),
        functionBody = dsl.functionBody,
    )
}