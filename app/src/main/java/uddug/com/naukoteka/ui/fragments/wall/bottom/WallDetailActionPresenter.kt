package uddug.com.naukoteka.ui.fragments.wall.bottom

import androidx.paging.PagingData
import kotlinx.coroutines.flow.FlowCollector
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.presentation.profile.ProfileAvatarActionPresenter

@InjectConstructor
@InjectViewState
class WallDetailActionPresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<WallDetailActionView>() {

    companion object {
        private const val errorTag = "ProfilePresenterError"
    }

    var userProfileFullInfo: UserProfileFullInfo? = null

    var feedContainer: FeedContainer? = null


    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
    }

    fun initFeed(feedContainer: FeedContainer) {
        this.feedContainer = feedContainer
    }

    fun sharePost() {
        feedContainer?.let { viewState.showShareDialog(it) }
    }

    fun addToFavorite() {
        feedContainer?.let {
            userProfileInteractor.addToFavorite(
                it
            ).subscribe {
                viewState.showAddedToFavorite(true)
            }
        }
    }

    fun hideAuthorPosts() {
        feedContainer?.let {
            userProfileInteractor.hideAuthorPosts(
                it
            )
        }
    }

    fun selectPostComplaint() {
        feedContainer?.let {
            userProfileInteractor.hideAuthorPosts(
                it
            )
        }
    }

    fun loadProfile() {

    }


}
