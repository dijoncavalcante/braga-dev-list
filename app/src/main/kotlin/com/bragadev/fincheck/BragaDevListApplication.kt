package com.bragadev.fincheck

import android.app.Application
import com.bragadev.fincheck.core.database.databaseModule
import com.bragadev.fincheck.core.di.appModule
import com.bragadev.fincheck.core.di.viewModelModule
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin

class BragaDevListApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            //if (BuildConfig.DEBUG) {
                androidLogger()
           // }
            androidContext(this@BragaDevListApplication)
            modules(databaseModule, appModule, viewModelModule)
        }
    }
}
