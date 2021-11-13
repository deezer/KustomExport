import { runTest } from "../shared_ts/RunTest"
import { assertEquals } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("Enum", () : void => {
    var konami = [
        sample._enum.js.Directions.NORTH,
        sample._enum.js.Directions.NORTH,
        sample._enum.js.Directions.SOUTH,
        sample._enum.js.Directions.SOUTH,
        sample._enum.js.Directions.WEST,
        sample._enum.js.Directions.EAST,
        sample._enum.js.Directions.WEST,
        sample._enum.js.Directions.EAST,
    ]
    var engine = new sample._enum.js.Engine()
    var konamiResult = ""
    konami.forEach(function(d) {
        konamiResult += engine.goTo(d)
    })
    assertEquals(konamiResult, "⬆️⬆️⬇️⬇️⬅️➡️⬅️➡️" , "can use basic enums")
})