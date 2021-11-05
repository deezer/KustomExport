package deezer.kustom.compiler.js.pattern.`interface`

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
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
import deezer.kustom.compiler.js.pattern.toFunSpec
import java.util.Locale

fun InterfaceDescriptor.transform() = transformInterface(this)

fun transformInterface(origin: InterfaceDescriptor): FileSpec {
    val originalClass = ClassName(origin.packageName, origin.classSimpleName)

    val delegateName = origin.classSimpleName.replaceFirstChar { it.lowercase(Locale.getDefault()) }
    val jsClassPackage = if (CompilerArgs.erasePackage) "" else origin.packageName.jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, origin.classSimpleName)
    val importedClass = ClassName(jsClassPackage, "Imported${origin.classSimpleName}")
    val exportedClass = ClassName(jsClassPackage, "Exported${origin.classSimpleName}")

    if (origin.generics.isNotEmpty()) {
        Logger.error("ClassTransformer - ${origin.classSimpleName} superTypes - generics=${origin.generics}")
    }

    return FileSpec.builder(jsClassPackage, origin.classSimpleName)
        .addAliasedImport(originalClass, "Common${origin.classSimpleName}")
        .autoImport(origin)
        .addType(
            TypeSpec.interfaceBuilder(origin.classSimpleName)
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
                                func.toFunSpec(
                                    body = false,
                                    import = false,
                                    delegateName = delegateName,
                                    mnd = mnd
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
                import = true,
                properties = origin.properties,
                functions = origin.functions,
            )
        )
        .addType(
            buildWrapperClass(
                delegateName = "common", // delegateName,
                originalClass = originalClass,
                import = false,
                properties = origin.properties,
                functions = origin.functions,
            )
        )
        .addFunction(
            FunSpec.builder("export${origin.classSimpleName}")
                .receiver(originalClass)
                .returns(jsExportedClass)
                .addStatement("return (this as? ${importedClass.simpleName})?.exported ?: ${exportedClass.simpleName}(this)")
                .build()
        )
        .addFunction(
            FunSpec.builder("import${origin.classSimpleName}")
                .receiver(jsExportedClass)
                .returns(originalClass)
                .addStatement("return (this as? ${exportedClass.simpleName})?.common ?: ${importedClass.simpleName}(this)")
                .build()
        )
        .indent(INDENTATION)
        .build()
}
