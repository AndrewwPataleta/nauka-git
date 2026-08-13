package uddug.com.data.services.models.request.user_profile

import com.google.gson.annotations.SerializedName

// Тело PATCH core/user_subscription — подписка/отписка на сущность.
// rEntity — uref профиля ("номер объекта:ID"), subscribed — целевое состояние.
data class EntitySubscriptionDto(
    @SerializedName("rEntity")
    val rEntity: String,
    @SerializedName("subscribed")
    val subscribed: Boolean
)
