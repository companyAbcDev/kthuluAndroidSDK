package io.kthulu.sdk

import android.app.Application
import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.kthulu.sdk.MyContext.Companion.setAppContext
import kotlinx.coroutines.*
import java.math.BigInteger

// Application에서 Context를 가져올 수 있도록 구현
class MyContext : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        lateinit var context: Context
            private set // make the setter private so it can't be changed from outside

        fun setAppContext(appContext: Context) {
            context = appContext
        }
    }
}


class MainActivity : AppCompatActivity(), CoroutineScope by MainScope() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Application에서 Context를 가져올 수 있도록 구현
        setAppContext(applicationContext)

        launch {
            withContext(Dispatchers.IO) {
                var colelctionId = "0xa84cb2207cb80f8af82c44f7f41f804323f86289"
                var getAccount = chkNFTHolder("cypress", "0x1400594A07925C7110B9D22791f220Ee924C0513", colelctionId, "91148")
                println("getAccount ===  $getAccount")
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cancel() // Cancel all coroutines when the activity is destroyed
    }
}


