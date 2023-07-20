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
        setConfiguration(
            getString(R.string.kthulu_database_url),
            getString(R.string.kthulu_database_username),
            getString(R.string.kthulu_database_password),
            getString(R.string.kthulu_database_driver_class_name)
        )

        val networkArray = arrayOf("ethereum", "cypress", "polygon", "bnb")
        val accountArray = arrayOf("0x7760E0243cA9BAA630412865DF7b39AfbA42Ff0f", "0x7760E0243cA9BAA630412865DF7b39AfbA42Ff0f")
//        val accountsAddtess = "0x1C3c32DFB8cBe1E144d5e79eB41392e535405C40"
        // Using coroutines to avoid blocking the UI thread
        launch {
            withContext(Dispatchers.IO) {
//                account()
//                transaction()
//                var getNFTsByWalletTest = getNFTsByWallet(networkArray, account)
                var getNFTsByWalletTest = getNFTsByWallet(networkArray, accountArray)
                println("getNFTsByWalletTest ===== " + getNFTsByWalletTest)
//                var getNFTTransaction = getNFTTransaction("ethereum", "0x3296379a4F0fFEcE6Da595d1206f3dD85fC08508", "9072")
//                println("getNFTTransaction ==== " + getNFTTransaction)
//                  var gas = getEstimateGas("ethereum", "baseFee")
//                  println(gas)
//                    var gasBydeploy = getEstimateGas("goerli", "deployERC721", null, "0x0eae45485F2D14FDEB3dAa1143E5170752D5EAe8", null, null, null, null, null, null, null, null, "asd", "asd", "0x0eae45485F2D14FDEB3dAa1143E5170752D5EAe8", "asd", "1")
//                    println(gasBydeploy)
//                  var sendNFT = sendNFT1155BatchTransactionAsync("goerli", "0x772A779d280d19C2bd582B4F3840De703249CA39", "0x54fbF887EdB01983DD373E79a0f37413B4565De3", arrayOf("182","189"), "0x5a644acd663d7e4d07eeabe43df0f985670f8f9a", arrayOf("1","1"))
//                  println(sendNFT)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel() // Cancel all coroutines when the activity is destroyed
    }
}

