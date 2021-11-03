export function runTest(testName:string, block: () => void) {
    try {
        console.log("Testing " + testName)
        block()
    } catch(error) {
        console.log("❌  " + testName + ": Exception occurred during test")
        console.error(error)
    }
}