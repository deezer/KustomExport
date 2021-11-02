import { runTest } from "../../shared_ts/RunTest"
import { assert } from "../../shared_ts/Assert"
import Samples from '@kustom/Samples'

runTest("EmptyClass", () : void => {
    var emptyClass = new Samples.sample._class.empty.js.EmptyClass()
    assert(true, "can instantiate")
})