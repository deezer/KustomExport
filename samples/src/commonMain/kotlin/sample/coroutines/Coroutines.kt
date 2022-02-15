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

package sample.coroutines

import deezer.kustomexport.KustomExport
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import kotlin.coroutines.coroutineContext
import kotlin.js.Date

@KustomExport
interface IComputer {
    suspend fun longCompute(abortSignal: AbortSignal): Int
}

@KustomExport
class Computer : IComputer {
    override suspend fun longCompute(abortSignal: AbortSignal): Int {
        // listen abortSignal , if aborted => throw
        delay(1000)
        return 42
    }
}

@KustomExport
class ComputerTester(private val computer: IComputer) {
    suspend fun testAsync(): Int {
        return withContext(Dispatchers.Unconfined) {
            println(Date())
            val task1 = async { computer.longCompute() } //coroutineContext.job::isActive) }
            val task2 = async { computer.longCompute() } //coroutineContext.job::isActive) }

            coroutineContext.job.invokeOnCompletion { error ->
                if (error is CancellationException) {
                    //ctrler.abort()
                }
            }
            return@withContext task1.await() + task2.await()
        }
    }

    suspend fun startAndCancelAfter(duration: Long) {
        withContext(Dispatchers.Unconfined) {
            withTimeout(duration) {
                computer.longCompute()// { coroutineContext.job.isActive }
            }
        }
    }

    suspend fun startCancellable(): Cancellable {
        val coroutineScope = CoroutineScope(Dispatchers.Unconfined + SupervisorJob())
        val job = coroutineScope.launch {
            computer.longCompute() //coroutineContext.job::isActive)
        }

        return object : Cancellable {
            override fun cancel() {
                job.cancel()
            }
        }
    }
}

@KustomExport
interface Cancellable {
    fun cancel()
}