package deezer.kustom.compiler.js

import com.squareup.kotlinpoet.ClassName

fun String.jsPackage() = "$this.js"
val jsExport = ClassName("kotlin.js", "JsExport")
val jsName = ClassName("kotlin.js", "JsName")
