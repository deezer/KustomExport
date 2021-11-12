import { runTest } from "../shared_ts/RunTest"
import { assert } from "../shared_ts/Assert"
import { sample, deezer } from '@kustom/Samples'

//TODO : WIP
runTest("ExceptionBuilder", () : void => {
    var builder = new sample._exception.js.ExceptionBuilder()
    var exception = builder.buildException("data")
    assert(exception.stackTrace.includes("data") && // original message is embedded
            exception.stackTrace.includes("at ExceptionBuilder.buildException") // Stacktrace is embedded
            , "mapping for Exception")

    var ise = builder.buildIllegalStateException("data")

    assert(exception instanceof deezer.kustom.Exception, "Exception instanceof Exception")
    assert(!(exception instanceof deezer.kustom.IllegalStateException), "Exception !instanceof IllegalStateException")
    //console.log(exception instanceof Samples.deezer.kustom.Exception)
    //console.log(exception instanceof Samples.deezer.kustom.IllegalStateException)
    //console.log(ise instanceof Samples.deezer.kustom.Exception)
    //console.log(ise instanceof Samples.deezer.kustom.IllegalStateException)

    var consumer = new sample._exception.js.ExceptionConsumer()
    consumer.consume(exception)
    consumer.consume(ise)
})