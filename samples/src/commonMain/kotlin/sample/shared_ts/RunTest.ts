export var testScopeName: string
export function runTest(testName:string, block: () => void) {
    testScopeName=testName
    try {
        block()
    } catch(error) {
        console.log("❌   " + testScopeName + ": Exception occurred during test")
        console.error(error)
    }
}