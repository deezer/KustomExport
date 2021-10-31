package deezer.kustom.compiler.js

import com.squareup.kotlinpoet.TypeName

// Describes format used between the parser and the writer
// Should avoid references to KSP or KotlinPoet so that we can easily swap to other tools if required.

data class PropertyDescriptor(
    val name: String,
    val type: TypeName, // Shortcut: type should be recursive and handle nullability+multiple generics
    val isMutable: Boolean,
    val isOverride: Boolean,
    // val namedArgs: List<String>, // Names of property arguments, for lambda (not sure if we want that here)
)

data class ParameterDescriptor(
    val name: String,
    val type: TypeName, // Shortcut: type should be recursive and handle nullability+multiple generics
)

data class FunctionDescriptor(
    val name: String,
    val isOverride: Boolean,
    val returnType: TypeName, // Shortcut: type should be recursive and handle nullability+multiple generics
    val parameters: List<ParameterDescriptor>,
)

data class InterfaceDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val superTypes: List<TypeName>, // Shortcut: allow automatic imports via KotlinPoet
    val properties: List<PropertyDescriptor>,
    val functions: List<FunctionDescriptor>,
)

data class ClassDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val superTypes: List<TypeName>, // Shortcut: allow automatic imports via KotlinPoet
    val constructorParams: List<ParameterDescriptor>,
    val properties: List<PropertyDescriptor>,
    val functions: List<FunctionDescriptor>
)

data class EnumDescriptor(
    val packageName: String,
    val classSimpleName: String,
    val entries: List<Entry>
) {
    data class Entry(val name: String)
}
