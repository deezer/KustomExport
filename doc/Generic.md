# Generic

## Problem

Kotlin:

```kotlin
@JsExport
open class A

@JsExport
class B : A()

@JsExport
class Example<T : A> {
    fun consume(t: T) {
        println(t) // t could be an instance of A or B
    }
}
```

will generate this typescript:
```typescript
class A {
    constructor();
}
class B extends sample._class.A {
    constructor();
}
class Example<T> {
    constructor();
    consume(t: T): void;
}
```

There are some problems with generics when using a wrapping approach:
1) support for typescript with Kotlin 1.6.0 and before doesn't provide generics bounds. That means if you define a class with `Foo<T : String>`, it'll generate a typescript class with a generic `T` but no bounds, so that you can pass anything really.
2) wrapping approach like KustomExport requires to be able to wrap the entire public API of your library. That means if you provide a class with a function that takes a `T` the wrapper have to figure out what's the type of T and select the good wrapper. 
Unfortunately, it's not something easy when dealing with typescript, as the implementation can be defined in Kotlin or in Typescript. If we limit the wrapper to the generic bounds, it also means the wrapped object will likely have a wrong type, possibly creating issue with `as`, `is` and possibly more.

Also an important point for Kotlin developers: Typescript is way more flexible than Kotlin.

For example given an interface `X` defining a method `foo`, and a consumer class expecting an instance of `X`, a typescript developer can create a class `Y`, just define a method `foo()` and typescript will be ok to pass this instance into the consumer expecting a `X`. Yes even if `Y` doesn't implement `X`, methods are matching so that's the same.
As such, you can always bypass a `sealed class` or a final class rules and define your own implementations / override implementation from the outside.

As a lib developer it can be quite problematic to be too flexible with generics when you need to enforce some rules. 

## Solution

Our usage is currently focused on delivering the best API, but we know we're all dealing with our own needs, so please feel free to create an issue and discuss your needs.

We offer a solution that generates one or multiple wrappers by replacing the generics by the final class.
Back to the original example, you can replace annotations on `A` and `B` and then add those lines on top of your file:

```kotlin
@file:KustomExportGenerics(
    exportGenerics = [
        KustomGenerics(Example::class, arrayOf(A::class), "AExample"),
        KustomGenerics(Example::class, arrayOf(B::class), "BExample"),
    ]
)
```

This tells the compiler to generate 2 wrappers like if there were 2 different implementations for each type:

```typescript
class AExample {
    constructor();
    consume(t: A): void;
}
class BExample {
    constructor();
    consume(t: B): void;
}
```

so technically these 2 wrappers have no doubt how to handle `A` and `B`, and typescript developers can choose one or the other depending of the expected behaviour. Those 2 wrappers are still using the same "common" class, so usage is transparent, and you can safely use a `if (foo is B)` in your class.

If you don't specify the 3rd parameters for the generated class name, the processor re-use the class name. Indeed, it's problematic if you have more than one possible implementation, and then you will have to use the name.

Yes, you can have multiple generics, defines how much you want in the 2nd parameter (the array).

A type forced in this way is visible in the build module. It means that if you deal with a `Example<Long>` in a class, and have already defined an export for `Example<Long>`, the right one will be taken. But this definition is not propagated to other modules. Please create an issue if you need it.

---

### Typealias


Using `@file:KustomExportGenerics` is limiting the code generation:
- the order of generics are not checked against the defined bounds, be careful
- it's verbose and intent is now explicit enough

Another approach based on typealias **could be available soon**, to replace

```kotlin
@file:KustomExportGenerics(
    exportGenerics = [
        KustomGenerics(Example::class, arrayOf(A::class), "AExample"),
        KustomGenerics(Example::class, arrayOf(B::class), "BExample"),
    ]
)
```

by 

```kotlin
@KustomExport
typealias AExample = Example<A>
@KustomExport
typealias BExample = Example<B>
```

Stay tuned, or create/vote on the related issue.
