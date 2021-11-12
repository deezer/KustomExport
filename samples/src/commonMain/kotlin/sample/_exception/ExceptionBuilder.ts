import { runTest } from "../shared_ts/RunTest"
import { assert } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

//TODO : WIP
runTest("Exceptions", () : void => {
    var builder = new sample._exception.js.ExceptionBuilder()
    var exception = builder.buildException("exception data")
    assert(exception.message == "exception data", "Exception contains original message")
    assert(exception.stack.includes("at ExceptionBuilder.buildException"), "Exception contains stackTrace")

    var iae = builder.buildIllegalArgumentException("iae data")
    var ise = builder.buildIllegalStateException("ise data")
    var ioobe = builder.buildIndexOutOfBoundsException("ioobe data")

    assert(exception.name == "Exception", "Exception instanceof Exception")
    //assert(!(exception instanceof kotlin.IllegalArgumentException), "Exception NOT instanceof IllegalStateException")
    assert(!(exception.name == "IllegalStateException"), "Exception NOT instanceof IllegalStateException")

    // With exception.name we cannot check for inheritance :( https://kotlinlang.slack.com/archives/C0B8L3U69/p1636713134354000
    //assert(iae.name == "Exception", "IllegalArgumentException instanceof Exception")
    //assert(iae instanceof kotlin.IllegalArgumentException, "IllegalArgumentException instanceof IllegalArgumentException")
    assert(!(iae.name == "IllegalStateException"), "IllegalArgumentException NOT instanceof IllegalStateException")

    var consumer = new sample._exception.js.ExceptionConsumer()
    assert(consumer.consume(exception) == "Exception:exception data", "Exception type is preserved")
    assert(consumer.consume(iae) == "IllegalArgumentException:iae data", "IllegalArgumentException type is preserved")
    assert(consumer.consume(ise) == "IllegalStateException:ise data", "IllegalStateException type is preserved")
    console.log(consumer.consume2(ioobe)) // Kotlin "is" is working
})