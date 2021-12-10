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

package deezer.kustomexport.compiler

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSCallableReference
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSClassifierReference
import com.google.devtools.ksp.symbol.KSDeclaration
import com.google.devtools.ksp.symbol.KSDeclarationContainer
import com.google.devtools.ksp.symbol.KSDynamicReference
import com.google.devtools.ksp.symbol.KSFile
import com.google.devtools.ksp.symbol.KSFunctionDeclaration
import com.google.devtools.ksp.symbol.KSModifierListOwner
import com.google.devtools.ksp.symbol.KSNode
import com.google.devtools.ksp.symbol.KSParenthesizedReference
import com.google.devtools.ksp.symbol.KSPropertyAccessor
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSPropertyGetter
import com.google.devtools.ksp.symbol.KSPropertySetter
import com.google.devtools.ksp.symbol.KSReferenceElement
import com.google.devtools.ksp.symbol.KSTypeAlias
import com.google.devtools.ksp.symbol.KSTypeArgument
import com.google.devtools.ksp.symbol.KSTypeParameter
import com.google.devtools.ksp.symbol.KSTypeReference
import com.google.devtools.ksp.symbol.KSValueArgument
import com.google.devtools.ksp.symbol.KSValueParameter
import com.google.devtools.ksp.symbol.KSVisitor

class LoggerVisitor(private val environment: SymbolProcessorEnvironment) : KSVisitor<Unit, Unit> {
    private val debugFile = environment.codeGenerator.createNewFile(Dependencies(false), "com.greg", "debug", "txt")

    private fun log(str: String) {
        debugFile.appendText(str)
        environment.logger.warn(str)
    }

    override fun visitPropertyDeclaration(property: KSPropertyDeclaration, data: Unit) {
        log("visitPropertyDeclaration: $property $data")
    }

    override fun visitPropertyAccessor(accessor: KSPropertyAccessor, data: Unit) {
        log("visitPropertyAccessor: $accessor $data")
    }

    override fun visitAnnotated(annotated: KSAnnotated, data: Unit) {
        log("visitPropertyAccessor: $annotated $data")
    }

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit) {
        log("visitAnnotation: $annotation $data")
    }

    override fun visitCallableReference(reference: KSCallableReference, data: Unit) {
        log("visitCallableReference: $reference $data")
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        log("visitClassDeclaration: $classDeclaration $data")
    }

    override fun visitClassifierReference(reference: KSClassifierReference, data: Unit) {
        log("visitClassifierReference: $reference $data")
    }

    override fun visitDeclaration(declaration: KSDeclaration, data: Unit) {
        log("visitDeclaration: $declaration $data")
    }

    override fun visitDeclarationContainer(declarationContainer: KSDeclarationContainer, data: Unit) {
        log("visitDeclarationContainer: $declarationContainer $data")
    }

    override fun visitDynamicReference(reference: KSDynamicReference, data: Unit) {
        log("visitDynamicReference: $reference $data")
    }

    override fun visitFile(file: KSFile, data: Unit) {
        log("visitFile: $file $data")
    }

    override fun visitFunctionDeclaration(function: KSFunctionDeclaration, data: Unit) {
        log("visitFunctionDeclaration: $function $data")
    }

    override fun visitModifierListOwner(modifierListOwner: KSModifierListOwner, data: Unit) {
        log("visitModifierListOwner: $modifierListOwner $data")
    }

    override fun visitNode(node: KSNode, data: Unit) {
        log("visitNode: $node $data")
    }

    override fun visitParenthesizedReference(reference: KSParenthesizedReference, data: Unit) {
        log("visitParenthesizedReference: $reference $data")
    }

    override fun visitPropertyGetter(getter: KSPropertyGetter, data: Unit) {
        log("visitPropertyGetter: $getter $data")
    }

    override fun visitPropertySetter(setter: KSPropertySetter, data: Unit) {
        log("visitPropertySetter: $setter $data")
    }

    override fun visitReferenceElement(element: KSReferenceElement, data: Unit) {
        log("visitReferenceElement: $element $data")
    }

    override fun visitTypeAlias(typeAlias: KSTypeAlias, data: Unit) {
        log("visitTypeAlias: $typeAlias $data")
    }

    override fun visitTypeArgument(typeArgument: KSTypeArgument, data: Unit) {
        log("visitTypeArgument: $typeArgument $data")
    }

    override fun visitTypeParameter(typeParameter: KSTypeParameter, data: Unit) {
        log("visitTypeParameter: $typeParameter $data")
    }

    override fun visitTypeReference(typeReference: KSTypeReference, data: Unit) {
        log("visitTypeReference: $typeReference $data")
    }

    override fun visitValueArgument(valueArgument: KSValueArgument, data: Unit) {
        log("visitValueArgument: $valueArgument $data")
    }

    override fun visitValueParameter(valueParameter: KSValueParameter, data: Unit) {
        log("visitValueParameter: $valueParameter $data")
    }
}
