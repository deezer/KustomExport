import { runTest } from "../../shared_ts/RunTest"
import { assert, assertEquals } from "../../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("Statics", () : void => {
    var str = sample._class.static.js.StaticFactory.create(44)
    assertEquals(str, "string from factory object stuff=44", "static factory can be used")
})