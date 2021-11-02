package sample._class.data.js

import sample._class.data.DataClass as CommonDataClass

@JsExport
val singletonDataClass = DataClass(CommonDataClass())
