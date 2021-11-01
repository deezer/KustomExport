import Samples from '@kustom/Samples'

try {
    var simpleClass = new Samples.sample._class.simple.js.SimpleClass()
    if (simpleClass.simpleValue == 42) {
        console.log("✅  SimpleClass: can retrieve the value")
    } else {
        console.log("❌  SimpleClass: can retrieve the value")
    }
} catch(error) {
    console.log("🔥  SimpleClass")
    console.error(error)
}
