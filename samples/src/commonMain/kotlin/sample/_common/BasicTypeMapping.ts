import { runTest } from "../shared_ts/RunTest"
import { assert } from "../shared_ts/Assert"
import Samples from '@kustom/Samples'

runTest("BasicTypeMapping", () : void => {
    var basicTypes = new Samples.sample._common.js.BasicTypeMapping()
    assert(basicTypes.bool == true, "mapping for Boolean")
    assert(basicTypes.byte == 0x42, "mapping for Byte")
    assert(basicTypes.char == 'c', "mapping for Char")
    assert(basicTypes.short == 0x7F7F, "mapping for Short")
    assert(basicTypes.int == 0x7F4251, "mapping for Int")
    assert(basicTypes.float == 12345.678, "mapping for Float")
    assert(basicTypes.double == 123456789.123456789, "mapping for Double")
    assert(basicTypes.string == "magic", "mapping for String")

    // Current export for typed array looks a bit off, need to check with typescript dev
    console.log(basicTypes.booleanArray)

    assert(JSON.stringify(basicTypes.any) == JSON.stringify(new Object()), "mapping for Any")
    assert(JSON.stringify(basicTypes.unit) == "{}", "mapping for Unit")
})