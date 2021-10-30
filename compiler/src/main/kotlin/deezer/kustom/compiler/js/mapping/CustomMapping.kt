package deezer.kustom.compiler.js.mapping

import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.BOOLEAN_ARRAY
import com.squareup.kotlinpoet.BYTE
import com.squareup.kotlinpoet.BYTE_ARRAY
import com.squareup.kotlinpoet.CHAR
import com.squareup.kotlinpoet.CHAR_ARRAY
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.DOUBLE_ARRAY
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.FLOAT_ARRAY
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.INT_ARRAY
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.LONG_ARRAY
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.SHORT_ARRAY
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import deezer.kustom.compiler.firstParameterizedType
import deezer.kustom.compiler.js.mapping.TypeMapping.MappingOutput
import deezer.kustom.compiler.js.pattern.qdot

const val INDENTATION = "    "

// val FUNCTION1 = ClassName("kotlin", "Function1")
val EXCEPTION = ClassName("kotlin", "Exception")
val EXCEPTION_JS = ClassName("deezer.kmp", "Exception")

fun initCustomMapping() {
    // Doc interop: https://kotlinlang.org/docs/js-to-kotlin-interop.html#primitive-arrays
    // No special mappings
    TypeMapping.mappings += listOf(
        // TODO: Check that 'char' is properly re-interpreted
        BOOLEAN, BYTE, SHORT, INT, CHAR, FLOAT, DOUBLE, // => Js "number"
        STRING, // => Js "string"
        ARRAY, // => Js "array"
        BOOLEAN_ARRAY, BYTE_ARRAY, // => Js "Int8Array"
        SHORT_ARRAY, // => Js "Int16Array"
        INT_ARRAY, // => Js "Int32Array"
        FLOAT_ARRAY, // => Js "Float32Array"
        DOUBLE_ARRAY, // => Js "Float64Array"
        CHAR_ARRAY, // => Js "UInt16Array"
        ANY, // => Js "Object"
        UNIT // => disappear?
    ).map { exportableType ->
        exportableType to MappingOutput(
            exportType = { exportableType },
            importMethod = { "" },
            exportMethod = { "" },
        )
    }

    TypeMapping.mappings += mapOf<TypeName, MappingOutput>(
        // https://kotlinlang.org/docs/js-to-kotlin-interop.html#kotlin-types-in-javascript
        // doc: kotlin.Long is not mapped to any JavaScript object, as there is no 64-bit integer number type in JavaScript. It is emulated by a Kotlin class.
        LONG to MappingOutput(
            exportType = { DOUBLE },
            importMethod = { "${it.qdot}toLong()" },
            exportMethod = { "${it.qdot}toDouble()" },
        ),

        // TODO: LongArray to be tested!
        LONG_ARRAY to MappingOutput(
            exportType = { ARRAY.parameterizedBy(TypeMapping.exportedType(it.firstParameterizedType())) },
            importMethod = { "${it.qdot}map { it${TypeMapping.importMethod(it.firstParameterizedType())} }" },
            exportMethod = { "${it.qdot}map { it${TypeMapping.exportMethod(it.firstParameterizedType())} }${it.qdot}toTypedArray()" },
        ),

        LIST to MappingOutput(
            exportType = { ARRAY.parameterizedBy(TypeMapping.exportedType(it.firstParameterizedType())) },
            importMethod = { "${it.qdot}map { it${TypeMapping.importMethod(it.firstParameterizedType())} }" },
            exportMethod = { "${it.qdot}map { it${TypeMapping.exportMethod(it.firstParameterizedType())} }${it.qdot}toTypedArray()" },
        ),
        // TODO: Handle other collections

        EXCEPTION to MappingOutput(
            exportType = { EXCEPTION_JS },
            importMethod = { "${it.qdot}import()" },
            exportMethod = { "${it.qdot}export()" },
        )

        // TODO: Implement all lambda possible? -> just enforce SAM interface for now...
        /*
        FUNCTION1 to MappingOutput(
            exportType = {
                LambdaTypeName.get(
                    receiver = null,
                    parameters = listOf(
                        ParameterSpec.builder(
                            "", CustomMappings.exportedType(it.firstParameterizedType())
                        ).build()
                    ),
                    returnType = UNIT
                )
            },
            importMethod = { "TBD" },
            exportMethod = { "TBD" },
        )*/
    )
}
