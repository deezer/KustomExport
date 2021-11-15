import { runTest } from "../../shared_ts/RunTest"
import { assert } from "../../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("Inherit", () : void => {
    var baseImpl = new sample._class.inherit.js.BaseImpl()
    var wrapper = new sample._class.inherit.js.WrapperImpl(baseImpl)
    assert(true, "can instantiate")
})