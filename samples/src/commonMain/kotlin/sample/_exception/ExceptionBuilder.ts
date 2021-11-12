import { runTest } from "../shared_ts/RunTest"
import { assert } from "../shared_ts/Assert"
import { sample, deezer } from '@kustom/Samples'

//TODO : WIP
runTest("Exceptions", () : void => {
    var builder = new sample._exception.js.ExceptionBuilder()
    var exception = builder.buildException("exception data")
    assert(exception.stackTrace.includes("data") && // original message is embedded
            exception.stackTrace.includes("at ExceptionBuilder.buildException") // Stacktrace is embedded
            , "Exception contains stackTrace and message")

    var iae = builder.buildIllegalArgumentException("iae data")
    var ise = builder.buildIllegalStateException("ise data")

    assert(exception instanceof deezer.kustom.Exception, "Exception instanceof Exception")
    assert(!(exception instanceof deezer.kustom.IllegalArgumentException), "Exception NOT instanceof IllegalStateException")
    assert(!(exception instanceof deezer.kustom.IllegalStateException), "Exception NOT instanceof IllegalStateException")

    assert(iae instanceof deezer.kustom.Exception, "IllegalArgumentException instanceof Exception")
    assert(iae instanceof deezer.kustom.IllegalArgumentException, "IllegalArgumentException instanceof IllegalArgumentException")
    assert(!(iae instanceof deezer.kustom.IllegalStateException), "IllegalArgumentException NOT instanceof IllegalStateException")

    var consumer = new sample._exception.js.ExceptionConsumer()
    assert(consumer.consume(exception) == "Exception:exception data", "Exception type is preserved")
    assert(consumer.consume(iae) == "IllegalArgumentException:iae data", "IllegalArgumentException type is preserved")
    assert(consumer.consume(ise) == "IllegalStateException:ise data", "IllegalStateException type is preserved")
})