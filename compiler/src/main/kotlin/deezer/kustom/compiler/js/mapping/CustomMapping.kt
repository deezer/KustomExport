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
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.SHORT
import com.squareup.kotlinpoet.SHORT_ARRAY
import com.squareup.kotlinpoet.STRING
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.UNIT
import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.firstParameterizedType
import deezer.kustom.compiler.js.mapping.TypeMapping.MappingOutput
import deezer.kustom.compiler.js.mapping.TypeMapping.exportMethod
import deezer.kustom.compiler.js.mapping.TypeMapping.exportedType
import deezer.kustom.compiler.js.mapping.TypeMapping.importMethod
import deezer.kustom.compiler.js.pattern.isKotlinFunction
import deezer.kustom.compiler.js.pattern.qdot
import deezer.kustom.compiler.shortNamesForIndex

const val INDENTATION = "    "

val EXCEPTION = ClassName("kotlin", "Exception")
val EXCEPTION_JS = ClassName("deezer.kmp", "Exception")

fun initCustomMapping() {
    // Doc interop: https://kotlinlang.org/docs/js-to-kotlin-interop.html#primitive-arrays
    // No special mappings
    TypeMapping.mappings += listOf(
        // TODO: Check that 'char' is properly re-interpreted
        BOOLEAN, BYTE, CHAR, SHORT, INT, FLOAT, DOUBLE, // => Js "number"
        STRING, // => Js "string"
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
            importMethod = { targetName, _ -> targetName },
            exportMethod = { targetName, _ -> targetName },
        )
    }

    TypeMapping.mappings += mapOf<TypeName, MappingOutput>(
        // https://kotlinlang.org/docs/js-to-kotlin-interop.html#kotlin-types-in-javascript
        // doc: kotlin.Long is not mapped to any JavaScript object, as there is no 64-bit integer number type in JavaScript. It is emulated by a Kotlin class.
        LONG to MappingOutput(
            exportType = { DOUBLE },
            importMethod = { targetName, typeName -> "$targetName${typeName.qdot}toLong()" },
            exportMethod = { targetName, typeName -> "$targetName${typeName.qdot}toDouble()" },
        ),

        LONG_ARRAY to MappingOutput(
            exportType = { ARRAY.parameterizedBy(TypeMapping.exportedType(LONG)) },
            // TODO: improve perf by avoiding useless double transformation
            // LongArray(value.size) { index -> value[index].toLong() }
            importMethod = { targetName, typeName ->
                targetName +
                    "${typeName.qdot}map { ${TypeMapping.importMethod("it", LONG)} }" +
                    "${typeName.qdot}toLongArray()"
            },
            exportMethod = { targetName, typeName ->
                targetName +
                    "${typeName.qdot}map { ${TypeMapping.exportMethod("it", LONG)} }" +
                    "${typeName.qdot}toTypedArray()"
            },
        ),

        ARRAY to MappingOutput(
            exportType = { ARRAY.parameterizedBy(TypeMapping.exportedType(it.firstParameterizedType())) },
            importMethod = { targetName, typeName ->
                val importMethod = TypeMapping.importMethod("it", typeName.firstParameterizedType())
                if (importMethod == "it") targetName else {
                    "$targetName${typeName.qdot}map { $importMethod }${typeName.qdot}toTypedArray()"
                }
            },
            exportMethod = { targetName, typeName ->
                val exportMethod = TypeMapping.exportMethod("it", typeName.firstParameterizedType())
                if (exportMethod == "it") targetName else {
                    "$targetName${typeName.qdot}map { $exportMethod }${typeName.qdot}toTypedArray()"
                }
            },
        ),

        LIST to MappingOutput(
            exportType = { ARRAY.parameterizedBy(TypeMapping.exportedType(it.firstParameterizedType())) },
            importMethod = { targetName, typeName ->
                targetName +
                    "${typeName.qdot}map { ${TypeMapping.importMethod("it", typeName.firstParameterizedType())} }"
            },
            exportMethod = { targetName, typeName ->
                targetName +
                    "${typeName.qdot}map { ${TypeMapping.exportMethod("it", typeName.firstParameterizedType())} }" +
                    "${typeName.qdot}toTypedArray()"
            },
        ),
        // TODO: Handle other collections

        EXCEPTION to MappingOutput(
            exportType = { EXCEPTION_JS },
            importMethod = { targetName, typeName -> "$targetName${typeName.qdot}import()" },
            exportMethod = { targetName, typeName -> "$targetName${typeName.qdot}export()" },
        )
    )

    TypeMapping.advancedMappings += mapOf<(TypeName) -> Boolean, MappingOutput>(
        { type: TypeName -> type.isKotlinFunction() } to MappingOutput(
            exportType = {
                Logger.warn("FunctionX $it")
                val lambda = it as ParameterizedTypeName
                val args = lambda.typeArguments.dropLast(1)
                val returnType = lambda.typeArguments.last()
                LambdaTypeName.get(
                    parameters = args.map { arg ->
                        ParameterSpec.builder("", exportedType(arg)).build()
                    },
                    returnType = returnType
                )
            },
            importMethod = { targetName, typeName ->
                val lambda = typeName as ParameterizedTypeName
                val returnType = lambda.typeArguments.last()
                val namedArgs = lambda.typeArguments.dropLast(1)
                    .mapIndexed { index, typeName -> typeName to shortNamesForIndex(index) }
                val signature = namedArgs
                    .joinToString { (type, name) -> "$name: $type" }
                val importedArgs: String = namedArgs
                    .joinToString { (type, name) -> exportMethod(name, type) }

                """
                    |{ $signature ->
                    |$INDENTATION${importMethod("$targetName($importedArgs)", returnType)}
                    |}
                """.trimMargin()
            },
            exportMethod = { targetName, typeName ->
                val lambda = typeName as ParameterizedTypeName
                val returnType = lambda.typeArguments.last()
                val namedArgs = lambda.typeArguments.dropLast(1)
                    .mapIndexed { index, typeName -> typeName to shortNamesForIndex(index) }
                val signature = namedArgs
                    .joinToString { (type, name) -> "$name: ${exportedType(type)}" }
                val importedArgs: String = namedArgs
                    .joinToString { (type, name) -> importMethod(name, type) }

                """
                    |{ $signature ->
                    |$INDENTATION${exportMethod("$targetName($importedArgs)", returnType)}
                    |}
                """.trimMargin()
            },
        )
    )
}
