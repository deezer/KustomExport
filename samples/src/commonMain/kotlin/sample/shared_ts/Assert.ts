import { testScopeName } from "./RunTest"


export function assertQuiet(condition: boolean, message: string) {
    if (!condition) assert(false, message)
}

export function assert(condition: boolean, message: string) {
    if (condition) {
        console.log("✅   " + testScopeName + ": " + message)
    } else {
        console.log("❌   " + testScopeName + ": " + message)
    }
}

export function assertEqualsQuiet<T>(expected: T, actual: T, message: string) {
    if (expected != actual) assertEquals(expected, actual, message)
}

export function assertEquals<T>(expected: T, actual: T, message: string) {
    if (expected == actual) {
        console.log("✅   " + testScopeName + ": " + message)
    } else {
        console.log("❌   " + testScopeName + ": " + message + "\n    Expected: "+expected + "\n    Actual  : " + actual + "")
    }
}
