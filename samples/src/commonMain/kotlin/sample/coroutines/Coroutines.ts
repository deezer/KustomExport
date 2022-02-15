import { runTest } from "../shared_ts/RunTest"
import { assert, assertEquals, assertQuiet, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("Coroutines", async () : Promise<void> => {
    console.log("start at " + Date())
    var computer = new sample.coroutines.js.Computer()
    var res = await computer.longCompute()
    assertEquals(42, res, "execute Kotlin coroutines")

    var p = computer.longCompute()
        .then((res) => {
            return res
        })
    assertEquals(42, await p, "execute Kotlin coroutines with 'then'")

    class MyComputer implements sample.coroutines.js.IComputer {
        async longCompute() {
            return new Promise((resolve, reject) => {
                setTimeout(() => {
                    resolve(999);
                }, 2000);
              });
        }
    }

    var tester = new sample.coroutines.js.ComputerTester(new MyComputer())
    assertEquals(1998, await tester.testAsync(), "parallel setTimeout")

    class RejectComputer implements sample.coroutines.js.IComputer {
        async longCompute() {
            return new Promise<void>((resolve, reject) => {
                setTimeout(() => {
                    //reject(new Error("bug"));
                    //reject(undefined); // null, undefined, <nothing> == resolve()
                    resolve()
                    //resolve(55)
                }, 1000);
              });
        }
    }

    var rejectTester = new sample.coroutines.js.ComputerTester(new RejectComputer())
    await rejectTester.testAsync()
    assertEquals(0, await rejectTester.testAsync(), "parallel reject")


    class CancellableComputer implements sample.coroutines.js.IComputer {
        // Should be passed in parameters in the longCompute method for cooperative cancellation
        // Cancellation is not available yet.
        isJobActive() {
            return true
        }
        async longCompute() {
            return new Promise((resolve, reject) => {
                var timeout = 5000
                var currTime = 0
                this.rec(this.isJobActive, resolve, reject, currTime, timeout)
              });
        }
        rec(isJobActive, resolve, reject, currTime, timeout) {
            setTimeout(() => {
                console.log("rec " + currTime + " / " + timeout)
                if (currTime >= timeout) {
                    resolve(1234)
                } else {
                    if (isJobActive()) {
                        this.rec(isJobActive, resolve, reject, currTime + 100, timeout)
                    }
                }
            }, 100);
        }
    }

    var cancellableTester = new sample.coroutines.js.ComputerTester(new CancellableComputer())
    try {
        console.log(Date() + " startAndCancelAfter")
        await cancellableTester.startAndCancelAfter(1000)
    } catch (tce) {
        // this check is ok, but the Promise is not really cancelled, so not good enough...
        // assert(tce.name === "TimeoutCancellationException", "can cancel (timeout)")
    }
    //assertEquals(91, await infiniteTester.testAsync(), "parallel reject")

    //console.log(await new RejectComputer().longCompute())

    //var cancellableTester = new sample.coroutines.js.ComputerTester(new InfiniteComputer())
    //var cancellable = cancellableTester.startCancellable()
    /*
    setTimeout(() => {
        console.log("time !")
        //cancellable.cancel()
    }, 500);
    */
})