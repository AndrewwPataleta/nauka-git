package uddug.com.naukoteka.ui.fragments.nausphere

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.view.isVisible
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.Job
import moxy.presenter.InjectPresenter
import moxy.presenter.ProvidePresenter
import uddug.com.domain.entities.feed.FeedContainer
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentNaushpereBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.utils.viewBinding
import uddug.com.naukoteka.databinding.FragmentProfileBinding
import uddug.com.naukoteka.presentation.profile.ProfileAvatarActionPresenter
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.presentation.profile.ProfilePresenter
import uddug.com.naukoteka.presentation.profile.ProfileView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.FEED_ARGS
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.custom.WorkExperienceView
import uddug.com.naukoteka.ui.fragments.profile.ProfileFullInfoBottomSheetFragment
import uddug.com.naukoteka.ui.fragments.profile.adapter.FeedContainerAdapter
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.DELETE_AVATAR_RESULT
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.UPLOAD_AVATAR_RESULT
import uddug.com.naukoteka.ui.fragments.wall.bottom.WallDetailActionBottomSheetFragment
import uddug.com.naukoteka.utils.doIfIsNotNullOrEmptyString
import uddug.com.naukoteka.utils.getHashCodeToString
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import uddug.com.naukoteka.utils.ui.load

class NauSphereFragment : BaseFragment(R.layout.fragment_naushpere), NauSphereView {

    companion object {
        private const val FRAGMENT_FULL_INFO_TAG = "FRAGMENT_FULL_INFO_TAG"
    }

    private var observePlanJob: Job? = null

    override val contentView by viewBinding(FragmentNaushpereBinding::bind)

    private var navigationView: ContainerNavigationView? = null

    @InjectPresenter
    lateinit var presenter: NauSpherePresenter

    private var pulseAnimation: Animation? = null

    private var feedItemAdapter: FeedContainerAdapter = FeedContainerAdapter(
        onFeedContainerClick = {
            presenter.onPostDetailClick(it)
        },
        onPostMenuClick = {
            presenter.onPostMenuClick(it)
        },
        onPostImageClick = {
            presenter.onPostImageClick(it)
        },
        onLikeClick = {
            presenter.onLikeClick(it)
        }
    )

    @ProvidePresenter
    fun providePresenter(): NauSpherePresenter {
        return getScope().getInstance(NauSpherePresenter::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted {
            loadUserWall()
        }
    }

    private fun loadUserWall() {
        observePlanJob?.cancel()
        observePlanJob = viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            presenter.loadUserWall() {

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contentView.feedContainer.adapter = feedItemAdapter
    }

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {


    }

    override fun showProgress(progress: Boolean) {

    }

    override fun onResume() {
        super.onResume()
        presenter.loadProfile()
        navigationView?.showNavigationBottomBar(true)
    }

    override fun showFullInfoBottomProfile(profileInfo: UserProfileFullInfo) {
        ProfileFullInfoBottomSheetFragment.newInstance(profileInfo)
            .show(requireFragmentManager(), FRAGMENT_FULL_INFO_TAG)
    }

    override fun showPhotoActionView(profileInfo: UserProfileFullInfo) {
        ProfileAvatarEditActionBottomSheetFragment.newInstance(
            profileInfo,
            ProfileAvatarActionPresenter.IMAGE_TYPE_AVATAR.AVATAR
        ).show(requireFragmentManager(), "")
    }

    override fun setFeeds(feeds: List<FeedContainer>) {
        feedItemAdapter.submitList(feeds)
    }

    override fun openPostDetailInfo(profileInfo: UserProfileFullInfo, feed: FeedContainer) {
        val bundle = Bundle()
        bundle.putParcelable(PROFILE_ARGS, profileInfo)
        bundle.putParcelable(FEED_ARGS, feed)
        findNavController().navigate(R.id.wallFeedDetail, bundle)
    }

    override fun openPostActionInfo(profileInfo: UserProfileFullInfo, feed: FeedContainer) {
        WallDetailActionBottomSheetFragment.newInstance(
            profileInfo,
            feed
        ).show(requireFragmentManager(), "")
    }

}
