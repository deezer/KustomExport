export var testScopeName: string
export async function runTest(testName:string, block: () => void) {
    testScopeName = testName
    try {
        await block()
    } catch(error) {
        console.log("❌   " + testScopeName + ": Exception occurred during test")
        console.error(error)
    }
}