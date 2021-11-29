import { runTest } from "../shared_ts/RunTest"
import { assert, assertEquals, assertQuiet } from "../shared_ts/Assert"
import { Nullable, sample } from '@kustom/Samples'

runTest("Generics", () : void => {
    class CustomImpl implements sample.generics.js.GenericsInterface {
        bar: Nullable<number>
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

    class CustomImplFloat implements sample.generics.js.GenericsInterfaceFloat {
        bar: Nullable<number>
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

    var implLong = new CustomImpl()
    var implFloat = new CustomImplFloat()
    var consumer = new sample.generics.js.GenericsConsumer()
    var factory = new sample.generics.js.GenericsFactory()
    assertEquals("consumed custom 125 / customs 1,2,3", consumer.consumeILong(implLong), "generics interface re-typed")
    assertEquals("consumed custom 125 / customs 1,2,3", consumer.consumeILong(implFloat), "generics interface re-typed") // Same because Float & Long are exported as numbers
    assertEquals("consumed 22", consumer.consumeCLong(factory.buildCLong(22.11)), "generics interface re-typed")
    assertEquals("consumed 33.55", consumer.consumeCFloat(factory.buildCFloat(33.55)), "generics interface re-typed")
    //assertEquals("consumed custom 125 / customs 1,2,3", consumer.consumeCLong(), "generics interface re-typed via TypeAlias")

    // TODO: https://github.com/google/ksp/issues/731
    //var defaultImpl = new sample.generics.js.TypeAliasInterfaceDefault()
    //assertEquals("consumed custom 125 / customs 1,2,3", consumer.consume(impl), "generics interface re-typed via TypeAlias")

})