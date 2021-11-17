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

package deezer.kustom.compiler.js.pattern

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.js.ClassDescriptor
import deezer.kustom.compiler.js.Descriptor
import deezer.kustom.compiler.js.EnumDescriptor
import deezer.kustom.compiler.js.FunctionDescriptor
import deezer.kustom.compiler.js.InterfaceDescriptor
import deezer.kustom.compiler.js.OriginTypeName
import deezer.kustom.compiler.js.ParameterDescriptor
import deezer.kustom.compiler.js.PropertyDescriptor
import deezer.kustom.compiler.js.SealedClassDescriptor
import deezer.kustom.compiler.js.SealedSubClassDescriptor
import deezer.kustom.compiler.js.SuperDescriptor
import deezer.kustom.compiler.js.TypeParameterDescriptor

@KotlinPoetKspPreview
fun parseClass(
    classDeclaration: KSClassDeclaration,
    forcedConcreteTypeParameters: List<Pair<String, TypeName>>? = null
): Descriptor? {
    val typeParamResolver = classDeclaration.typeParameters.toTypeParameterResolver()

    val concreteTypeParameters: MutableList<TypeParameterDescriptor> = mutableListOf()
    (forcedConcreteTypeParameters ?: emptyList())/* ?: typeParamResolver.parametersMap.values*/
        .forEach { (name, type) ->
            val className = try {
                type.asClassName()
            } catch (t: Throwable) {
                Logger.error(
                    "Cannot use @KustomException on a not concrete generics class.",
                    classDeclaration.typeParameters[0]
                )
                return null
            }
            concreteTypeParameters.add(
                TypeParameterDescriptor(
                    name = name,
                    origin = className.cached(concreteTypeParameters),
                )
            )
        }

    val packageName = classDeclaration.packageName.asString()
    val classSimpleName = classDeclaration.simpleName.asString()

    //val superTypes = classDeclaration.getAllSuperTypes()
    val superTypes = classDeclaration.superTypes
        .map {
            val superType = it.toTypeNamePatch(typeParamResolver)

            val declaration = it.resolve().declaration

            val superParams: List<ParameterDescriptor>? =
                if (declaration is KSClassDeclaration && declaration.primaryConstructor != null) {
                    // Impossible to get the value from a constructor to another. Ex:
                    // class Foo(): Bar(33) // Cannot retrieve 33 as it's only available at runtime
                    // So instead we create an empty constructor and all properties are abstract
                    /*
                    Logger.warn("Inheriting of $declaration with ${declaration.primaryConstructor}")
                    declaration.primaryConstructor!!.parameters.map { p ->
                        Logger.warn(" - ctor param - " + p.name + " : " + p.type.toTypeNamePatch(typeParamResolver))
                        ParameterDescriptor(p.name?.asString() ?: "", p.type.toTypeNamePatch(typeParamResolver))
                    }*/
                    emptyList()
                } else {
                    null
                }
            SuperDescriptor(superType.cached(concreteTypeParameters), superParams)
        }
        .toList()

    val constructorParams = classDeclaration.primaryConstructor?.parameters?.map {
        ParameterDescriptor(
            name = it.name!!.asString(),
            type = it.type.toTypeNamePatch(typeParamResolver).cached(concreteTypeParameters)
        )
    } ?: emptyList()

    val properties = classDeclaration.parseProperties(typeParamResolver, concreteTypeParameters)
    val functions = classDeclaration.parseFunctions(typeParamResolver, concreteTypeParameters)

    val isSealedClass = classDeclaration.modifiers.contains(Modifier.SEALED)

    val classKind = classDeclaration.classKind
    return when {
        classKind == ClassKind.INTERFACE -> {
            InterfaceDescriptor(
                packageName = packageName,
                classSimpleName = classSimpleName,
                concreteTypeParameters = concreteTypeParameters,
                supers = superTypes,
                properties = properties,
                functions = functions,
            )
        }
        classKind == ClassKind.CLASS && isSealedClass -> {
            val sealedSubClasses = classDeclaration.getSealedSubclasses().map { sub ->
                SealedSubClassDescriptor(
                    packageName = sub.packageName.asString(),
                    classSimpleName = sub.simpleName.asString(),
                )
            }.toList()
            return SealedClassDescriptor(
                packageName = packageName,
                classSimpleName = classSimpleName,
                constructorParams = constructorParams,
                properties = properties,
                functions = functions,
                subClasses = sealedSubClasses,
            )
        }
        classKind == ClassKind.CLASS -> ClassDescriptor(
            packageName = packageName,
            classSimpleName = classSimpleName,
            concreteTypeParameters = concreteTypeParameters,
            supers = superTypes,
            constructorParams = constructorParams,
            properties = properties,
            functions = functions,
        )
        classKind == ClassKind.ENUM_CLASS -> EnumDescriptor(
            packageName = packageName,
            classSimpleName = classSimpleName,
            entries = classDeclaration.declarations
                .filterIsInstance<KSClassDeclaration>()
                .map { EnumDescriptor.Entry(it.simpleName.asString()) }
                .toList()
        )
        else -> error("The compiler can't handle '${classDeclaration.classKind}' class kind")
    }
}

private val nonExportableFunctions = listOf(
    "<init>",
    "equals",
    "hashCode",
    "toString",
    "copy"
) + (1..30).map { "component$it" }

@OptIn(KotlinPoetKspPreview::class)
fun KSClassDeclaration.parseFunctions(
    typeParamResolver: TypeParameterResolver,
    concreteTypeParameters: MutableList<TypeParameterDescriptor>
): List<FunctionDescriptor> {
    val declaredNames = getDeclaredFunctions().mapNotNull { it.simpleName }
    return getAllFunctions()
        .filter { it.simpleName.asString() !in nonExportableFunctions }
        .filter { it.isPublic() }
        .map { func ->
            FunctionDescriptor(
                name = func.simpleName.asString(),
                isOverride = func.findOverridee() != null || !declaredNames.contains(func.simpleName),
                returnType = func.returnType!!.toTypeNamePatch(typeParamResolver).cached(concreteTypeParameters),
                parameters = func.parameters.map { p ->
                    ParameterDescriptor(
                        name = p.name?.asString() ?: TODO("not sure what we want here"),
                        type = p.type.toTypeNamePatch(typeParamResolver).cached(concreteTypeParameters),
                    )
                }
            )
        }.toList()
}

@OptIn(KotlinPoetKspPreview::class)
fun KSClassDeclaration.parseProperties(
    typeParamResolver: TypeParameterResolver,
    concreteTypeParameters: MutableList<TypeParameterDescriptor>
): List<PropertyDescriptor> {
    val declaredNames = getDeclaredProperties().mapNotNull { it.simpleName }
    return getAllProperties().mapNotNull { prop ->
        if (prop.isPrivate()) {
            null // Cannot be accessed
        } else {
            val type = prop.type.toTypeNamePatch(typeParamResolver)
            // Retrieve the names of function arguments, like: (*MyName*: String) -> Unit
            // Problem: we use KotlinPoet TypeName for the mapping, and it doesn't have the value available.
            val namedArgs: List<String> = if (type.isKotlinFunction()) {
                prop.type.resolve().arguments.map { arg ->
                    arg.annotations.firstOrNull { it.shortName.asString() == "ParameterName" }?.arguments?.get(0)?.value.toString()
                }
            } else emptyList()

            PropertyDescriptor(
                name = prop.simpleName.asString(),
                type = type.cached(concreteTypeParameters),
                isMutable = prop.isMutable,
                isOverride = prop.findOverridee() != null || !declaredNames.contains(prop.simpleName),
                // namedArgs = namedArgs
            )
        }
    }.toList()
}

fun TypeName.cached(concreteTypeParameters: List<TypeParameterDescriptor>) = OriginTypeName(this, concreteTypeParameters)