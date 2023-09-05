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

        setAppContext(this)

        launch {
            withContext(Dispatchers.IO) {
                // parameters
                val network = "polygon"
                val collection_id = "0xEB2f1B36479d995b6753a86Ce70472723497e018"
                val from = "0x1400594a07925c7110b9d22791f220ee924c0513"
                val to = "0x3d180de1eb687a594206c68f2cf3363c18e875c8"
                val token_id = "7070"
                val token_uri = "https://raw.githubusercontent.com/companyAbcDev/metadatas/master/metadatas/erc721/7070.json"
                val amount = "1"
//                //ERC721
//                val mintERC721 = mintErc721Async(network,
//                    from,
//                    to,
//                    token_uri,
//                    token_id,
//                    collection_id)
//                println(mintERC721)
                //ERC721
                val sendERC721 = sendNFT721TransactionAsync(network,
                    from,
                    to,
                    token_id,
                    collection_id)
                println(sendERC721)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        cancel() // Cancel all coroutines when the activity is destroyed
    }
}


