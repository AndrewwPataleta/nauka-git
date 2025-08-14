package uddug.com.naukoteka.ui.fragments.nausphere

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(OneExecutionStateStrategy::class)
interface NauSphereView : MvpView, LoadingView, InformativeView {
    fun setMainInformation(profileInfo: UserProfileFullInfo)
    fun showProgress(progress: Boolean)
    fun showFullInfoBottomProfile(profileInfo: UserProfileFullInfo)
    fun showPhotoActionView(profileInfo: UserProfileFullInfo)
    fun setFeeds(feeds: List<FeedContainer>)
    fun openPostDetailInfo(profileInfo: UserProfileFullInfo, feed: FeedContainer)
    fun openPostActionInfo(profileInfo: UserProfileFullInfo, feed: FeedContainer)
}
