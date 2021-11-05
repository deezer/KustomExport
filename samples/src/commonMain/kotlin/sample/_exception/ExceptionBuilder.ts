import { runTest } from "../shared_ts/RunTest"
import { assert } from "../shared_ts/Assert"
import Samples from '@kustom/Samples'

runTest("ExceptionBuilder", () : void => {
    var builder = new Samples.sample._exception.js.ExceptionBuilder()
    var exception = builder.buildException("data")
    console.log(exception)
    assert(exception.stackTrace.includes("data") && // original message is embedded
            exception.stackTrace.includes("at ExceptionBuilder.buildException") // Stacktrace is embedded
            , "mapping for Exception")

    var ise = builder.buildIllegalStateException("data")

    assert(exception instanceof Samples.deezer.kustom.Exception, "Exception instanceof Exception")
    assert(!(exception instanceof Samples.deezer.kustom.IllegalStateException), "Exception !instanceof IllegalStateException")
    console.log(ise)
    console.log(exception instanceof Samples.deezer.kustom.Exception)
    console.log(exception instanceof Samples.deezer.kustom.IllegalStateException)
    console.log(ise instanceof Samples.deezer.kustom.Exception)
    console.log(ise instanceof Samples.deezer.kustom.IllegalStateException)

    var consumer = new Samples.sample._exception.js.ExceptionConsumer()
    consumer.consume(exception)
    consumer.consume(ise)
})