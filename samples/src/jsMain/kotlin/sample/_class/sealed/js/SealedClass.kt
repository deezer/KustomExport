/*
 * Copyright 2021 Deezer.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package sample._class.sealed.js
/*
@JsExport
// Same class modifiers & constructor
sealed class SealedParent(val ctorParam: String) {
    // All fields/functions are abstract
    abstract val field: Int
    abstract val hardcoded: Double
    abstract fun computeFoo(): Int
}
// List all childs of the sealed class in the compilation module
public fun sample._class.sealed.SealedParent.exportSealedParent(): SealedParent {
    return when (this) {
        is sample._class.sealed.SealedChild1 -> exportSealedChild1()
        is sample._class.sealed.SealedChild2 -> exportSealedChild2()
    }
}

public fun SealedParent.importSealedParent(): sample._class.sealed.SealedParent {
    return when (this) {
        is SealedChild1 -> importSealedChild1()
        is SealedChild2 -> importSealedChild2()
    }
}*/
/*
@JsExport
public class SealedChild1(
    `field`: Int,
    ctorParam: Double
) : SealedParent(ctorParam) {
    internal lateinit var common: sample._class.sealed.SealedChild1

    init {
        if (field != deezer.kustom.dynamicNull) {
            common = sample._class.sealed.SealedChild1(
                field = field,
                ctorParam = ctorParam.toLong()
            )
        }
    }

    public val child1Field: String
        get() = common.child1Field

    public override val `field`: Int
        get() = common.field

    public override val hardcoded: Double
        get() = common.hardcoded.toDouble()

    override fun computeFoo(): Int {
        return common.computeFoo()
    }

    internal constructor(common: sample._class.sealed.SealedChild1) : this(
        field = deezer.kustom.dynamicNull,
        ctorParam = deezer.kustom.dynamicNull
    ) {
        this.common = common
    }
}

public fun sample._class.sealed.SealedChild1.exportSealedChild1(): SealedChild1 = SealedChild1(this)

public fun SealedChild1.importSealedChild1(): sample._class.sealed.SealedChild1 = this.common

@JsExport
public class SealedChild2(
    `field`: Int
) : SealedParent(4242.0) {
    internal lateinit var common: sample._class.sealed.SealedChild2

    init {
        if (field != deezer.kustom.dynamicNull) {
            common = sample._class.sealed.SealedChild2(
                field = field
            )
        }
    }

    public val child2Field: String
        get() = common.child2Field

    public override var `field`: Int = 0
        get() = common.field
        set(`field`) {
            common.field = field
        }

    public override val hardcoded: Double
        get() = common.hardcoded.toDouble()

    internal constructor(common: sample._class.sealed.SealedChild2) : this(field = deezer.kustom.dynamicNull) {
        this.common = common
    }

    public override fun computeFoo(): Int {
        val result = common.computeFoo()
        return result
    }
}

public fun sample._class.sealed.SealedChild2.exportSealedChild2(): SealedChild2 = SealedChild2(this)

public fun SealedChild2.importSealedChild2(): sample._class.sealed.SealedChild2 = this.common
*/
