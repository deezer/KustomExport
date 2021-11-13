package sample._class.delegate

import deezer.kustom.KustomExportSamples
import sample.DepInterface

@KustomExportSamples
interface SharedInterface {
    fun providesStuff(): String
}

@KustomExportSamples
class Wrapper(
    _delegate: DepInterface
) : DepInterface by _delegate {
    fun wrapperStuff() = "wrapper"
}

@KustomExportSamples
class DefaultSharedImpl : SharedInterface {
    override fun providesStuff(): String = "default"
}
