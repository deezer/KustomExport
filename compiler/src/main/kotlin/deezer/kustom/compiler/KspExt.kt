package deezer.kustom.compiler

import com.google.devtools.ksp.processing.Dependencies
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.symbol.KSFile
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import java.io.OutputStream

fun FileSpec.writeCode(environment: SymbolProcessorEnvironment, vararg sources: KSFile) {
    environment.codeGenerator.createNewFile(
        dependencies = Dependencies(aggregating = false, *sources),
        packageName = packageName,
        fileName = name
    ).use { outputStream ->
        outputStream.writer()
            .use {
                writeTo(it)
            }
    }
}

fun OutputStream.appendText(str: String) {
    this.write(str.toByteArray())
}

fun TypeName.firstParameterizedType() = (this as ParameterizedTypeName).typeArguments.first()
