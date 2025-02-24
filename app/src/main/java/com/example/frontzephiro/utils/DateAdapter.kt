package com.example.frontzephiro.utils

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.text.SimpleDateFormat
import java.util.*

class DateAdapter : TypeAdapter<Date?>() {
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    override fun write(out: JsonWriter, value: Date?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(dateFormat.format(value))
        }
    }

    override fun read(`in`: JsonReader): Date? {
        return if (`in`.peek() == com.google.gson.stream.JsonToken.NULL) {
            `in`.nextNull()
            null
        } else {
            dateFormat.parse(`in`.nextString())
        }
    }
}
