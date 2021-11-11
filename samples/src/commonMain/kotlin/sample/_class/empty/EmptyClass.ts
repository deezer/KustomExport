import { runTest } from "../../shared_ts/RunTest"
import { assert } from "../../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("EmptyClass", () : void => {
    var emptyClass = new sample._class.empty.js.EmptyClass()
    assert(true, "can instantiate")
})