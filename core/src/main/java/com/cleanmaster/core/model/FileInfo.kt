package com.cleanmaster.core.model

data class FileInfo(
    val path: String,
    val name: String,
    val size: Long,
    val lastModified: Long,
    val mimeType: String,
    val hash: String? = null
)
