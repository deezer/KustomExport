import { runTest } from "../shared_ts/RunTest"
import { assert, assertEquals, assertQuiet, assertEqualsQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("Enum", () : void => {
    var konami = [
        sample._enum.js.Direction_NORTH,
        sample._enum.js.Direction_NORTH,
        sample._enum.js.Direction_SOUTH,
        sample._enum.js.Direction_SOUTH,
        sample._enum.js.Direction_WEST,
        sample._enum.js.Direction_EAST,
        sample._enum.js.Direction_WEST,
        sample._enum.js.Direction_EAST,
    ]
    var engine = new sample._enum.js.Engine()
    var konamiResult = ""
    konami.forEach(function(d) {
        konamiResult += engine.goTo(d)
    })
    assertEquals(konamiResult, "⬆️⬆️⬇️⬇️⬅️➡️⬅️➡️" , "can consume enums in Kotlin code")

    // values
    assertEquals(4, sample._enum.js.Direction_values().length, "values() contains 4 items")
    assert(sample._enum.js.Direction_values().includes(sample._enum.js.Direction_NORTH), "values() contains NORTH")
    assertQuiet(sample._enum.js.Direction_values().includes(sample._enum.js.Direction_SOUTH), "values() contains SOUTH")
    assertQuiet(sample._enum.js.Direction_values().includes(sample._enum.js.Direction_WEST), "values() contains WEST")
    assertQuiet(sample._enum.js.Direction_values().includes(sample._enum.js.Direction_EAST), "values() contains EAST")

    // valueOf
    assertEquals(sample._enum.js.Direction_NORTH, sample._enum.js.Direction_valueOf("NORTH"), "valueOf(NORTH) -> NORTH")
    assertEqualsQuiet(sample._enum.js.Direction_SOUTH, sample._enum.js.Direction_valueOf("SOUTH"), "valueOf(SOUTH) -> SOUTH")
    assertEqualsQuiet(sample._enum.js.Direction_WEST, sample._enum.js.Direction_valueOf("WEST"), "valueOf(WEST) -> WEST")
    assertEqualsQuiet(sample._enum.js.Direction_EAST, sample._enum.js.Direction_valueOf("EAST"), "valueOf(EAST) -> EAST")
    assertEquals(null, sample._enum.js.Direction_valueOf("UP"), "valueOf(UP) is null")

    assertEquals(sample._enum.js.Direction_SOUTH, engine.south(), "export doesn't duplicate wrappers")

    assertEqualsQuiet("South", engine.translateEnName(sample._enum.js.DirectionWithData_SOUTH), "check additional field 1")
    assertEqualsQuiet("Sud", engine.translateFrName(sample._enum.js.DirectionWithData_SOUTH), "check additional field 2")
})