package sample._class.cascade

import deezer.kustom.KustomExportSamples

@KustomExportSamples
class Wheel {
    val diameter: Long = 22
}

@KustomExportSamples
class Car {
    val wheels = listOf<Wheel>(Wheel(), Wheel(), Wheel(), Wheel())
}

@KustomExportSamples
class Parking {
    val currentCars = listOf(Car(), Car())
}
