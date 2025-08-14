package uddug.com.naukoteka.ui.fragments.profile.settings

import io.reactivex.disposables.CompositeDisposable
import moxy.InjectViewState
import toothpick.InjectConstructor
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.profile.UserProfileFullInfo
import uddug.com.domain.interactors.country.LocationInteractor
import uddug.com.naukoteka.global.base.BasePresenterImpl
import uddug.com.naukoteka.presentation.profile.edit.ProfileEditAddressesListPresenter
import uddug.com.naukoteka.presentation.profile.edit.models.CountryType
import uddug.com.naukoteka.utils.text.isNotNullOrEmpty
import java.util.Locale


@InjectConstructor
@InjectViewState
class UserBlockListPresenter(
    private val locationInteractor: LocationInteractor,
) : BasePresenterImpl<UserBlockListView>() {

    var compositeDisposable: CompositeDisposable = CompositeDisposable()

    var userProfileFullInfo: UserProfileFullInfo? = null


    fun setProfileFullInfo(profileFullInfo: UserProfileFullInfo) {
        this.userProfileFullInfo = profileFullInfo
    }

}
