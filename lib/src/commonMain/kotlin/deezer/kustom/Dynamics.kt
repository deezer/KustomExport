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

// KSP issue is limiting JS generation at the moment
// https://github.com/google/ksp/issues/728
// As a hack, we generate JS facade for KotlinMetadata instead, so we define this expect/actual pattern
// Eventually we should only have dynamics in jsMain.


expect val dynamicNull: Any
expect val dynamicString: Any
expect val dynamicNotString: Any

