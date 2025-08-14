package uddug.com.naukoteka.di.utils.http

import com.google.gson.annotations.SerializedName
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

class EnumConverterFactory : Converter.Factory() {
    override fun stringConverter(type: Type?, annotations: Array<out Annotation>?,
                                 retrofit: Retrofit?): Converter<*, String>? {
        if (type is Class<*> && type.isEnum) {
            return Converter<Any?, String> { value -> (value as Enum<*>).getSerializedNameValue() }
        }
        return null
    }

    private fun Enum<*>.getSerializedNameValue(): String {
        return try {
            javaClass.getField(name).getAnnotation(SerializedName::class.java).value
        } catch (exception: NoSuchFieldException) {
            exception.printStackTrace()
            ""
        }
    }
}