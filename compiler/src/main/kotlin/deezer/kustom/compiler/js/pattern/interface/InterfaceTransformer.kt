package deezer.kustom.compiler.js.pattern.`interface`

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.TypeVariableName
import deezer.kustom.compiler.CompilerArgs
import deezer.kustom.compiler.Logger
import deezer.kustom.compiler.js.InterfaceDescriptor
import deezer.kustom.compiler.js.MethodNameDisambiguation
import deezer.kustom.compiler.js.jsExport
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.mapping.INDENTATION
import deezer.kustom.compiler.js.mapping.TypeMapping.exportedType
import deezer.kustom.compiler.js.pattern.autoImport
import deezer.kustom.compiler.js.pattern.buildWrapperClass
import deezer.kustom.compiler.js.pattern.buildWrappingFunction
import java.util.Locale

fun InterfaceDescriptor.transform() = transformInterface(this)

fun transformInterface(origin: InterfaceDescriptor): FileSpec {
    val originalClass = origin.asTypeName()

    val typeParametersMap = origin.typeParameters.map { (_, value) ->
        value to TypeVariableName("__" + value.name, value.bounds.map { exportedType(it) })
    }
    val allTypeParameters = typeParametersMap.flatMap { (origin, exported) -> listOf(origin, exported) }
    val allTypeParamsStr = if (allTypeParameters.isEmpty()) "" else
        allTypeParameters.joinToString(prefix = "<", postfix = ">", transform = { it.name })

    val delegateName = origin.classSimpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }
    val jsClassPackage = if (CompilerArgs.erasePackage) "" else origin.packageName.jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, origin.classSimpleName).let {
        if (origin.typeParameters.isNotEmpty()) {
            it.parameterizedBy(typeParametersMap.map { (_, exportedTp) -> exportedTp })
        } else it
    }

    val importedClass = ClassName(jsClassPackage, "Imported${origin.classSimpleName}")
    val exportedClass = ClassName(jsClassPackage, "Exported${origin.classSimpleName}")

    return FileSpec.builder(jsClassPackage, origin.classSimpleName)
        .addAliasedImport(origin.asClassName(), "Common${origin.classSimpleName}")
        .autoImport(origin)
        .addType(
            TypeSpec.interfaceBuilder(origin.classSimpleName)//ClassName(jsClassPackage, origin.classSimpleName).parameterizedBy(origin.generics.values.first()))
                .also { b ->
                    origin.typeParameters.map { (_, value) ->
                        b.addTypeVariable(TypeVariableName("__" + value.name, value.bounds.map { exportedType(it) }))
                    }
                }
                .addModifiers(KModifier.EXTERNAL)
                .addAnnotation(jsExport)
                .also { builder ->
                    origin.superTypes.forEach { superType ->
                        if (!superType.toString().contains("ERROR") && superType is ClassName) {
                            val superClassName = ClassName(superType.packageName.jsPackage(), superType.simpleName)
                            builder.addSuperinterface(superClassName)
                        } else {
                            Logger.warn("ClassTransformer - ${origin.classSimpleName} superTypes - ClassName($jsClassPackage, $superType)")
                        }
                    }

                    origin.properties.filter { it.isOverride.not() }.forEach { prop ->
                        val modifiers = if (prop.isOverride) listOf(KModifier.OVERRIDE) else emptyList()
                        builder.addProperty(
                            PropertySpec.builder(prop.name, exportedType(prop.type), modifiers)
                                .mutable(prop.isMutable)
                                .build()
                        )
                    }

                    val mnd = MethodNameDisambiguation()
                    origin.functions
                        .filter { !it.isOverride }
                        .forEach { func ->
                            builder.addFunction(
                                func.buildWrappingFunction(
                                    body = false,
                                    import = false,
                                    delegateName = delegateName,
                                    mnd = mnd,
                                    typeParametersMap = typeParametersMap
                                )
                            )
                        }
                }
                .build()
        )
        .addType(
            buildWrapperClass(
                delegateName = "exported",
                originalClass = originalClass,
                typeParameters = origin.typeParameters,
                import = true,
                properties = origin.properties,
                functions = origin.functions,
            )
        )
        .addType(
            buildWrapperClass(
                delegateName = "common", // delegateName,
                originalClass = originalClass,
                typeParameters = origin.typeParameters,
                import = false,
                properties = origin.properties,
                functions = origin.functions,
            )
        )
        .addFunction(
            FunSpec.builder("export${origin.classSimpleName}")
                .also { b ->
                    if (typeParametersMap.isNotEmpty()) {
                        typeParametersMap.forEach {
                            b.addTypeVariable(it.first)
                            b.addTypeVariable(it.second)
                        }
                    }
                }
                .receiver(originalClass)
                .returns(jsExportedClass)
                .addStatement(
                    "return (this as? ${importedClass.simpleName}$allTypeParamsStr)?.exported ?: ${exportedClass.simpleName}(this)"
                )
                .build()
        )
        .addFunction(
            FunSpec.builder("import${origin.classSimpleName}")
                .also { b ->
                    if (typeParametersMap.isNotEmpty()) {
                        typeParametersMap.forEach {
                            b.addTypeVariable(it.first)
                            b.addTypeVariable(it.second)
                        }
                    }
                }
                .receiver(jsExportedClass)
                .returns(originalClass)
                .addStatement(
                    "return (this as? ${exportedClass.simpleName}$allTypeParamsStr)?.common ?: ${importedClass.simpleName}(this)"
                )
                .build()
        )
        .indent(INDENTATION)
        .build()
}
