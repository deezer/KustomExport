import { runTest } from "../shared_ts/RunTest"
import { assert, assertQuiet, assertEquals, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample, deezer } from '@kustom/Samples'

function assertException(e: deezer.kustom.Exception, msg: string, ...klass: any[]) {
    var name = e.constructor.name
    if (name.endsWith("_0")) {
        // Remove the '_0' added by Kotlin when using user types
        name = name.substring(0, name.length - 2)
    }

    klass.forEach(function (k) {
        assertQuiet(e instanceof k, name + " instanceof " + k.name)
    })

    assertEqualsQuiet(e.message, msg, name + " has original message")
    var consumer = new sample._exception.js.ExceptionConsumer()
    assert(consumer.consume(e).startsWith(name + "=" + msg), name + " type is preserved")
}

runTest("Exceptions", () : void => {
    var consumer = new sample._exception.js.ExceptionConsumer()
   var myEx1 = new sample._exception.js.MyEx1()// msg="hello" in Kotlin
    assertException(myEx1, "hello",
        sample._exception.js.MyEx1, Error, deezer.kustom.Exception,
        deezer.kustom.RuntimeException, deezer.kustom.IllegalStateException)

    var myEx2 = new sample._exception.js.MyEx2("testing MyEx2")
    assertException(myEx2, "testing MyEx2",
        sample._exception.js.MyEx2, Error, deezer.kustom.Exception,
        deezer.kustom.RuntimeException, deezer.kustom.IllegalStateException)
    assertEqualsQuiet("custom exception val", myEx2.ex2Bonus, "custom field")

    var builder = new sample._exception.js.ExceptionBuilder()
    //assertException(builder.buildError("error"), "error", deezer.kustom.Error)
    //assertException(builder.buildException("exception"), "exception", deezer.kustom.Exception)
    assertException(builder.buildRuntimeException("re"), "re",
        Error, deezer.kustom.Exception, deezer.kustom.RuntimeException)
    assertException(builder.buildIllegalArgumentException("iae"), "iae",
        Error, deezer.kustom.Exception, deezer.kustom.RuntimeException, deezer.kustom.IllegalArgumentException)
    assertException(builder.buildIllegalStateException("ise"), "ise",
        Error, deezer.kustom.Exception, deezer.kustom.RuntimeException, deezer.kustom.IllegalStateException)
    assertException(builder.buildIndexOutOfBoundsException("ioobe"), "ioobe",
        Error, deezer.kustom.Exception, deezer.kustom.RuntimeException, deezer.kustom.IndexOutOfBoundsException)
    assertException(builder.buildConcurrentModificationException("cme"), "cme",
        Error, deezer.kustom.Exception, deezer.kustom.RuntimeException, deezer.kustom.ConcurrentModificationException)
    assertException(builder.buildUnsupportedOperationException("uoe"), "uoe",
        Error, deezer.kustom.Exception, deezer.kustom.RuntimeException, deezer.kustom.UnsupportedOperationException)
    assertException(builder.buildNumberFormatException("nfe"), "nfe",
        Error, deezer.kustom.Exception, deezer.kustom.RuntimeException, deezer.kustom.IllegalArgumentException, deezer.kustom.NumberFormatException)
    assertException(builder.buildNullPointerException("uoe"), "uoe",
        Error, deezer.kustom.Exception, deezer.kustom.RuntimeException, deezer.kustom.NullPointerException)
    assertException(builder.buildClassCastException("cce"), "cce",
        Error, deezer.kustom.Exception, deezer.kustom.RuntimeException, deezer.kustom.ClassCastException)
    //assertException(builder.buildAssertionError("az"), "az",
        //deezer.kustom.AssertionError, deezer.kustom.Error)
    assertException(builder.buildNoSuchElementException("nsee"), "nsee",
        Error, deezer.kustom.Exception, deezer.kustom.RuntimeException, deezer.kustom.NoSuchElementException)
    assertException(builder.buildArithmeticException("ae"), "ae",
        Error, deezer.kustom.Exception, deezer.kustom.RuntimeException, deezer.kustom.ArithmeticException)
})