package com.kneelawk.ledcontroller1

object ByteUtils {
    fun startsWith(array: ByteArray, prefix: ByteArray): Boolean {
        if (array.size < prefix.size) {
            return false
        }

        for (i in prefix.indices) {
            if (array[i] != prefix[i]) {
                return false
            }
        }

        return true
    }
}