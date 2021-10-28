package deezer.kustom.compiler.js.mapping

import com.squareup.kotlinpoet.ARRAY
import com.squareup.kotlinpoet.BOOLEAN
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.DOUBLE
import com.squareup.kotlinpoet.FLOAT
import com.squareup.kotlinpoet.INT
import com.squareup.kotlinpoet.LIST
import com.squareup.kotlinpoet.LONG
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
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
    TypeMapping.mappings += mapOf<TypeName, MappingOutput>(
        STRING to MappingOutput(
            exportType = { STRING },
            importMethod = { "" },
            exportMethod = { "" },
        ),
        INT to MappingOutput(
            exportType = { INT },
            importMethod = { "" },
            exportMethod = { "" },
        ),
        BOOLEAN to MappingOutput(
            exportType = { BOOLEAN },
            importMethod = { "" },
            exportMethod = { "" },
        ),
        FLOAT to MappingOutput(
            exportType = { FLOAT },
            importMethod = { "" },
            exportMethod = { "" },
        ),
        UNIT to MappingOutput(
            exportType = { UNIT },
            importMethod = { "" },
            exportMethod = { "" },
        ),
        // https://kotlinlang.org/docs/js-to-kotlin-interop.html#kotlin-types-in-javascript
        // doc: kotlin.Long is not mapped to any JavaScript object, as there is no 64-bit integer number type in JavaScript. It is emulated by a Kotlin class.
        LONG to MappingOutput(
            exportType = { DOUBLE },
            importMethod = { "${it.qdot}toLong()" },
            exportMethod = { "${it.qdot}toDouble()" },
        ),
        LIST to MappingOutput(
            exportType = { ARRAY.parameterizedBy(TypeMapping.exportedType(it.firstParameterizedType())) },
            importMethod = { "${it.qdot}map { it${TypeMapping.importMethod(it.firstParameterizedType())} }" },
            exportMethod = { "${it.qdot}map { it${TypeMapping.exportMethod(it.firstParameterizedType())} }${it.qdot}toTypedArray()" },
        ),
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
