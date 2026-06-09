package com.cleanmaster.core.model

import androidx.room.TypeConverter

class Converters {
    @TypeConverter
    fun fromFileType(value: FileType): String = value.name

    @TypeConverter
    fun toFileType(value: String): FileType = FileType.valueOf(value)
}
