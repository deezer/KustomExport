package sample._class.simple

import deezer.kustomexport.KustomExport

@KustomExport
class SimpleClass {
    val simpleValue: Int = 42
    fun foo(act: () -> Unit) = act()
}
