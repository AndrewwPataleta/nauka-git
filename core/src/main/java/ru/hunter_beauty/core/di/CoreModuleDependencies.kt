package com.nauchat.core.di


import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import com.nauchat.domain.usecase.cities.GetCitiesUseCase
import com.nauchat.domain.usecase.settings.GetSettingsUseCase
import com.nauchat.domain.usecase.settings.UpdateTokenSettingsUseCase
import com.nauchat.domain.usecase.user.GetUserUseCase
import com.nauchat.domain.usecase.user.RegisterUserUseCase
import com.nauchat.domain.usecase.user.UpdateGeoUseCase

@EntryPoint
@InstallIn(SingletonComponent::class)
interface CoreModuleDependencies {




}
