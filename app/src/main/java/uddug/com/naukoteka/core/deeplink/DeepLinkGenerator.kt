package uddug.com.naukoteka.core.deeplink

import android.content.Intent
import android.net.Uri
import uddug.com.naukoteka.core.deeplink.links.DeepLink
import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.reflect.typeOf

object DeepLinkGenerator {

    /**
     * Генерация Uri
     *
     * @param deeplink [DeepLink]
     * @return [Intent]
     */
    @Suppress("UNCHECKED_CAST")
    fun generate(deeplink: DeepLink): Intent {
        val deeplinkBuilder = StringBuilder("trendapp://${deeplink.scheme.module}.trendagent.ru")
        (deeplink::class as KClass<DeepLink>).declaredMemberProperties.forEach { property ->
            property.get(deeplink)?.let {
                if (it.toString().isNotBlank()) {
                    deeplinkBuilder.append("/${property.name}/${it.toString().toUrlEncode()}")
                }
            }
        }
        val deeplinkIntent = Intent(Intent.ACTION_VIEW)
        deeplinkIntent.data = Uri.parse(deeplinkBuilder.toString())
        return deeplinkIntent
    }
}

inline fun <reified T : DeepLink> Uri?.parseDeeplink(): T {
    val mapConstructorParameters = mutableMapOf<KParameter, Any?>()
    val instance = T::class.objectInstance ?: T::class.createInstance()

    this ?: return instance::class.primaryConstructor!!.callBy(mapConstructorParameters)

    instance::class.primaryConstructor?.parameters?.forEach { constructorParameter ->
        val valueIndexInPath = run {
            var index = pathSegments.indexOf(constructorParameter.name)
            if (index < 0) {
                index = pathSegments.lastIndex
            }
            return@run ++index
        }

        val parameterValue = when (constructorParameter.type) {
            typeOf<Boolean>(), typeOf<Boolean?>() -> pathSegments.getOrNull(valueIndexInPath)
                ?.toBoolean()

            typeOf<String?>() -> pathSegments.getOrNull(valueIndexInPath)?.toUrlDecode()
            typeOf<Int?>() -> pathSegments.getOrNull(valueIndexInPath)?.toIntOrNull()
            typeOf<Float?>() -> pathSegments.getOrNull(valueIndexInPath)?.toFloatOrNull()
            typeOf<Double?>() -> pathSegments.getOrNull(valueIndexInPath)?.toDouble()
            typeOf<Long?>() -> pathSegments.getOrNull(valueIndexInPath)?.toLong()
            else -> null
        }

        mapConstructorParameters[constructorParameter] = parameterValue
    }

    return instance::class.primaryConstructor!!.callBy(mapConstructorParameters).apply {}
}
