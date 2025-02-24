package com.example.frontzephiro.utils

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateAdapter : TypeAdapter<LocalDate>() {
    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: LocalDate) {
        out.value(value.format(formatter))
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @Throws(IOException::class)
    override fun read(`in`: JsonReader): LocalDate {
        return LocalDate.parse(
            `in`.nextString(),
            formatter
        )
    }

    companion object {
        @RequiresApi(Build.VERSION_CODES.O)
        private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
    }
}