package com.cadena.pataspacasa

import android.app.Application
import com.google.firebase.FirebaseApp
import com.cadena.pataspacasa.data.service.PushRegistrationManager
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class PatasPaCasaApp : Application() {
    @javax.inject.Inject lateinit var pushRegistrationManager: PushRegistrationManager
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.FIREBASE_ENABLED && FirebaseApp.initializeApp(this) != null) {
            pushRegistrationManager.start()
        }
    }
}
