package uddug.com.naukoteka.presentation.tutorial

import uddug.com.data.cache.first_launch.FirstLaunchCache
import uddug.com.domain.entities.TutorialEntity
import uddug.com.naukoteka.R
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.navigation.AppRouter
import uddug.com.naukoteka.navigation.Screens
import toothpick.InjectConstructor

@InjectConstructor
class TutorialPresenter(
    private val router: AppRouter,
    private val isFirstLaunched : FirstLaunchCache,
) : BasePresenterImpl<TutorialView>() {


    override fun onFirstViewAttach() {
        super.onFirstViewAttach()
        viewState.showTutorialInformation(authorizationResources)
    }

    fun finishTutorial() {
        isFirstLaunched.entity = false
        router.newRootScreen(Screens.Login())
    }

    fun exit() {
        router.exit()
    }

    private val authorizationResources
        get() = listOf(
            TutorialEntity(
                R.drawable.ic_tutorial_1,
                R.string.the_best_scientific_works
            ),
            TutorialEntity(
                R.drawable.ic_tutorial_2,
                R.string.smart_community
            ),
            TutorialEntity(
                R.drawable.ic_tutorial_3,
                R.string.new_ideas
            )
        )
}