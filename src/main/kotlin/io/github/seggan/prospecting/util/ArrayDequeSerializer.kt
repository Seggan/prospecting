package io.github.seggan.prospecting.util

import io.github.seggan.sf4k.serial.serializers.DelegatingSerializer
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.ListSerializer

class ArrayDequeSerializer<T>(item: KSerializer<T>) :
    DelegatingSerializer<ArrayDeque<T>, List<T>>(ListSerializer(item)) {
    override fun toData(value: ArrayDeque<T>): List<T> = value.toList()
    override fun fromData(value: List<T>): ArrayDeque<T> = ArrayDeque(value)
}