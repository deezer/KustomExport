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
    console.log(consumer.consume(e))
    assert(consumer.consume(e).startsWith(name + "=" + msg), name + " type is preserved")
}

runTest("Exceptions", () : void => {
    var myEx = new sample._exception.js.MyEx2("salut toi")
    /*
    console.log("MyEx created")
    console.log("is MyEx: " + (myEx instanceof sample._exception.js.MyEx2))
    console.log("is Error: " + (myEx instanceof Error))
    console.log("is Exception: " + (myEx instanceof deezer.kustom.Exception))
    console.log("is ISE: " + (myEx instanceof deezer.kustom.IllegalStateException))
    console.log("is RE: " + (myEx instanceof deezer.kustom.RuntimeException))
    console.log("is IOOBE: " + (myEx instanceof deezer.kustom.IndexOutOfBoundsException))
    */
    //console.log(myEx)
    console.log(myEx.msg)
    console.log(myEx.causedBy)
    var consumer = new sample._exception.js.ExceptionConsumer()
    console.log(consumer.consume(myEx))
    assertException(myEx, "salut toi",
        sample._exception.js.MyEx2, Error, deezer.kustom.Exception,
        deezer.kustom.RuntimeException, deezer.kustom.IllegalStateException)
    //console.log(myEx.message)
    //console.log(myEx.cause)

    var builder = new sample._exception.js.ExceptionBuilder()
    //assertException(builder.buildError("error"), "error", deezer.kustom.Error)
    //assertException(builder.buildException("exception"), "exception", deezer.kustom.Exception)
    assertException(builder.buildRuntimeException("re"), "re")
    assertException(builder.buildIllegalArgumentException("iae"), "iae")
    assertException(builder.buildIllegalStateException("ise"), "ise")
    assertException(builder.buildIndexOutOfBoundsException("ioobe"), "ioobe")
    assertException(builder.buildConcurrentModificationException("cme"), "cme")
    assertException(builder.buildUnsupportedOperationException("uoe"), "uoe")
    assertException(builder.buildNumberFormatException("uoe"), "uoe")
    assertException(builder.buildNullPointerException("uoe"), "uoe")
    assertException(builder.buildClassCastException("cce"), "cce")
    //assertException(builder.buildAssertionError("az"), "az",
        //deezer.kustom.AssertionError, deezer.kustom.Error)
    assertException(builder.buildNoSuchElementException("nsee"), "nsee")
    assertException(builder.buildArithmeticException("ae"), "ae")
})