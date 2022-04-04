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

package deezer.kustomexport

import kotlin.reflect.KClass

@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPEALIAS, AnnotationTarget.FUNCTION)
public annotation class KustomExport(
    /**
     * [usedByKustomExportGeneric]==true means the class will be used by a KustomExportGeneric:
     * - it's ignored by the @KustomExport generation (will not generate the wrapper classes),
     * - but it's used to know if the class is handled by the compiler or by KotlinJs directly.
     * It's required when mixing JsExport and KustomExport (and possibly no annotation),
     * on multi-module projects. Multi-module means that a low-level module can
     * expose a generic class that a higher level module will use to generate some
     * derived classes with [KustomExportGenerics].
     * In this setup, if a middle-level module expose the generic class, it will
     * require knowing the class is mapped or not by the compiler at some point.
     */
    val usedByKustomExportGeneric: Boolean = false
)

@Target(AnnotationTarget.FILE)
public annotation class KustomExportGenerics(
    public val exportGenerics: Array<KustomGenerics> = []
)

@Target() // No target, only there for data container
public annotation class KustomGenerics(
    public val kClass: KClass<*>,
    public val typeParameters: Array<KClass<*>>,
    public val name: String = "",
)