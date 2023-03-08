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

@file:Suppress("DEPRECATION")

package deezer.kustomexport.compiler

import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ksp.toClassName
import deezer.kustomexport.KustomExportGenerics
import deezer.kustomexport.KustomGenerics

data class Generics(val exportName: String, val originType: ClassName, val typeParameters: List<ClassName>)

class GenericsVisitor(val resolver: Resolver) : KSVisitorVoid() {
    companion object {
        // Generics interface/class can generate wrappers if the type is "resolved" to a type (interface or class).
        // 1 generic can generate multiple wrappers, for example List<T> -> List<Int>, List<Float>
        // This map collects all resolutions for this build/compilation module (understand gradle module).
        val resolvedGenerics = mutableListOf<Generics>()
    }

    override fun visitFile(file: KSFile, data: Unit) {
        file.annotations// All file annotations
            // Get only the KustomExportGenerics one
            .filter { it.annotationType.resolve().declaration.qualifiedName?.asString() == KustomExportGenerics::class.qualifiedName }
            // Pick the first arguments matching 'exportGenerics' and flatmap all entries
            .flatMap {
                @Suppress("UNCHECKED_CAST")
                it.arguments
                    .first { arg -> arg.name?.asString() == KustomExportGenerics::exportGenerics.name }
                    .value as List<KSAnnotation>
            }
            .forEach { generics ->
                val kClass = generics.getArg<KSType>(KustomGenerics::kClass).toClassName()
                val typeParameters = generics.getArg<List<KSType>>(KustomGenerics::typeParameters).map { it.toClassName() }
                val name = generics.getArg<String?>(KustomGenerics::name) ?: kClass.simpleName
                resolvedGenerics.add(Generics(name, kClass, typeParameters))
            }
    }

    /*
    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit) {
        val target = (typeAlias.type.element?.parent as? KSTypeReference)?.resolve() ?: return
        // targetClassDeclaration is templated
        val targetClassDeclaration = target.declaration as? KSClassDeclaration ?: return

        // Contains "Template" list
        val targetTypeParameters = targetClassDeclaration.typeParameters
        val targetTypeNames = typeAlias.type.element?.typeArguments
            ?.map { it.type!!.resolve().toClassName() }
            ?.mapIndexed { index, className -> targetTypeParameters[index].name.asString() to className }
            ?: return
    }*/
}