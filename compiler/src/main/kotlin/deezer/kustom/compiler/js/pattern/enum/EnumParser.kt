package deezer.kustom.compiler.js.pattern.enum

import com.google.devtools.ksp.symbol.KSClassDeclaration
import deezer.kustom.compiler.js.EnumDescriptor

fun parseEnum(classDeclaration: KSClassDeclaration): EnumDescriptor {
    return EnumDescriptor(
        packageName = classDeclaration.packageName.asString(),
        classSimpleName = classDeclaration.simpleName.asString(),
        entries = classDeclaration.declarations
            .filterIsInstance<KSClassDeclaration>()
            .map { EnumDescriptor.Entry(it.simpleName.asString()) }
            .toList()
    )
}
