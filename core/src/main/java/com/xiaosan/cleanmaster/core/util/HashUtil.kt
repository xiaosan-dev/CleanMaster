package com.xiaosan.cleanmaster.core.util

import java.io.File
import java.security.MessageDigest

object HashUtil {

    fun calculateSha256(file: File): String {
        val buffer = ByteArray(8192)
        val digest = MessageDigest.getInstance("SHA-256")
        file.inputStream().buffered().use { stream ->
            var bytes = stream.read(buffer)
            while (bytes != -1) {
                digest.update(buffer, 0, bytes)
                bytes = stream.read(buffer)
            }
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
