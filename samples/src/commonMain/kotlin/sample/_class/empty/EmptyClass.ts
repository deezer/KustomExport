import Samples from '@kustom/Samples'

try {
    var emptyClass = new Samples.sample._class.empty.js.EmptyClass()
    console.log("✅  EmptyClass: Can instantiate")
} catch(error) {
    console.log("❌  EmptyClass: Cannot instantiate")
    console.error(error)
}
