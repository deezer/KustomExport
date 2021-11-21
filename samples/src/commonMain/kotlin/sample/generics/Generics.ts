import { runTest } from "../shared_ts/RunTest"
import { assert, assertEquals, assertQuiet } from "../shared_ts/Assert"
import { sample } from '@kustom/Samples'

runTest("Generics", () : void => {
    class CustomImpl implements sample.generics.js.GenericsInterface {
        addListener(listener: (p0: number, p1: number) => void, _default: number): void {
            throw new Error("Method not implemented.")
        }
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
    var consumer = new sample.generics.js.GenericsConsumer()
    assertEquals("consumed custom 125 / customs 1,2,3", consumer.consume(impl), "generics interface re-typed via TypeAlias")

    // TODO: https://github.com/google/ksp/issues/731
    //var defaultImpl = new sample.generics.js.TypeAliasInterfaceDefault()
    //assertEquals("consumed custom 125 / customs 1,2,3", consumer.consume(impl), "generics interface re-typed via TypeAlias")

})