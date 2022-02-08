import { runTest } from "../shared_ts/RunTest"
import { assert, assertEquals, assertQuiet, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("ValueClass", () : void => {
    var consumer = new sample._value.js.ValueClassConsumer()
    assertEquals("raw string from ts", consumer.consume("raw string from ts"), "value class can wrap String")
    assertEquals("123", consumer.consumePl(123.456), "value class can wrap Long")
    
    try {
        consumer.consumePl(-3)
        assertEquals("should fail", "fail", "value class logic is applied")
    } catch (error) {
        assertEquals("IllegalArgumentException: Failed requirement.", error, "value class can wrap Long")        
    }
})