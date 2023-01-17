import { runTest } from "../shared_ts/RunTest"
import { assert, assertEquals, assertQuiet, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("Coroutines", async () : Promise<void> => {
    var neverAbortController = new AbortController()
    var neverAbortSignal = neverAbortController.signal

    var computer = new sample.coroutines.js.Computer()
    var res = await computer.longCompute(neverAbortSignal)
    assertEquals(42, res, "execute Kotlin coroutines")

    var p = computer.longCompute(neverAbortSignal)
        .then((res) => {
            return res
        })
    assertEquals(42, await p, "execute Kotlin coroutines with 'then'")

    class MyComputer implements sample.coroutines.js.IComputer {
        async longCompute(abortSignal: AbortSignal): Promise<number> {
            return new Promise((resolve, reject) => {
                setTimeout(() => {
                    resolve(999);
                }, 2000);
              });
        }
    }

    var tester = new sample.coroutines.js.ComputerTester(new MyComputer())
    assertEquals(1998, await tester.testAsync(neverAbortSignal), "parallel setTimeout")

    class RejectComputer implements sample.coroutines.js.IComputer {
        async longCompute(abortSignal: AbortSignal): Promise<number> {
            return new Promise<number>((resolve, reject) => {
                setTimeout(() => {
                    reject(new Error("bug"));
                }, 1000);
              });
        }
    }

    var rejectTester = new sample.coroutines.js.ComputerTester(new RejectComputer())
    try {
        await rejectTester.testAsync(neverAbortSignal)
    } catch(e) {
        assert(e instanceof Error, "reject throws an Error")
    }
    cancelTypescriptPromiseFromKotlin()

    cancelKotlinCoroutinesFromTypescript()
})

async function cancelTypescriptPromiseFromKotlin() {
    var neverAbortController = new AbortController()
    var neverAbortSignal = neverAbortController.signal

    class CancellableComputer implements sample.coroutines.js.IComputer {
        workAborted: boolean
        // Should be passed in parameters in the longCompute method for cooperative cancellation
        // Cancellation is not available yet.
        async longCompute(abortSignal: AbortSignal): Promise<number> {
            return new Promise((resolve, reject) => {
                var timeout = 5000
                var currTime = 0
                this.rec(abortSignal, resolve, reject, currTime, timeout)
              });
        }
        rec(abortSignal, resolve, reject, currTime, timeout) {
            setTimeout(() => {
                if (currTime >= timeout) {
                    resolve(1234)
                } else {
                    if (!abortSignal.aborted) {
                        this.rec(abortSignal, resolve, reject, currTime + 100, timeout)
                    } else {
                        this.workAborted = true
                    }
                }
            }, 100);
        }
    }

    var cancellableComputer = new CancellableComputer()
    var cancellableTester = new sample.coroutines.js.ComputerTester(cancellableComputer)
    try {
        var abortController = new AbortController()
        var abortSignal = neverAbortController.signal
        await cancellableTester.startAndCancelAfter(1000, abortSignal)
        assertQuiet(false, "TimeoutCancellationException should have been thrown")
    } catch (tce) {
        // this check is ok, but the Promise is not really cancelled, so not good enough...
        assert(tce.name === "TimeoutCancellationException", "can cancel (timeout throw CancellationException)")
        // Exception is thrown immediately when the signal is emitted, work is really stopped a few ms after that
        setTimeout(() => {
            assertEquals(true, cancellableComputer.workAborted, "can cancel (work is aborted)")
        }, 100);
    }
}

async function cancelKotlinCoroutinesFromTypescript() {
    var abortController = new AbortController()
    var abortSignal = abortController.signal
    var computer = new sample.coroutines.js.Computer()
    var promise = computer.longCompute(abortSignal) // Trigger promise, duration = 1s
    // Catch on promise is required, or else the test will stop before the end
    var cancellationException = null
    promise.catch((e) => {
        cancellationException = e
    })

    abortController.abort()

    setTimeout(() => {
        assertEquals(false, computer.completed, "can cancel Kotlin coroutines from Typescript (work has been cancelled and will never complete)")
        assertEqualsQuiet("JobCancellationException", cancellationException.name, "cancellation exception")
        assertEqualsQuiet("DeferredCoroutine was cancelled", cancellationException.message, "cancellation exception")
    }, 1200);
}