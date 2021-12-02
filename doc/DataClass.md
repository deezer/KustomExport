# Data class

## Problem

`data class` is a very powerful Kotlin tool that handle struct implementation requirements (mainly for JVM):

- equals()/hashcode() are generated and compare each properties from the primary constructor
- toString() to create a pretty string of the instance
- copy to be able to change a few values and clone the other ones.

So a basic data class like that

```kotlin
@JsExport
data class Example(val a: Int = 1, val b: Int = 2, val c: Int = 3)
```

will generate

```typescript
class Example {
    constructor(a: number, b: number, c: number);
    readonly a: number;
    readonly b: number;
    readonly c: number;
    component1(): number;
    component2(): number;
    component3(): number;
    copy(a: number, b: number, c: number): sample._class.Example;
    toString(): string;
    hashCode(): number;
    equals(other: Nullable<any>): boolean;
}
```

## Solution

By using `@KustomExport` instead, you remove all those unused methods and expose a simple class instead.

```typescript
class Example {
    constructor(a: number, b: number, c: number);
    readonly a: number;
    readonly b: number;
    readonly c: number;
}
```

