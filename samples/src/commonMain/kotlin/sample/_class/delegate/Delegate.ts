import { runTest } from "../../shared_ts/RunTest"
import { assert, assertEquals } from "../../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("Delegate", () : void => {
    var wrapper = new sample._class.delegate.js.Wrapper(new sample.js.DepClass("ctorTs"))
    assertEquals(wrapper.wrapperStuff(), "wrapper", "method of the wrapper is visible")
    assertEquals(wrapper.interfaceVar, "wrapper", "method of the wrapper is visible")
    //assertEquals(wrapper.providesStuff(), "default", "can use method through delegates")
})
