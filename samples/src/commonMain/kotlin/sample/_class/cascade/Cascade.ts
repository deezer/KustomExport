import { runTest } from "../../shared_ts/RunTest"
import { assert } from "../../shared_ts/Assert"
import Samples from '@kustom/Samples'

runTest("Cascade", () : void => {
    var parking = new Samples.sample._class.cascade.js.Parking()
    //var parking = new Samples.Parking() // erasePackage=true
    assert(parking.currentCars.length == 2, "direct child")
    assert(parking.currentCars[1].wheels[3].diameter == 22, "sub childs")
})