import { runTest } from "../shared_ts/RunTest"
import { assert, assertQuiet, assertEquals, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample, deezer } from '@kustom/Samples'

function assertException(e: deezer.kustom.Exception, msg: string, ...klass: any[]) {
    var name = e.constructor.name
    name = name.substring(0, name.length - 2) // Remove the '_0' added by Kotlin

    klass.forEach(function (k) {
        assertQuiet(e instanceof k, name + " instanceof " + k.name)
    })

    assertQuiet(e.stackTrace.includes("at ExceptionBuilder.build" + name), name + " has stacktrace")
    assertEqualsQuiet(e.message, msg, name + " has original message")
    var consumer = new sample._exception.js.ExceptionConsumer()
    assert(consumer.consume(e).startsWith(name + "=" + msg), name + " type is preserved")

}

//TODO : WIP
runTest("Exceptions", () : void => {
    var builder = new sample._exception.js.ExceptionBuilder()
    assertException(builder.buildError("error"), "error", deezer.kustom.Error)
    assertException(builder.buildException("exception"), "exception", deezer.kustom.Exception)
    assertException(builder.buildRuntimeException("re"), "re",
        deezer.kustom.RuntimeException, deezer.kustom.Exception)
    assertException(builder.buildIllegalArgumentException("iae"), "iae",
        deezer.kustom.IllegalArgumentException, deezer.kustom.RuntimeException, deezer.kustom.Exception)
    assertException(builder.buildIllegalStateException("ise"), "ise",
        deezer.kustom.IllegalStateException, deezer.kustom.RuntimeException, deezer.kustom.Exception)
    assertException(builder.buildIndexOutOfBoundsException("ioobe"), "ioobe",
        deezer.kustom.IndexOutOfBoundsException, deezer.kustom.RuntimeException, deezer.kustom.Exception)
    assertException(builder.buildConcurrentModificationException("cme"), "cme",
        deezer.kustom.ConcurrentModificationException, deezer.kustom.RuntimeException, deezer.kustom.Exception)
    assertException(builder.buildUnsupportedOperationException("uoe"), "uoe",
        deezer.kustom.UnsupportedOperationException, deezer.kustom.RuntimeException, deezer.kustom.Exception)
    assertException(builder.buildNumberFormatException("uoe"), "uoe",
        deezer.kustom.NumberFormatException, deezer.kustom.IllegalArgumentException, deezer.kustom.Exception)
    assertException(builder.buildNullPointerException("uoe"), "uoe",
        deezer.kustom.NullPointerException, deezer.kustom.RuntimeException, deezer.kustom.Exception)
    assertException(builder.buildClassCastException("cce"), "cce",
        deezer.kustom.ClassCastException, deezer.kustom.RuntimeException, deezer.kustom.Exception)
    assertException(builder.buildAssertionError("az"), "az",
        deezer.kustom.AssertionError, deezer.kustom.Error)
    assertException(builder.buildNoSuchElementException("nsee"), "nsee",
        deezer.kustom.NoSuchElementException, deezer.kustom.RuntimeException, deezer.kustom.Exception)
    assertException(builder.buildArithmeticException("ae"), "ae",
        deezer.kustom.ArithmeticException, deezer.kustom.RuntimeException, deezer.kustom.Exception)
})