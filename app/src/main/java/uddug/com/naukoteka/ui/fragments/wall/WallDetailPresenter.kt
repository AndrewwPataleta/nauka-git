package uddug.com.naukoteka.ui.fragments.wall

import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.feed.PostComment
import uddug.com.domain.entities.feed.PostCommentAddRequest
import uddug.com.domain.interactors.country.LocationInteractor

import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.user_profile.UserProfileInteractor

import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty


@InjectConstructor
@InjectViewState
class WallDetailPresenter(
    private val locationInteractor: LocationInteractor,
    private val userProfileInteractor: UserProfileInteractor
) : BasePresenterImpl<WallDetailView>() {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null

    private var feedContainer: FeedContainer? = null

    private var cacheMessage: String? = null

    private var currentComments: MutableList<PostComment> = mutableListOf()

    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
        compositeDisposable.addAll(
            userProfileInteractor.getUserSettings()
                .subscribe({ forms ->

                }, {
                    it.printStackTrace()
                })
        )
    }

    fun getWallComments(feedContainer: FeedContainer) {
        this.feedContainer = feedContainer
        viewState.setCurrentFeed(feedContainer)
        feedContainer.body?.id?.let {
            userProfileInteractor.getWallComments(it).subscribe({ comments ->
                currentComments = comments.toMutableList()
                viewState.setWallComments(comments)
            }, {
                it.printStackTrace()
            })
        }

    }

    fun setCurrentMessageText(text: String) {
        this.cacheMessage = text
    }

    fun sendMessage() {
        if (cacheMessage.isNullOrEmpty()) {
            viewState.showMessageSendEmpty()
        } else {
            userProfileInteractor.sendComment(
                PostCommentAddRequest(
                    rAuthor = feedContainer?.body?.rAuthor,
                    cStatus = feedContainer?.body?.cStatus,
                    rPost = feedContainer?.body?.uref,
                    text = cacheMessage
                )
            ).subscribe({
                cacheMessage = ""
                currentComments.add(it)
                viewState.setWallComments(currentComments)
                viewState.setCurrentMessageText("")
            }, {
                it.printStackTrace()
            })
        }

    }

}
