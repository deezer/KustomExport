import { runTest } from "../shared_ts/RunTest"
import { assert } from "../shared_ts/Assert"
import Samples from '@kustom/Samples'

runTest("CollectionMapping", () : void => {
    var collections = new Samples.sample._common.js.CollectionMapping()
    assert(JSON.stringify(collections.listLong) == JSON.stringify([1, 2, 3]), "mapping for List<Long>")
})