import { runTest } from "../shared_ts/RunTest"
import { assert } from "../shared_ts/Assert"
import { assertQuiet } from "../shared_ts/Assert"
import Samples from '@kustom/Samples'

runTest("Generics (interface)", () : void => {
    class TsImplKmpGenerics implements Samples.sample.generics.js.InterfaceGenerics<Samples.sample.generics.js.KmpGenericsBase> {
        generateBase(): Samples.sample.generics.js.KmpGenericsBase {
            return new Samples.sample.generics.js.KmpGenericsBase()
        }
        check(input: Samples.sample.generics.js.KmpGenericsBase): string {
            return "TsImplKmpGenerics<KmpGenericsBase> checking " + input.b
        }
    }

    class TsImplBase implements Samples.sample.generics.js.GenericsBase {
        b: string
        extras:string
        constructor() {
            this.b = "TsImplBase"
            this.extras = "some extras"
        }
    }

    class TsImplTsGenerics implements Samples.sample.generics.js.InterfaceGenerics<TsImplBase> {
        generateBase(): TsImplBase {
            return new TsImplBase()
        }
        check(input: TsImplBase): string {
            return "TsImplTsGenerics<TsImplBase> checking " + input.b
        }
    }


    var tsImplKmpGenerics = new TsImplKmpGenerics()
    var tsImplTsGenerics = new TsImplTsGenerics()
    var kmpBase = new Samples.sample.generics.js.KmpGenericsBase()
    var tsBase = new TsImplBase()

    assertQuiet(kmpBase instanceof Samples.sample.generics.js.KmpGenericsBase, "kmp instance instanceof is working")
    assertQuiet(!(kmpBase instanceof TsImplBase), "kmp instance is not interpreted as a TS class")

    assertQuiet(tsBase instanceof TsImplBase, "TS instance instanceof is working")
    assertQuiet(!(tsBase instanceof Samples.sample.generics.js.KmpGenericsBase), "TS instance is not interpreted as a Kmp class")

    assert(tsImplKmpGenerics.check(kmpBase) == "TsImplKmpGenerics<KmpGenericsBase> checking KmpGenericsBase", "Ts implementation with Kotlin type param can deal with Kotlin instances")
    assert(tsImplKmpGenerics.check(tsBase) == "TsImplKmpGenerics<KmpGenericsBase> checking TsImplBase", "Ts implementation with Kotlin type param can deal with Ts instances")

    // Can't build (as expected)
    //assert(tsImplTsGenerics.check(kmpBase) == "TsImplTsGenerics<TsImplBase> checking KmpGenericsBase", "Ts implementation with Kotlin type param can deal with Kotlin instances")
    assert(tsImplTsGenerics.check(tsBase) == "TsImplTsGenerics<TsImplBase> checking TsImplBase", "Ts implementation with Ts type param can deal with Ts instances")
})