import { runTest } from "../shared_ts/RunTest"
import { assertEquals } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("Enum", () : void => {
    var konami = [
        sample._enum.js.Directions_NORTH,
        sample._enum.js.Directions_NORTH,
        sample._enum.js.Directions_SOUTH,
        sample._enum.js.Directions_SOUTH,
        sample._enum.js.Directions_WEST,
        sample._enum.js.Directions_EAST,
        sample._enum.js.Directions_WEST,
        sample._enum.js.Directions_EAST
    ]
    var engine = new sample._enum.js.DirectionEngine()
    var konamiResult = ""
    konami.forEach(function(d) {
        konamiResult += engine.goTo(d)
    })
    assertEquals(konamiResult, "⬆️⬆️⬇️⬇️⬅️➡️⬅️➡️" , "can use basic enums")
})