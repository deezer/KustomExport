package sample._enum.js

import kotlin.String
import kotlin.js.JsExport
import sample._enum.Direction as CommonDirection

typealias Direction = String

public fun Direction.importDirection(): CommonDirection = CommonDirection.valueOf(this)

public fun CommonDirection.exportDirection(): Direction = this.name

@JsExport
public val Directions_NORTH: Direction = "NORTH"
@JsExport
public val Directions_SOUTH: Direction = "SOUTH"
@JsExport
public val Directions_WEST: Direction = "WEST"
@JsExport
public val Directions_EAST: Direction = "EAST"