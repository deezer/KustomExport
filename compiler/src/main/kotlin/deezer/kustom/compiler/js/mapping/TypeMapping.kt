package deezer.kustom.compiler.js.mapping

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.pattern.asClassName
import deezer.kustom.compiler.js.pattern.qdot

object TypeMapping {
    val mappings = mutableMapOf<TypeName, MappingOutput>()

    init {
        // TODO: make it dynamic to open-source the project and be extensible enough
        initCustomMapping()
    }

    // Mapped with the domain/origin type as key
    data class MappingOutput(
        val exportType: (TypeName) -> TypeName,
        val importMethod: (TypeName) -> String, // Translates a domainType to an exportType
        val exportMethod: (TypeName) -> String, // Translates an exportType to a domainType
    )

    private fun getMapping(origin: TypeName): MappingOutput? =
        when (origin) {
            // Mapping is defined for non-nullable type only, nullable type follow the same transformations pattern than their non-null counterparts.
            is ParameterizedTypeName -> mappings[origin.rawType.copy(nullable = false)]
            is LambdaTypeName -> error("foo")
            else -> mappings[origin.copy(nullable = false)]
        }

    fun exportedType(origin: TypeName): TypeName {
        return getMapping(origin)?.exportType?.invoke(origin)?.copy(nullable = origin.isNullable)
            ?: run {
                // If no mapping, assume it's a project class, and it has a generated file
                assert(origin is ClassName) {
                    "$origin (${origin::class.java}) is not a ClassName instance, we can't ensure the portability yet."
                }

                return ClassName(
                    packageName = (origin as ClassName).packageName.jsPackage(),
                    simpleNames = listOf(origin.simpleName)
                ).copy(nullable = origin.isNullable)
            }
    }

    fun exportMethod(origin: TypeName): String {
        return getMapping(origin)?.exportMethod?.invoke(origin) ?: run {
            // If no mapping, assume it's a project class, and it has a generated file
            // origin.qdot + "${importExportPrefix(origin.asClassName().packageName)}jExport()"
            origin.qdot + "export${origin.asClassName().simpleName}()"
        }
    }

    fun importMethod(origin: TypeName): String {
        return getMapping(origin)?.importMethod?.invoke(origin) ?: run {
            // If no mapping, assume it's a project class, and it has a generated file
            // origin.qdot + "${importExportPrefix(origin.asClassName().packageName)}jImport()"
            origin.qdot + "import${origin.asClassName().simpleName}()"
        }
    }
}
