# How we see the future

Mapping from type to type should be parameterizable, potentially via a gradle plugin. Example:

```
customMapping {
    js {
        longToDouble = true // Enable the export long to double
        enum = EnumMode.Wrapping // Select one of the available mapping mode for an enum
        custom = listOf(
            CustomMapping(
                original = "kotlin.Int",
                exported = "kotlin.Float", // Because... why not, that's a stupid example
                exportMethod = "toFloat()",
                importMethod = "toInt()",
            )
        )
    }
}
```
