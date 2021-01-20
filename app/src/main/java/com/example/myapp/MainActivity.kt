package com.example.myapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.shrek.klib.extension.uiThread
import org.jetbrains.anko.relativeLayout

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}