import { runTest } from "../../shared_ts/RunTest"
import { assert } from "../../shared_ts/Assert"
import Samples from '@kustom/Samples'

runTest("SealedClass", () : void => {
    var sealed1 = new Samples.sample._class.sealed.js.SealedChild1(33, "hey")
    var sealed2 = new Samples.sample._class.sealed.js.SealedChild2(28)

    var consumer = new Samples.sample._class.sealed.js.SealedClassConsumer()
    assert(consumer.consume(sealed1) == "child1:child1", "Sealed class 1")
    assert(consumer.consume(sealed2) == "child2:child2", "Sealed class 2")
})