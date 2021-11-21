import { runTest } from "../../shared_ts/RunTest"
import { assert } from "../../shared_ts/Assert"
import { assertQuiet } from "../../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("SealedClass", () : void => {
    var sealed1 = new sample._class.sealed.js.SealedChild1(33, 101)
    var sealed2 = new sample._class.sealed.js.SealedChild2(28)
    var extSealed = new sample._class.sealed.js.ExtendedSealedChild(28, "bliblibli")

    var consumer = new sample._class.sealed.js.SealedClassConsumer()
    assert(consumer.consume(sealed1) == "child1:child1", "Sealed class 1")
    assert(consumer.consume(sealed2) == "child2:child2", "Sealed class 2")
    assert(consumer.consume(extSealed) == "child2:child2", "Extended sealed class")

    assertQuiet(sealed1.prop == 33, "SealedChild1:field")
    assertQuiet(sealed1.ctorParam == 101, "SealedChild1:ctorParam")
    assertQuiet(sealed1.hardcoded == 42, "SealedChild1:hardcoded") // Value from Kotlin
    assertQuiet(sealed1.computeFoo() == 12345, "SealedChild1:computeFoo()") // Value from Kotlin
    assertQuiet(sealed1.special() == "33-101-child1", "SealedChild1:special()") // Compute value from both sides

    assertQuiet(sealed2.prop == 28, "SealedChild2:field")
    assertQuiet(sealed2.ctorParam == 94949, "SealedChild2:ctorParam") // Value from Kotlin
    assertQuiet(sealed2.hardcoded == 42, "SealedChild2:hardcoded") // Value from Kotlin
    assertQuiet(sealed2.computeFoo() == 12345, "SealedChild2:computeFoo()") // Value from Kotlin
})