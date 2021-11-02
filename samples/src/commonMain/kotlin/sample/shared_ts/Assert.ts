export function assert(condition: boolean, message: string) {
    if (condition) {
        console.log(" - ✅  " + message)
    } else {
        console.log(" - ❌  " + message)
    }
}
