package sample.coroutines

import deezer.kustomexport.KustomExport

@KustomExport
interface IThingDoer {
    suspend fun doThings(): List<Long>
}

class NonWebThingDoer : IThingDoer {
    override suspend fun doThings(): List<Long> {
        return listOf(1, 2, 3)
    }
}

@KustomExport
class WebThingDoer : IThingDoer {
    override suspend fun doThings(): List<Long> {
        return listOf(3, 2, 1)
    }
}
