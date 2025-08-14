package com.nauchat.core.ext

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

fun <T> setOnce(): SetOnce<T> = SetOnce()

class SetOnce<T> : ReadWriteProperty<Any?, T?> {
    private var value: T? = null

    override fun getValue(thisRef: Any?, property: KProperty<*>): T? {
        return this.value
    }

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T?) {
        this.value ?: run {
            this.value = value
        }
    }
}
