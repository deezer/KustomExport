import { runTest } from "../shared_ts/RunTest"
import { assert, assertEquals, assertQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("TypeAlias", () : void => {
    class CustomImpl implements sample.type_alias.js.TypeAliasInterface {
        fooBar(input: number): string {
            return "custom " + (input + 2)
        }
        fooBars(inputs: Array<number>): string {
            return "customs " + inputs.join()
        }
        baz() : sample._class.data.js.DataClass {
            return new sample._class.data.js.DataClass("4", 4)
        }
    }
    var impl = new CustomImpl()
    var consumer = new sample.type_alias.js.TypeAliasConsumer()
    assertEquals("consumed custom 125 / customs 1,2,3", consumer.consume(impl), "generics interface re-typed via TypeAlias")
})