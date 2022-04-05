import { runTest } from "../shared_ts/RunTest"
import { assertEquals } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("FunctionTypes", () : void => {
    var functionTypes = new sample._common.js.FunctionTypes()
    assertEquals(51, functionTypes.reduce((a, b)=> a + b)(5), "mapping for function types")
})