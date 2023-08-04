package com.example.android_sdk

import account
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

        var network = arrayOf("ethereum", "cypress", "polygon", "bnb")
        var accountArray = arrayOf("0xe2Ce91F22ed39520e8b099F3800BD21f5b090b56")
//        var account = "0x13418f99467D20A7970a36fe4F2fF6Ec494A3A12"
//        var collection_id = "0x22d5f9B75c524Fec1D6619787e582644CD4D7422"
//        var sort = "desc"
//        var limit = 10
//        var page_number = 1
//        val accountsAddtess = "0x1C3c32DFB8cBe1E144d5e79eB41392e535405C40"
        // Using coroutines to avoid blocking the UI thread
        launch {
            withContext(Dispatchers.IO) {
                account()
//                transaction()
//                var getNFTsByWallet= getNFTsByWallet(
//                    network = network,
//                    account = account,
//                    collection_id = collection_id,
//                    sort = sort,
//                    limit = limit,
//                    page_number = page_number
//                )
//                println("getNFTsByWallet ===== " + getNFTsByWallet)
                var getNFTsByWalletArray= getNFTsByWalletArray(
                    network = network,
                    account = accountArray
//                    collection_id = collection_id,
//                    sort = sort,
//                    limit = limit,
//                    page_number = page_number
                )
                println("getNFTsByWalletArray ===== " + getNFTsByWalletArray)
//                var getNFTsTransferHistory =
//                    getNFTsTransferHistory(
//                        network = "polygon",
//                        collection_id = "0xba6666B118f8303F990f3519DF07e160227cCE87",
//                        token_id = "11",
//                        type = "transfer",
//                        sort = "desc",
//                        limit = 10,
//                        page_number = 1
//                    )
//                println("getNFTsTransferHistory ==== " + getNFTsTransferHistory)
//                  var gas = getEstimateGas("ethereum", "baseFee")
//                  println(gas)
//                    var gasBydeploy = getEstimateGas("goerli", "deployERC721", null, "0x0eae45485F2D14FDEB3dAa1143E5170752D5EAe8", null, null, null, null, null, null, null, null, "asd", "asd", "0x0eae45485F2D14FDEB3dAa1143E5170752D5EAe8", "asd", "1")
//                    println(gasBydeploy)
//                  var sendNFT = sendNFT1155BatchTransactionAsync("goerli", "0x772A779d280d19C2bd582B4F3840De703249CA39", "0x54fbF887EdB01983DD373E79a0f37413B4565De3", arrayOf("182","189"), "0x5a644acd663d7e4d07eeabe43df0f985670f8f9a", arrayOf("1","1"))
//                  println(sendNFT)
//                var setNFTsTrash = setNFTsTrash("bnb", "zvjqjxew7c", "0xe2Ce91F22ed39520e8b099F3800BD21f5b090b56", "0xa2e4F38f50c00b2B30Cb1EC295301ce1DE825F06", "1")
//                println("setNFTsTrash === "+ setNFTsTrash)
//                var deleteNFTsTrash = deleteNFTsTrash("bnb", "zvjqjxew7c", "0xe2Ce91F22ed39520e8b099F3800BD21f5b090b56", "0x33763cf6CEbbD70db9613e5073B10b195DFB7cdA", "1")
//                println("deleteNFTsTrash === "+ deleteNFTsTrash)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cancel() // Cancel all coroutines when the activity is destroyed
    }
}


