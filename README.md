### Status: ⚠ Experimentation ⚠
# KustomExport: a KSP generator of JS facade

## Motivation
Providing a nice JS API can sometimes be complex from a Kotlin Multiplatform Project.

A common example, let's say you want to expose an object to iOS, Android and Web that is defined by: 
```kotlin
data class SomeDataObject(
    val timestamp: Long,
    val state: StateEnum, // enum class StateEnum { IDLE, RUNNING }
    val idList: List<String>
)
```
Android and iOS will be happy with that, but there is no proper way to export that simple object to Js/Typescript today:
- `Long` will not produce a `number` but a `kotlin.Long` ([doc](https://kotlinlang.org/docs/js-to-kotlin-interop.html#kotlin-types-in-javascript)), but web developers usually use `number` to store timestamp.
- `Enum`s are not handled yet ([KT-37916](https://youtrack.jetbrains.com/issue/KT-37916)) and so exported as `any`
- `List` could be better in 99% of cases if it was exported in Arrays

There are good reasons why it's not supported by KotlinJs directly, but it's not practical to provide a clean Typescript API.

## Technical approach

While changing the typescript output is probably the more efficient way, it's usually a bit simpler to generate some Kotlin Facades to do the work. For example, the previous class could be cleaned with:

```kotlin
// jsMain/StateEnumJs.kt
// Export the enum in a class, so it's providing a real class in JS instead of 'any'
@JsExport
class StateEnumJs internal constructor(internal val stateEnum: StateEnum) {
    val name: String = stateEnum.name
}
fun StateEnumJs.import(): StateEnum = value
fun StateEnum.export(): StateEnumJs = Encoding(this)
// Object that exposes all possible values of the enum (note the 's')
@JsExport
object StateEnumsJs {
    val IDLE: StateEnumJs = StateEnum.IDLE.exportEncoding()
    val RUNNING: StateEnumJs = StateEnum.RUNNING.exportEncoding()
}

// jsMain/SomeDataObjectJs.kt
@JsExport
class SomeDataObjectJs(
    val timestamp: Double,// -> number in Typescript
    val state: StateEnumJs,
    val idList: Array<String>
)
fun SomeDataObjectJs.import(): SomeDataObject = ...
fun SomeDataObject.export(): SomeDataObjectJs = ...
```

This way, you expose a more typical Typescript API, but it's a lot more boilerplate that you've to write, hence the use of KSP to generate this boilerplate.

If you write similar facades yourself, this generator could help you avoid writing them manually. Please open issues to discuss your needs!

Note that it's adding more code, so if you're exposing already a lot of classes, you should be prepared to a significant increase in the JS bundle size. It's the cost to have a great Typescript API with KotlinJs today.

## Current status

The current project is partially tested (unit tests + Typescript integration tests in `samples`).
What we can generate today: 
- `Long` to `number` (by using toLong/toDouble, so be careful with precision issues here!)
- `List<...>` to `Array<...>` and it's ready to support more collections, please open a ticket with your needs.
- enum classes
- class / data class (equals/toString/componentX methods are removed)
- functions and dynamic properties are wrapped (ex `val rand: Long get() = Random.nextLong()` will be wrapped and called again each time the exposed object is called in Typescript)
- interfaces

What we don't support yet:
- generics (quite tricky as we need to export/import an unknown object, not sure about feasability)
- abstract/open/sealed class (not written yet, no technical blockers so far)

You can have a look to the [Samples](samples/src/commonMain/kotlin/sample) to have a feel of how it can be used.


## Licence
```
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
```
