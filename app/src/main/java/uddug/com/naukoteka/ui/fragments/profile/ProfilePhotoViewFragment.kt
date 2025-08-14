package uddug.com.naukoteka.ui.fragments.profile

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.BuildConfig
import uddug.com.naukoteka.R
import uddug.com.naukoteka.databinding.FragmentProfilePhotoViewBinding
import uddug.com.naukoteka.global.base.BaseFragment
import uddug.com.naukoteka.presentation.profile.ProfileAvatarActionPresenter.IMAGE_TYPE_AVATAR
import uddug.com.naukoteka.presentation.profile.navigation.ContainerNavigationView
import uddug.com.naukoteka.ui.activities.main.ContainerActivity
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.IMAGE_TYPE_AVATAR
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.IMAGE_TYPE_BANNER
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.IMAGE_TYPE_PARAM
import uddug.com.naukoteka.utils.ui.load
import uddug.com.naukoteka.utils.viewBinding


class ProfilePhotoViewFragment : BaseFragment(R.layout.fragment_profile_photo_view) {

    override val contentView: FragmentProfilePhotoViewBinding by viewBinding(
        FragmentProfilePhotoViewBinding::bind
    )

    private var navigationView: ContainerNavigationView? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        navigationView = requireActivity() as ContainerNavigationView
    }

    override fun onResume() {
        super.onResume()
        navigationView?.showNavigationBottomBar(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_profile_photo_view, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.getParcelable<UserProfileFullInfo>(ContainerActivity.PROFILE_ARGS)
            ?.let {
                val imageType = arguments?.getString(IMAGE_TYPE_PARAM)
                when (imageType) {
                    IMAGE_TYPE_AVATAR -> {
                        it.image?.path?.let {
                            contentView.photoView.load(
                                withAnimation = false,
                                model = BuildConfig.IMAGE_SERVER_URL.plus(it)
                            )
                        }
                    }

                    IMAGE_TYPE_BANNER -> {
                        it.bannerUrl?.let {
                            contentView.photoView.load(
                                withAnimation = false,
                                model = BuildConfig.IMAGE_SERVER_URL.plus(it)
                            )
                        }
                    }

                    else -> {
                        it.image?.path?.let {
                            contentView.photoView.load(
                                withAnimation = false,
                                model = BuildConfig.IMAGE_SERVER_URL.plus(it)
                            )
                        }
                    }
                }

            }
        contentView.back.setOnClickListener {
            findNavController().popBackStack()
        }
        contentView.done.setOnClickListener {
            findNavController().popBackStack()
        }
    }

}
