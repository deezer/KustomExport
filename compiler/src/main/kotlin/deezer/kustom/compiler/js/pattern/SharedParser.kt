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
            val type = prop.type.toTypeNamePatch()
            // Retrieve the names of function arguments, like: (*MyName*: String) -> Unit
            // Problem: we use KotlinPoet TypeName for the mapping, and it doesn't have the value available right now.
            // Keeping this snippet for now, not sure if it would make more sense in KotlinPoet-Ksp eventually...
            val namedArgs: List<String> = if (type.isKotlinFunction()) {
                prop.type.resolve().arguments.map { arg ->
                    arg.annotations.firstOrNull { it.shortName.asString() == "ParameterName" }?.arguments?.get(0)?.value.toString()
                }
            } else emptyList()

            PropertyDescriptor(
                name = prop.simpleName.asString(),
                type = type,
                isMutable = prop.isMutable,
                isOverride = prop.findOverridee() != null || !declaredNames.contains(prop.simpleName),
                //namedArgs = namedArgs
            )
        }
    }.toList()
}
