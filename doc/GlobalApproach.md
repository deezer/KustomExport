## Our case study

Wrapping a `Long` to `number` or a `List<String>` to `array[String]` is tedious, but when you have some depths in your hierarchy, it becomes more and more difficult to provide a nice API everywhere.

As an example, let's say you provide a Factory that creates some classes that have a data class inside them:

```kotlin
@JsExport
data class Wheel(val model: String, val createdAt: Int)

@JsExport
class Car {
    val wheels = Array(4) { Wheel("Michelin", 10) }
}

@JsExport
class Factory {
    fun create() = Car()
}
```

Then later you decide to change the type of the `createdAt` to `Long` because that's usually how milliseconds timestamp are stored.
Indeed, it's fine for most platforms, but not JS because Long are not exportable. 

At this point you can adopt a strategy:
1) always use Double instead of Long
PROs: simple for JS/Typescript developers
CONs: Android, iOS, desktop, ... don't like to have to deal with floating point when it's an integer, just make no sense for them.
2) Try to play with some actual/expect and define new classes for each occurrence of Long in your code.
3) Use a actual/expect on a value class: please tell me if someone can make this work!
4) Define a wrapper class for Wheel that will handle the Long<->Double conversion.

So you decide to write a JS facade for Wheel, something like this:

```kotlin
// Not exportable
data class Wheel(val model: String, val diameter: Long)

@JsExport
class WheelJs(wheel:Wheel) {
    val model: String = wheel.model
    // Double will be mapped in number and precision should be enough in most cases
    val diameter: Double = wheel.diameter.toDouble()
}
```
You also need to map from Wheel to WheelJs and vice-versa, so 2 import/export methods.
You could define a expect/actual for Wheel, but you'll still need to define the type at some point so that's not a solution.
Indeed, these wrapping methods have to be called at some points, but when and by who? 

And then Typescript wants to create a Car directly for some reasons, so you have to provide a constructor for Car and it should exposes WheelJs instead of Wheel.
If Car has some functions, then you also need to wrap function parameters and return types, from a KMP/common standard class to the Js version.

Eventually you realize that wrapping has to be done in the entire hierarchy, for every class/instance/functions, if you want it to be managed everywhere with no hiccup. And that's a ton of work, but thankfully we can generate that.

# Global approach

The current approach we recommend is to simply replace all your `@JsExport` by `@KustomExport`, so that KustomExport ensure for you that your API will provide a nice mapping everywhere.

Indeed, you can also remove the duplicated `actual interface` or other mapping stuff you could have, and appreciate a better Typescript API with less code to maintain.

## Cost

As it's KSP powered, it means KustomExport generates Kotlin code and cannot change bytecode or write Javascript/Typescript directly.
As a direct result, there is a cost on the JS bundle size that can vary depending on the API surface you're providing.
If 90% of your library is exported for example, then the several generated classes will probably almost double the JS bundle size.
Anyway, we advise you to test your bundle size after installing the solution, and confirm the cost is OK for your project.

Wrapping also means potentially more function calls or performance impact if it's on a critical path. Most wrapping are straightforward and could be optimized by your js packaging, but if it's critical for you, you should definitely make some benchmarks first. 
