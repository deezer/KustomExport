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
package deezer.kustomexport.compiler.js.mapping

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import deezer.kustomexport.compiler.firstParameterizedType
import deezer.kustomexport.compiler.js.ALL_KOTLIN_EXCEPTIONS
import deezer.kustomexport.compiler.js.FormatString
import deezer.kustomexport.compiler.js.mapping.TypeMapping.MappingOutput
import deezer.kustomexport.compiler.js.pattern.cached
import deezer.kustomexport.compiler.js.pattern.isKotlinFunction
import deezer.kustomexport.compiler.js.pattern.qdot
import deezer.kustomexport.compiler.js.toFormatString
import deezer.kustomexport.compiler.shortNamesForIndex

const val INDENTATION = "    "

val toLongArray = MemberName("kotlin.collections", "toLongArray")
val toTypedArray = MemberName("kotlin.collections", "toTypedArray")

fun initCustomMapping() {
    // Doc interop: https://kotlinlang.org/docs/js-to-kotlin-interop.html#primitive-arrays
    // No special mappings
    TypeMapping.mappings += (listOf(
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
    ) + ALL_KOTLIN_EXCEPTIONS).map { exportableType ->
        exportableType to MappingOutput(
            exportType = { _, _, _ -> exportableType },
            importMethod = { targetName, _, _, _ -> targetName },
            exportMethod = { targetName, _, _, _ -> targetName },
        )
    }

    TypeMapping.mappings += THROWABLE to MappingOutput(
        exportType = { _, _, _ -> THROWABLE },
        importMethod = { targetName, _, _, _ -> targetName },
        exportMethod = { targetName, _, _, _ -> targetName },
    )

    TypeMapping.mappings += mapOf<TypeName, MappingOutput>(
        // https://kotlinlang.org/docs/js-to-kotlin-interop.html#kotlin-types-in-javascript
        // doc: kotlin.Long is not mapped to any JavaScript object, as there is no 64-bit integer number type in JavaScript. It is emulated by a Kotlin class.
        LONG to MappingOutput(
            exportType = { _, _, _ -> DOUBLE },
            importMethod = { targetName, typeName, _, _ -> targetName + "${typeName.qdot}toLong()" },
            exportMethod = { targetName, typeName, _, _ -> targetName + "${typeName.qdot}toDouble()" },
        ),

        LONG_ARRAY to MappingOutput(
            exportType = { _, concreteTypeParameters, _ ->
                ARRAY.parameterizedBy(LONG.cached(concreteTypeParameters).exportedTypeName)
            },
            // TODO: improve perf by avoiding useless double transformation
            importMethod = { targetName, typeName, concreteTypeParameters, _ ->
                targetName +
                    "${typeName.qdot}map { " +
                    LONG.cached(concreteTypeParameters).importedMethod("it".toFormatString()) +
                    " }" +
                    "${typeName.qdot}%M()".toFormatString(toLongArray)
            },
            exportMethod = { targetName, typeName, concreteTypeParameters, _ ->
                targetName +
                    "${typeName.qdot}map { " +
                    LONG.cached(concreteTypeParameters).exportedMethod("it".toFormatString()) +
                    " }" +
                    "${typeName.qdot}%M()".toFormatString(toTypedArray)
            },
        ),

        ARRAY to MappingOutput(
            exportType = { typeName, concreteTypeParameters, _ ->
                ARRAY.parameterizedBy(
                    typeName.firstParameterizedType()
                        .cached(concreteTypeParameters).exportedTypeName
                )
            },
            importMethod = { targetName, typeName, concreteTypeParameters, _ ->
                val importMethod = typeName.firstParameterizedType().cached(concreteTypeParameters)
                    .importedMethod("it".toFormatString())
                if (importMethod.eq("it")) {
                    targetName
                } else {
                    targetName +
                        "${typeName.qdot}map { " +
                        importMethod +
                        " }${typeName.qdot}%M()".toFormatString(toTypedArray)
                }
            },
            exportMethod = { targetName, typeName, concreteTypeParameters, _ ->
                val exportMethod = typeName.firstParameterizedType()
                    .cached(concreteTypeParameters)
                    .exportedMethod("it".toFormatString())
                if (exportMethod.eq("it")) {
                    targetName
                } else {
                    targetName + "${typeName.qdot}map { ".toFormatString() + exportMethod +
                        " }${typeName.qdot}%M()".toFormatString(toTypedArray)
                }
            },
        ),

        LIST to MappingOutput(
            exportType = { _, _, typeArgs ->
                ARRAY.parameterizedBy(typeArgs[0].exportedTypeName)
            },
            importMethod = { targetName, typeName, _, typeArgs ->
                val importedMethod = typeArgs[0].importedMethod("it".toFormatString())
                targetName + "${typeName.qdot}map { " + importedMethod + " }"
            },
            exportMethod = { targetName, typeName, _, typeArgs ->
                val exportedMethod = typeArgs[0].exportedMethod("it".toFormatString())
                targetName +
                    "${typeName.qdot}map { " +
                    exportedMethod +
                    " }${typeName.qdot}%M()".toFormatString(toTypedArray)
            },
        ),
        // TODO: Handle other collections
    )

    TypeMapping.advancedMappings += mapOf<(TypeName) -> Boolean, MappingOutput>(
        { type: TypeName -> type.isKotlinFunction() } to MappingOutput(
            exportType = { _, _, typeArgs ->
                val returnType = typeArgs.last().exportedTypeName
                LambdaTypeName.get(
                    parameters = typeArgs.dropLast(1).map { arg ->
                        ParameterSpec.builder("", arg.exportedTypeName).build()
                    },
                    returnType = returnType
                )
            },
            importMethod = { targetName, _, _, typeArgs ->
                val returnType = typeArgs.last()

                if (typeArgs.size == 1) {
                    return@MappingOutput "{ ".toFormatString() +
                        returnType.importedMethod(targetName + "()") +
                        "}"
                }

                val namedArgs = typeArgs.dropLast(1)
                    .mapIndexed { index, tn -> tn to shortNamesForIndex(index) }

                val (lastNamedArgsType, lastNamedArgsName) = namedArgs.last()
                val signature = namedArgs.fold(FormatString("")) { acc, (type, name) ->
                    val separator = if (lastNamedArgsType == type && lastNamedArgsName == name) "" else ", "
                    acc + "$name: %T$separator".toFormatString(type.concreteTypeName)
                }
                val importedArgs =
                    namedArgs.fold(FormatString(if (namedArgs.size <= 1) "" else "\n")) { acc, (type, name) ->
                        val separator = if (lastNamedArgsType == type && lastNamedArgsName == name) "" else ",\n"
                        val indentation = if (namedArgs.size <= 1) "" else INDENTATION + INDENTATION
                        acc + indentation + type.exportedMethod(name.toFormatString()) + separator
                    }
                "{ ".toFormatString() + signature + " -> \n$INDENTATION" +
                    returnType.importedMethod(targetName + "(" + importedArgs + ")") +
                    "\n}"
            },
            exportMethod = { targetName, _, _, typeArgs ->
                val returnType = typeArgs.last()

                if (typeArgs.size == 1) {
                    return@MappingOutput "{ ".toFormatString() +
                        returnType.exportedMethod(targetName + "()") +
                        "}"
                }
                val namedArgs = typeArgs.dropLast(1)
                    .mapIndexed { index, tn -> tn to shortNamesForIndex(index) }

                val (lastNamedArgsType, lastNamedArgsName) = namedArgs.last()
                val signature = namedArgs.fold(FormatString("")) { acc, (type, name) ->
                    val separator = if (lastNamedArgsType == type && lastNamedArgsName == name) "" else ", "
                    acc + "$name: %T$separator".toFormatString(type.exportedTypeName)
                }

                val importedArgs =
                    namedArgs.fold(FormatString(if (namedArgs.size <= 1) "" else "\n")) { acc, (type, name) ->
                        val separator = if (lastNamedArgsType == type && lastNamedArgsName == name) "" else ",\n"
                        val indentation = if (namedArgs.size <= 1) "" else INDENTATION + INDENTATION
                        acc + indentation + type.importedMethod(name.toFormatString()) + separator
                    }

                "{ ".toFormatString() + signature + " -> \n" + INDENTATION +
                    returnType.exportedMethod(targetName + "(" + importedArgs + ")") +
                    "\n}"
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