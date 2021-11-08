export function assertQuiet(condition: boolean, message: string) {
    if (!condition) assert(false, message)
}


export function assert(condition: boolean, message: string) {
    if (condition) {
        console.log(" - ✅  " + message)
    } else {
        console.log(" - ❌  " + message)
    }
}
