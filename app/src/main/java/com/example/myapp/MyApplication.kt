package com.example.myapp

import androidx.multidex.MultiDex
import com.shrek.klib.KApp

class MyApplication: KApp() {

    override fun onCreate() {
        super.onCreate()
        MultiDex.install(this)
    }

}