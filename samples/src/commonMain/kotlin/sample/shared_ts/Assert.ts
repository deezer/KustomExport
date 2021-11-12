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
