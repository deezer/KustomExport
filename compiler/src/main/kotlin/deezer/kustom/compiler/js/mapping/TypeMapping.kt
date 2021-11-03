package deezer.kustom.compiler.js.mapping

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import deezer.kustom.compiler.CompilerArgs
import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.pattern.asClassName
import deezer.kustom.compiler.js.pattern.qdot

object TypeMapping {
    val mappings = mutableMapOf<TypeName, MappingOutput>()
    val advancedMappings = mutableMapOf<(TypeName) -> Boolean, MappingOutput>()

    init {
        // TODO: make it dynamic to open-source the project and be extensible enough
        initCustomMapping()
    }

    // Mapped with the domain/origin type as key
    data class MappingOutput(
        val exportType: (TypeName) -> TypeName,
        val importMethod: (targetName: String, TypeName) -> String, // Translates a domainType to an exportType
        val exportMethod: (targetName: String, TypeName) -> String, // Translates an exportType to a domainType
    )

    private fun getMapping(origin: TypeName): MappingOutput? {
        Logger.warn("getMapping $origin (${origin::class}")
        return when (origin) {
            // Simple mapping is defined for non-nullable type only,
            // nullable type follow the same transformations pattern than their non-null counterparts.
            // Advanced mappings requires the original type without transformation
            is ParameterizedTypeName -> {
                mappings[origin.rawType.copy(nullable = false)]
                    ?: advancedMappings.firstNotNullOfOrNull { if (it.key(origin)) it.value else null }
            }
            else ->
                mappings[origin.copy(nullable = false)]
                    ?: advancedMappings.firstNotNullOfOrNull { if (it.key(origin)) it.value else null }
        }
    }

    fun exportedType(origin: TypeName): TypeName {
        return getMapping(origin)?.exportType?.invoke(origin)?.copy(nullable = origin.isNullable)
            ?: run {
                // If no mapping, assume it's a project class, and it has a generated file
                assert(origin is ClassName) {
                    "$origin (${origin::class.java}) is not a ClassName instance, we can't ensure the portability yet."
                }
                origin as ClassName

                return ClassName(
                    packageName = if (CompilerArgs.erasePackage) "" else origin.packageName.jsPackage(),
                    simpleNames = listOf(origin.simpleName)
                ).copy(nullable = origin.isNullable)
            }
    }

    fun exportMethod(targetName: String, origin: TypeName): String {
        return getMapping(origin)?.exportMethod?.invoke(targetName, origin) ?: run {
            // If no mapping, assume it's a project class, and it has a generated file
            "$targetName${origin.qdot}export${origin.asClassName().simpleName}()"
        }
    }

    fun importMethod(targetName: String, origin: TypeName): String {
        return getMapping(origin)?.importMethod?.invoke(targetName, origin) ?: run {
            // If no mapping, assume it's a project class, and it has a generated file
            "$targetName${origin.qdot}import${origin.asClassName().simpleName}()"
        }
    }
}
