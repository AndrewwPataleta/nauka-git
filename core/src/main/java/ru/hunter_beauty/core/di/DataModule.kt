package com.nauchat.core.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.nauchat.data.di.DatabaseModule
import com.nauchat.data.di.NetworkModule
import com.nauchat.data.repository.IApparatusesRepository
import com.nauchat.data.repository.ICityRepository
import com.nauchat.data.repository.IDialogRepository
import com.nauchat.data.repository.IFavoriteRepositoryLocal
import com.nauchat.data.repository.IFavoriteRepositoryRemote
import com.nauchat.data.repository.IOrganisationRepository
import com.nauchat.data.repository.IRecommendationsRepository
import com.nauchat.data.repository.IReviewRepository
import com.nauchat.data.repository.ISettingsRepository
import com.nauchat.data.repository.ISpecialistsRepository
import com.nauchat.data.repository.IUserRepository
import com.nauchat.data.repository.IUtilityRepository
import com.nauchat.data.repository.impl.ApparatusesRepositoryImpl
import com.nauchat.data.repository.impl.CityRepositoryImpl
import com.nauchat.data.repository.impl.DialogRepositoryImpl
import com.nauchat.data.repository.impl.FavoriteLocalRepositoryImpl
import com.nauchat.data.repository.impl.FavoriteRemoteRepositoryImpl
import com.nauchat.data.repository.impl.OrganisationRepositoryImpl
import com.nauchat.data.repository.impl.RecommendationsRepositoryImpl
import com.nauchat.data.repository.impl.ReviewRepositoryImpl
import com.nauchat.data.repository.impl.SettingsRepositoryImpl
import com.nauchat.data.repository.impl.SpecialistsRepositoryImpl
import com.nauchat.data.repository.impl.UserRepositoryImpl
import com.nauchat.data.repository.impl.UtilityRepositoryImpl
import com.nauchat.data.source.favorites.FavoritesDataSourceImpl
import com.nauchat.data.source.favorites.FavoritesDataSourceLocal
import com.nauchat.data.source.favorites.FavoritesListSingleton
import com.nauchat.data.source.favorites.FavoritesListSingletonImp
import com.nauchat.data.source.settings.SettingsDataSourceLocal
import com.nauchat.data.source.settings.SettingsDataSourceLocalImpl
import com.nauchat.domain.mapper.CityDTOToCityItem
import com.nauchat.domain.mapper.ListCityDTOToListCityItem
import com.nauchat.domain.mapper.SettingsEntityToSettingsItem
import com.nauchat.domain.mapper.TokenDTOtoTokenItem
import com.nauchat.domain.mapper.UserDTOToUserItem
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module(includes = [NetworkModule::class, DatabaseModule::class, DataProviderModule::class])
interface DataModule {


}
