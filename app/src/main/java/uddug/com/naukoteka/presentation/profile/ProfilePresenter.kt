package uddug.com.naukoteka.presentation.profile

import android.util.Log
import androidx.paging.PagingData
import kotlinx.coroutines.flow.FlowCollector
import uddug.com.domain.interactors.user_profile.UserProfileInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo

@InjectConstructor
@InjectViewState
class ProfilePresenter(
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<ProfileView>() {

    companion object {
        private const val errorTag = "ProfilePresenterError"
    }

    var userProfileFullInfo: UserProfileFullInfo? = null


    fun loadProfile() {
        userProfileInteractor.getUserProfilePreviewInfo().subscribe({
            userProfileFullInfo = it
            viewState.setMainInformation(it)
        })
    }

    fun selectOpenPhotoAction() {
        userProfileFullInfo?.let { viewState.showPhotoActionView(it) }
    }

    fun askForMoreInfo() {
        userProfileFullInfo?.let {
            viewState.showFullInfoBottomProfile(it)
        }
    }


    fun loadUserWall(wallCollector: FlowCollector<PagingData<FeedContainer>>) {
        userProfileInteractor.getFeedWritable()
            .subscribe({
                it.firstOrNull()?.rOwner?.let { rOwner ->
                    userProfileInteractor.getUserFeed(userId = rOwner)
                        .subscribe({ wallData ->
                            viewState.setFeeds(wallData)
                        })
                }
            })
    }

    fun onPostDetailClick(postContainer: FeedContainer) {
        userProfileFullInfo?.let {
            viewState.openPostDetailInfo(
                profileInfo = it,
                postContainer
            )
        }
    }

    fun onPostMenuClick(postContainer: FeedContainer) {
        userProfileFullInfo?.let {
            viewState.openPostActionInfo(
                profileInfo = it,
                postContainer
            )
        }
    }

    fun onPostImageClick(postContainer: FeedContainer) {

    }

    fun onLikeClick(postContainer: FeedContainer) {

    }

}
