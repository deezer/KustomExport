/*
 * Copyright 2021 Deezer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package sample._enum

import deezer.kustomexport.KustomExport

@KustomExport
enum class Direction {
    NORTH, SOUTH, WEST, EAST
}

val d = Direction.NORTH

@KustomExport
enum class DirectionWithData(val enName: String, val frName: String) {
    NORTH("North", "Nord"),
    SOUTH("South", "Sud"),
    WEST("West", "Ouest"),
    EAST("East", "Est")
}


@KustomExport
class Engine {
    fun goTo(direction: Direction): String {
        return when (direction) {
            Direction.NORTH -> "⬆️"
            Direction.SOUTH -> "⬇️"
            Direction.WEST -> "⬅️"
            Direction.EAST -> "➡️"
        }
    }

    fun translateEnName(d: DirectionWithData): String {
        return d.enName
    }

    fun translateFrName(d: DirectionWithData): String {
        return d.frName
    }
}
