# Long

## Problem

Long are officially not supported by Kotlin JS as exportable.

Most timestamp are Long for example, and so requires to be usable through Typescript, also Typescript usually uses `number` to handle that.

## Solution

By creating a Facade that uses a Double instead of a Long, we keep a high precision while providing an easy/natural solution for Typescript.


# WARNING

>Note that it's DANGEROUS if you deal with high values that a Long can handle but not a Double/number, you risk to loose precision in the process.


#### Precision in JS

Js number precision (without decimal point) is capped at something around 9_000_000_000_000_000

Compared to Kotlin 9_223_372_036_854_775_807 (Long.MAX_VALUE), Kotlin Long is 1000 times bigger.
After that point, adding 1 probably means precision issue.

Some examples of JS precision issues after this limit:

    // 9100000000000001 + 1 = 9100000000000000
    // 9100000000000001 + 2 = 9100000000000002
    // 9100000000000001 + 3 = 9100000000000004 // actually OK
    // 9100000000000001 + 4 = 9100000000000004
    // 9100000000000001 + 5 = 9100000000000004
    // 9100000000000001 + 6 = 9100000000000006

Given your values are always above this limit, you should be fine. If not please open a ticket! Suggestions are also welcome!

[Go back to README](../README.md)
