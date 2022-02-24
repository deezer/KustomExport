import { runTest } from "../shared_ts/RunTest"
import { assert, assertEquals, assertQuiet, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("Top-level function", async () : Promise<void> => {
    // First param of adds is Float, second is Long so decimal part (0.4) is ignored
    assertEquals(8.2, sample.toplevelfunction.js.adds(3.2, 5.4), "is supported")

    var abortController = new AbortController()
    var abortSignal = abortController.signal
    sample.toplevelfunction.js.addsAsync(3.2, 5.4, abortSignal)
        .then((res) => {
            assertEquals(8.2, res, "suspend is supported")
        })
})
