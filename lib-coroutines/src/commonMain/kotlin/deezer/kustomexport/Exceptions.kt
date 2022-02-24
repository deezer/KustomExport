/*
 * Copyright 2022 Deezer.
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

@file:Suppress("NON_EXPORTABLE_TYPE")
@file:OptIn(ExperimentalJsExport::class)

package deezer.kustomexport

import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport
import kotlinx.coroutines.CancellationException as CommonCancellationException
import kotlinx.coroutines.TimeoutCancellationException as CommonTimeoutCancellationException

@JsExport
open class CancellationException(message: String? = null, cause: Throwable? = null) :
    IllegalStateException(message, cause) {
    override fun import() = CommonCancellationException(message, cause)
}

fun CommonCancellationException.export() = CancellationException(message, cause)

// WARNING: as `coroutine` field and ctor are internal in kotlin implementation, we can't export it properly.
// Current solution: ignore this field and import()
@JsExport
open class TimeoutCancellationException(message: String?) : CancellationException(message)

fun CommonTimeoutCancellationException.export() = TimeoutCancellationException(message)
