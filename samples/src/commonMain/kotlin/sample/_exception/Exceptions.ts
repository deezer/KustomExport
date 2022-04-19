import { runTest } from "../shared_ts/RunTest"
import { assert, assertQuiet, assertEquals, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("Exceptions", () : void => {
    var consumer = new sample._exception.js.ExceptionConsumer()
    var myEx = new sample._exception.js.MyEx(new Error("from TS"))
    assertEquals(myEx.cause.message, "from TS", "TS can interact with an object containing an Exception")

    var consumer = new sample._exception.js.ExceptionConsumer()
    assertEquals(consumer.consumeMessage(myEx), "MyEx.cause.message=from TS", "Kotlin can interface with an object containing an Exception")
    var stacktrace = consumer.consumeStackTrace(myEx)
    assert(
        stacktrace.startsWith("MyEx.cause.stackTraceToString=Error: from TS") &&
        stacktrace.includes("samples/src/commonMain/kotlin/sample/_exception/Exceptions.ts:7:46"),
        "Kotlin can retrieve the stacktrace from the exception"
    )
})