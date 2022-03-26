import { runTest } from "../shared_ts/RunTest"
import { assertEquals, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("LongMapping", () : void => {
    var maxInt = new sample._common.js.LongMapping(2147483647)
    assertEquals(maxInt.timestamp, 2147483647, "mapping for Long (2147483647)")

    var maxUInt = new sample._common.js.LongMapping(4294967295)
    assertEquals(maxUInt.timestamp, 4294967295, "mapping for Long (4294967295)")

    // See Documentation in doc/Long.md for precision details

    // Here is the point where Longs are loosing precision and it's hidden by 'number' precision
    // Kotlin Long.MAX_VALUE = 9223372036854775807
    var kotlinLongMaxValue = 9223372036854775807
    assertEqualsQuiet(kotlinLongMaxValue, 9223372036854775807, "mapping for Long (9223372036854775807)")
    assertEqualsQuiet(kotlinLongMaxValue.toString(), "9223372036854776000", "mapping for Long (9223372036854776000 ??)")
    assertEqualsQuiet(9223372036854775807, 9223372036854776000, "mapping for Long (precision issue not visible in JS)")

    // You can still input really large data, up to Number.MAX_VALUE (1.8e+308), but the precision will be quite limited.
})