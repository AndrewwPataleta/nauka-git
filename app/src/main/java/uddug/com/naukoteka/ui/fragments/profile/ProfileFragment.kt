package uddug.com.naukoteka.ui.fragments.profile

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
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.databinding.FragmentProfileBinding
import uddug.com.naukoteka.presentation.profile.ProfileAvatarActionPresenter
import uddug.com.naukoteka.presentation.profile.ProfilePresenter
import uddug.com.naukoteka.presentation.profile.ProfileView
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.FEED_ARGS
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.custom.WorkExperienceView
import uddug.com.naukoteka.ui.fragments.profile.adapter.FeedContainerAdapter
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.DELETE_AVATAR_RESULT
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileAvatarEditActionBottomSheetFragment.Companion.UPLOAD_AVATAR_RESULT
import uddug.com.naukoteka.ui.fragments.wall.bottom.WallDetailActionBottomSheetFragment
import uddug.com.naukoteka.utils.doIfIsNotNullOrEmptyString
import uddug.com.naukoteka.utils.profileGradientRes
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import uddug.com.naukoteka.utils.ui.load
import uddug.com.naukoteka.utils.viewBinding

class ProfileFragment : BaseFragment(R.layout.fragment_profile), ProfileView {

    companion object {
        private const val FRAGMENT_FULL_INFO_TAG = "FRAGMENT_FULL_INFO_TAG"
    }

    private var observePlanJob: Job? = null
    override val contentView by viewBinding(FragmentProfileBinding::bind)
    private var navigationView: ContainerNavigationView? = null

    @InjectPresenter
    lateinit var presenter: ProfilePresenter

    private var pulseAnimation: Animation? = null

    private var feedItemAdapter: FeedContainerAdapter = FeedContainerAdapter(
        onFeedContainerClick = { presenter.onPostDetailClick(it) },
        onPostMenuClick = { presenter.onPostMenuClick(it) },
        onPostImageClick = { presenter.onPostImageClick(it) },
        onLikeClick = { presenter.onLikeClick(it) }
    )

    @ProvidePresenter
    fun providePresenter(): ProfilePresenter =
        getScope().getInstance(ProfilePresenter::class.java)

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launchWhenStarted { loadUserWall() }
    }

    private fun loadUserWall() {
        observePlanJob?.cancel()
        observePlanJob = viewLifecycleOwner.lifecycleScope.launchWhenCreated {
            presenter.loadUserWall() { }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        pulseAnimation = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
        contentView.progressLogo.startAnimation(pulseAnimation)
        contentView.moreBtn.setOnClickListener {
            presenter.userProfileFullInfo?.let { info ->
                ProfileAdditionalActionBottomSheetFragment.newInstance(profileFullInfo = info)
                    .show(requireFragmentManager(), "")
            }
        }
        contentView.feedContainer.adapter = feedItemAdapter
        contentView.profileImage.setOnClickListener { presenter.selectOpenPhotoAction() }
        contentView.moreInfoContainer.setOnClickListener { presenter.askForMoreInfo() }
        setFragmentResultListener(DELETE_AVATAR_RESULT) { _, _ -> presenter.loadProfile() }
        setFragmentResultListener(UPLOAD_AVATAR_RESULT) { _, _ -> presenter.loadProfile() }
    }

    override fun setMainInformation(profileInfo: UserProfileFullInfo) {
        contentView.name.text = profileInfo.fullName
        contentView.profileImage.setImageDrawable(null)
        contentView.initials.isVisible = false
        contentView.profileTopBackground.setImageDrawable(null)
        profileInfo.bannerUrl?.let { bannerPath ->
            contentView.profileTopBackground.load(
                withAnimation = false,
                model = BuildConfig.IMAGE_SERVER_URL.plus(bannerPath)
            )
        }
        if (profileInfo.image?.path.isNotNullOrEmpty()) {
            profileInfo.image?.path?.let { avatarPath ->
                contentView.profileImage.load(
                    withAnimation = false,
                    model = BuildConfig.IMAGE_SERVER_URL.plus(avatarPath)
                )
            }
        } else {
            contentView.profileImage.background = resources.getDrawable(profileGradientRes(profileInfo.id))
            val firstInitial = profileInfo.firstName?.firstOrNull()?.toString() ?: ""
            val lastInitial = profileInfo.lastName?.firstOrNull()?.toString() ?: ""
            contentView.initials.text = firstInitial + lastInitial
            contentView.initials.isVisible = true
        }
        doIfIsNotNullOrEmptyString(model = profileInfo.dsc) { description ->
            contentView.mainDescription.text = description
            contentView.mainDescription.isVisible = true
        }
        contentView.followersFollowingAmount.text = resources.getString(
            R.string.amount_followers_following,
            profileInfo.meta?.subscnCount.toString(),
            profileInfo.meta?.subscrCount.toString()
        )
        doIfIsNotNullOrEmptyString(model = profileInfo.placeOfResidence) {
            contentView.currentLocationValue.text = profileInfo.placeOfResidence
            contentView.currentLocation.isVisible = true
        }
        contentView.workExperienceContainer.removeAllViews()
        profileInfo.laborActivity.forEach { labor ->
            val workView = WorkExperienceView(context = requireContext()).apply {
                attachWorkExperience(
                    jobId = labor.id.toString(),
                    jobTitle = labor.position.toString(),
                    jobPlace = labor.orgName.toString(),
                    parent = contentView.workExperienceContainer,
                    onWorkExperienceClick = { }
                )
            }
            contentView.workExperienceContainer.addView(workView)
        }
        contentView.progressLogo.clearAnimation()
        contentView.progressLogo.isVisible = false
        contentView.infoContainer.isVisible = true
    }

    override fun showProgress(progress: Boolean) {
        contentView.progressLogo.isVisible = progress
        contentView.infoContainer.isVisible = !progress
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
        val bundle = Bundle().apply {
            putParcelable(PROFILE_ARGS, profileInfo)
            putParcelable(FEED_ARGS, feed)
        }
        findNavController().navigate(R.id.wallFeedDetail, bundle)
    }

    override fun openPostActionInfo(profileInfo: UserProfileFullInfo, feed: FeedContainer) {
        WallDetailActionBottomSheetFragment.newInstance(profileInfo, feed)
            .show(requireFragmentManager(), "")
    }
}
