package uddug.com.naukoteka.presentation.registration_first_step

import uddug.com.domain.repositories.auth.models.SocialType
import uddug.com.naukoteka.global.base.SocialLoginView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.StateStrategyType

@StateStrategyType(AddToEndSingleStrategy::class)
interface RegistrationFirstStepView : SocialLoginView {
    fun showButtonState(isEnabled: Boolean)
    fun showSocialTypes(socialTypes: List<SocialType>)
}