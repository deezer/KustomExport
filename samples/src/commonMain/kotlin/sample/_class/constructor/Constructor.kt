package sample._class.constructor

import deezer.kustom.KustomExport

@KustomExport
open class Constructible(val count: Int, val foo: String)

@KustomExport
class Deconstructible(foo: String) : Constructible(33, foo)

@KustomExport
class Ctor1ParamNonNull(@Suppress("UNUSED") foo: String)

@KustomExport
class Ctor1ParamStringNullable(@Suppress("UNUSED") foo: String?)

@KustomExport
class Ctor1ParamIntNullable(@Suppress("UNUSED") foo: Int?)