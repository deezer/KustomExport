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

@file:OptIn(KotlinPoetKspPreview::class)

package deezer.kustomexport.compiler.js.pattern

import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import deezer.kustomexport.compiler.Logger
import deezer.kustomexport.compiler.js.ALL_KOTLIN_EXCEPTIONS
import deezer.kustomexport.compiler.js.ClassDescriptor
import deezer.kustomexport.compiler.js.Descriptor
import deezer.kustomexport.compiler.js.EnumDescriptor
import deezer.kustomexport.compiler.js.FunctionDescriptor
import deezer.kustomexport.compiler.js.InterfaceDescriptor
import deezer.kustomexport.compiler.js.ParameterDescriptor
import deezer.kustomexport.compiler.js.PropertyDescriptor
import deezer.kustomexport.compiler.js.SealedClassDescriptor
import deezer.kustomexport.compiler.js.SealedSubClassDescriptor
import deezer.kustomexport.compiler.js.SuperDescriptor
import deezer.kustomexport.compiler.js.TopLevelFunctionDescriptor
import deezer.kustomexport.compiler.js.TypeParameterDescriptor
import deezer.kustomexport.compiler.js.ValueClassDescriptor
import deezer.kustomexport.compiler.js.mapping.OriginTypeName

fun parseFunction(
    function: KSFunctionDeclaration,
    forcedConcreteTypeParameters: List<Pair<String, TypeName>>? = null,
): TopLevelFunctionDescriptor = TopLevelFunctionDescriptor(
    packageName = function.packageName.asString(),
    function = function.toDescriptor(
        sequenceOf(),
        function.typeParameters.toTypeParameterResolver(),
        buildConcreteTypeParameters(forcedConcreteTypeParameters) { function.typeParameters[0] }
    )
)

@KotlinPoetKspPreview
fun parseClass(
    classDeclaration: KSClassDeclaration,
    forcedConcreteTypeParameters: List<Pair<String, TypeName>>? = null,
    exportedClassSimpleName: String
): Descriptor {
    if (classDeclaration.isThrowable()) {
        error(
            "Cannot parse a class that is Throwable: " +
                (classDeclaration.qualifiedName?.asString() ?: classDeclaration.simpleName.asString())
        )
    }
    val typeParamResolver = classDeclaration.typeParameters.toTypeParameterResolver()

    val concreteTypeParameters: MutableList<TypeParameterDescriptor> =
        buildConcreteTypeParameters(forcedConcreteTypeParameters) { classDeclaration.typeParameters[0] }

    val packageName = classDeclaration.packageName.asString()
    val classSimpleName = classDeclaration.simpleName.asString()

    val superTypes = classDeclaration.superTypes
        .map { superType ->
            val superTypeName = superType.toTypeNamePatch(typeParamResolver).cached(concreteTypeParameters)

            val declaration = superType.resolve().declaration
            //val qualifiedName = (declaration as? KSClassDeclaration)?.qualifiedName
            //val isKotlinException = ALL_KOTLIN_EXCEPTIONS.any { it.canonicalName == qualifiedName }

            if (declaration is KSClassDeclaration) {
                val ctors = declaration.getConstructors().toList()
                val superParams = if (ctors.isNotEmpty()) emptyList<ParameterDescriptor>() else null
                val isSealed = declaration.modifiers.contains(Modifier.SEALED)
                SuperDescriptor(superTypeName, superParams, isSealed)
            } else {
                SuperDescriptor(superTypeName, null, false)
            }
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

    val isSealed = classDeclaration.modifiers.contains(Modifier.SEALED)
    val isOpen = classDeclaration.modifiers.contains(Modifier.OPEN)
    val isValueClass =
        classDeclaration.classKind == ClassKind.CLASS && classDeclaration.modifiers.contains(Modifier.VALUE)

    val classKind = classDeclaration.classKind
    return when {
        classKind == ClassKind.INTERFACE -> {
            InterfaceDescriptor(
                packageName = packageName,
                classSimpleName = classSimpleName,
                exportedClassSimpleName = exportedClassSimpleName,
                concreteTypeParameters = concreteTypeParameters,
                supers = superTypes,
                properties = properties,
                functions = functions,
            )
        }
        isValueClass -> {
            if (constructorParams.size != 1) Logger.error("value class with more than 1 constructor param is not exportable")
            return ValueClassDescriptor(
                packageName = packageName,
                classSimpleName = classSimpleName,
                inlinedType = constructorParams[0]
            )
        }
        classKind == ClassKind.CLASS && isSealed -> {
            val sealedSubClasses = classDeclaration.getSealedSubclasses().map { sub ->
                SealedSubClassDescriptor(
                    packageName = sub.packageName.asString(),
                    classSimpleName = sub.simpleName.asString(),
                )
            }.toList()
            return SealedClassDescriptor(
                packageName = packageName,
                classSimpleName = classSimpleName,
                supers = superTypes,
                constructorParams = constructorParams,
                properties = properties,
                functions = functions,
                subClasses = sealedSubClasses,
            )
        }
        classKind == ClassKind.CLASS || classKind == ClassKind.OBJECT -> ClassDescriptor(
            packageName = packageName,
            classSimpleName = classSimpleName,
            exportedClassSimpleName = exportedClassSimpleName,
            isOpen = isOpen,
            isObject = classKind == ClassKind.OBJECT,
            concreteTypeParameters = concreteTypeParameters,
            supers = superTypes,
            constructorParams = constructorParams,
            properties = properties,
            functions = functions,
        )
        classKind == ClassKind.ENUM_CLASS -> EnumDescriptor(
            packageName = packageName,
            classSimpleName = classSimpleName,
            properties = properties,
            entries = classDeclaration.declarations
                .filterIsInstance<KSClassDeclaration>()
                .map { EnumDescriptor.Entry(it.simpleName.asString()) }
                .toList()
        )
        else -> error("The compiler can't handle '${classDeclaration.classKind}' class kind")
    }
}

private fun buildConcreteTypeParameters(
    forcedConcreteTypeParameters: List<Pair<String, TypeName>>?,
    firstTypeParameterProvider: () -> KSTypeParameter
): MutableList<TypeParameterDescriptor> {
    val concreteTypeParameters: MutableList<TypeParameterDescriptor> = mutableListOf()
    (forcedConcreteTypeParameters ?: emptyList())/* ?: typeParamResolver.parametersMap.values*/
        .forEach { (name, type) ->
            val className = try {
                type.asClassName()
            } catch (t: Throwable) {
                Logger.error(
                    "Cannot use @KustomException on a not concrete generics class.",
                    firstTypeParameterProvider()
                )
                error(t)
            }
            concreteTypeParameters.add(
                TypeParameterDescriptor(
                    name = name,
                    origin = className.cached(concreteTypeParameters),
                )
            )
        }
    return concreteTypeParameters
}

private val nonExportableFunctions = listOf(
    "<init>",
    "equals",
    "hashCode",
    "toString",
    "copy",

    // For exceptions :
    "getLocalizedMessage",
    "initCause",
    "printStackTrace",
    "fillInStackTrace",
    "getStackTrace",
    "setStackTrace",
    "addSuppressed",
    "getSuppressed",

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
            func.toDescriptor(declaredNames, typeParamResolver, concreteTypeParameters)
        }.toList()
}

private fun KSFunctionDeclaration.toDescriptor(
    declaredNames: Sequence<KSName>,
    typeParamResolver: TypeParameterResolver,
    concreteTypeParameters: MutableList<TypeParameterDescriptor>
) =
    FunctionDescriptor(
        name = simpleName.asString(),
        isOverride = findOverridee() != null || !declaredNames.contains(simpleName),
        isSuspend = modifiers.contains(Modifier.SUSPEND),
        returnType = returnType!!.toTypeNamePatch(typeParamResolver).cached(concreteTypeParameters),
        parameters = parameters.map { p ->
            ParameterDescriptor(
                name = p.name?.asString() ?: TODO("not sure what we want here"),
                type = p.type.toTypeNamePatch(typeParamResolver).cached(concreteTypeParameters),
            )
        }
    )

@OptIn(KotlinPoetKspPreview::class)
fun KSClassDeclaration.parseProperties(
    typeParamResolver: TypeParameterResolver,
    concreteTypeParameters: MutableList<TypeParameterDescriptor>
): List<PropertyDescriptor> {
    val declaredNames = getDeclaredProperties().mapNotNull { it.simpleName }
    return getAllProperties().mapNotNull { prop ->
        // TODO: rework configuration by naming
        val classExtendsException = this.simpleName.asString().endsWith("Exception")
        if (prop.isPrivate()) {
            null // Cannot be accessed
        } else {
            val type = prop.type.toTypeNamePatch(typeParamResolver)
            // Retrieve the names of function arguments, like: (*MyName*: String) -> Unit
            // Problem: we use KotlinPoet TypeName for the mapping, and it doesn't have the value available.
            /*val namedArgs: List<String> = if (type.isKotlinFunction()) {
                prop.type.resolve().arguments.map { arg ->
                    arg.annotations.firstOrNull { it.shortName.asString() == "ParameterName" }?.arguments?.get(0)?.value.toString()
                }
            } else emptyList()
            */

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

fun KSClassDeclaration.isThrowable(): Boolean {
    superTypes
        .forEach { superType ->
            val superTypeResolved = superType.resolve()
            if (superTypeResolved.toClassName() in ALL_KOTLIN_EXCEPTIONS) {
                return true
            }

            val declaration = superTypeResolved.declaration
            if (declaration is KSClassDeclaration && declaration.isThrowable()) {
                return true
            }
        }
    return false
}

fun TypeName.cached(concreteTypeParameters: List<TypeParameterDescriptor>) =
    OriginTypeName(this, concreteTypeParameters)