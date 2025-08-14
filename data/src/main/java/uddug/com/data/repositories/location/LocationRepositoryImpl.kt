package uddug.com.data.repositories.location

import io.reactivex.Single
import toothpick.InjectConstructor
import uddug.com.data.repositories.user_profile.UserProfileMapper
import uddug.com.data.services.LocationApiService
import uddug.com.data.services.models.request.country.FindSettlementRequest
import uddug.com.data.utils.toDomain
import uddug.com.domain.SchedulersProvider
import uddug.com.domain.entities.country.Country
import uddug.com.domain.entities.country.Settlement

import uddug.com.domain.repositories.country.LocationRepository

@InjectConstructor
class LocationRepositoryImpl(
    private val locationApiService: LocationApiService,
    private val userProfileMapper: UserProfileMapper,
    private val schedulers: SchedulersProvider,
) : LocationRepository {

    override fun getCountries(): Single<List<Country>> {
        return locationApiService.getCounties().subscribeOn(schedulers.io())
            .observeOn(schedulers.ui()).map {
                it.map {
                    Country(
                        id = it.id.toString(),
                        term = it.term ?: "",
                        uref = it.uref,
                        isSelected = false
                    )
                }

            }
    }

    override fun findSettlementsByCountry(
        countryId: String,
        query: String
    ): Single<List<Settlement>> {
        return locationApiService.findSettlementsByCountries(
            FindSettlementRequest(
                countryNum = countryId,
                query = query
            )
        )
            .subscribeOn(schedulers.io())
            .observeOn(schedulers.ui()).map {
                it.map {
                    it.toDomain()
                }
            }
    }
}
