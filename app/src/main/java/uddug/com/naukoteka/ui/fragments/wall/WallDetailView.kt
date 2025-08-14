package uddug.com.naukoteka.ui.fragments.wall

import uddug.com.naukoteka.global.views.InformativeView
import uddug.com.naukoteka.global.views.LoadingView
import moxy.MvpView
import moxy.viewstate.strategy.OneExecutionStateStrategy
import moxy.viewstate.strategy.StateStrategyType
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.feed.PostComment
import uddug.com.domain.entities.profile.FormContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo

@StateStrategyType(OneExecutionStateStrategy::class)
interface WallDetailView : MvpView, LoadingView, InformativeView {


    fun setCurrentFeed(feedContainer: FeedContainer)

    fun setWallComments(comments: List<PostComment>)

    fun showMessageSendEmpty()

    fun setCurrentMessageText(messageText: String)

}
