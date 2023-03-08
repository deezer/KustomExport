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

package deezer.kustomexport.compiler.js

import java.util.*

/**
 * You should create 1 instance of this class by generated class.
 */
class MethodNameDisambiguation {
    private val generatedNames = mutableMapOf<FunctionDescriptor, String>()

    fun getMethodName(origin: FunctionDescriptor): String {
        generatedNames[origin]?.let { return it } // Already in cache

        if (!generatedNames.containsValue(origin.name)) {
            generatedNames[origin] = origin.name
            return origin.name
        }

        val homonym = generatedNames.filterValues { it == origin.name }.keys.first()
        val newParams = origin.parameters - homonym.parameters.toSet()
        val newName = origin.name + newParams.firstOrNull()?.name?.replaceFirstChar {it.titlecase() }
        if (!generatedNames.containsValue(newName)) {
            generatedNames[origin] = newName
            return newName
        }

        var poorName = origin.name
        while (generatedNames.containsValue(poorName)) {
            poorName += "_"
        }
        generatedNames[origin] = poorName
        return poorName
    }
}
