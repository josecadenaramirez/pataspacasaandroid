package com.cadena.pataspacasa.di

import com.cadena.pataspacasa.data.repository.*
import com.cadena.pataspacasa.domain.repository.*
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
    abstract fun bindPetRepository(impl: BackendPetRepository): PetRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: UnavailableLiveTagRepository): TagRepository

    @Binds
    @Singleton
    abstract fun bindLostPetScanRepository(impl: RestLostPetScanRepository): LostPetScanRepository

    @Binds
    @Singleton
    abstract fun bindBreedCatalogRepository(impl: BackendBreedCatalogRepository): BreedCatalogRepository

}
