package uddug.com.naukoteka.di.utils

import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter
import toothpick.InjectConstructor
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

@InjectConstructor
class CalendarTypeAdapter : TypeAdapter<Calendar>() {

    companion object {
        const val MAIN_SERVER_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"
        val PARSE_DATE_FORMATS = arrayOf(MAIN_SERVER_DATE_FORMAT)
    }


    @Throws(IOException::class)
    override fun write(writer: JsonWriter, value: Calendar?) {
        if (value == null) writer.nullValue()
        else {
            val dateFormat = SimpleDateFormat(MAIN_SERVER_DATE_FORMAT, Locale.getDefault())
//                dateFormat.timeZone = timeZone
            writer.value(dateFormat.format(value.time))
        }
    }

    @Throws(IOException::class)
    override fun read(jsonReader: JsonReader): Calendar? {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull()
            return null
        }
        var calendar: Calendar? = null
        var timeInMillisServer = jsonReader.nextLong()

        for (format in PARSE_DATE_FORMATS) {
            try {
                calendar = Calendar.getInstance().apply { timeInMillis = timeInMillisServer * 1000 }
            } catch (e: Exception) {
                continue
            }
            break
        }
        return calendar
    }
}

inline fun <reified T : Any> GsonBuilder.registerIntTypeAdapter(
    noinline getKey: (T) -> Int,
    noinline getVal: (Int) -> T?
) = apply {
    registerTypeAdapter(T::class.java, IntTypeAdapter(getKey, getVal))
}

class IntTypeAdapter<T : Any>(
    private val getKey: (T) -> Int,
    private val getVal: (Int) -> T?
) : TypeAdapter<T>() {
    override fun read(reader: JsonReader) = reader.readInt(getVal)
    override fun write(writer: JsonWriter, value: T?) = writer.writeInt(value, getKey)
}

private fun <T : Any> JsonReader.readInt(getVal: (Int) -> T?): T? =
    if (peek() == JsonToken.NULL) run { nextNull(); null }
    else getVal(nextInt())

private fun <T : Any> JsonWriter.writeInt(value: T?, getKey: (T) -> Int) {
    value?.let { value(getKey(it)) } ?: nullValue()
}

inline fun <reified T : Any> GsonBuilder.registerStringTypeAdapter(
    noinline getKey: (T) -> String,
    noinline getVal: (String) -> T?) = apply {
    registerTypeAdapter(T::class.java, StringTypeAdapter(getKey, getVal))
}

class StringTypeAdapter<T : Any>(
    private val getKey: (T) -> String,
    private val getVal: (String) -> T?
) : TypeAdapter<T>() {
    override fun read(reader: JsonReader) = reader.readString(getVal)
    override fun write(writer: JsonWriter, value: T?) = writer.writeString(value, getKey)
}

private fun <T : Any> JsonReader.readString(getVal: (String) -> T?): T? =
    if (peek() == JsonToken.NULL) run { nextNull(); null }
    else getVal(nextString())

private fun <T : Any> JsonWriter.writeString(value: T?, getKey: (T) -> String) {
    value?.let { value(getKey(it)) } ?: nullValue()
}