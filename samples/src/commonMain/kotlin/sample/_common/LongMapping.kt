package sample._common

import deezer.kustomexport.KustomExport
import deezer.kustomexport.dynamicCastTo
import deezer.kustomexport.dynamicNull
import sample._class.data.DataClass

@KustomExport
data class LongMapping(val timestamp: Long)
