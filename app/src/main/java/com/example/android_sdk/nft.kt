package com.example.android_sdk

//import com.google.gson.Gson
//import com.google.gson.JsonArray
//import com.google.gson.JsonObject
//import com.google.gson.JsonParser
//import com.google.gson.reflect.TypeToken
//import okhttp3.OkHttpClient
//import okhttp3.Request
//import okhttp3.RequestBody
import android.annotation.SuppressLint
import getAccountInfoAsync
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
import java.sql.Statement

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
suspend fun getMintableAddress(
    owner: Array<String>
): JSONObject = withContext(Dispatchers.IO) {

    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val CAArray = JSONArray()
    val CAData = JSONObject()

    val own = owner?.joinToString("','", "'", "'")

    val CAQuery=
        "SELECT " +
                "network, " +
                "collection_id, " +
                "collection_name, " +
                "collection_symbol, " +
                "nft_type, " +
                "creator, " +
                "owner, " +
                "total_supply, " +
                "deployment_date, " +
                "slug, " +
                "category, "+
                "logo_url, "+
                "image_s3_url, "+
                "isverified, "+
                "numOwners, "+
                "currency, "+
                "discord_link, "+
                "twitter_link, "+
                "instagram_link, "+
                "facebook_link, "+
                "telegram_link, "+
                "external_url "+
                "FROM " +
                "nft_collection_table " +
                "WHERE " +
                "network IN ('ethereum','cypress','polygon','bnb') " +
                "AND " +
                "creator IN ('0x780A19638D126d59f4Ed048Ae1e0DC77DAf39a77','0x7E055Cb85FBE64da619865Df8a392d12f009aD81')" +
                "AND " +
                " owner IN (${own})"
    try {
        if (connection != null) {
            val dbQueryExector = DBQueryExector(connection)
            val getCA: ResultSet? = dbQueryExector.executeQuery(CAQuery)

            if (getCA != null) {
                try {
                    while (getCA.next()) {
                        val objRes = JSONObject()

                        val network = getCA.getString("network")
                        val collection_id = getCA.getString("collection_id")
                        val collection_name = getCA.getString("collection_name")
                        val collection_symbol = getCA.getString("collection_symbol")
                        val nft_type = getCA.getString("nft_type")
                        val creator = getCA.getString("creator")
                        val owner = getCA.getString("owner")
                        val total_supply = getCA.getString("total_supply")
                        val deployment_date = getCA.getString("deployment_date")
                        val slug = getCA.getString("slug")
                        val category = getCA.getString("category")
                        val logo_url = getCA.getString("logo_url")
                        val image_s3_url = getCA.getString("image_s3_url")
                        val isverified = getCA.getString("isverified")
                        val numOwners = getCA.getString("numOwners")
                        val currency = getCA.getString("currency")
                        val discord_link = getCA.getString("discord_link")
                        val twitter_link = getCA.getString("twitter_link")
                        val instagram_link = getCA.getString("instagram_link")
                        val facebook_link = getCA.getString("facebook_link")
                        val telegram_link = getCA.getString("telegram_link")
                        val external_url = getCA.getString("external_url")

                        objRes.put("network", network)
                        objRes.put("collection_id", collection_id)
                        objRes.put("collection_name", collection_name)
                        objRes.put("collection_symbol", collection_symbol)
                        objRes.put("nft_type", nft_type)
                        objRes.put("creator", creator)
                        objRes.put("owner", owner)
                        objRes.put("total_supply", total_supply)
                        objRes.put("deployment_date", deployment_date)
                        objRes.put("slug", slug)
                        objRes.put("category", category)
                        objRes.put("logo_url", logo_url)
                        objRes.put("image_s3_url", image_s3_url)
                        objRes.put("isverified", isverified)
                        objRes.put("numOwners", numOwners)
                        objRes.put("currency", currency)
                        objRes.put("discord_link", discord_link)
                        objRes.put("twitter_link", twitter_link)
                        objRes.put("instagram_link", instagram_link)
                        objRes.put("facebook_link", facebook_link)
                        objRes.put("telegram_link", telegram_link)
                        objRes.put("external_url", external_url)

                        CAArray.put(objRes)
                    }
                }
                catch (ex: SQLException){
                    ex.printStackTrace()
                }
                finally {
                    getCA.close()
                }
            }
        }
        dbConnector.disconnect()

        CAData.put("result", "OK")
        CAData.put("value", CAArray)
    }catch (e: Exception){
        CAData.put("result", "FAIL")
        CAData.put("reason", e)
    }
}

suspend fun setNFTsHide(
    network: String,
    owner: String,
    account: String,
    collection_id: String,
    token_id: String
): JSONObject = withContext(Dispatchers.IO) {
    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val hideData = JSONObject()

    try {
        val insertQuery =
            "INSERT INTO " +
                    "nft_hide_table (network, account, collection_id, token_id, owner, image_url, collection_name, nft_name) " +
                    "SELECT " +
                    "'${network}', " +
                    "'${account}', " +
                    "'${collection_id}', " +
                    "'${token_id}', " +
                    "'${owner}', " +
                    "token.image_url AS image_url, " +
                    "collection.collection_name AS collection_name , " +
                    "token.nft_name AS nft_name " +
                    "FROM " +
                    "nft_token_table AS token " +
                    "JOIN " +
                    "nft_collection_table AS collection " +
                    "ON " +
                    "token.collection_id = collection.collection_id " +
                    "WHERE " +
                    "token.collection_id = '${collection_id}' " +
                    "AND " +
                    "token.token_id = '${token_id}'"

        println(insertQuery)

        val statement: Statement = connection!!.createStatement()
        statement.executeUpdate(insertQuery)
        hideData.put("result", "OK")
    } catch (e: SQLException) {
        e.printStackTrace()
        hideData.put("result", "FAIL")
        if (e.message?.contains("Duplicate entry") == true) {
            hideData.put("reason", "Duplicate entry")
        }
        else if (e.message?.contains("Table not found") == true) {
            hideData.put("reason", "Table not found")
        }
        else if (e.message?.contains("Column not found") == true) {
            hideData.put("reason", "Column not found")
        }
        else if (e.message?.contains("Connection timeout") == true) {
            hideData.put("reason", "Connection timeout")
        }
        else if (e.message?.contains("Connection refused") == true) {
            hideData.put("reason", "Connection refused")
        }
        else {
            hideData.put("reason", e.printStackTrace())
        }
    } finally {
        connection?.close()
    }
    hideData
}
suspend fun deleteNFTsHide(
    network: String,
    owner: String,
    account: String,
    collection_id: String,
    token_id: String
): JSONObject = withContext(Dispatchers.IO) {
    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val hideData = JSONObject()

    try {
        val deleteQuery =
            "DELETE FROM " +
                    "nft_hide_table " +
                    "WHERE " +
                    "network = '$network' " +
                    "AND " +
                    "account = '$account' " +
                    "AND " +
                    "collection_id = '$collection_id' " +
                    "AND " +
                    "token_id = '$token_id' " +
                    "AND " +
                    "owner = '$owner'"

        println(deleteQuery)

        val statement: Statement = connection!!.createStatement()
        val rowsAffected = statement.executeUpdate(deleteQuery)

        if (rowsAffected > 0) {
            hideData.put("result", "OK")
        } else {
            hideData.put("result", "FAIL1")
        }
    } catch (e: SQLException) {
        e.printStackTrace()
        hideData.put("result", "FAIL2")
    } finally {
        connection?.close()
    }
    hideData
}

@SuppressLint("SuspiciousIndentation")
suspend fun getNFTsByWallet(
    network: Array<String>,
    account: String ?= null,
    collection_id: String ?= null,
    sort: String ?= null,
    limit: Int ?= null,
    page_number: Int ?= null
): JSONObject = withContext(Dispatchers.IO) {

    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val nftArray = JSONArray() // { ..., value : [ nftArray ]}
    val nftData = JSONObject() // { "result": OK, "sum": 1, "sort": "desc", "page_count": 1, "value" : nftArray }

    val net = network.joinToString("','", "'", "'")

//    var offset = limit?.let { (page_number?.minus(1))?.times(it) }
    var offset = if (page_number != null && limit != null) {
        (page_number - 1) * limit
    } else {
        0 // 또는 적절한 기본값 설정
    }
    if(page_number==null || page_number==0 || page_number==1){
        offset = 0
    }
    var strQuery =
        "SELECT" +
                " owner.network AS network," +
                " collection.collection_id AS collection_id," +
                " collection.collection_name AS collection_name," +
                " collection.collection_symbol AS collection_symbol," +
                " collection.creator AS creator," +
                " collection.deployment_date AS deployment_date," +
                " collection.total_supply AS total_supply," +
                " token.nft_type AS nft_type," +
                " token.minted_time AS minted_time," +
                " token.block_number AS block_number," +
                " owner.owner_account AS owner_account," +
                " token.token_id AS token_id," +
                " owner.balance AS balance," +
                " token.token_uri AS token_uri," +
                " token.nft_name AS nft_name," +
                " token.description AS description," +
                " token.image_url AS image_url," +
                " token.external_url AS external_url," +
                " token.attribute AS attribute," +
                " token.token_info AS token_info" +
                " FROM " +
                "nft_owner_table AS owner" +
                " JOIN " +
                "nft_token_table AS token" +
                " ON" +
                " owner.collection_id = token.collection_id" +
                " AND " +
                " owner.token_id = token.token_id" +
                " AND " +
                " owner.network = token.network" +
                " JOIN " +
                "nft_collection_table AS collection" +
                " ON" +
                " token.collection_id = collection.collection_id" +
                " AND" +
                " token.network = collection.network" +
                " WHERE" +
                " owner.network IN (${net})" +
                " AND" +
                " owner.balance != '0'"
    if (account != null) {
        strQuery += " AND owner.owner_account = '$account'"
    }
    if (collection_id != null) {
        strQuery += " AND owner.collection_id = '$collection_id'"
    }
    strQuery += " AND NOT EXISTS ( SELECT 1 FROM nft_hide_table AS hide JOIN users_table AS users ON users.owner_eigenvalue = hide.owner WHERE hide.network = owner.network AND hide.account = owner.owner_account AND owner.owner_account = users.user_account AND hide.token_id = owner.token_id AND hide.collection_id = owner.collection_id AND hide.owner = users.owner_eigenvalue)"
    strQuery += " ORDER BY token.block_number"
    if (sort == " asc") {
        strQuery += " asc"
    } else {
        strQuery += " desc"
    }
    strQuery += ", CAST(token.token_id AS SIGNED) desc"
    if (limit != null) {
        strQuery += " LIMIT $limit OFFSET $offset"
    }
    println(strQuery)

    var sumQuery =
        "SELECT" +
                " count(*) AS sum" +
                " FROM" +
                " nft_owner_table AS owner" +
                " JOIN" +
                " nft_token_table AS token" +
                " ON" +
                " owner.collection_id = token.collection_id" +
                " AND" +
                " owner.token_id = token.token_id" +
                " AND" +
                " owner.network = token.network" +
                " JOIN" +
                " nft_collection_table AS collection" +
                " ON" +
                " token.collection_id = collection.collection_id" +
                " AND" +
                " token.network = collection.network" +
                " WHERE" +
                " owner.network IN ($net)" +
                " AND" +
                " owner.balance != '0'"
    if (account != null) {
        sumQuery += " AND owner.owner_account = '$account' "
    }
    if (collection_id != null) {
        sumQuery += " AND owner.collection_id = '$collection_id' "
    }
    sumQuery += " AND NOT EXISTS ( SELECT 1 FROM nft_hide_table AS hide JOIN users_table AS users ON users.owner_eigenvalue = hide.owner WHERE hide.network = owner.network AND hide.account = owner.owner_account AND owner.owner_account = users.user_account AND hide.token_id = owner.token_id AND hide.collection_id = owner.collection_id AND hide.owner = users.owner_eigenvalue)"

    println(sumQuery)
    try{
        var sum: Int? = null
        if ((account==null && collection_id==null) || (limit == null && page_number != null)) {
            throw Exception() // 예외 발생
        }
        if (connection != null) {
            val dbQueryExector = DBQueryExector(connection)
            val getNFT: ResultSet? = dbQueryExector.executeQuery(strQuery)
            val getSum: ResultSet? = dbQueryExector.executeQuery(sumQuery)
            if (getNFT != null) {
                try {
                    while (getNFT.next()) {
                        val objRes = JSONObject()

                        val network = getNFT.getString("network")
                        val collection_id = getNFT.getString("collection_id")
                        val collection_name = getNFT.getString("collection_name")
                        val collection_symbol = getNFT.getString("collection_symbol")
                        val collection_creator = getNFT.getString("creator")
                        val deployment_date = getNFT.getInt("deployment_date")
                        val total_supply = getNFT.getString("total_supply")
                        val nft_type = getNFT.getString("nft_type")
                        val minted_time = getNFT.getInt("minted_time")
                        val block_number = getNFT.getInt("block_number")
                        val owner_account = getNFT.getString("owner_account")
                        val token_id = getNFT.getString("token_id")
                        val balance = getNFT.getString("balance")
                        val token_uri = getNFT.getString("token_uri")
                        val nft_name = getNFT.getString("nft_name")
                        val description = getNFT.getString("description")
                        val image_url = getNFT.getString("image_url")
                        val external_url = getNFT.getString("external_url")
                        val attribute = getNFT.getString("attribute")
                        val token_info = getNFT.getString("token_info")

//                        val replace_attributes = JSONArray(attribute ?: "[]")
//                        val replace_metadata = JSONObject(token_info ?: "{}")

                        objRes.put("network", network)
                        objRes.put("collection_id", collection_id ?: JSONObject.NULL)
                        objRes.put("collection_name", collection_name ?: JSONObject.NULL)
                        objRes.put("collection_symbol", collection_symbol ?: JSONObject.NULL)
                        objRes.put("collection_creator", collection_creator ?: JSONObject.NULL)
                        objRes.put("collection_timestamp", deployment_date ?: JSONObject.NULL)
                        objRes.put("collection_total_supply", total_supply ?: JSONObject.NULL)
                        objRes.put("nft_type", nft_type ?: JSONObject.NULL)
                        objRes.put("minted_timestamp", minted_time ?: JSONObject.NULL)
                        objRes.put("block_number", block_number ?: JSONObject.NULL)
                        objRes.put("owner", owner_account ?: JSONObject.NULL)
                        objRes.put("token_id", token_id ?: JSONObject.NULL)
                        objRes.put("token_balance", balance ?: JSONObject.NULL)
                        objRes.put("token_uri", token_uri ?: JSONObject.NULL)
                        objRes.put("name", nft_name ?: JSONObject.NULL)
                        objRes.put("description", description ?: JSONObject.NULL)
                        objRes.put("image", image_url ?: JSONObject.NULL)
                        objRes.put("external_url", external_url ?: JSONObject.NULL)
                        objRes.put("attributes", attribute ?: JSONObject.NULL)
                        objRes.put("metadata", token_info ?: JSONObject.NULL)

                        nftArray.put(objRes)
                    }
                }
                catch (ex: SQLException){
                    ex.printStackTrace()
                }
                finally {
                    getNFT.close()
                }
            }
            //sum 출력
            if(getSum != null){
                try {
                    while (getSum.next()) {
                        sum = getSum.getInt("sum")
                        //nftData.put("sum", sum)
                    }
                }
                catch (ex: SQLException){
                    ex.printStackTrace()
                }
                finally {
                    getSum.close()
                }
            }
        }
        dbConnector.disconnect()

        val limit: Int? = limit
        val page_count: Int? = if (sum != null && limit != null) {
            Math.ceil(sum.toDouble() / limit.toDouble()).toInt()
        } else {
            0
        }

        nftData.put("result", "OK")
        nftData.put("sum", sum)
        val sort = sort ?: "desc" // 기본값을 "default_value"로 지정하거나 원하는 대체값을 사용하세요.
        nftData.put("sort", sort)
        nftData.put("page_count", page_count)
        nftData.put("value", nftArray)
    }
    catch (e: Exception) {
        nftData.put("result", "FAIL")
        nftData.put("error", e.message)
    }
}
suspend fun getNFTsByWalletArray(
    network: Array<String>,
    account: Array<String> ?= null,
    collection_id: String ?= null,
    sort: String ?= null,
    limit: Int ?= null,
    page_number: Int ?= null
): JSONObject = withContext(Dispatchers.IO) {

    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val nftArray = JSONArray() // { ..., value : [ nftArray ]}
    val nftData = JSONObject() // { "result": OK, "sum": 1, "sort": "desc", "page_count": 1, "value" : nftArray }

    val net = network.joinToString("','", "'", "'")
    val acc = account?.joinToString("','", "'", "'")

//    var offset = limit?.let { (page_number?.minus(1))?.times(it) }
    var offset = if (page_number != null && limit != null) {
        (page_number - 1) * limit
    } else {
        0 // 또는 적절한 기본값 설정
    }
    if(page_number==null || page_number==0 || page_number==1){
        offset = 0
    }
    var strQuery =
        "SELECT" +
                " owner.network AS network," +
                " collection.collection_id AS collection_id," +
                " collection.collection_name AS collection_name," +
                " collection.collection_symbol AS collection_symbol," +
                " collection.creator AS creator," +
                " collection.deployment_date AS deployment_date," +
                " collection.total_supply AS total_supply," +
                " token.nft_type AS nft_type," +
                " token.minted_time AS minted_time," +
                " token.block_number AS block_number," +
                " owner.owner_account AS owner_account," +
                " token.token_id AS token_id," +
                " owner.balance AS balance," +
                " token.token_uri AS token_uri," +
                " token.nft_name AS nft_name," +
                " token.description AS description," +
                " token.image_url AS image_url," +
                " token.external_url AS external_url," +
                " token.attribute AS attribute," +
                " token.token_info AS token_info" +
                " FROM " +
                "nft_owner_table AS owner" +
                " JOIN " +
                "nft_token_table AS token " +
                "ON " +
                "owner.collection_id = token.collection_id " +
                "AND " +
                "owner.token_id = token.token_id " +
                "AND " +
                "owner.network = token.network" +
                " JOIN " +
                "nft_collection_table AS collection " +
                "ON " +
                "token.collection_id = collection.collection_id " +
                "AND " +
                "token.network = collection.network " +
                "WHERE " +
                "owner.network IN (${net}) " +
                "AND " +
                "owner.balance != '0'"
    if (account != null) {
        strQuery += " AND owner.owner_account IN ($acc)"
    }
    if (collection_id != null) {
        strQuery += " AND owner.collection_id = '$collection_id'"
    }
    strQuery += " AND NOT EXISTS ( SELECT 1 FROM nft_hide_table AS hide JOIN users_table AS users ON users.owner_eigenvalue = hide.owner WHERE hide.network = owner.network AND hide.account = owner.owner_account AND owner.owner_account = users.user_account AND hide.token_id = owner.token_id AND hide.collection_id = owner.collection_id AND hide.owner = users.owner_eigenvalue)"
    strQuery += " ORDER BY token.block_number"
    if (sort == "asc") {
        strQuery += " asc"
    } else {
        strQuery += " desc"
    }
    strQuery += ", CAST(token.token_id AS SIGNED) desc"
    if (limit != null) {
        strQuery += " LIMIT $limit OFFSET $offset"
    }
    println(strQuery)

    var sumQuery =
        "SELECT " +
                " count(*) AS sum" +
                " FROM" +
                " nft_owner_table AS owner" +
                " JOIN" +
                " nft_token_table AS token" +
                " ON" +
                " owner.collection_id = token.collection_id" +
                " AND" +
                " owner.token_id = token.token_id" +
                " AND" +
                " owner.network = token.network" +
                " JOIN" +
                " nft_collection_table AS collection" +
                " ON" +
                " token.collection_id = collection.collection_id" +
                " AND" +
                " token.network = collection.network" +
                " WHERE" +
                " owner.network IN ($net)" +
                " AND" +
                " owner.balance != '0'"
    if (account != null) {
        sumQuery += " AND owner.owner_account IN ($acc) "
    }
    if (collection_id != null) {
        sumQuery += " AND owner.collection_id = '$collection_id' "
    }
    sumQuery += " AND NOT EXISTS ( SELECT 1 FROM nft_hide_table AS hide JOIN users_table AS users ON users.owner_eigenvalue = hide.owner WHERE hide.network = owner.network AND hide.account = owner.owner_account AND owner.owner_account = users.user_account AND hide.token_id = owner.token_id AND hide.collection_id = owner.collection_id AND hide.owner = users.owner_eigenvalue)"

    println(sumQuery)
    try{
        var sum: Int? = null
        if ((account==null && collection_id==null) || (limit == null && page_number != null)) {
            throw Exception() // 예외 발생
        }
        if (connection != null) {
            val dbQueryExector = DBQueryExector(connection)
            val getNFT: ResultSet? = dbQueryExector.executeQuery(strQuery)
            val getSum: ResultSet? = dbQueryExector.executeQuery(sumQuery)
            if (getNFT != null) {
                try {
                    while (getNFT.next()) {
                        val objRes = JSONObject()

                        val network = getNFT.getString("network")
                        val collection_id = getNFT.getString("collection_id")
                        val collection_name = getNFT.getString("collection_name")
                        val collection_symbol = getNFT.getString("collection_symbol")
                        val collection_creator = getNFT.getString("creator")
                        val deployment_date = getNFT.getInt("deployment_date")
                        val total_supply = getNFT.getString("total_supply")
                        val nft_type = getNFT.getString("nft_type")
                        val minted_time = getNFT.getInt("minted_time")
                        val block_number = getNFT.getInt("block_number")
                        val owner_account = getNFT.getString("owner_account")
                        val token_id = getNFT.getString("token_id")
                        val balance = getNFT.getString("balance")
                        val token_uri = getNFT.getString("token_uri")
                        val nft_name = getNFT.getString("nft_name")
                        val description = getNFT.getString("description")
                        val image_url = getNFT.getString("image_url")
                        val external_url = getNFT.getString("external_url")
                        val attribute = getNFT.getString("attribute")
                        val token_info = getNFT.getString("token_info")

//                        val replace_attributes = JSONArray(attribute ?: "[]")
//                        val replace_metadata = JSONObject(token_info ?: "{}")

                        objRes.put("network", network)
                        objRes.put("collection_id", collection_id ?: JSONObject.NULL)
                        objRes.put("collection_name", collection_name ?: JSONObject.NULL)
                        objRes.put("collection_symbol", collection_symbol ?: JSONObject.NULL)
                        objRes.put("collection_creator", collection_creator ?: JSONObject.NULL)
                        objRes.put("collection_timestamp", deployment_date ?: JSONObject.NULL)
                        objRes.put("collection_total_supply", total_supply ?: JSONObject.NULL)
                        objRes.put("nft_type", nft_type ?: JSONObject.NULL)
                        objRes.put("minted_timestamp", minted_time ?: JSONObject.NULL)
                        objRes.put("block_number", block_number ?: JSONObject.NULL)
                        objRes.put("owner", owner_account ?: JSONObject.NULL)
                        objRes.put("token_id", token_id ?: JSONObject.NULL)
                        objRes.put("token_balance", balance ?: JSONObject.NULL)
                        objRes.put("token_uri", token_uri ?: JSONObject.NULL)
                        objRes.put("name", nft_name ?: JSONObject.NULL)
                        objRes.put("description", description ?: JSONObject.NULL)
                        objRes.put("image", image_url ?: JSONObject.NULL)
                        objRes.put("external_url", external_url ?: JSONObject.NULL)
                        objRes.put("attributes", attribute ?: JSONObject.NULL)
                        objRes.put("metadata", token_info ?: JSONObject.NULL)

                        nftArray.put(objRes)
                    }
                }
                catch (ex: SQLException){
                    ex.printStackTrace()
                }
                finally {
                    getNFT.close()
                }
            }
            //sum 출력
            if(getSum != null){
                try {
                    while (getSum.next()) {
                        sum = getSum.getInt("sum")
                        //nftData.put("sum", sum)
                    }
                }
                catch (ex: SQLException){
                    ex.printStackTrace()
                }
                finally {
                    getSum.close()
                }
            }
        }
        dbConnector.disconnect()

        val limit: Int? = limit
        val page_count: Int? = if (sum != null && limit != null) {
            Math.ceil(sum.toDouble() / limit.toDouble()).toInt()
        } else {
            0
        }

        nftData.put("result", "OK")
        nftData.put("sum", sum)
        val sort = sort ?: "desc" // 기본값을 "default_value"로 지정하거나 원하는 대체값을 사용하세요.
        nftData.put("sort", sort)
        nftData.put("page_count", page_count)
        nftData.put("value", nftArray)
    }
    catch (e: Exception) {
        nftData.put("result", "FAIL")
        nftData.put("error", e.message)
    }
}

//getNFTTransaction
@SuppressLint("SuspiciousIndentation")
suspend fun getNFTsTransferHistory(
    network: String,
    collection_id: String ?= null,
    token_id: String ?= null,
    type: String ?= null,
    sort: String ?= null,
    limit: Int ?= null,
    page_number: Int ?= null
) : JSONObject = withContext(Dispatchers.IO){

    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val transactionArray = JSONArray()
    val transferData = JSONObject()

    var offset = if (page_number != null && limit != null) {
        (page_number - 1) * limit
    } else {
        0 // 또는 적절한 기본값 설정
    }
    if(page_number==null || page_number==0 || page_number==1){
        offset = 0
    }

    var transferQuery =
        " SELECT" +
                " transfer.network AS network," +
                " sales.buyer_account AS buyer_account," +
                " transfer.`from` AS from_address," +
                " transfer.`to` AS to_address," +
                " transfer.collection_id AS collection_id," +
                " transfer.block_number AS block_number," +
                " transfer.`timestamp` AS timestamp," +
                " transfer.transaction_hash AS transaction_hash," +
                " transfer.log_id AS log_id," +
                " transfer.token_id AS token_id," +
                " transfer.amount AS amount," +
                " sales.currency AS currency," +
                " sales.currency_symbol AS currency_symbol," +
                " sales.decimals AS decimals," +
                " sales.price AS price," +
                " sales.market AS market," +
                " sales.sales_info AS sales_info," +
                " CASE" +
                " WHEN" +
                " sales.sales_info IS NOT NULL THEN 'sales'" +
                " ELSE" +
                " 'transfer'" +
                " END AS" +
                " transaction_type" +
                " FROM" +
                " nft_transfer_table AS transfer" +
                " LEFT OUTER JOIN" +
                " nft_sales_table AS sales" +
                " ON" +
                " transfer.transaction_hash = sales.transaction_hash" +
                " LEFT JOIN" +
                " nft_transaction_type_table AS type" +
                " ON" +
                " transfer.transaction_hash = type.transaction_hash" +
                " WHERE" +
                " transfer.network = '${network}'"
    if(token_id != null){
        transferQuery += " AND transfer.token_id = '${token_id}' "
    }
    if(collection_id != null){
        transferQuery += " AND transfer.collection_id= '${collection_id}' "
    }
    if(type=="transfer"){
        transferQuery += "AND type.transaction_type = 'transfer' ORDER BY transfer.block_number"
    }
    else if(type=="sales"){
        transferQuery += "AND type.transaction_type = 'sales' ORDER BY transfer.block_number"
    }
    else{
        transferQuery += " ORDER BY transfer.block_number"
    }
    if(sort == "asc"){
        transferQuery += " asc"
    } else {
        transferQuery += " desc"
    }
    transferQuery += ", CAST(transfer.token_id AS SIGNED) desc"
    if (limit != null) {
        transferQuery += " LIMIT ${limit} OFFSET ${offset}"
    }
    println(transferQuery)

    var sumQuery =
        "SELECT" +
                " count(*) AS sum" +
                " FROM " +
                " nft_transfer_table AS transfer" +
                " LEFT JOIN " +
                " nft_sales_table AS sales" +
                " ON " +
                " transfer.transaction_hash = sales.transaction_hash" +
                " LEFT JOIN " +
                " nft_transaction_type_table AS type" +
                " ON " +
                " transfer.transaction_hash = type.transaction_hash" +
                " WHERE " +
                " transfer.network = '$network'"
    if (token_id != null) {
        sumQuery += " AND transfer.token_id = '$token_id' "
    }
    if (collection_id != null) {
        sumQuery += " AND transfer.collection_id = '$collection_id' "
    }
    if(type != null){
        sumQuery += " AND type.transaction_type = '${type}'"
    }
    println(sumQuery)
    try {
        var sum: Int? = null
        if ((token_id == null && collection_id == null) || (limit == null && page_number != null)) {
            throw Exception() // 예외 발생
        }
        if (connection != null) {
            val dbQueryExector = DBQueryExector(connection)
            val getTransaction1: ResultSet? = dbQueryExector.executeQuery(transferQuery)
            val getSum: ResultSet? = dbQueryExector.executeQuery(sumQuery)
            if (getTransaction1 != null) {
                try {
                    while (getTransaction1.next()) {
                        val jsonData = JSONObject()
                        // Select data = network, from, to, collection_id, block_number, timestamp, transaction_hash, log_id, token_id, amount, transaction_type
                        val network = getTransaction1.getString("network")
                        val buyer_account = getTransaction1.getString("buyer_account")
                        val from_address = getTransaction1.getString("from_address")
                        val to_address = getTransaction1.getString("to_address")
                        val collection_id = getTransaction1.getString("collection_id")
                        val block_number = getTransaction1.getInt("block_number")
                        val timestamp = getTransaction1.getInt("timestamp")
                        val transaction_hash = getTransaction1.getString("transaction_hash")
                        val log_id = getTransaction1.getString("log_id")
                        val token_id = getTransaction1.getString("token_id")
                        val amount = getTransaction1.getString("amount")
                        val currency = getTransaction1.getString("currency")
                        val currency_symbol = getTransaction1.getString("currency_symbol")
                        val decimals = getTransaction1.getInt("decimals")
                        val price = getTransaction1.getString("price")
                        val market = getTransaction1.getString("market")
                        val sales_info = getTransaction1.getString("sales_info")
                        val transaction_type = getTransaction1.getString("transaction_type")

                        val replace_sales_info = JSONArray(sales_info ?: "[]")

                        jsonData.put("network", network)
                        jsonData.put("buyer", buyer_account)
                        jsonData.put("from", from_address)
                        jsonData.put("to", to_address)
                        jsonData.put("collection_id", collection_id)
                        jsonData.put("block_number", block_number)
                        jsonData.put("timestamp", timestamp)
                        jsonData.put("transaction_hash", transaction_hash)
                        jsonData.put("log_id", log_id)
                        jsonData.put("token_id", token_id)
                        jsonData.put("amount", amount)
                        jsonData.put("currency", currency)
                        jsonData.put("currency_symbol", currency_symbol)
                        jsonData.put("decimals", decimals)
                        jsonData.put("price", price)
                        jsonData.put("market", market)
                        jsonData.put("sales_info", replace_sales_info)
                        jsonData.put("type", transaction_type)

                        transactionArray.put(jsonData)
                    }
                }
                catch (ex: SQLException) {
                    ex.printStackTrace()
                } finally {
                    getTransaction1.close()
                }
            }
            if(getSum != null){
                try {
                    while (getSum.next()) {
                        sum = getSum.getInt("sum")
                    }
                }
                catch (ex: SQLException){
                    ex.printStackTrace()
                }
                finally {
                    getSum.close()
                }
            }
        }
        dbConnector.disconnect()

        val limit: Int? = limit
        val page_count: Int? = if (sum != null && limit != null) {
            Math.ceil(sum.toDouble() / limit.toDouble()).toInt()
        } else {
            0
        }

        transferData.put("result", "OK")
        transferData.put("sum", sum)
        val sort = sort ?: "desc" // 기본값을 "default_value"로 지정하거나 원하는 대체값을 사용하세요.
        transferData.put("sort", sort)
        transferData.put("page_count", page_count)
        transferData.put("value", transactionArray)
    }
    catch (e: Exception){
        transferData.put("result", "FAIL")
        transferData.put("error", e.message)
    }
}
//숨김테이블 조회
suspend fun getNFTsHide(
    network: Array<String>,
    owner: String,
    sort: String ?= null,
    limit: Int ?= null,
    page_number: Int ?= null
): JSONObject = withContext(Dispatchers.IO){
    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val hideData = JSONObject()
    val hideArray = JSONArray()

    val net = network.joinToString("','", "'", "'")

    var offset = if (page_number != null && limit != null) {
        (page_number - 1) * limit
    } else {
        0 // 또는 적절한 기본값 설정
    }
    if(page_number==null || page_number==0 || page_number==1){
        offset = 0
    }

    var hideQuery =
        "SELECT " +
                "hide.network AS network, " +
                "hide.owner AS owner, " +
                "hide.account AS account, " +
                "hide.collection_id AS collection_id, " +
                "hide.token_id AS token_id, " +
                "hide.image_url AS image_url, " +
                "hide.collection_name AS collection_name, " +
                "hide.nft_name AS nft_name " +
                "FROM " +
                "nft_hide_table AS hide " +
                "WHERE " +
                "hide.network IN (${net}) " +
                "AND " +
                "hide.owner = '${owner}'"
    hideQuery += " ORDER BY idx"
    if (sort == "asc") {
        hideQuery += " asc"
    } else {
        hideQuery += " desc"
    }

    if (limit != null) {
        hideQuery += " LIMIT $limit OFFSET $offset"
    }

    var sumQuery =
        "SELECT" +
                " count(*) AS sum" +
                " FROM " +
                " nft_hide_table" +
                " WHERE " +
                "network IN (${net}) " +
                "AND " +
                "owner = '${owner}'"
    println(sumQuery)
    print(hideQuery)

    try {
        var sum: Int? = null

        if (connection != null) {
            val dbQueryExector = DBQueryExector(connection)
            val getTransaction1: ResultSet? = dbQueryExector.executeQuery(hideQuery)
            val getSum: ResultSet? = dbQueryExector.executeQuery(sumQuery)
            if (getTransaction1 != null) {
                try {
                    while (getTransaction1.next()) {
                        val jsonData = JSONObject()

                        val network = getTransaction1.getString("network")
                        val owner = getTransaction1.getString("owner")
                        val account = getTransaction1.getString("account")
                        val collection_id = getTransaction1.getString("collection_id")
                        val token_id = getTransaction1.getString("token_id")
                        val collection_name = getTransaction1.getString("collection_name")
                        val image_url = getTransaction1.getString("image_url")
                        val nft_name = getTransaction1.getString("nft_name")

                        jsonData.put("network", network)
                        jsonData.put("owner", owner)
                        jsonData.put("account", account)
                        jsonData.put("collection_id", collection_id)
                        jsonData.put("token_id", token_id)
                        jsonData.put("collection_name", collection_name)
                        jsonData.put("image", image_url)
                        jsonData.put("name", nft_name)

                        hideArray.put(jsonData)
                    }
                }
                catch (ex: SQLException) {
                    ex.printStackTrace()
                } finally {
                    getTransaction1.close()
                }
            }
            if(getSum != null){
                try {
                    while (getSum.next()) {
                        sum = getSum.getInt("sum")
                    }
                }
                catch (ex: SQLException){
                    ex.printStackTrace()
                }
                finally {
                    getSum.close()
                }
            }
        }
        dbConnector.disconnect()

        val limit: Int? = limit
        val page_count: Int? = if (sum != null && limit != null) {
            Math.ceil(sum.toDouble() / limit.toDouble()).toInt()
        } else {
            0
        }

        hideData.put("result", "OK")
        hideData.put("sum", sum)
        val sort = sort ?: "desc" // 기본값을 "default_value"로 지정하거나 원하는 대체값을 사용하세요.
        hideData.put("sort", sort)
        hideData.put("page_count", page_count)
        hideData.put("value", hideArray)
    }
    catch (e: Exception){
        hideData.put("result", "FAIL")
        hideData.put("error", e.message)
    }
}

suspend fun sendNFT721TransactionAsync(
    network: String,
    fromAddress: String,
    toAddress: String,
    tokenId: String,
    nftContractAddress: String
): JSONObject = withContext(Dispatchers.IO){
    networkSettings(network)
    val jsonData = JSONObject()

    try {
        val getAddressInfo = getAccountInfoAsync(fromAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            println("Error while fetching the private key: ${it.message}")
            null
        }

        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

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
                //1gwei
                BigInteger("33000000000"),
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
    networkSettings(network)
    try {
        val getAddressInfo = getAccountInfoAsync(fromAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            println("Error while fetching the private key: ${it.message}")
            null
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

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
                //1gwei
                BigInteger("33000000000"),
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

suspend fun sendNFT721BatchTransactionAsync(
    network: String,
    fromAddress: String,
    toAddress: String,
    tokenId: Array<String>,
    nftContractAddress: String
): JSONObject = withContext(Dispatchers.IO) {
    val jsonData = JSONObject()
    networkSettings(network)
    try {
        val getAddressInfo = getAccountInfoAsync(fromAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            println("Error while fetching the private key: ${it.message}")
            null
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val ethGasPrice = web3j.ethGasPrice().sendAsync().get()

        val batchTokenId = tokenId.map { Uint256(BigInteger(it)) }

        val function = Function(
            "safeBatchTransferFrom",
            listOf(
                Address(fromAddress), Address(toAddress), DynamicArray(batchTokenId)
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
                    "batchTransferERC721",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    null,
                    null,
                    null,
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
                    "batchTransferERC721",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    null,
                    null,
                    null,
                    null,
                    tokenId
                ), // Add 20% to the gas limit
                nftContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //1gwei
                BigInteger("33000000000"),
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

    networkSettings(network)
    val jsonData = JSONObject()

    try {
        val getAddressInfo = getAccountInfoAsync(fromAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            println("Error while fetching the private key: ${it.message}")
            null
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

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
                //1gwei
                BigInteger("33000000000"),
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
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    try {
        val getAddressInfo = getAccountInfoAsync(fromAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            println("Error while fetching the private key: ${it.message}")
            null
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val function = Function(
            "deployedERC721",
            listOf(
                Utf8String(name),
                Utf8String(symbol),
                Utf8String(baseURI),
                Uint8(BigInteger(uriType)),
                Address(owner)
            ),
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
                erc721DeployContractAddress,
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
                erc721DeployContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger("33000000000"),
                getEstimateGas(network, "baseFee")
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val txHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        jsonData.put("result", "OK")
        jsonData.put("transactionHash", txHash)
    } catch (e: Exception) {
        jsonData.put("result", "FAIL")
        jsonData.put("error", e.message)
    }
}

suspend fun deployErc1155Async(
    network: String,
    fromAddress: String,
    name: String,
    symbol: String,
    baseURI: String,
    owner: String,
    uriType: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    try {
        val getAddressInfo = getAccountInfoAsync(fromAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            println("Error while fetching the private key: ${it.message}")
            null
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val function = Function(
            "deployedERC1155",
            listOf(
                Utf8String(name),
                Utf8String(symbol),
                Utf8String(baseURI),
                Uint8(BigInteger(uriType)),
                Address(owner)
            ),
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
                    "deployERC1155",
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
                erc1155DeployContractAddress,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                getEstimateGas(
                    network,
                    "deployERC1155",
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
                erc1155DeployContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger("33000000000"),
                getEstimateGas(network, "baseFee")
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val txHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        jsonData.put("result", "OK")
        jsonData.put("transactionHash", txHash)
    } catch (e: Exception) {
        jsonData.put("result", "FAIL")
        jsonData.put("error", e.message)
    }
}


suspend fun mintErc721Async(
    network: String,
    fromAddress: String,
    toAddress: String,
    tokenURI: String,
    tokenId: String,
    nftContractAddress: String
): JSONObject = withContext(Dispatchers.IO){
    networkSettings(network)
    val jsonData = JSONObject()

    try {
        val getAddressInfo = getAccountInfoAsync(fromAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            println("Error while fetching the private key: ${it.message}")
            null
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val function = Function(
            "mint",
            listOf(Address(toAddress), Uint256(BigInteger(tokenId)), Utf8String(tokenURI)),
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
                    "mintERC721",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    tokenId,
                    null, null, null, null, null, null, null, null, null, null,
                    tokenURI
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
                    "mintERC721",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    tokenId,
                    null, null, null, null, null, null, null, null, null, null,
                    tokenURI
                ), // Add 20% to the gas limit
                nftContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger("33000000000"),
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

suspend fun mintErc1155Async(
    network: String,
    fromAddress: String,
    toAddress: String,
    tokenURI: String,
    tokenId: String,
    nftContractAddress: String,
    amount: String
): JSONObject = withContext(Dispatchers.IO){
    networkSettings(network)
    val jsonData = JSONObject()

    try {
        val getAddressInfo = getAccountInfoAsync(fromAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            println("Error while fetching the private key: ${it.message}")
            null
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val function = Function(
            "mint",
            listOf(Address(toAddress), Uint256(BigInteger(tokenId)), Uint256(BigInteger(amount)), Utf8String(tokenURI), DynamicBytes(byteArrayOf(0))),
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
                    "mintERC1155",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    amount,
                    tokenId,
                    null, null, null, null, null, null, null, null, null, null,
                    tokenURI
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
                    "mintERC1155",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    amount,
                    tokenId,
                    null, null, null, null, null, null, null, null, null, null,
                    tokenURI
                ), // Add 20% to the gas limit
                nftContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger("33000000000"),
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

suspend fun batchMintErc721Async(
    network: String,
    fromAddress: String,
    toAddress: String,
    tokenURI: Array<String>,
    tokenId: Array<String>,
    nftContractAddress: String
): JSONObject = withContext(Dispatchers.IO){
    networkSettings(network)
    val jsonData = JSONObject()

    try {
        val getAddressInfo = getAccountInfoAsync(fromAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            println("Error while fetching the private key: ${it.message}")
            null
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val batchTokenId = tokenId.map { Uint256(BigInteger(it)) }
        val batchTokenURI = tokenURI.map { Utf8String(it) }

        val function = Function(
            "mintBatch",
            listOf(Address(toAddress), DynamicArray(batchTokenId), DynamicArray(batchTokenURI), DynamicBytes(byteArrayOf(0))),
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
                    "batchMintERC721",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    null,
                    null, null, null, tokenId, null, null, null, null, null, null,
                    null, tokenURI
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
                    "batchMintERC721",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    null,
                    null, null, null, tokenId, null, null, null, null, null, null,
                    null, tokenURI
                ), // Add 20% to the gas limit
                nftContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger("33000000000"),
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
suspend fun batchMintErc1155Async(
    network: String,
    fromAddress: String,
    toAddress: String,
    tokenURI: Array<String>,
    tokenId: Array<String>,
    nftContractAddress: String,
    amount: Array<String>
): JSONObject = withContext(Dispatchers.IO){
    networkSettings(network)
    val jsonData = JSONObject()

    try {
        val getAddressInfo = getAccountInfoAsync(fromAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            println("Error while fetching the private key: ${it.message}")
            null
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val batchTokenId = tokenId.map { Uint256(BigInteger(it)) }
        val batchAmount = amount.map { Uint256(BigInteger(it)) }
        val batchTokenURI = tokenURI.map { Utf8String(it) }

        val function = Function(
            "mintBatch",
            listOf(Address(toAddress), DynamicArray(batchTokenId), DynamicArray(batchAmount), DynamicArray(batchTokenURI), DynamicBytes(byteArrayOf(0))),
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
                    "batchMintERC1155",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    null,
                    null, null, null, tokenId, amount, null, null, null, null, null,
                    null, tokenURI
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
                    "batchMintERC1155",
                    nftContractAddress,
                    fromAddress,
                    toAddress,
                    null,
                    null,
                    null, null, null, tokenId, amount, null, null, null, null, null,
                    null, tokenURI
                ), // Add 20% to the gas limit
                nftContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger("33000000000"),
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
