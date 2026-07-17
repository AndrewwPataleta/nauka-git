package uddug.com.naukoteka.presentation.profile.edit.adapter

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.naukoteka.presentation.profile.edit.EducationScreenType
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.EDUCATION_SCREEN_TYPE
import uddug.com.naukoteka.ui.activities.main.ContainerActivity.Companion.PROFILE_ARGS
import uddug.com.naukoteka.ui.fragments.profile.edit.ProfileActionEducationFragment

class EducationTypeAdapter(fragment: Fragment, val userProfileFullInfo: UserProfileFullInfo) :
    FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment {
        val screenType = when (position) {
            1 -> EducationScreenType.HIGH
            2 -> EducationScreenType.ADDITIONAL
            else -> EducationScreenType.MIDDLE
        }
        return ProfileActionEducationFragment().apply {
            arguments = Bundle().apply {
                putParcelable(PROFILE_ARGS, userProfileFullInfo)
                putString(EDUCATION_SCREEN_TYPE, screenType.name)
            }
        }
    }
}
