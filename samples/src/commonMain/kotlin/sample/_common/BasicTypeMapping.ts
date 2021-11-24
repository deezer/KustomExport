import { runTest } from "../shared_ts/RunTest"
import { assert } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("BasicTypeMapping", () : void => {
    var basicTypes = new sample._common.js.BasicTypeMapping()
    assert(basicTypes.bool == true, "mapping for Boolean")
    assert(basicTypes.byte == 0x42, "mapping for Byte")
    // TODO : assert(basicTypes.char == 'c', "mapping for Char")
    assert(basicTypes.short == 0x7F7F, "mapping for Short")
    assert(basicTypes.int == 0x7F4251, "mapping for Int")
    assert(basicTypes.float == 12345.678, "mapping for Float")
    assert(basicTypes.double == 123456789.123456789, "mapping for Double")
    assert(basicTypes.string == "magic", "mapping for String")

    // Current export for typed array looks a bit off, need to check with typescript dev
    console.log("//TODO: Typed arrays")

    assert(JSON.stringify(basicTypes.any) == JSON.stringify(new Object()), "mapping for Any")
    // TODO : assert(JSON.stringify(basicTypes.unit) == "{}", "mapping for Unit")
})