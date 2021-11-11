import { runTest } from "../../shared_ts/RunTest"
import { assert } from "../../shared_ts/Assert"
import { assertQuiet } from "../../shared_ts/Assert"
import Samples from '@kustom/Samples'

runTest("SealedClass", () : void => {
    var sealed1 = new Samples.sample._class.sealed.js.SealedChild1(33, 101)
    var sealed2 = new Samples.sample._class.sealed.js.SealedChild2(28)

    var consumer = new Samples.sample._class.sealed.js.SealedClassConsumer()
    assert(consumer.consume(sealed1) == "child1:child1", "Sealed class 1")
    assert(consumer.consume(sealed2) == "child2:child2", "Sealed class 2")

    assertQuiet(sealed1.field == 33, "SealedChild1:field")
    assertQuiet(sealed1.ctorParam == 101, "SealedChild1:ctorParam")
    assertQuiet(sealed1.hardcoded == 42, "SealedChild1:hardcoded")
    assertQuiet(sealed1.computeFoo() == 12345, "SealedChild1:computeFoo()")

    assertQuiet(sealed2.field == 28, "SealedChild2:field")
    assertQuiet(sealed2.ctorParam == 4242, "SealedChild2:ctorParam")
    assertQuiet(sealed2.hardcoded == 42, "SealedChild2:hardcoded")
    assertQuiet(sealed2.computeFoo() == 12345, "SealedChild2:computeFoo()")
})