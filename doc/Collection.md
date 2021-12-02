# Collection

## Problem

By default, exporting a collection like `List<Int>` is not generating an easy-to-use object and is considered not
exportable: `Exported declaration uses non-exportable property type: List<Int>`.

If you want to export the class anyway, the generated typescript looks like that:

```typescript
class Example {
    constructor();
    readonly myList: kotlin.collections.List<number>;
}
```

As `kotlin.collections.List<>` is not exported to Typescript, you have to convert list to array and vice-versa.
Even a simple `forEach` on this List will look complex enough, and may end up with duplicated tricks several times in the typescript source code.

Previously we were using this trick on the KMP side

```kotlin
@Suppress("NON_EXPORTABLE_TYPE")
@JsExport
public class CrossListHelper {
    @JsName("CreateListFromJSArray")
    public fun <T> CreateListFromJSArray(jsArray: Array<T>?): List<T>? {
        return if (jsArray == null || jsArray.isEmpty()) null else jsArray.toList()
    }
}
```
and using the helper on a lot of API points from Kotlin to Ts.

## Solution

By annotating a class with `KustomExport`, we'll generate a wrapper for the class to provide a better API.

```kotlin
@KustomExport
class Example {
    val myList = listOf(1, 2, 3)
}
```

Generated typescript:
```typescript
class Example {
    constructor();
    readonly myList: Array<number>;
}
```

[Go back to README](../README.md)
