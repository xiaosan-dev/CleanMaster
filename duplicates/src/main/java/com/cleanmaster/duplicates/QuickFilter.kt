package com.cleanmaster.duplicates

import com.cleanmaster.core.model.FileInfo

object QuickFilter {

    /**
     * Groups files by (name, size). Returns only groups with 2+ files.
     */
    fun filter(files: List<FileInfo>): List<List<FileInfo>> {
        return files
            .groupBy { "${it.name}_${it.size}" }
            .values
            .filter { it.size >= 2 }
    }
}
