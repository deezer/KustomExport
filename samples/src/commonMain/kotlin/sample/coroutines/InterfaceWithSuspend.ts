import { runTest } from "../shared_ts/RunTest"
import { assert, assertEquals, assertQuiet, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("InterfaceWithSuspend", async () : Promise<void> => {
    var neverAbortController = new AbortController()
    var neverAbortSignal = neverAbortController.signal

    const thingDoer = new sample.coroutines.js.WebThingDoer()
    const res = await thingDoer.doThings(neverAbortSignal)
    assertEquals([3, 2, 1].join(","), res.join(","), "executes suspend function with JS friendly types")
})
