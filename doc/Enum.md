# Enum

## Problem with Kotlin before 1.6.20

KMP just doesn't support enum export yet, and this example does not compile. [KT-37916](https://youtrack.jetbrains.com/issue/KT-37916)

```kotlin
@JsExport
enum class Example {
    YES,
    NO
}
```

`Declaration of such kind (enum class) cant be exported to JS`

Well ok, and if I don't export it?

```kotlin
enum class Example {
    YES,
    NO
}

@JsExport
class ExampleConsumer {
    fun consume(example: Example) {
        println(example)
    }
}
```

will export this in typescript:

```typescript
class ExampleConsumer {
    constructor();
    consume(example: any /*Class sample._class.Example with kind: ENUM_CLASS*/): void;
}
```

Pretty unusable. And yes typescript dev could deal with `any` by copy-pasting some magic casting code, but then it could create issues if signature changes in the future, it's just not great. 


## Limitation with Kotlin after 1.6.20

Since 1.6.20, KotlinJs exports handle the enums ([KT-37916](https://youtrack.jetbrains.com/issue/KT-37916)). 
But if your enum has methods or properties that deal with non-exportable types (Long, List, ...) it may be interesting to use the `@KustomExport` instead of the more limited `@JsExport`.

Knowing that `@KustomExport` adds code, it's better for your bundle size to use `@JsExport` as much as possible.

> As it's only recently supported by Kotlin, and the Kotlin generated Ts code is not so far from our code generation, we may want to align generated code to have a similar look.

## Solution

Annotating your enum with `@KustomExport` will generate a class with a private constructor to mimic the enum behaviour, along with a list of constants for each enum entries.
With the previous example, the generated code in typescript now looks like that:

```typescript
const Example_YES: sample._class.js.Example;
const Example_NO: sample._class.js.Example;
class Example {
    private constructor();
    readonly name: string;
}
function Example_values(): Array<sample._class.js.Example>;
function Example_valueOf(name: string): Nullable<sample._class.js.Example>;


class ExampleConsumer {
    constructor();
    consume(example: sample._class.js.Example): void;
}
```

Why entries are not in the class/companion object?
It was our first implementation to have an object just to hold the entries, and so have a nice way to access values, like `Example.YES`. 

After some testing we experienced a non-negligible impact on creating an object to hold them, it adds 426 bytes to the bundle JS for each enum. So we decided to have a "_" instead of "." mainly for optimizations.
If it's choice is not right for you, please don't hesitate to create an issue, it could be an option in the compiler to you can choose what you prefer...

Additional notes:
- An issue already exists to be able to export an enum as Int/String, please cast your vote if you're interested.
- 2 additional methods values/valueOf are generated as helpers.

[Go back to README](../README.md)
