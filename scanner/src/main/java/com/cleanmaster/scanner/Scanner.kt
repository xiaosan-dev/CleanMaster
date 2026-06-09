package com.cleanmaster.scanner

import kotlinx.coroutines.flow.Flow

interface Scanner<T> {
    suspend fun scan(rootPath: String): Flow<ScanProgress>
    fun getResult(): T
}
