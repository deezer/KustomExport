package deezer.kustom.compiler.js.pattern

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import deezer.kustom.compiler.js.FunctionDescriptor
import deezer.kustom.compiler.js.MethodNameDisambiguation
import deezer.kustom.compiler.js.PropertyDescriptor
import deezer.kustom.compiler.js.jsPackage
import deezer.kustom.compiler.js.mapping.INDENTATION
import deezer.kustom.compiler.js.mapping.TypeMapping

fun FunctionDescriptor.toFunSpec(
    body: Boolean,
    import: Boolean,
    delegateName: String,
    mnd: MethodNameDisambiguation,
    forceOverride: Boolean = false // TODO: rework that shortcut for testing...
): FunSpec {
    val funExportedName = mnd.getMethodName(this)
    val fb = FunSpec.builder(if (!import) funExportedName else name)
        .returns(if (import) returnType else TypeMapping.exportedType(returnType))

    if (forceOverride || isOverride) {
        fb.addModifiers(KModifier.OVERRIDE)
    } // else = interface defines the method

    parameters.forEach {
        fb.addParameter(it.name, if (import) it.type else TypeMapping.exportedType(it.type))
    }

    if (body) {
        val funcName = if (import) funExportedName else name
        fb.addStatement(
            "return $delegateName.$funcName(${
                parameters.joinToString(", ", prefix = "\n", postfix = "\n", transform = {
                    INDENTATION + it.name + " = " +
                        if (import) TypeMapping.exportMethod(it.name, it.type)
                        else TypeMapping.importMethod(it.name, it.type)
                })
            })${if (import) TypeMapping.importMethod("", returnType) else TypeMapping.exportMethod("", returnType)}"
        )
    }
    return fb.build()
}

fun buildWrapperClass(
    delegateName: String,
    originalClass: ClassName,
    import: Boolean,
    properties: List<PropertyDescriptor>,
    functions: List<FunctionDescriptor>
): TypeSpec {
    val jsClassPackage = originalClass.packageName.jsPackage()
    val jsExportedClass = ClassName(jsClassPackage, originalClass.simpleName)
    val wrapperPrefix = if (import) "Imported" else "Exported"
    val wrapperClass =
        ClassName(jsClassPackage, wrapperPrefix + originalClass.simpleName)
    val delegatedClass = if (import) jsExportedClass else originalClass
    val superClass = if (import) originalClass else jsExportedClass

    return TypeSpec.classBuilder(wrapperClass)
        .addModifiers(KModifier.PRIVATE)
        .primaryConstructor(
            FunSpec.constructorBuilder()
                .addParameter(delegateName, delegatedClass, KModifier.INTERNAL)
                .build()
        )
        .addProperty(PropertySpec.builder(delegateName, delegatedClass).initializer(delegateName).build())
        .addSuperinterface(superClass)
        .also { builder ->
            properties.forEach { prop ->
                // forceOverride = true because only used by interface right now
                builder.addProperty(overrideGetterSetter(prop, delegateName, import, forceOverride = true))
            }

            val mnd = MethodNameDisambiguation()
            functions.forEach { func ->
                builder.addFunction(
                    func.toFunSpec(
                        body = true,
                        import = import,
                        delegateName = delegateName,
                        mnd = mnd,
                        forceOverride = true,
                    )
                )
            }
        }
        .build()
}

fun overrideGetterSetter(
    prop: PropertyDescriptor,
    target: String,
    import: Boolean,
    forceOverride: Boolean // true for interface
): PropertySpec {
    val fieldName = prop.name
    val exportedType = TypeMapping.exportedType(prop.type)
    val fieldClass = if (import) prop.type else exportedType
    val setterValueClass = if (import) exportedType else prop.type

    val getterMappingMethod =
        if (import) TypeMapping.importMethod("$target.$fieldName", prop.type) else TypeMapping.exportMethod("$target.$fieldName", prop.type)
    val setterMappingMethod =
        if (import) TypeMapping.exportMethod(fieldName, prop.type) else TypeMapping.importMethod(fieldName, prop.type)

    val modifiers = if (forceOverride || prop.isOverride) listOf(KModifier.OVERRIDE) else emptyList()

    return PropertySpec.builder(fieldName, fieldClass, modifiers)
        // .getter(FunSpec.getterBuilder().addCode("$target.$fieldName").build())
        // One-line version `get() = ...` is less verbose
        .getter(
            FunSpec.getterBuilder()
                .addStatement("return $getterMappingMethod")
                .build()
        )
        .also { builder ->
            if (prop.isMutable) {
                builder
                    .mutable()
                    .setter(
                        FunSpec.setterBuilder()
                            .addParameter(fieldName, setterValueClass)
                            .addStatement("$target.$fieldName = $setterMappingMethod")
                            .build()
                    )
            }
        }
        .build()
}
