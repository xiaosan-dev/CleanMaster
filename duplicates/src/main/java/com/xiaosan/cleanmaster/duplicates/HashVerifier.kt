package com.xiaosan.cleanmaster.duplicates

import com.xiaosan.cleanmaster.core.model.DuplicateGroup
import com.xiaosan.cleanmaster.core.model.FileInfo
import com.xiaosan.cleanmaster.core.util.HashUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

object HashVerifier {

    /**
     * Takes candidate groups from QuickFilter, computes SHA-256,
     * and returns only groups where all files have the same hash.
     */
    suspend fun verify(candidates: List<List<FileInfo>>): List<DuplicateGroup> {
        return withContext(Dispatchers.IO) {
            candidates.mapNotNull { group ->
                val filesWithHash = group.map { file ->
                    file to HashUtil.calculateSha256(File(file.path))
                }

                val hashGroups = filesWithHash.groupBy { it.second }
                val duplicateHashGroup = hashGroups.entries.find { it.value.size >= 2 }

                duplicateHashGroup?.let { entry ->
                    DuplicateGroup(
                        id = UUID.randomUUID().toString(),
                        files = entry.value.map { it.first.copy(hash = entry.key) },
                        totalSize = entry.value.sumOf { it.first.size },
                        fileHash = entry.key
                    )
                }
            }
        }
    }
}
