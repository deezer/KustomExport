package sample._class.data

import deezer.kustom.KustomExport
import kotlin.js.JsExport

@KustomExport
data class DataClass(val data1: String = "data", val data2: Int = 2)
