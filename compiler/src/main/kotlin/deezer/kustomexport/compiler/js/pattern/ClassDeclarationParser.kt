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

@file:OptIn(KotlinPoetKspPreview::class, KotlinPoetKspPreview::class)

package deezer.kustomexport.compiler.js.pattern

import com.google.devtools.ksp.getAllSuperTypes
import com.google.devtools.ksp.getConstructors
import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSName
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.Modifier
import com.squareup.kotlinpoet.ANY
import com.squareup.kotlinpoet.THROWABLE
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import com.squareup.kotlinpoet.ksp.TypeParameterResolver
import com.squareup.kotlinpoet.ksp.toClassName
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.toTypeParameterResolver
import deezer.kustomexport.KustomExport
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
    classDeclaration.assertNotThrowable(classDeclaration)
    val typeParamResolver = classDeclaration.typeParameters.toTypeParameterResolver()

    val concreteTypeParameters: MutableList<TypeParameterDescriptor> =
        buildConcreteTypeParameters(forcedConcreteTypeParameters) { classDeclaration.typeParameters[0] }

    val packageName = classDeclaration.packageName.asString()
    val classSimpleName = classDeclaration.simpleName.asString()

    val superTypes = classDeclaration.superTypes
        .mapNotNull { superType ->
            // KSP 1.6.20-1.0.5 now returns kotlin.Any even if we're parsing an interface.
            // That doesn't make sense for us, so we're just ignoring them
            // https://github.com/google/ksp/issues/815#issuecomment-1105676539
            val typeName = superType.toTypeNamePatch(typeParamResolver)
            if (typeName == ANY) return@mapNotNull null
            // End of trick


            val isKustomExportAnnotated = superType.isKustomExportAnnotated()
            val superTypeName = typeName.cached(concreteTypeParameters, isKustomExportAnnotated)

            val declaration = superType.resolve().declaration
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
        it.type.assertNotThrowable(it)
        val isKustomExportAnnotated = it.type.isKustomExportAnnotated()
        val typeArgs = it.type.resolve().arguments.map {
            it.toTypeName(typeParamResolver)
                .cached(concreteTypeParameters, it.type?.isKustomExportAnnotated() ?: false)
        }
        ParameterDescriptor(
            name = it.name!!.asString(),
            type = it.type.toTypeNamePatch(typeParamResolver).cached(
                concreteTypeParameters,
                isKustomExportAnnotated,
                typeArgs,
            )
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
                    message = "Cannot use @KustomExport on a not concrete generics class.",
                    symbol = firstTypeParameterProvider()
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
): FunctionDescriptor {
    returnType?.assertNotThrowable(this)
    val isReturnKustomExportAnnotated = returnType!!.isKustomExportAnnotated()

    val returnTypeArgs = returnType!!.resolve().arguments.map {
        it.toTypeName(typeParamResolver)
            .cached(concreteTypeParameters, it.type?.isKustomExportAnnotated() ?: false)
    }
    return FunctionDescriptor(
        name = simpleName.asString(),
        isOverride = findOverridee() != null || !declaredNames.contains(simpleName),
        isSuspend = modifiers.contains(Modifier.SUSPEND),
        returnType = returnType!!.toTypeNamePatch(typeParamResolver)
            .cached(concreteTypeParameters, isReturnKustomExportAnnotated, returnTypeArgs),
        parameters = parameters.map { p ->
            p.type.assertNotThrowable(p)

            val isKustomExportAnnotated = p.type.isKustomExportAnnotated()
            val typeResolved = p.type.resolve()

            val typeArgs = typeResolved.arguments.map {
                // Just logging here
                it.type?.let { t ->
                    t.resolve().declaration.annotations.forEach { a ->
                        val ksDeclaration = a.annotationType.resolve().declaration
                    }
                }

                it.toTypeName(typeParamResolver)
                    .cached(concreteTypeParameters, it.type?.isKustomExportAnnotated() ?: false)
            }

            ParameterDescriptor(
                name = p.name?.asString() ?: TODO("not sure what we want here"),
                type = p.type.toTypeNamePatch(typeParamResolver)
                    .cached(concreteTypeParameters, isKustomExportAnnotated, typeArgs),
            )
        }
    )
}

@OptIn(KotlinPoetKspPreview::class)
fun KSClassDeclaration.parseProperties(
    typeParamResolver: TypeParameterResolver,
    concreteTypeParameters: MutableList<TypeParameterDescriptor>
): List<PropertyDescriptor> {
    val declaredNames = getDeclaredProperties().mapNotNull { it.simpleName }
    return getAllProperties().mapNotNull { prop ->
        // TODO: rework configuration by naming
        if (prop.isPrivate()) {
            null // Cannot be accessed
        } else {
            prop.type.assertNotThrowable(prop)

            val type = prop.type.toTypeNamePatch(typeParamResolver)
            // Retrieve the names of function arguments, like: (*MyName*: String) -> Unit
            // Problem: we use KotlinPoet TypeName for the mapping, and it doesn't have the value available.
            /*val namedArgs: List<String> = if (type.isKotlinFunction()) {
                prop.type.resolve().arguments.map { arg ->
                    arg.annotations.firstOrNull { it.shortName.asString() == "ParameterName" }?.arguments?.get(0)?.value.toString()
                }
            } else emptyList()
            */
            val isKustomExportAnnotated = prop.type.isKustomExportAnnotated()
            val typeResolved = prop.type.resolve()
            val typeArgs = typeResolved.arguments.map {
                it.toTypeName(typeParamResolver)
                    .cached(concreteTypeParameters, it.type?.isKustomExportAnnotated() ?: false)
            }
            PropertyDescriptor(
                name = prop.simpleName.asString(),
                type = type.cached(concreteTypeParameters, isKustomExportAnnotated, typeArgs),
                isMutable = prop.isMutable,
                isOverride = prop.findOverridee() != null || !declaredNames.contains(prop.simpleName),
                // namedArgs = namedArgs
            )
        }
    }.toList()
}

fun KSTypeReference.assertNotThrowable(symbol: KSNode) {
    val decl = resolve().declaration
    if (decl is KSClassDeclaration) {
        decl.assertNotThrowable(symbol)
    }
}

fun KSClassDeclaration.assertNotThrowable(symbol: KSNode) {
    if (isThrowable() && qualifiedName?.asString() != THROWABLE.canonicalName) {
        Logger.error(
            message = "Cannot export ${this.qualifiedName?.asString() ?: simpleName.asString()}.\n" +
                "You cannot export an Exception or subclasses of Exception but you can export a Throwable instead.",
            symbol = symbol
        )
    }
}

fun KSTypeReference.isKustomExportAnnotated(): Boolean {
    return resolve().declaration.annotations.any { a ->
        val ksDeclaration = a.annotationType.resolve().declaration
        ksDeclaration.qualifiedName?.asString() == KustomExport::class.qualifiedName
    }
}

fun KSClassDeclaration.isThrowable(): Boolean {
    if (toClassName() in ALL_KOTLIN_EXCEPTIONS) {
        return true
    }

    getAllSuperTypes()
        .forEach { superType ->
            if (superType.toClassName() in ALL_KOTLIN_EXCEPTIONS) {
                return true
            }

            val declaration = superType.declaration
            if (declaration is KSClassDeclaration && declaration.isThrowable()) {
                return true
            }
        }
    return false
}

fun TypeName.cached(
    concreteTypeParameters: List<TypeParameterDescriptor>,
    isKustomExportAnnotated: Boolean = false,
    typeArgs: List<OriginTypeName> = emptyList()
) = OriginTypeName.invoke(this, concreteTypeParameters, isKustomExportAnnotated, typeArgs)
