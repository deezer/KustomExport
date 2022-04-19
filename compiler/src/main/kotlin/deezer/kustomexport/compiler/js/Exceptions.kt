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

import com.squareup.kotlinpoet.ClassName

//https://github.com/JetBrains/kotlin/blob/master/libraries/stdlib/js-ir/src/kotlin/exceptions.kt

val THROWABLE = ClassName("kotlin", "Throwable")
val ERROR = ClassName("kotlin", "Error")
val EXCEPTION = ClassName("kotlin", "Exception")
val RUNTIME_EXCEPTION = ClassName("kotlin", "RuntimeException")
val ILLEGAL_ARGUMENT_EXCEPTION = ClassName("kotlin", "IllegalArgumentException")
val ILLEGAL_STATE_EXCEPTION = ClassName("kotlin", "IllegalStateException")
val INDEX_OUT_OF_BOUNDS_EXCEPTION = ClassName("kotlin", "IndexOutOfBoundsException")
val CONCURRENT_MODIFICATION_EXCEPTION = ClassName("kotlin", "ConcurrentModificationException")
val UNSUPPORTED_OPERATION_EXCEPTION = ClassName("kotlin", "UnsupportedOperationException")
val NUMBER_FORMAT_EXCEPTION = ClassName("kotlin", "NumberFormatException")
val NULL_POINTER_EXCEPTION = ClassName("kotlin", "NullPointerException")
val CLASS_CAST_EXCEPTION = ClassName("kotlin", "ClassCastException")
val ASSERTION_ERROR = ClassName("kotlin", "AssertionError")
val NO_SUCH_ELEMENT_EXCEPTION = ClassName("kotlin", "NoSuchElementException")
val ARITHMETIC_EXCEPTION = ClassName("kotlin", "ArithmeticException")

// Coroutines
val CANCELLATION_EXCEPTION = ClassName("kotlinx.coroutines", "CancellationException")
val TIMEOUT_CANCELLATION_EXCEPTION = ClassName("kotlinx.coroutines", "TimeoutCancellationException")

val ALL_KOTLIN_EXCEPTIONS = listOf(
    THROWABLE,
    ERROR,
    EXCEPTION,
    RUNTIME_EXCEPTION,
    ILLEGAL_ARGUMENT_EXCEPTION,
    ILLEGAL_STATE_EXCEPTION,
    INDEX_OUT_OF_BOUNDS_EXCEPTION,
    CONCURRENT_MODIFICATION_EXCEPTION,
    UNSUPPORTED_OPERATION_EXCEPTION,
    NUMBER_FORMAT_EXCEPTION,
    NULL_POINTER_EXCEPTION,
    CLASS_CAST_EXCEPTION,
    ASSERTION_ERROR,
    NO_SUCH_ELEMENT_EXCEPTION,
    ARITHMETIC_EXCEPTION,
    CANCELLATION_EXCEPTION,
    TIMEOUT_CANCELLATION_EXCEPTION
)
