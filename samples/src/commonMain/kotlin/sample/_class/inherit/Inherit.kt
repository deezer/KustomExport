package sample._class.inherit

import deezer.kustomexport.KustomExport

@KustomExport
interface BaseInterface {
    val baseValue: Long
}

@KustomExport
class BaseImpl : BaseInterface {
    override val baseValue: Long = 42L
}

// Inherit by delegation
@KustomExport
class WrapperImpl(delegate: BaseInterface) : BaseInterface by delegate {
    val wrapperStuff: String = "wrapper"
}