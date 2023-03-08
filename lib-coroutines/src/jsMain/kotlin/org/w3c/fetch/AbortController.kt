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
//package org.w3c.fetch // I want to rely on the external type, no need for namespace here

// Inspired from https://github.com/JetBrains/kotlin-wrappers/blob/master/kotlin-browser/src/main/kotlin/org/w3c/fetch/AbortController.kt

public external class AbortController {
    /**
     * Returns the AbortSignal object associated with this object.
     */
    public val signal: AbortSignal

    /**
     * Invoking this method will set this object's AbortSignal's aborted flag and signal to any observers that the associated activity is to be aborted.
     */
    public fun abort()
}

/** A signal object that allows you to communicate with a DOM request (such as a Fetch) and abort it if required via an AbortController object. */
public external class AbortSignal {
    /**
     * Returns true if this AbortSignal's AbortController has signaled to abort, and false otherwise.
     */
    public val aborted: Boolean
    public var onabort: ((event: dynamic) -> Unit)?
}
