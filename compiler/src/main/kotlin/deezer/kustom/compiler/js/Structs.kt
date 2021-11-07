package deezer.kustom.compiler.js

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.TypeVariableName

// Describes format used between the parser and the writer
// Should avoid references to KSP or KotlinPoet so that we can easily swap to other tools if required, but
// describing a TypeName is complex enough that take some shortcuts.

data class PropertyDescriptor(
    val name: String,
    val type: TypeName,
    val isMutable: Boolean,
    val isOverride: Boolean,
    // val namedArgs: List<String>, // Names of property arguments, for lambda (not sure if we want that here)
)

data class ParameterDescriptor(
    val name: String,
    val type: TypeName,
)

data class FunctionDescriptor(
    val name: String,
    val isOverride: Boolean,
    val returnType: TypeName,
    val parameters: List<ParameterDescriptor>,
)

sealed class Descriptor

data class InterfaceDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val typeParameters: Map<String, TypeVariableName>,
    val superTypes: List<TypeName>,
    val properties: List<PropertyDescriptor>,
    val functions: List<FunctionDescriptor>,
) : Descriptor() {
    // Useful for aliased imports (don't care about type parameters)
    fun asClassName() = ClassName(packageName, classSimpleName)
    fun asTypeName() = ClassName(packageName, classSimpleName).let {
        if (typeParameters.isNotEmpty()) {
            it.parameterizedBy(typeParameters.values.toList())
        } else it
    }
}

data class ClassDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val typeParameters: Map<String, TypeVariableName>,
    val superTypes: List<TypeName>,
    val constructorParams: List<ParameterDescriptor>,
    val properties: List<PropertyDescriptor>,
    val functions: List<FunctionDescriptor>
) : Descriptor() {
    // Useful for aliased imports (don't care about type parameters)
    fun asClassName() = ClassName(packageName, classSimpleName)
    fun asTypeName() = ClassName(packageName, classSimpleName).let {
        if (typeParameters.isNotEmpty()) {
            it.parameterizedBy(typeParameters.values.toList())
        } else it
    }
}

data class EnumDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val entries: List<Entry>
) : Descriptor() {
    data class Entry(val name: String)
}
