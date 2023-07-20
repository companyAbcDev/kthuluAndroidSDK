package com.example.android_sdk

//import com.google.gson.Gson
//import com.google.gson.JsonArray
//import com.google.gson.JsonObject
//import com.google.gson.JsonParser
//import com.google.gson.reflect.TypeToken
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.sql.ResultSet
import java.sql.SQLException
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.http.HttpService
import org.web3j.tx.Transfer
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger

public val addrTransferGoerli = "0x25df7c4d54ce69faf37352cbe98e2d3f9281eaf7"

//data class Nft(
//    var network: List<network>?,
//    var account: String?,
//    var balance: String?,
//    var collection_id: String?,
//    var collection_name: String?,
//    var collection_symbol: String?,
//    var nft_type: String?,
//    var block_number: String?,
//    var token_id: String?,
//    var token_name: String?,
//    var token_info: String?
////    var tokenURI: String?,
////    var balance: String?,
////    var h_timestamp: String?,// holder certification date
////    var name: String?,
////    var image: String?,
////    var description: String?,
////    var attributes: List<Attributes>?,
////    var account_address: String?
//)

//data class Attributes(
//    var value: String?,
//    var trait_type: String?
//)
suspend fun getNFTsByWallet(
    network: Array<String>,
    account: Array<String>
): JSONArray = withContext(Dispatchers.IO) {

    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val nftArray = JSONArray()

    val net = network.joinToString("','", "'", "'")
    val acc = account.joinToString("','", "'", "'")
    val query = """
           SELECT owner.network, 
                owner.owner_account, 
                owner.balance,
                collection.collection_id, 
                collection.collection_name, 
                collection.collection_symbol,
                collection.creator, 
                collection.total_supply, 
                collection.deployment_date,
                collection.slug, 
                collection.category, 
                collection.logo_url, 
                collection.image_s3_url, 
                collection.isverified,
                collection.numOwners, 
                collection.currency, 
                collection.discord_link, 
                collection.twitter_link, 
                collection.instagram_link,
                collection.facebook_link, 
                collection.telegram_link, 
                collection.external_url, 
                collection.updated_date,
                token.token_id,
                token.nft_type, 
                token.nft_name,
                token.tag,
                token.description,
                token.block_number,
                token.minted_time,
                token.license,
                token.video,
                token.image_url,
                token.token_uri,
                token.ipfs,
                token.attribute,
                token.token_info
            FROM nft_owner_table AS owner
            JOIN nft_token_table AS token ON owner.collection_id = token.collection_id AND owner.token_id = token.token_id AND owner.network = token.network
            JOIN nft_collection_table AS collection ON token.collection_id = collection.collection_id AND token.network = collection.network
             WHERE owner.network IN ($net) AND owner.owner_account IN ($acc) AND owner.balance != '0'
             ORDER BY token.block_number DESC
        """
    if (connection != null) {
        val dbQueryExector = DBQueryExector(connection)
        val getNFT: ResultSet? = dbQueryExector.executeQuery(query)

        if (getNFT != null) {
            try {
                while (getNFT.next()) {
                    val objRes = JSONObject()

                    val network = getNFT.getString("network")
                    val owner_account = getNFT.getString("owner_account")
                    val balance = getNFT.getString("balance")

                    val collection_id = getNFT.getString("collection_id")
                    val collection_name = getNFT.getString("collection_name")
                    val collection_symbol = getNFT.getString("collection_symbol")
                    val creator = getNFT.getString("creator")
                    val total_supply = getNFT.getString("total_supply")
                    val deployment_date = getNFT.getString("deployment_date")
                    val slug = getNFT.getString("slug")
                    val category = getNFT.getString("category")
                    val logo_url = getNFT.getString("logo_url")
                    val image_s3_url = getNFT.getString("image_s3_url")
                    val isverified = getNFT.getString("isverified")
                    val numOwners = getNFT.getString("numOwners")
                    val currency = getNFT.getString("currency")
                    val discord_link = getNFT.getString("discord_link")
                    val twitter_link = getNFT.getString("twitter_link")
                    val instagram_link = getNFT.getString("instagram_link")
                    val facebook_link = getNFT.getString("facebook_link")
                    val telegram_link = getNFT.getString("telegram_link")
                    val external_url = getNFT.getString("external_url")
                    val updated_date = getNFT.getString("updated_date")

                    val token_id = getNFT.getString("token_id")
                    val nft_type = getNFT.getString("nft_type")
                    val nft_name = getNFT.getString("nft_name")
                    val tag = getNFT.getString("tag")
                    val description = getNFT.getString("description")
                    val block_number = getNFT.getInt("block_number")
                    val minted_time = getNFT.getString("minted_time")
                    val license = getNFT.getString("license")
                    val video = getNFT.getString("video")
                    val image_url = getNFT.getString("image_url")
                    val token_uri = getNFT.getString("token_uri")
                    val ipfs = getNFT.getString("ipfs")
                    val attribute = getNFT.getString("attribute")
                    val token_info = getNFT.getString("token_info")

                    objRes.put("network", network)
                    objRes.put("owner_account", owner_account)
                    objRes.put("balance", balance)
                    objRes.put("collection_id", collection_id)
                    objRes.put("collection_name", collection_name ?: JSONObject.NULL)
                    objRes.put("collection_symbol", collection_symbol ?: JSONObject.NULL)
                    objRes.put("creator", creator ?: JSONObject.NULL)
                    objRes.put("total_supply", total_supply ?: JSONObject.NULL)
                    objRes.put("deployment_date", deployment_date ?: JSONObject.NULL)

                    objRes.put("slug", slug ?: JSONObject.NULL)
                    objRes.put("category", category ?: JSONObject.NULL)
                    objRes.put("logo_url", logo_url ?: JSONObject.NULL)
                    objRes.put("image_s3_url", image_s3_url ?: JSONObject.NULL)
                    objRes.put("isverified", isverified ?: JSONObject.NULL)
                    objRes.put("numOwners", numOwners ?: JSONObject.NULL)
                    objRes.put("currency", currency ?: JSONObject.NULL)
                    objRes.put("discord_link", discord_link ?: JSONObject.NULL)
                    objRes.put("twitter_link", twitter_link ?: JSONObject.NULL)
                    objRes.put("instagram_link", instagram_link ?: JSONObject.NULL)
                    objRes.put("facebook_link", facebook_link ?: JSONObject.NULL)
                    objRes.put("telegram_link", telegram_link ?: JSONObject.NULL)
                    objRes.put("external_url", external_url ?: JSONObject.NULL)
                    objRes.put("updated_date", updated_date ?: JSONObject.NULL)

                    objRes.put("token_id", token_id)
                    objRes.put("nft_type", nft_type)
                    objRes.put("nft_name", nft_name ?: JSONObject.NULL)
                    objRes.put("tag", tag ?: JSONObject.NULL)
                    objRes.put("description", description ?: JSONObject.NULL)
                    objRes.put("block_number", block_number)
                    objRes.put("minted_time", minted_time ?: JSONObject.NULL)
                    objRes.put("license", license ?: JSONObject.NULL)
                    objRes.put("video", video ?: JSONObject.NULL)
                    objRes.put("image_url", image_url ?: JSONObject.NULL)
                    objRes.put("token_uri", token_uri ?: JSONObject.NULL)
                    objRes.put("ipfs", ipfs ?: JSONObject.NULL)
                    objRes.put("attribute", attribute ?: JSONObject.NULL)
                    objRes.put("token_info", token_info ?: JSONObject.NULL)

                    var chkCollection = false

                    for(i in 0 until nftArray.length()){
                        if(nftArray.optJSONObject(i).getString("collection_id") == objRes.getString("collection_id")
                            && nftArray.optJSONObject(i).getString("network") == objRes.getString("network")){

                            nftArray.getJSONObject(i).getJSONArray("token").put(objRes)
                            chkCollection = true
                            break
                        }
                    }
                    if(chkCollection == false){
                        var data = JSONObject()
                        data.put("network", network)
                        data.put("owner_account", owner_account)
                        data.put("balance", balance)
                        data.put("collection_id", collection_id)
                        data.put("collection_name", collection_name)
                        data.put("collection_symbol", collection_symbol)
                        data.put("nft_type", nft_type)
                        data.put("creator", creator ?: JSONObject.NULL)
                        data.put("total_supply", total_supply ?: JSONObject.NULL)
                        data.put("deployment_date", deployment_date ?: JSONObject.NULL)
                        data.put("slug", slug ?: JSONObject.NULL)
                        data.put("category", category ?: JSONObject.NULL)
                        data.put("logo_url", logo_url ?: JSONObject.NULL)
                        data.put("image_s3_url", image_s3_url ?: JSONObject.NULL)
                        data.put("isverified", isverified ?: JSONObject.NULL)
                        data.put("numOwners", numOwners ?: JSONObject.NULL)
                        data.put("currency", currency ?: JSONObject.NULL)
                        data.put("discord_link", discord_link ?: JSONObject.NULL)
                        data.put("twitter_link", twitter_link ?: JSONObject.NULL)
                        data.put("instagram_link", instagram_link ?: JSONObject.NULL)
                        data.put("facebook_link", facebook_link ?: JSONObject.NULL)
                        data.put("telegram_link", telegram_link ?: JSONObject.NULL)
                        data.put("external_url", external_url ?: JSONObject.NULL)
                        data.put("updated_date", updated_date ?: JSONObject.NULL)

                        val objResArray = JSONArray()
                        objResArray.put(objRes)
                        data.put("token", objResArray)
                        nftArray.put(data)
                    }
                }
            }
            catch (ex: SQLException){
                ex.printStackTrace()
            }
            finally {
                getNFT.close()
            }
        }
    }
    dbConnector.disconnect()
    nftArray
}
//suspend fun getNFTsByWallet(
//    network: Array<String>,
//    account: String
//): JSONArray = withContext(Dispatchers.IO) {
//
//    val dbConnector = DBConnector()
//    dbConnector.connect()
//    val connection = dbConnector.getConnection()
//    val nftArray = JSONArray()
//
//    val net = network.joinToString("','", "'", "'")
//    val query = """
//           SELECT owner.network, owner.account, owner.balance,
//                collection.collection_id, collection.collection_name, collection.collection_symbol,
//                token.nft_type, token.block_number, token.token_id, token.name as token_name,
//                token.description, token.image_url, token.attributes, token.token_info, token.minted_time
//            FROM nft_owner_table AS owner
//            JOIN nft_token_table AS token ON owner.collection_id = token.collection_id AND owner.token_id = token.token_id AND owner.network = token.network
//            JOIN nft_collection_table AS collection ON token.collection_id = collection.collection_id AND token.network = collection.network
//             WHERE owner.network IN ($net) AND owner.account = '$account' AND owner.balance != '0'
//             ORDER BY token.block_number DESC
//        """
//    if (connection != null) {
//        val dbQueryExector = DBQueryExector(connection)
//        val getNFT: ResultSet? = dbQueryExector.executeQuery(query)
//
//        if (getNFT != null) {
//            try {
//                while (getNFT.next()) {
//                    val objRes = JSONObject()
//
//                    val network = getNFT.getString("network")
//                    val account = getNFT.getString("account")
//                    val balance = getNFT.getString("balance")
//                    val collection_id = getNFT.getString("collection_id")
//                    val collection_name = getNFT.getString("collection_name")
//                    val collection_symbol = getNFT.getString("collection_symbol")
//                    val nft_type = getNFT.getString("nft_type")
//                    val block_number = getNFT.getInt("block_number")
//                    val token_id = getNFT.getString("token_id")
//                    val token_name = getNFT.getString("token_name")
//                    val description = getNFT.getString("description")
//                    val image_url = getNFT.getString("image_url")
//                    val attributes = getNFT.getString("attributes")
//                    val token_info = getNFT.getString("token_info")
//                    val minted_time = getNFT.getString("minted_time")
//
//                    objRes.put("network", network)
//                    objRes.put("account", account)
//                    objRes.put("balance", balance)
//                    objRes.put("collection_id", collection_id)
//                    objRes.put("collection_name", collection_name ?: JSONObject.NULL)
//                    objRes.put("collection_symbol", collection_symbol ?: JSONObject.NULL)
//                    objRes.put("nft_type", nft_type)
//                    objRes.put("block_number", block_number)
//                    objRes.put("token_id", token_id)
//                    objRes.put("token_name", token_name ?: JSONObject.NULL)
//                    objRes.put("description", description ?: JSONObject.NULL)
//                    objRes.put("image_url", image_url ?: JSONObject.NULL)
//                    objRes.put("attributes", attributes ?: JSONObject.NULL)
//                    objRes.put("token_info", token_info ?: JSONObject.NULL)
//                    objRes.put("minted_time", minted_time ?: JSONObject.NULL)
//
//                    var chkCollection = false
//
//                    for(i in 0 until nftArray.length()){
//                        if(nftArray.optJSONObject(i).getString("collection_id") == objRes.getString("collection_id")
//                            && nftArray.optJSONObject(i).getString("network") == objRes.getString("network")){
//
//                            nftArray.getJSONObject(i).getJSONArray("token").put(objRes)
//                            chkCollection = true
//                            break
//                        }
//                    }
//                    if(chkCollection == false){
//                        var data = JSONObject()
//                        data.put("network", network)
//                        data.put("account", account)
//                        data.put("balance", balance)
//                        data.put("collection_id", collection_id)
//                        data.put("collection_name", collection_name)
//                        data.put("collection_symbol", collection_symbol)
//                        val objResArray = JSONArray()
//                        objResArray.put(objRes)
//                        data.put("token", objResArray)
//                        nftArray.put(data)
//                    }
//                }
//            }
//            catch (ex: SQLException){
//                ex.printStackTrace()
//            }
//            finally {
//                getNFT.close()
//            }
//        }
//    }
//    dbConnector.disconnect()
//    nftArray
//}

//getNFTTransaction
suspend fun getNFTsTransaction(
    network: String,
    collection_id: String,
    token_id: String
) : JSONArray = withContext(Dispatchers.IO){
    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val transactionArray = JSONArray()

    var sqlQuery1 = """
                SELECT
                    transfer.network AS network,
                    transfer.`from`,
                    transfer.`to`,
                    transfer.collection_id,
                    transfer.block_number,
                    transfer.`timestamp`,
                    transfer.transaction_hash,
                    transfer.log_id,
                    transfer.token_id,
                    transfer.amount,
                    'transfer' as 'transaction_type'
                FROM nft_transfer_table AS transfer
                WHERE transfer.network = '$network' AND transfer.collection_id = '$collection_id' AND transfer.token_id = '$token_id'
                """

    var sqlQuery2 = """
                SELECT
                    sales.network AS network,
                    sales.buyer,
                    sales.block_number,
                    sales.collection_id,
                    sales.token_id,
                    sales.transaction_hash,
                    sales.log_id,
                    sales.`timestamp`,
                    sales.currency,
                    sales.price,
                    sales.symbol,
                    sales.decimals,
                    sales.market,
                    sales.NFTs,
                    'sales' as 'transaction_type'
                FROM nft_sales_table AS sales
                WHERE sales.network = '$network' AND sales.collection_id = '$collection_id' AND sales.token_id = '$token_id'
                """
    if (connection != null) {
        val dbQueryExector = DBQueryExector(connection)
        val getTransaction1: ResultSet? = dbQueryExector.executeQuery(sqlQuery1)
        val getTransaction2: ResultSet? = dbQueryExector.executeQuery(sqlQuery2)

        if (getTransaction1 != null) {
            try {
                while (getTransaction1.next()) {
                    val jsonData = JSONObject()
                    // Select data = network, from, to, collection_id, block_number, timestamp, transaction_hash, log_id, token_id, amount, transaction_type
                    val network = getTransaction1.getString("network")
                    val from = getTransaction1.getString("from")
                    val to = getTransaction1.getString("to")
                    val collection_id = getTransaction1.getString("collection_id")
                    val block_number = getTransaction1.getInt("block_number")
                    val timestamp = getTransaction1.getInt("timestamp")
                    val transaction_hash = getTransaction1.getString("transaction_hash")
                    val log_id = getTransaction1.getString("log_id")
                    val token_id = getTransaction1.getString("token_id")
                    val amount = getTransaction1.getString("amount")

                    jsonData.put("network", network)
                    jsonData.put("from", from)
                    jsonData.put("to", to)
                    jsonData.put("collection_id", collection_id)
                    jsonData.put("block_number", block_number)
                    jsonData.put("timestamp", timestamp)
                    jsonData.put("transaction_hash", transaction_hash)
                    jsonData.put("log_id", log_id)
                    jsonData.put("token_id", token_id)
                    jsonData.put("amount", amount)
                    jsonData.put("transaction_type", "transfer")

                    transactionArray.put(jsonData)
                }
            }
            catch (ex: SQLException){
                ex.printStackTrace()
            }
            finally {
                getTransaction1.close()
            }
        }
        if (getTransaction2 != null){
            try {
                while (getTransaction2.next()){
                    val jsonData = JSONObject()
                    // Select data = network, buyer, collection_id, block_number, timestamp, transaction_hash, log_id, token_id, currency, price, symbol, decimals, market, NFTs, transaction_type
                    val network = getTransaction2.getString("network")
                    val buyer = getTransaction2.getString("buyer")
                    val collection_id = getTransaction2.getString("collection_id")
                    val block_number = getTransaction2.getInt("block_number")
                    val timestamp = getTransaction2.getInt("timestamp")
                    val transaction_hash = getTransaction2.getString("transaction_hash")
                    val log_id = getTransaction2.getString("log_id")
                    val token_id = getTransaction2.getString("token_id")
                    val currency = getTransaction2.getString("currency")
                    val price = getTransaction2.getString("price")
                    val symbol = getTransaction2.getString("symbol")
                    val decimals = getTransaction2.getInt("decimals")
                    val market = getTransaction2.getString("market")
                    val NFTs = getTransaction2.getString("NFTs")

                    jsonData.put("network", network)
                    jsonData.put("buyer", buyer)
                    jsonData.put("block_number", block_number)
                    jsonData.put("collection_id", collection_id)
                    jsonData.put("token_id", token_id)
                    jsonData.put("transaction_hash", transaction_hash)
                    jsonData.put("log_id", log_id)
                    jsonData.put("timestamp", timestamp)
                    jsonData.put("currency", currency)
                    jsonData.put("price", price)
                    jsonData.put("symbol", symbol)
                    jsonData.put("decimals", decimals)
                    jsonData.put("market", market)
                    jsonData.put("NFTs", NFTs)
                    jsonData.put("transaction_type", "sales")

                    transactionArray.put(jsonData)
                }
            }
            catch (ex: SQLException){
                ex.printStackTrace()
            }
            finally {
                getTransaction2.close()
            }
        }
    }
    // Convert JSONArray to List<JSONObject>
    val transactionList = (0 until transactionArray.length()).map { i ->
        transactionArray.getJSONObject(i)
    }

    // Sort the list by timestamp in descending order
    val sortedList = transactionList.sortedByDescending { it.getInt("timestamp") }

    // Convert the sorted list back to JSONArray
    val sortedArray = JSONArray(sortedList)

    dbConnector.disconnect()
    sortedArray
}
suspend fun sendNFT721TransactionAsync(
    network: String,
    fromAddress: String,
    toAddress: String,
    tokenId: String,
    nftContractAddress: String
): JSONObject = withContext(Dispatchers.IO){
    val rpcUrl = when (network) {
        "ethereum" -> "https://mainnet.infura.io/v3/02c509fda7da4fed882ac537046cfd66"
        "cypress" -> "https://rpc.ankr.com/klaytn"
        "polygon" -> "https://rpc-mainnet.maticvigil.com/v1/96ab7849c9d3f105416383dd284c3f7e6511208c"
        "bnb" -> "https://bsc-dataseed.binance.org"
        "goerli" -> "https://goerli.infura.io/v3/9aa3d95b3bc440fa88ea12eaa4456161"
        "mumbai" -> "https://polygon-mumbai.infura.io/v3/4458cf4d1689497b9a38b1d6bbf05e78"
        "bnbTest" -> "https://data-seed-prebsc-1-s1.binance.org:8545"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    val jsonData = JSONObject()

    try {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create("0x40ceea6075422a7f04e6d5250e482ab1bc857816d20e99bd6ab819afe1a2bfb6")

        val function = Function(
            "safeTransferFrom",
            listOf(Address(fromAddress), Address(toAddress), Uint256(BigInteger(tokenId))),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()
        val tx = if (network == "bnb" || network == "bnbTest") {
            RawTransaction.createTransaction(
                nonce,
                getEstimateGas(network, "baseFee"), // Add 20% to the gas price
                getEstimateGas(
                    network,
                    "transferERC721",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    tokenId
                ), // Add 20% to the gas limit
                nftContractAddress,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                getEstimateGas(
                    network,
                    "transferERC721",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    tokenId
                ),
                nftContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger("100000000"),
                getEstimateGas(network, "baseFee")
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val txHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        jsonData.put("result","OK")
        jsonData.put("transactionHash",txHash)
    } catch (e: Exception) {
        jsonData.put("result", "FAIL")
        jsonData.put("error", e.message)
    }
}

suspend fun sendNFT1155TransactionAsync(
    network: String,
    fromAddress: String,
    toAddress: String,
    tokenId: String,
    nftContractAddress: String,
    amount: String,
): JSONObject = withContext(Dispatchers.IO) {
    val jsonData = JSONObject()
    val rpcUrl = when (network) {
        "ethereum" -> "https://mainnet.infura.io/v3/02c509fda7da4fed882ac537046cfd66"
        "cypress" -> "https://rpc.ankr.com/klaytn"
        "polygon" -> "https://rpc-mainnet.maticvigil.com/v1/96ab7849c9d3f105416383dd284c3f7e6511208c"
        "bnb" -> "https://bsc-dataseed.binance.org"
        "goerli" -> "https://goerli.infura.io/v3/9aa3d95b3bc440fa88ea12eaa4456161"
        "mumbai" -> "https://polygon-mumbai.infura.io/v3/4458cf4d1689497b9a38b1d6bbf05e78"
        "bnbTest" -> "https://data-seed-prebsc-1-s1.binance.org:8545"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    try {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create("0x40ceea6075422a7f04e6d5250e482ab1bc857816d20e99bd6ab819afe1a2bfb6")

        val ethGasPrice = web3j.ethGasPrice().sendAsync().get()

        // Ensure amount is a valid number
        if (BigInteger(amount) <= BigInteger.ZERO) {
            jsonData.put("result", "FAIL")
            jsonData.put("error", "insufficient funds")
            return@withContext jsonData
        }

        val function = Function(
            "safeTransferFrom",
            listOf(
                Address(fromAddress), Address(toAddress), Uint256(BigInteger(tokenId)),
                Uint256(BigInteger(amount)), DynamicBytes(byteArrayOf(0))
            ),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val gasPrice = ethGasPrice.gasPrice

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()
        val tx = if (network == "bnb" || network == "bnbTest") {
            RawTransaction.createTransaction(
                nonce,
                getEstimateGas(network, "baseFee"), // Add 20% to the gas price
                getEstimateGas(
                    network,
                    "transferERC1155",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    amount,
                    tokenId
                ), // Add 20% to the gas limit
                nftContractAddress,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                getEstimateGas(network, "transferERC1155",nftContractAddress, fromAddress, toAddress, amount, tokenId),
                nftContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger("100000000"),
                getEstimateGas(network, "baseFee")
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val txHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        jsonData.put("result","OK")
        jsonData.put("transactionHash",txHash)
    } catch (e: Exception) {
        jsonData.put("result", "FAIL")
        jsonData.put("error", e.message)
    }
}

suspend fun sendNFT1155BatchTransactionAsync(
    network: String,
    fromAddress: String,
    toAddress: String,
    tokenId: Array<String>,
    nftContractAddress: String,
    amount: Array<String>,
): JSONObject = withContext(Dispatchers.IO) {
    val jsonData = JSONObject()
    val rpcUrl = when (network) {
        "ethereum" -> "https://mainnet.infura.io/v3/02c509fda7da4fed882ac537046cfd66"
        "cypress" -> "https://rpc.ankr.com/klaytn"
        "polygon" -> "https://rpc-mainnet.maticvigil.com/v1/96ab7849c9d3f105416383dd284c3f7e6511208c"
        "bnb" -> "https://bsc-dataseed.binance.org"
        "goerli" -> "https://goerli.infura.io/v3/9aa3d95b3bc440fa88ea12eaa4456161"
        "mumbai" -> "https://polygon-mumbai.infura.io/v3/4458cf4d1689497b9a38b1d6bbf05e78"
        "bnbTest" -> "https://data-seed-prebsc-1-s1.binance.org:8545"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    try {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create("0x40ceea6075422a7f04e6d5250e482ab1bc857816d20e99bd6ab819afe1a2bfb6")

        val ethGasPrice = web3j.ethGasPrice().sendAsync().get()

        // Ensure amount is a valid number
        for(a in amount) {
            if (BigInteger(a) <= BigInteger.ZERO) {
                jsonData.put("result", "FAIL")
                jsonData.put("error", "insufficient funds")
                return@withContext jsonData
            }
        }
        val batchTokenId = tokenId.map { Uint256(BigInteger(it)) }
        val batchAmount = amount.map { Uint256(BigInteger(it)) }

        val function = Function(
            "safeBatchTransferFrom",
            listOf(
                Address(fromAddress), Address(toAddress), DynamicArray(batchTokenId), DynamicArray(batchAmount), DynamicBytes(byteArrayOf(0))
            ),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val gasPrice = ethGasPrice.gasPrice

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()
        val tx = if (network == "bnb" || network == "bnbTest") {
            RawTransaction.createTransaction(
                nonce,
                getEstimateGas(network, "baseFee"), // Add 20% to the gas price
                getEstimateGas(
                    network,
                    "batchTransferERC1155",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    null,
                    null,
                    null,
                    null,
                    tokenId,
                    amount
                ), // Add 20% to the gas limit
                nftContractAddress,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                getEstimateGas(
                    network,
                    "batchTransferERC1155",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    null,
                    null,
                    null,
                    null,
                    tokenId,
                    amount
                ), // Add 20% to the gas limit
                nftContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger("100000000"),
                getEstimateGas(network, "baseFee")
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val txHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        jsonData.put("result","OK")
        jsonData.put("transactionHash",txHash)
    } catch (e: Exception) {
        jsonData.put("result", "FAIL")
        jsonData.put("error", e.message)
    }
}

suspend fun deployErc721Async(
    network: String,
    fromAddress: String,
    name: String,
    symbol: String,
    baseURI: String,
    owner: String,
    uriType: String
): JSONObject = withContext(Dispatchers.IO){
    val rpcUrl = when (network) {
        "ethereum" -> "https://mainnet.infura.io/v3/02c509fda7da4fed882ac537046cfd66"
        "cypress" -> "https://rpc.ankr.com/klaytn"
        "polygon" -> "https://rpc-mainnet.maticvigil.com/v1/96ab7849c9d3f105416383dd284c3f7e6511208c"
        "bnb" -> "https://bsc-dataseed.binance.org"
        "goerli" -> "https://goerli.infura.io/v3/9aa3d95b3bc440fa88ea12eaa4456161"
        "mumbai" -> "https://polygon-mumbai.infura.io/v3/4458cf4d1689497b9a38b1d6bbf05e78"
        "bnbTest" -> "https://data-seed-prebsc-1-s1.binance.org:8545"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    val jsonData = JSONObject()

    try {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create("")

        val function = Function(
            "deployWrapped721",
            listOf(Utf8String(name), Utf8String(symbol), Utf8String(baseURI), Utf8String(owner), Uint8(BigInteger(uriType))),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()
        val tx = if (network == "bnb" || network == "bnbTest") {
            RawTransaction.createTransaction(
                nonce,
                getEstimateGas(network, "baseFee"), // Add 20% to the gas price
                getEstimateGas(
                    network,
                    "deployERC721",
                    null,
                    fromAddress,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    name, symbol, owner, baseURI, uriType
                ), // Add 20% to the gas limit
                addrTransferGoerli,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                getEstimateGas(
                    network,
                    "deployERC721",
                    null,
                    fromAddress,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    name, symbol, owner, baseURI, uriType
                ),
                addrTransferGoerli,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger("100000000"),
                getEstimateGas(network, "baseFee")
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val txHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        jsonData.put("result","OK")
        jsonData.put("transactionHash",txHash)
    } catch (e: Exception) {
        jsonData.put("result", "FAIL")
        jsonData.put("error", e.message)
    }
}

//fun getNftsByWallet(mainNet: List<String>, address: String) {
//    var res: JSONArray? = null
//    var nfts = JSONArray()
//    val client = OkHttpClient()
//    for (network in mainNet) {
//        if (network == "ethereum") {
//            val request = Request.Builder()
//                .url("https://eth-mainnet.g.alchemy.com/nft/v3/wQ1_uDwVHfjUnBs6_mkGxxGeC_v0yk71/getNFTsForOwner?owner=${address}&withMetadata=true&pageSize=100")
//                .get()
//                .addHeader("accept", "application/json")
//                .build()
//
//            val response = client.newCall(request).execute()
//            var strRes = response.body?.string()
//            res = JSONObject(strRes).getJSONArray("ownedNfts")
//        }
//        if (network == "klaytn") {
//            val request = Request.Builder()
//                .url("https://th-api.klaytnapi.com/v2/account/${address}/token?kind=nft,mt")
//                .get()
//                .addHeader("Content_Type", "application/json")
//                .addHeader("x-chain-id", "8217")
//                .addHeader(
//                    "Authorization",
//                    "Basic S0FTS1JRVDUyWEJJRVZQT0E1TjQyR1lUOkE2djZpYXRRdEtEclB2RUlRNkszTHpLa1B2dC1pN0s0SXJ0N0dnUW8="
//                )
//                .build()
//
//            val response = client.newCall(request).execute()
//            var strRes = response.body?.string()
//            res = JSONObject(strRes).getJSONArray("items")
//        }
//        if (network == "polygon") {
//            val request = Request.Builder()
//                .url("https://polygon-mainnet.g.alchemy.com/nft/v3/9t7Gb13XKxec6h4TFepZa53BL5eTYLu9/getNFTsForOwner?owner=${address}&withMetadata=true&pageSize=100")
//                .get()
//                .addHeader("accept", "application/json")
//                .build()
//
//            val response = client.newCall(request).execute()
//            var strRes = response.body?.string()
//            res = JSONObject(strRes).getJSONArray("ownedNfts")
//        }
//
//        if (network == "binance") {
//            val request = Request.Builder()
//                .url("\"https://deep-index.moralis.io/api/v2/${address}/nft?chain=bsc&format=decimal&normalizeMetadata=true&media_items=false\"")
//                .get()
//                .addHeader(
//                    "X-API-Key",
//                    "nE1eXZWbbuaBlB0knb1vjNKs5WFcfv5SiqMqnXhRW3s3w1YeA4VAUhaMU2hFJ46i"
//                )
//                .addHeader("Content_Type", "application/json")
//                .build()
//
//            val response = client.newCall(request).execute()
//            var strRes = response.body?.string()
//            res = JSONObject(strRes).getJSONArray("result")
//        }
//
//        //println("Get Account:\n$res\n")
//
//        for (i in 0 until res!!.length()) {
//            val item = res.getJSONObject(i)
//
//            var collection_id: String? = null
//            var collection_name: String? = null
//            var nft_type: String? = null
//            var token_id: String? = null
//            var tokenURI: String? = null
//            var balance: String? = null
//            var h_timestamp: String? = null // holder certification date
//            var convertName: String? = null
//            var convertImage: String? = null
//            var convertDescription: String? = null
//            var attributes: JSONArray? = null
//            var convertAttributes: List<Attributes>? = null
//            val turnsType = object : TypeToken<List<Attributes>>() {}.type
//            var account_address: String? = null
//            if (network == "ethereum" || network == "polygon") {
//                // if(item.spamInfo) continue
//                collection_id = item.getJSONObject("contract").getString("address")
//                collection_name = item.getJSONObject("contract").optString("name")
//                if (item.getJSONObject("contract").isNull("name")) {
//                    collection_name =
//                        item.getJSONObject("contract").optJSONObject("openSeaMetadata")
//                            ?.optString("collectionName")
//                }
//                nft_type = item.getString("tokenType").lowercase()
//                token_id = item.getString("tokenId")
//                balance = item.getString("balance").toString()
//                convertName = item.optString("name")
//                convertDescription = item.optString("description")
//                convertImage =
//                    item.optJSONObject("raw")?.optJSONObject("metadata")?.optString("image")
//                attributes =
//                    item.optJSONObject("raw")?.optJSONObject("metadata")?.optJSONArray("attributes")
//                convertAttributes = Gson().fromJson(attributes?.toString(), turnsType)
//            } else if (network == "klaytn") {
//                collection_id = item.getString("contractAddress")
//                token_id = hexToDecimalString(item.getJSONObject("extras").getString("tokenId"))
//                tokenURI = item.getJSONObject("extras").getString("tokenUri")
//                if (tokenURI.startsWith("ipfs://")) tokenURI =
//                    "https://ipfs.io/ipfs/" + tokenURI.split("ipfs://")[1]
//                balance = hexToDecimalString(item.getString("balance"))
//                nft_type = "erc721"
//                if (item.getString("kind") == "mt") nft_type = "erc1155"
//                var tokenRes = getMetadata(tokenURI!!)
//                convertImage = tokenRes.optString("image")
//                convertName = tokenRes.optString("name")
//                convertDescription = tokenRes.optString("description")
//                attributes = tokenRes.optJSONArray("attributes")
//                convertAttributes = Gson().fromJson(attributes?.toString(), turnsType)
//            } else if (network == "binance") {
//                collection_id = item.getString("token_address")
//                collection_name = item.optString("name")
//                token_id = item.getString("token_id")
//                balance = item.getString("amount")
//                nft_type = item.getString("contract_type").lowercase()
//                convertImage = item.optJSONObject("normalized_metadata")?.optString("image")
//                convertName = item.optJSONObject("normalized_metadata")?.optString("name")
//                convertDescription =
//                    item.optJSONObject("normalized_metadata")?.optString("description")
//                attributes = item.optJSONObject("normalized_metadata")?.optJSONArray("attributes")
//                if (!item.isNull("token_uri") && item.optJSONObject("normalized_metadata")
//                        .isNull("image")
//                ) {
//                    if (item.optString("token_uri").startsWith("ar://")) tokenURI =
//                        "https://arweave.net/" + item.optString("token_uri").split("ar://")[1];
//                    var tokenRes = getMetadata(tokenURI!!)
//                    convertImage = tokenRes.optString("image")
//                    convertName = tokenRes.optString("name")
//                    convertDescription = tokenRes.optString("description")
//                    attributes = tokenRes.optJSONArray("attributes")
//                }
//                convertAttributes = Gson().fromJson(attributes?.toString(), turnsType)
//            }
//            h_timestamp = getHolder(collection_id, token_id, address)
//
//            if (convertImage!!.startsWith("ipfs://")) convertImage =
//                "https://ipfs.io/ipfs/" + convertImage.split("ipfs://")[1]
//            if (convertImage.startsWith("ar://")) convertImage =
//                "https://arweave.net/" + convertImage.split("ar://")[1]
//            if (convertName.isNullOrEmpty() || convertName == "null") convertName = token_id
//
//            account_address = address
//
//            var nft = Nft(
//                collection_id,
//                collection_name,
//                nft_type,
//                token_id,
//                tokenURI,
//                balance,
//                h_timestamp,// holder certification date
//                convertName,
//                convertImage,
//                convertDescription,
//                convertAttributes,
//                account_address
//            )
//            var objRes = JSONObject(Gson().toJson(nft))
//
//            var chkCollection = false
//            for (i in 0 until nfts.length()) {
//                var item2 = nfts.getJSONObject(i)
//                if (collection_id == item2.optString("collection_id")) {
//                    item2.optJSONArray("token")?.put(objRes)
//                    chkCollection = true
//                    break
//                }
//            }
//            if (!chkCollection) {
//                var data = JSONObject()
//                data.put("collection_id", collection_id)
//                if (network == "klaytn" && nft_type == "erc721") collection_name =
//                    getKlayNftCollectionName(collection_id)
//                if (collection_name.isNullOrEmpty() || collection_name == "null") collection_name =
//                    "$convertName ..."
//                data.put("collection_name", collection_name)
//                data.put("nft_type", nft_type)
//                data.put("network", network)
//                data.put("token", JSONArray())
//                data.optJSONArray("token")?.put(objRes)
//                nfts.put(data)
//            }
//        }
//    }
//    println(nfts)
//}
//
//fun getMetadata(tokenURI: String): JSONObject {
//    val client = OkHttpClient()
//
//    val request = Request.Builder()
//        .url(tokenURI)
//        .get()
//        .addHeader("Content_Type", "application/json")
//        .build()
//
//    val response = client.newCall(request).execute()
//    var strRes = response.body?.string()
//    return JSONObject(strRes)
//}
//
//fun getKlayNftCollectionName(collection_id: String?): String {
//    val client = OkHttpClient()
//
//    val request = Request.Builder()
//        .url("https://th-api.klaytnapi.com/v2/contract/nft/$collection_id")
//        .get()
//        .addHeader("Content_Type", "application/json")
//        .addHeader("x-chain-id", "8217")
//        .addHeader(
//            "Authorization",
//            "Basic S0FTS1JRVDUyWEJJRVZQT0E1TjQyR1lUOkE2djZpYXRRdEtEclB2RUlRNkszTHpLa1B2dC1pN0s0SXJ0N0dnUW8="
//        )
//        .build()
//
//    val response = client.newCall(request).execute()
//    var strRes = response.body?.string()
//    return JSONObject(strRes).optString("name")
//}
//
//fun getHolder(collection_id: String?, token_id: String?, address: String?): String? {
//    val client = OkHttpClient()
//
//    val request = Request.Builder()
//        .url("https://project.abc.ne.kr:19000/nft/holder/$collection_id/$token_id/$address")
//        .get()
//        .addHeader("Content_Type", "application/json")
//        .build()
//
//    val response = client.newCall(request).execute()
//    var strRes = response.body?.string()
//    println(strRes)
//    return JSONObject(strRes)?.optString("value")
//}
//
//// Convert to hexadecimal string
//fun hexToDecimalString(hex: String): String {
//    return Integer.decode(hex).toString()
//}
//
//fun decimalToHexString(decimal: Int): String {
//    return Integer.toHexString(decimal)
//}



