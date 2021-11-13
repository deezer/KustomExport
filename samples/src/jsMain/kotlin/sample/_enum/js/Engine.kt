package sample._enum.js

import kotlin.String
import kotlin.js.JsExport
import sample._enum.js.exportDirection
import sample._enum.js.importDirection
import sample._enum.Direction as CommonDirection
import sample._enum.DirectionEngine as CommonEngine

@JsExport
public class DirectionEngine() {
    internal lateinit var common: CommonEngine

    init {
        common = CommonEngine()}

    internal constructor(common: CommonEngine) : this() {
        this.common = common
    }

    public fun goTo(direction: Direction): String {
        val result = common.goTo(
                direction = direction.importDirection()
        )
        return result
    }
}

public fun CommonEngine.exportEngine(): DirectionEngine = DirectionEngine(this)

public fun DirectionEngine.importEngine(): CommonEngine = this.common
