package com.cadena.pataspacasa.di

import com.cadena.pataspacasa.data.datasource.CloudFunctionDataSource
import com.cadena.pataspacasa.data.datasource.FirebaseAuthDataSource
import com.cadena.pataspacasa.data.repository.*
import com.cadena.pataspacasa.data.service.GooglePlayBillingService
import com.cadena.pataspacasa.domain.repository.*
import com.cadena.pataspacasa.domain.service.SubscriptionService
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindPetRepository(impl: FirebasePetRepository): PetRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: StubTagRepository): TagRepository

    @Binds
    @Singleton
    abstract fun bindLostPetScanRepository(impl: FirebaseLostPetScanRepository): LostPetScanRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionRepository(impl: SubscriptionRepositoryImpl): SubscriptionRepository

    @Binds
    @Singleton
    abstract fun bindSubscriptionService(impl: GooglePlayBillingService): SubscriptionService
}
