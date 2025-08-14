package uddug.com.domain.entities.profile

import com.google.gson.annotations.SerializedName


data class SettingsForm(
    @SerializedName("forms") var forms: ArrayList<FormContainer> = arrayListOf()
)