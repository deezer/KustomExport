package deezer.kustom.compiler.js.pattern

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName

val TypeName.qdot: String
    get() = if (this.isNullable) "?." else "."

fun TypeName.asClassName(): ClassName =
    (this as? ClassName)
        ?: (this as? ParameterizedTypeName)?.rawType
        ?: TODO("$this")

private val regexFunctionX = Regex("kotlin\\.Function[0-9]+")
fun TypeName.isKotlinFunction() =
    this is ParameterizedTypeName && regexFunctionX.matches(this.rawType.canonicalName)
