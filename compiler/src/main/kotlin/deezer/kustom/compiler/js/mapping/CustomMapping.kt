/*
 * Copyright 2021 Deezer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
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
import deezer.kustom.compiler.firstParameterizedType
import deezer.kustom.compiler.js.ALL_KOTLIN_EXCEPTIONS
import deezer.kustom.compiler.js.mapping.TypeMapping.MappingOutput
import deezer.kustom.compiler.js.pattern.cached
import deezer.kustom.compiler.js.pattern.isKotlinFunction
import deezer.kustom.compiler.js.pattern.qdot
import deezer.kustom.compiler.shortNamesForIndex

const val INDENTATION = "    "

const val EXCEPTION_JS_PACKAGE = "deezer.kustom"
private fun TypeName.toJsException() = ClassName(EXCEPTION_JS_PACKAGE, (this as ClassName).simpleName)

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
        UNIT, // => disappear?
    ).map { exportableType ->
        exportableType to MappingOutput(
            exportType = { _, _ -> exportableType },
            importMethod = { targetName, _, _ -> targetName },
            exportMethod = { targetName, _, _ -> targetName },
        )
    }

    TypeMapping.mappings += ALL_KOTLIN_EXCEPTIONS.map { exportableType ->
        exportableType to MappingOutput(
            exportType = { typeName, _ -> typeName.toJsException() },
            importMethod = { targetName, typeName, _ -> "$targetName${typeName.qdot}import()" },
            exportMethod = { targetName, typeName, _ -> "$targetName${typeName.qdot}export()" },
        )
    }

    TypeMapping.mappings += mapOf<TypeName, MappingOutput>(
        // https://kotlinlang.org/docs/js-to-kotlin-interop.html#kotlin-types-in-javascript
        // doc: kotlin.Long is not mapped to any JavaScript object, as there is no 64-bit integer number type in JavaScript. It is emulated by a Kotlin class.
        LONG to MappingOutput(
            exportType = { _, _ -> DOUBLE },
            importMethod = { targetName, typeName, _ -> "$targetName${typeName.qdot}toLong()" },
            exportMethod = { targetName, typeName, _ -> "$targetName${typeName.qdot}toDouble()" },
        ),

        LONG_ARRAY to MappingOutput(
            exportType = { _, concreteTypeParameters -> ARRAY.parameterizedBy(LONG.cached(concreteTypeParameters).exportedTypeName) },
            // TODO: improve perf by avoiding useless double transformation
            // LongArray(value.size) { index -> value[index].toLong() }
            importMethod = { targetName, typeName, concreteTypeParameters ->
                targetName +
                    "${typeName.qdot}map { ${LONG.cached(concreteTypeParameters).importedMethod("it")} }" +
                    "${typeName.qdot}toLongArray()"
            },
            exportMethod = { targetName, typeName, concreteTypeParameters ->
                targetName +
                    "${typeName.qdot}map { ${LONG.cached(concreteTypeParameters).exportedMethod("it")} }" +
                    "${typeName.qdot}toTypedArray()"
            },
        ),

        ARRAY to MappingOutput(
            exportType = { typeName, concreteTypeParameters ->
                ARRAY.parameterizedBy(typeName.firstParameterizedType().cached(concreteTypeParameters).exportedTypeName)
            },
            importMethod = { targetName, typeName, concreteTypeParameters ->
                val importMethod = typeName.firstParameterizedType().cached(concreteTypeParameters).importedMethod("it")
                if (importMethod == "it") targetName else {
                    "$targetName${typeName.qdot}map { $importMethod }${typeName.qdot}toTypedArray()"
                }
            },
            exportMethod = { targetName, typeName, concreteTypeParameters ->
                val exportMethod =
                    typeName.firstParameterizedType().cached(concreteTypeParameters).exportedMethod("it")
                if (exportMethod == "it") targetName else {
                    "$targetName${typeName.qdot}map { $exportMethod }${typeName.qdot}toTypedArray()"
                }
            },
        ),

        LIST to MappingOutput(
            exportType = { typeName, concreteTypeParameters ->
                ARRAY.parameterizedBy(typeName.firstParameterizedType().cached(concreteTypeParameters).exportedTypeName)
            },
            importMethod = { targetName, typeName, concreteTypeParameters ->
                val firstParameterizedType = typeName.firstParameterizedType()
                val importedMethod = firstParameterizedType.cached(concreteTypeParameters).importedMethod("it")
                targetName + "${typeName.qdot}map { $importedMethod }"
            },
            exportMethod = { targetName, typeName, concreteTypeParameters ->
                val exportedMethod =
                    typeName.firstParameterizedType().cached(concreteTypeParameters).exportedMethod("it")
                targetName +
                    "${typeName.qdot}map { $exportedMethod }" +
                    "${typeName.qdot}toTypedArray()"
            },
        ),
        // TODO: Handle other collections
    )

    TypeMapping.advancedMappings += mapOf<(TypeName) -> Boolean, MappingOutput>(
        { type: TypeName -> type.isKotlinFunction() } to MappingOutput(
            exportType = { typeName, concreteTypeParameters ->
                val lambda = typeName as ParameterizedTypeName
                val args = lambda.typeArguments.dropLast(1).map { it.cached(concreteTypeParameters) }
                val returnType = lambda.typeArguments.last()
                LambdaTypeName.get(
                    parameters = args.map { arg ->
                        ParameterSpec.builder("", arg.exportedTypeName).build()
                    },
                    returnType = returnType
                )
            },
            importMethod = { targetName, typeName, concreteTypeParameters ->
                val lambda = typeName as ParameterizedTypeName
                val returnType = lambda.typeArguments.last()
                val namedArgs = lambda.typeArguments.dropLast(1)
                    .mapIndexed { index, typeName -> typeName to shortNamesForIndex(index) }
                val signature = namedArgs
                    .joinToString { (type, name) -> "$name: $type" }
                val importedArgs: String = namedArgs
                    .joinToString { (type, name) -> type.cached(concreteTypeParameters).exportedMethod(name) }

                """
                    |{ $signature ->
                    |$INDENTATION${
                    returnType.cached(concreteTypeParameters).importedMethod("$targetName($importedArgs)")
                }
                    |}
                """.trimMargin()
            },
            exportMethod = { targetName, typeName, concreteTypeParameters ->
                val lambda = typeName as ParameterizedTypeName
                val returnType = lambda.typeArguments.last()
                val namedArgs = lambda.typeArguments.dropLast(1)
                    .mapIndexed { index, typeName -> typeName to shortNamesForIndex(index) }
                val signature = namedArgs
                    .joinToString { (type, name) -> "$name: ${type.cached(concreteTypeParameters).exportedTypeName}" }
                val importedArgs: String = namedArgs
                    .joinToString { (type, name) -> type.cached(concreteTypeParameters).importedMethod(name) }

                """
                    |{ $signature ->
                    |$INDENTATION${
                    returnType.cached(concreteTypeParameters).exportedMethod("$targetName($importedArgs)")
                }
                    |}
                """.trimMargin()
            },
        )
    )
}

/**
 * Allow some Typenames to use generics notation.
 * For other types, it's forbidden and a `typelias` should be used instead.
 */
fun ParameterizedTypeName.isParameterizedAllowed(): Boolean {
    return (rawType.copy(false) in listOf(LIST, ARRAY, LONG_ARRAY)) ||
        rawType.isKotlinFunction()
}