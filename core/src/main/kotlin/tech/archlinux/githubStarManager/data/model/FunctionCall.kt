package tech.archlinux.githubStarManager.data.model

import kotlinx.serialization.Serializable
import kotlin.reflect.KType
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

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

class FunctionSpec<T : Any>(
    val name: String,
    val description: String,
    val parameters: List<ParameterSpec>,
    val returnType: KType,
    val executor: suspend T.() -> Any?
)

class ParameterSpec(
    val name: String,
    val type: KType,
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

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified T> function(name: String, description: String, init: ParametersDsl.(T) -> Unit) {
//        val dsl = ParametersDsl()
//        dsl.init()
//        properties += (name.first to Parameter(
//            type = name.second,
//            description = dsl.description
//        ))
        val typeName = typeOf<T>()
        println(typeName.javaType.typeName)
    }

    @OptIn(ExperimentalStdlibApi::class)
    inline fun <reified A, reified B> function(name: String, description: String, init: ParametersDsl.(A, B) -> Unit) {
//        val dsl = ParametersDsl()
//        dsl.init()
//        properties += (name.first to Parameter(
//            type = name.second,
//            description = dsl.description
//        ))
        val typeNameA = typeOf<A>()
        val typeNameB = typeOf<B>()
        println(typeNameA.javaType.typeName)
    }

    class ParametersDsl {

    }
}
