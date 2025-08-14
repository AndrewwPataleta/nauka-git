package uddug.com.naukoteka.presentation.profile.navigation

import uddug.com.domain.entities.profile.UserProfileFullInfo


interface ContainerNavigationView  {
    fun selectShowEditFragment(profileInfo: UserProfileFullInfo)
    fun showNavigationBottomBar(show: Boolean)
}
