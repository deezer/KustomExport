package deezer.kustom.compiler.js.pattern

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.firstParameterizedType
import deezer.kustom.compiler.js.ClassDescriptor
import deezer.kustom.compiler.js.InterfaceDescriptor
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.mapping.EXCEPTION
import deezer.kustom.compiler.js.mapping.EXCEPTION_JS

// TODO: Imports of properties are not always required by KotlinPoet as it can inline

fun FileSpec.Builder.autoImport(origin: InterfaceDescriptor): FileSpec.Builder {
    return autoImport(
        origin.superTypes +
            origin.properties.map { it.type } +
            origin.functions.flatMap { it.parameters }.map { it.type } +
            origin.functions.map { it.returnType }
    )
}

fun FileSpec.Builder.autoImport(origin: ClassDescriptor): FileSpec.Builder {
    return autoImport(origin.superTypes + origin.properties.map { it.type })
}

private fun FileSpec.Builder.autoImport(types: List<TypeName>): FileSpec.Builder {
    types.flatMap { type ->
        if (type is ParameterizedTypeName) {
            listOf(type, type.firstParameterizedType()) // TODO: limiting every classes to 1 generics
        } else {
            listOf(type)
        }
    }
        .distinct()
        .map { it.asClassName() }
        .filterNot { it.isFromStdlib() && it != EXCEPTION }
        .forEach { className ->
            Logger.warn(" ----- className=$className (is ex = ${className == EXCEPTION})")
            if (className == EXCEPTION) { // Special case where we cannot generate these methods
                addAliasedImport(EXCEPTION, "Common${className.simpleName}")
                if (packageName != EXCEPTION_JS.packageName) { // Useless import if same package
                    addImport(EXCEPTION_JS.packageName, "import")
                    addImport(EXCEPTION_JS.packageName, "export")
                }
            } else {
                addAliasedImport(className, "Common${className.simpleName}")
                val jsPackage = className.packageName.jsPackage()
                // if (packageName != jsPackage) { // Useless import if same package
                // addAliasedImport(MemberName(jsPackage, "import"), "${importExportPrefix(jsPackage)}Import")
                // addAliasedImport(MemberName(jsPackage, "export"), "${importExportPrefix(jsPackage)}Export")
                addImport(jsPackage, "import${className.simpleName}")
                addImport(jsPackage, "export${className.simpleName}")
                // }
            }
        }
    return this
}

// Reduce the package name to give a distinct name when there is multiple ext func
// with the same name in different packages creating ambiguity
fun importExportPrefix(packageName: String): String {
    return packageName.split(".")
        .map { it.first() }
        .joinToString("")
}

private fun TypeName.isFromStdlib() =
    (this as? ClassName)?.packageName?.startsWith("kotlin")
        ?: (this as? ParameterizedTypeName)?.rawType?.packageName?.startsWith("kotlin")
        ?: false
