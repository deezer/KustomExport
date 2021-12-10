package sample._class.cascade

import deezer.kustomexport.KustomExport

@KustomExport
class Wheel {
    val diameter: Long = 22
}

@KustomExport
class Car {
    val wheels = listOf<Wheel>(Wheel(), Wheel(), Wheel(), Wheel())
}

@KustomExport
class Parking {
    val currentCars = listOf(Car(), Car())
}
