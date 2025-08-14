package uddug.com.naukoteka.presentation.profile

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.AddToEndSingleStrategy
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.profile.ProfileInfoModel
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(OneExecutionStateStrategy::class)
interface ProfilePhotoActionView : MvpView, LoadingView, InformativeView {

    fun openProfilePhotoImageView(userProfileFullInfo: UserProfileFullInfo)

    fun openProfileBannerImageView(userProfileFullInfo: UserProfileFullInfo)

    fun showDeletePhotoDialog()

    fun successfulDeleteAvatar()

    fun successfulUpload()

    fun setOpenPhotoAvailable(available: Boolean)
}
