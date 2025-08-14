package uddug.com.naukoteka.ui.fragments.wall.bottom

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(OneExecutionStateStrategy::class)
interface WallDetailActionView : MvpView, LoadingView, InformativeView {

    fun showShareDialog(feedContainer: FeedContainer)

    fun showAddedToFavorite(success: Boolean)

    fun showHideAuthorStatus(success: Boolean)

    fun openComplaintPost()
}
