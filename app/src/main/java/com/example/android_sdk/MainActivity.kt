package com.example.android_sdk

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

// Application에서 Context를 가져올 수 있도록 구현
class MyContext : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        lateinit var context: Context
    }
}

class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val database_url = "jdbc:mariadb://210.207.161.10:3306/kthulu?useUnicode=true&amp;characterEncoding=UTF-8&amp;useSSL=false"
        val database_username = "kthulu"
        val database_password = "kthulu123"
        val database_driver_class_name = "org.mariadb.jdbc.Driver"

        setConfiguration(
            database_url,
            database_username,
            database_password,
            database_driver_class_name
        )

        launch {
            withContext(Dispatchers.IO) {


            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel() // Cancel all coroutines when the activity is destroyed
    }
}


