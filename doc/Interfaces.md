# Interfaces

## Problem

Interfaces are not exportable.

`Declaration of such kind (interface) cant be exported to JS`

Only `external` interfaces are exportable. That means you have to copy-paste the interface from commonMain to all your specific module (`androidMain`, `iosMain`, `iosSimulatorArm64`, ...), mark the commonMain one as `expect`, all the other ones as `actual` and eventually the jsMain one can be marked `external` and receive the `@JsExport` annotation.

That's a bit tedious and very painful to update several files when changing the signature of one method. 

## Solution

Just mark the interface with `@KustomExport` and it's now available for typescript.

Internally it's generating a wrapper + 2 import/export methods so that all input/outputs can be mapped from typescript to kotlin types.

[Go back to README](../README.md)
