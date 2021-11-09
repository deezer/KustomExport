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

package deezer.kustom

/**
 * As of today (2021/20/18) the KSP + KotlinJsIr compiler has some issues with JS sourceSets.
 * https://docs.google.com/spreadsheets/d/13lXyEHu1GzwgicWvTqnJf_qCcxOY2vwM04gSfx_f1fk/edit#gid=0
 * As a workaround, we're declaring a new annotation in each module and pass the annotation full name in args to ksp.
 * When KSP/Kotlin is more stable with source sets, this could be used to define once and for all the annotation.
 */

enum class ExportMode {
    ONLY_IMPORT, ONLY_EXPORT, IMPORT_EXPORT
}

// @Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
public annotation class KustomExport(
    public val mode: ExportMode = ExportMode.IMPORT_EXPORT
)
