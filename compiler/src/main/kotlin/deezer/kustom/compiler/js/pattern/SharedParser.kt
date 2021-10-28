package deezer.kustom.compiler.js.pattern

import com.google.devtools.ksp.getDeclaredFunctions
import com.google.devtools.ksp.getDeclaredProperties
import com.google.devtools.ksp.isPrivate
import com.google.devtools.ksp.isPublic
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.squareup.kotlinpoet.ksp.KotlinPoetKspPreview
import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.js.FunctionDescriptor
import deezer.kustom.compiler.js.ParameterDescriptor
import deezer.kustom.compiler.js.PropertyDescriptor

private val nonExportableFunctions = listOf(
    "<init>",
    "equals",
    "hashCode",
    "toString",
    "copy"
) + (1..30).map { "component$it" }

@OptIn(KotlinPoetKspPreview::class)
fun KSClassDeclaration.parseFunctions(): List<FunctionDescriptor> {
    val declaredNames = getDeclaredFunctions().mapNotNull { it.simpleName }
    return getAllFunctions()
        .filter { it.simpleName.asString() !in nonExportableFunctions }
        .also { it.forEach { f -> Logger.warn("Function $f") } }
        .filter { it.isPublic() }
        .map { func ->
            FunctionDescriptor(
                name = func.simpleName.asString(),
                isOverride = func.findOverridee() != null || !declaredNames.contains(func.simpleName),
                returnType = func.returnType.toTypeNamePatch(),
                parameters = func.parameters.map { p ->

                    /*
                    val maybeName = try { p.name?.asString() } catch (t:Throwable) { null }
                    logger.error(maybeName ?: "null name")
                    logger.warn(
                        "param $maybeName of type " +
                            "${p.type} < ${
                                p.type.element?.typeArguments?.map { it.type.toTypeNamePatch() }?.joinToString()
                            } > " +
                            "-> ${p.type.toTypeNamePatch()}"
                    )*/

                    ParameterDescriptor(
                        name = p.name?.asString() ?: TODO("not sure what we want here"),
                        type = p.type.toTypeNamePatch(),
                    )
                }
            )
        }.toList()
}

@OptIn(KotlinPoetKspPreview::class)
fun KSClassDeclaration.parseProperties(): List<PropertyDescriptor> {
    val declaredNames = getDeclaredProperties().mapNotNull { it.simpleName }
    return getAllProperties().mapNotNull { prop ->
        if (prop.isPrivate()) {
            null // Cannot be accessed
        } else {
            PropertyDescriptor(
                name = prop.simpleName.asString(),
                type = prop.type.toTypeNamePatch(),
                isMutable = prop.isMutable,
                isOverride = prop.findOverridee() != null || !declaredNames.contains(prop.simpleName)
            )
        }
    }.toList()
}
