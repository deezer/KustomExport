# Long

## Problem

Long are officially not supported by Kotlin JS as exportable.

Most timestamp are Long and so requires to be usable throught Typescript, also Typescript usually uses `number` to handle that.

## Solution

By creating a Facade that uses a Double instead of a Long, we keep a high precision while providing an easy/natural solution for Typescript.

>Note that it's DANGEROUS if you deal with high values that a Long can handle but not a Double/number, you risk to loose precision in the process.

[Go back to README](../README.md)
