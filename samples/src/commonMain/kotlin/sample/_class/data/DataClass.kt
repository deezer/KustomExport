package sample._class.data

import deezer.kustom.KustomExport

@KustomExport
data class DataClass(val data1: String = "data", val data2: Int = 2)

@KustomExport
class DataClassFactory {
    fun create() = DataClass()
}