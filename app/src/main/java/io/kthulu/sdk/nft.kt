package io.kthulu.sdk

import android.annotation.SuppressLint
import kotlinx.coroutines.*
import kotlinx.coroutines.future.await
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.*
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Bytes4
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.crypto.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.core.methods.response.TransactionReceipt
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Numeric
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URL
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Statement
import java.util.*


suspend fun getMintableAddress(
    owner: Array<String>
): JSONObject = withContext(Dispatchers.IO) {
    // return array & object
    var resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val dbConnector = DBConnector()
        dbConnector.connect()
        val connection = dbConnector.getConnection()

        val own = owner?.joinToString("','", "'", "'")

        val CAQuery =
            "SELECT " +
                "network, " +
                "collection_id, " +
                "collection_name, " +
                "collection_symbol, " +
                "nft_type, " +
                "owner, " +
                "base_uri " +
            "FROM " +
                "nft_collection_table " +
            "WHERE " +
                " owner IN (${own})"

        println("CAQuery: $CAQuery")

        if (connection != null) {
            val dbQueryExector = DBQueryExector(connection)
            val getCA: ResultSet? = dbQueryExector.executeQuery(CAQuery)

            if (getCA != null) {
                try {
                    while (getCA.next()) {
                        val jsonData = JSONObject()
                        val network = getCA.getString("network")
                        val collection_id = getCA.getString("collection_id")
                        val collection_name = getCA.getString("collection_name")
                        val collection_symbol = getCA.getString("collection_symbol")
                        val nft_type = getCA.getString("nft_type")
                        val owner = getCA.getString("owner")
                        val base_uri = getCA.getString("base_uri")


                        jsonData.put("network", network)
                        jsonData.put("collection_id", collection_id)
                        jsonData.put("collection_name", collection_name)
                        jsonData.put("collection_symbol", collection_symbol)
                        jsonData.put("nft_type", nft_type)
                        jsonData.put("owner", owner)
                        jsonData.put("base_uri", base_uri)

                        resultArray.put(jsonData)
                    }
                } catch (ex: SQLException) {
                    ex.printStackTrace()
                } finally {
                    getCA.close()
                }
            }
        }
        dbConnector.disconnect()

        resultData.put("result", "OK")
        resultData.put("value", resultArray)
    } catch (e: Exception) {
        resultData.put("result", "FAIL")
        resultData.put("error", e.message)
    }
}

suspend fun setNFTsHide(
    network: String,
    account: String,
    collection_id: String,
    token_id: String
): JSONObject = withContext(Dispatchers.IO) {

    val jsonData = JSONObject()

    // return array & object
    var resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")

    try {
        val dbConnector = DBConnector()
        dbConnector.connect()
        val connection = dbConnector.getConnection()
        val insertQuery =
            "INSERT INTO " +
                    "nft_hide_table (network, account, collection_id, token_id, image_url, nft_name) " +
                    "SELECT " +
                    "'${network}', " +
                    "'${account}', " +
                    "'${collection_id}', " +
                    "'${token_id}', " +
                    "token.image_url AS image_url, " +
                    "token.nft_name AS nft_name " +
                    "FROM " +
                    "nft_token_table AS token " +
                    "WHERE " +
                    "token.network = '${network}' " +
                    "AND " +
                    "token.collection_id = '${collection_id}' " +
                    "AND " +
                    "token.token_id = '${token_id}'"

        val statement: Statement = connection!!.createStatement()
        val rowsAffected = statement.executeUpdate(insertQuery)
        if (rowsAffected > 0) {
            resultData.put("result", "OK")
        } else {
            resultArray = JSONArray()
            jsonData.put("error", "nft does not exist to hide")
            resultArray.put(jsonData)
            resultData.put("value", resultArray)
        }

    } catch (e: SQLException) {
        resultArray = JSONArray()
        if (e.message?.contains("Duplicate entry") == true) {
            jsonData.put("error", "Duplicate entry")
        } else if (e.message?.contains("Table not found") == true) {
            jsonData.put("error", "Table not found")
        } else if (e.message?.contains("Column not found") == true) {
            jsonData.put("error", "Column not found")
        } else if (e.message?.contains("Connection timeout") == true) {
            jsonData.put("error", "Connection timeout")
        } else if (e.message?.contains("Connection refused") == true) {
            jsonData.put("error", "Connection refused")
        } else {
            jsonData.put("error", e.message)
        }
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
        return@withContext resultData
    }
}

suspend fun deleteNFTsHide(
    network: String,
    account: String,
    collection_id: String,
    token_id: String
): JSONObject = withContext(Dispatchers.IO) {
    val jsonData = JSONObject()

    // return array & object
    var resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")

    try {
        val dbConnector = DBConnector()
        dbConnector.connect()
        val connection = dbConnector.getConnection()
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
                    "token_id = '$token_id' "


        val statement: Statement = connection!!.createStatement()
        val rowsAffected = statement.executeUpdate(deleteQuery)

        if (rowsAffected > 0) {
            resultData.put("result", "OK")
        } else {
            resultArray = JSONArray()
            jsonData.put("error", "nft does not exist to cancel")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        resultArray = JSONArray()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
        return@withContext resultData
    }
}

@SuppressLint("SuspiciousIndentation")
suspend fun getNFTsByWallet(
    network: Array<String>,
    account: String? = null,
    collection_id: String? = null,
    sort: String? = null,
    limit: Int? = null,
    page_number: Int? = null
): JSONObject = withContext(Dispatchers.IO) {
    var resultArray = JSONArray() // { ..., value : [ nftArray ]}
    var resultData = JSONObject() // { "result": OK, "sum": 1, "sort": "desc", "page_count": 1, "value" : nftArray }

    try {
        val dbConnector = DBConnector()
        dbConnector.connect()
        val connection = dbConnector.getConnection()


        val net = network.joinToString("','", "'", "'")

        // var offset = limit?.let { (page_number?.minus(1))?.times(it) }
        val offset = if (page_number != null && limit != null) {
            (page_number - 1) * limit
        } else {
            0
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
                    " token.image_url AS image_url," +
                    " token.external_url AS external_url," +
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
        strQuery += " AND NOT EXISTS ( SELECT 1 FROM nft_hide_table AS hide WHERE hide.network = owner.network AND hide.account = owner.owner_account AND hide.token_id = owner.token_id AND hide.collection_id = owner.collection_id)"
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

        var sumQuery =
            "SELECT count(*) AS sum " +
                    "FROM nft_owner_table AS owner " +
                    "JOIN nft_token_table AS token " +
                    "ON owner.collection_id = token.collection_id " +
                    "AND owner.token_id = token.token_id " +
                    "AND owner.network = token.network " +
                    "JOIN nft_collection_table AS collection " +
                    "ON token.collection_id = collection.collection_id " +
                    "AND token.network = collection.network " +
                    "WHERE owner.network IN ($net) " +
                    "AND owner.balance != '0'";

        if (account != null) {
            sumQuery += " AND owner.owner_account = '$account' ";
        }

        if (collection_id != null) {
            sumQuery += " AND owner.collection_id = '$collection_id' ";
        }

        sumQuery += " AND NOT EXISTS ( " +
                "SELECT 1 " +
                "FROM nft_hide_table AS hide " +   // Added a space after 'hide'
                "WHERE hide.network = owner.network " +
                "AND hide.account = owner.owner_account " +
                "AND hide.token_id = owner.token_id " +
                "AND hide.collection_id = owner.collection_id)";
        var sum: Int? = null
        if ((account == null && collection_id == null) || (limit == null && page_number != null)) {
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
                        val image_url = getNFT.getString("image_url")
                        val external_url = getNFT.getString("external_url")
                        val token_info = getNFT.getString("token_info")
                        var description: String? = null
                        var attribute: JSONArray? = null
                        if (!token_info.isNullOrBlank()) {
                            val metadataJSON = JSONObject(token_info)
                            description = metadataJSON.optString("description")
                            attribute = metadataJSON.optJSONArray("attributes")
                        }

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

                        resultArray.put(objRes)
                    }
                } catch (ex: SQLException) {
                    ex.printStackTrace()
                } finally {
                    getNFT.close()
                }
            }
            //sum 출력
            if (getSum != null) {
                try {
                    while (getSum.next()) {
                        sum = getSum.getInt("sum")
                        //nftData.put("sum", sum)
                    }
                } catch (ex: SQLException) {
                    ex.printStackTrace()
                } finally {
                    getSum.close()
                }
            }
        }
        dbConnector.disconnect()

        val limit: Int? = limit
        val page_count: Int? = if (sum != null && limit != null) {
            Math.ceil(sum.toDouble() / limit.toDouble()).toInt()
        } else {
            1
        }

        resultData.put("result", "OK")
        resultData.put("sum", sum)
        resultData.put("page_count", page_count)
        resultData.put("sort", sort)
        resultData.put("value", resultArray)

    } catch (e: Exception) {
        resultArray = JSONArray()
        val jsonData = JSONObject()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }

}

suspend fun getNFTsByWalletArray(
    network: Array<String>,
    account: Array<String>,
    collection_id: String? = null,
    sort: String? = null,
    limit: Int? = null,
    page_number: Int? = null
): JSONObject = withContext(Dispatchers.IO) {
    var resultArray = JSONArray()
    var jsonData = JSONObject()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("value", resultArray)
    }

    try {
        val url = URL("https://app.kthulu.io:3302/nft/getNftListAsync")
        val connection = url.openConnection() as HttpURLConnection

        // 1. 요청 방법을 "POST"로 변경합니다.
        connection.requestMethod = "POST"

        // 2. JSON 데이터를 전송하도록 설정합니다.
        connection.doOutput = true

        // 3. "Content-Type" 헤더를 추가합니다.
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

        // 여기에 보낼 JSON 데이터를 작성합니다. 예를 들어:
        val jsonPayload = JSONObject()
        jsonPayload.put("network", JSONArray(network))
        jsonPayload.put("account", JSONArray(account))
        jsonPayload.put("collection_id", collection_id)
        jsonPayload.put("sort", sort)
        jsonPayload.put("limit", limit)
        jsonPayload.put("page_number", page_number)

        val outputStreamWriter = OutputStreamWriter(connection.outputStream)
        outputStreamWriter.write(jsonPayload.toString())
        outputStreamWriter.flush()
        outputStreamWriter.close()

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val responseBody = reader.use { it.readText() }
            val jsonResponse = JSONObject(responseBody)

            return@withContext jsonResponse
        } else {
            throw Exception("HTTP error code: ${connection.responseCode}")
        }
    } catch (e: Exception) {
        resultArray = JSONArray()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

//getNFTTransaction
@SuppressLint("SuspiciousIndentation")
suspend fun getNFTsTransferHistory(
    network: String,
    collection_id: String,
    token_id: String? = null,
    sort: String? = null,
    limit: Int? = null,
    page_number: Int? = null
): JSONObject = withContext(Dispatchers.IO) {
    var resultArray = JSONArray()
    var jsonData = JSONObject()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("value", resultArray)
    }

    try {
        val url = URL("https://app.kthulu.io:3302/nft/getNftHistoryAsync")
        val connection = url.openConnection() as HttpURLConnection

        // 1. 요청 방법을 "POST"로 변경합니다.
        connection.requestMethod = "POST"

        // 2. JSON 데이터를 전송하도록 설정합니다.
        connection.doOutput = true

        // 3. "Content-Type" 헤더를 추가합니다.
        connection.setRequestProperty("Content-Type", "application/json; charset=UTF-8")

        // 여기에 보낼 JSON 데이터를 작성합니다. 예를 들어:
        val jsonPayload = JSONObject()
        jsonPayload.put("network", network)
        jsonPayload.put("collection_id", collection_id)
        jsonPayload.put("token_id", token_id)
        jsonPayload.put("sort", sort)
        jsonPayload.put("limit", limit)
        jsonPayload.put("page_number", page_number)

        val outputStreamWriter = OutputStreamWriter(connection.outputStream)
        outputStreamWriter.write(jsonPayload.toString())
        outputStreamWriter.flush()
        outputStreamWriter.close()

        if (connection.responseCode == HttpURLConnection.HTTP_OK) {
            val reader = BufferedReader(InputStreamReader(connection.inputStream))
            val responseBody = reader.use { it.readText() }
            val jsonResponse = JSONObject(responseBody)

            return@withContext jsonResponse
        } else {
            throw Exception("HTTP error code: ${connection.responseCode}")
        }
    } catch (e: Exception) {
        resultArray = JSONArray()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

//숨김테이블 조회
suspend fun getNFTsHide(
    network: Array<String>,
    account: Array<String>,
    sort: String? = null,
    limit: Int? = null,
    page_number: Int? = null
): JSONObject = withContext(Dispatchers.IO) {
    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    var hideData = JSONObject()
    var hideArray = JSONArray()

    val net = network.joinToString("','", "'", "'")
    val acc = account.joinToString("','", "'", "'")

    var offset = if (page_number != null && limit != null) {
        (page_number - 1) * limit
    } else {
        0 // 또는 적절한 기본값 설정
    }
    if (page_number == null || page_number == 0 || page_number == 1) {
        offset = 0
    }

    var hideQuery =
        "SELECT " +
                "hide.network AS network, " +
                "hide.account AS account, " +
                "hide.collection_id AS collection_id, " +
                "hide.token_id AS token_id, " +
                "hide.image_url AS image_url, " +
                "hide.nft_name AS nft_name " +
                "FROM " +
                "nft_hide_table AS hide " +
                "WHERE " +
                "hide.network IN (${net}) " +
                "AND " +
                "hide.account IN (${acc}) "
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
                "account IN (${acc}) "

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
                        val account = getTransaction1.getString("account")
                        val collection_id = getTransaction1.getString("collection_id")
                        val token_id = getTransaction1.getString("token_id")
                        val image_url = getTransaction1.getString("image_url")
                        val nft_name = getTransaction1.getString("nft_name")

                        jsonData.put("network", network)
                        jsonData.put("account", account)
                        jsonData.put("collection_id", collection_id)
                        jsonData.put("token_id", token_id)
                        jsonData.put("image", image_url)
                        jsonData.put("name", nft_name)

                        hideArray.put(jsonData)
                    }
                } catch (ex: SQLException) {
                    ex.printStackTrace()
                } finally {
                    getTransaction1.close()
                }
            }
            if (getSum != null) {
                try {
                    while (getSum.next()) {
                        sum = getSum.getInt("sum")
                    }
                } catch (ex: SQLException) {
                    ex.printStackTrace()
                } finally {
                    getSum.close()
                }
            }
        }
        dbConnector.disconnect()

        val limit: Int? = limit
        val page_count: Int? = if (sum != null && limit != null) {
            Math.ceil(sum.toDouble() / limit.toDouble()).toInt()
        } else {
            1
        }

        hideData.put("result", "OK")
        hideData.put("sum", sum)
        val sort = sort ?: "desc" // 기본값을 "default_value"로 지정하거나 원하는 대체값을 사용하세요.
        hideData.put("sort", sort)
        hideData.put("page_count", page_count)
        hideData.put("value", hideArray)
    } catch (e: Exception) {
        hideArray = JSONArray()
        hideData.put("error", e.message)
        hideArray.put(hideData)
        hideData.put("result", "FAIL")
        hideData.put("value", hideArray)
        return@withContext hideData
    }
}

suspend fun sendNFT721TransactionAsync(
    network: String,
    from: String,
    to: String,
    token_id: String,
    collection_id: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val getAddressInfo = getAccountInfoAsync(from)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }

        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val function = Function(
            "safeTransferFrom",
            listOf(Address(from), Address(to), Uint256(BigInteger(token_id))),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "transferERC721",
            collection_id,
            from,
            to,
            null,
            token_id
        )

        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice),
                BigInteger(gasLimit),// Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit),
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash =
            web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun sendNFT1155TransactionAsync(
    network: String,
    from: String,
    to: String,
    token_id: String,
    collection_id: String,
    amount: String,
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)
    try {
        val getAddressInfo = getAccountInfoAsync(from)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
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
                Address(from), Address(to), Uint256(BigInteger(token_id)),
                Uint256(BigInteger(amount)), DynamicBytes(byteArrayOf(0))
            ),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "transferERC1155",
            collection_id,
            from,
            to,
            amount,
            token_id
        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice),
                BigInteger(gasLimit),// Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit),
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun sendNFT721BatchTransactionAsync(
    network: String,
    from: String,
    to: String,
    token_id: Array<String>,
    collection_id: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)
    try {
        val getAddressInfo = getAccountInfoAsync(from)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val ethGasPrice = web3j.ethGasPrice().sendAsync().get()

        val batchTokenId = token_id.map { Uint256(BigInteger(it)) }

        val function = Function(
            "transferFromBatch",
            listOf(
                Address(from), Address(to), DynamicArray(batchTokenId)
            ),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "batchTransferERC721",
            collection_id,
            from,
            to,
            null,
            null,
            null,
            null,
            token_id
        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice),
                BigInteger(gasLimit),// Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun sendNFT1155BatchTransactionAsync(
    network: String,
    from: String,
    to: String,
    token_id: Array<String>,
    collection_id: String,
    amount: Array<String>,
): JSONObject = withContext(Dispatchers.IO) {

    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val getAddressInfo = getAccountInfoAsync(from)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val ethGasPrice = web3j.ethGasPrice().sendAsync().get()

        // Ensure amount is a valid number
        for (a in amount) {
            if (BigInteger(a) <= BigInteger.ZERO) {
                jsonData.put("result", "FAIL")
                jsonData.put("error", "insufficient funds")
                return@withContext jsonData
            }
        }
        val batchTokenId = token_id.map { Uint256(BigInteger(it)) }
        val batchAmount = amount.map { Uint256(BigInteger(it)) }

        val function = Function(
            "safeBatchTransferFrom",
            listOf(
                Address(from),
                Address(to),
                DynamicArray(batchTokenId),
                DynamicArray(batchAmount),
                DynamicBytes(byteArrayOf(0))
            ),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "batchTransferERC1155",
            collection_id,
            from,
            to,
            null,
            null,
            null,
            null,
            token_id,
            amount
        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice), // Add 20% to the gas price
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

fun waitForTransactionReceipt(web3j: Web3j, transactionHash: String, maxAttempts: Int = 20, interval: Long = 500): Optional<TransactionReceipt> {
    var attempts = 0
    while (attempts < maxAttempts) {
        val receiptResponse = web3j.ethGetTransactionReceipt(transactionHash).send()

        if (receiptResponse.hasError()) {
            println("Error fetching transaction receipt: ${receiptResponse.error.message}")
            return Optional.empty()
        } else if (receiptResponse.transactionReceipt.isPresent) {
            return receiptResponse.transactionReceipt
        }

        Thread.sleep(interval)
        attempts++
    }
    return Optional.empty()
}

suspend fun deployErc721Async(
    network: String,
    from: String,
    name: String,
    symbol: String,
    token_base_uri: String,
    uri_type: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val getAddressInfo = getAccountInfoAsync(from)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val function = Function(
            "deployWrapped721",
            listOf(
                Utf8String(name),
                Utf8String(symbol),
                Utf8String(token_base_uri),
                Uint8(BigInteger(uri_type))
            ),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "deployERC721",
            null,
            from,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            name, symbol, token_base_uri, uri_type
        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice), // Add 20% to the gas price
                BigInteger(gasLimit), // Add 20% to the gas limit
                nftTransferContractAddress,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit),
                nftTransferContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)

            val receipt = waitForTransactionReceipt(web3j, transactionHash)

            if (receipt.isPresent) {
                if (receipt.get().logs.isNotEmpty()) {
                    val log = receipt.get().logs[0]
                    jsonData.put("contract_address", log.address)
                    val dbConnector = DBConnector()
                    dbConnector.connect()
                    val connection = dbConnector.getConnection()
                    val insertQuery =
                        "INSERT INTO nft_collection_table " +
                                "(network, collection_id, collection_name, collection_symbol, nft_type, owner, base_uri) " +
                                "VALUES (" +
                                "'${network}', " +
                                "'${log.address}', " +
                                "'${name}', " +
                                "'${symbol}', " +
                                "'${uri_type}', " +
                                "'${from}', " +
                                "'${token_base_uri}')"

                    val statement: Statement = connection!!.createStatement()
                    statement.executeUpdate(insertQuery)
                } else {
                    println("No logs found in the transaction receipt.")
                }
            } else {
                println("Transaction receipt not found after multiple attempts.")
            }

            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        val jsonData = JSONObject()
        val resultArray = JSONArray()
        var resultData = JSONObject()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun deployErc1155Async(
    network: String,
    from: String,
    name: String,
    symbol: String,
    token_base_uri: String,
    uri_type: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val getAddressInfo = getAccountInfoAsync(from)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val function = Function(
            "deployWrapped1155",
            listOf(
                Utf8String(name),
                Utf8String(symbol),
                Utf8String(token_base_uri),
                Uint8(BigInteger(uri_type))
            ),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "deployERC1155",
            null,
            from,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            name, symbol, token_base_uri, uri_type
        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice), // Add 20% to the gas price
                BigInteger(gasLimit), // Add 20% to the gas limit
                nftTransferContractAddress,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit),
                nftTransferContractAddress,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)

            val receipt = waitForTransactionReceipt(web3j, transactionHash)

            if (receipt.isPresent) {
                if (receipt.get().logs.isNotEmpty()) {
                    val log = receipt.get().logs[0]
                    jsonData.put("contract_address", log.address)
                } else {
                    println("No logs found in the transaction receipt.")
                }
            } else {
                println("Transaction receipt not found after multiple attempts.")
            }

            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}


suspend fun mintErc721Async(
    network: String,
    from: String,
    to: String,
    token_uri: String,
    token_id: String,
    collection_id: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val getAddressInfo = getAccountInfoAsync(from)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val function = Function(
            "mint",
            listOf(Address(to), Uint256(BigInteger(token_id)), Utf8String(token_uri)),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "mintERC721",
            collection_id,
            from,
            to,
            null,
            token_id,
            null, null, null, null, null, null, null, null,
            token_uri
        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice), // Add 20% to the gas price
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun mintErc721HyosungAsync(
    network: String,
    from: String,
    to: String,
    token_uri: String,
    token_id: String,
    collection_id: String,
    private_key: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(private_key)

        val function = Function(
            "mint",
            listOf(Address(to), Uint256(BigInteger(token_id)), Utf8String(token_uri)),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "mintERC721",
            collection_id,
            from,
            to,
            null,
            token_id,
            null, null, null, null, null, null, null, null,
            token_uri
        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice), // Add 20% to the gas price
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun mintErc1155Async(
    network: String,
    from: String,
    to: String,
    token_uri: String,
    token_id: String,
    collection_id: String,
    amount: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val getAddressInfo = getAccountInfoAsync(from)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val function = Function(
            "mint",
            listOf(Address(to), Uint256(BigInteger(token_id)), Uint256(BigInteger(amount)), Utf8String(token_uri)),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "mintERC1155",
            collection_id,
            from,
            to,
            amount,
            token_id,
            null, null, null, null, null, null, null, null,
            token_uri
        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice), // Add 20% to the gas price
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun mintErc1155HyosungAsync(
    network: String,
    from: String,
    to: String,
    token_uri: String,
    token_id: String,
    collection_id: String,
    amount: String,
    private_key: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(private_key)

        val function = Function(
            "mint",
            listOf(Address(to), Uint256(BigInteger(token_id)), Uint256(BigInteger(amount)), Utf8String(token_uri)),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "mintERC1155",
            collection_id,
            from,
            to,
            amount,
            token_id,
            null, null, null, null, null, null, null, null,
            token_uri
        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice), // Add 20% to the gas price
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun batchMintErc721Async(
    network: String,
    from: String,
    to: String,
    token_uri: Array<String>,
    start_id: String,
    end_id: String,
    collection_id: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val getAddressInfo = getAccountInfoAsync(from)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val batchTokenURI = token_uri.map { Utf8String(it) }

        val function = Function(
            "mintBatch",
            listOf(
                Address(to),
                Uint256(BigInteger(start_id)),
                Uint256(BigInteger(end_id)),
                DynamicArray(batchTokenURI)
            ),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "batchMintERC721",
            collection_id,
            from,
            to,
            null,
            null,
            null, null, null, null, null, null, null,
            null, null, token_uri, start_id, end_id
        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice), // Add 20% to the gas price
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun batchMintErc1155Async(
    network: String,
    from: String,
    to: String,
    token_uri: Array<String>,
    token_id: Array<String>,
    collection_id: String,
    amount: Array<String>
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val getAddressInfo = getAccountInfoAsync(from)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val batchTokenId = token_id.map { Uint256(BigInteger(it)) }
        val batchAmount = amount.map { Uint256(BigInteger(it)) }
        val batchTokenURI = token_uri.map { Utf8String(it) }

        val function = Function(
            "mintBatch",
            listOf(Address(to), DynamicArray(batchTokenId), DynamicArray(batchAmount), DynamicArray(batchTokenURI)),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(from, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

//        val gasLimitEstimate = getEstimateGasAsync(
//            network,
//            "batchMintERC1155",
//            collection_id,
//            from,
//            to,
//            null,
//            null,
//            null, null, token_id, amount, null, null, null, null, null,
//            token_uri
//        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

//        val gasLimit = gasLimitEstimate.getJSONArray("value")
//            .getJSONObject(0)
//            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice), // Add 20% to the gas price
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun burnErc721Async(
    network: String,
    owner: String,
    token_id: String,
    collection_id: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val getAddressInfo = getAccountInfoAsync(owner)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val function = Function(
            "burn",
            listOf(Uint256(BigInteger(token_id))),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(owner, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

//        val gasLimitEstimate = getEstimateGasAsync(
//            network,
//            "burnERC721",
//            collection_id,
//            owner,
//            null,
//            null,
//            token_id,
//            null, null, null, null, null, null, null, null, null, null,
//
//            )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

//        val gasLimit = gasLimitEstimate.getJSONArray("value")
//            .getJSONObject(0)
//            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice), // Add 20% to the gas price
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun approveSetupNftAsync(
    network: String,
    fromAddress: String,
    token_address: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)


    try {
        val getAddressInfo = getAccountInfoAsync(fromAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        var transactionHash = "";
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        var gasPrice = ""
        try {
            val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")
            gasPrice = gasPriceEstimate.getJSONArray("value")
                .getJSONObject(0)
                .getString("gas")

        } catch (e: Exception) {
            jsonData.put("error", e.message)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }

        val function =
            Function(
                "setApprovalForAll", listOf(Address(bridgeSetupContractAddress), Bool(true)),
                emptyList()
            )

        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val tx =
            if (network == "bnb" || network == "tbnb") {
                RawTransaction.createTransaction(
                    nonce,
                    BigInteger(gasPrice), // Add 20% to the gas price
                    BigInteger(gasLimit), // Add 20% to the gas limit
                    token_address,
                    encodedFunction
                )
            } else {
                RawTransaction.createTransaction(
                    chainId,
                    nonce,
                    BigInteger(gasLimit), // Add 20% to the gas limit
                    token_address,
                    BigInteger.ZERO, // value
                    encodedFunction,
                    BigInteger(maxPriorityFeePerGas), // maxPriorityFeePerGas
                    BigInteger(gasPrice) // Add 20% to the gas price
                )
            }
        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (transactionHash != "") {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }

}

suspend fun burnErc1155Async(
    network: String,
    owner: String,
    token_id: String,
    collection_id: String,
    amount: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val getAddressInfo = getAccountInfoAsync(owner)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            jsonData.put("error", "Error while fetching the private key: ${it.message}")
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val function = Function(
            "burn",
            listOf(Address(owner), Uint256(BigInteger(token_id)), Uint256(BigInteger(amount))),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(owner, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasLimitEstimate = getEstimateGasAsync(
            network,
            "burnERC1155",
            collection_id,
            owner,
            null,
            amount,
            token_id,
            null, null, null, null, null, null, null, null, null, null
        )
        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")

        val gasLimit = gasLimitEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx = if (network == "bnb" || network == "tbnb") {
            RawTransaction.createTransaction(
                nonce,
                BigInteger(gasPrice), // Add 20% to the gas price
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger(gasLimit), // Add 20% to the gas limit
                collection_id,
                BigInteger.ZERO,
                encodedFunction,
                //0.1gwei
                BigInteger(maxPriorityFeePerGas),
                BigInteger(gasPrice)
            )
        }

        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun bridgeErc721Async(
    network: String,
    fromAddress: String,
    toNetwork: String,
    token_id: String,
    token_address: String,
    name: String,
    symbol: String,
    ownership: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

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

    try {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        var toNetwork = when (toNetwork) {
            "ethereum" -> "ETHEREUM"
            "cypress" -> "KLAYTN"
            "polygon" -> "POLYGON"
            "bnb" -> "BNBMAIN"
            else -> throw IllegalArgumentException("Invalid main network type")
        }

        val hex = textToHex(toNetwork)

        // Convert hex string to BigInteger
        val toNetworkHex = BigInteger(hex, 16)

        val type = getNodeHomeAsync(network, toNetwork, token_address, "nft").getJSONArray("value")
            .getJSONObject(0)
            .getString("type")

        var function: Function
        var toContractAddress = ""
        if (type == "setup") {
            function = Function(
                "setupFromERC721",
                listOf(
                    Uint256(toNetworkHex), Utf8String(name),
                    Utf8String(symbol), Address(ownership), Address(token_address), Uint256(BigInteger(token_id))
                ),
                emptyList()
            )
            toContractAddress = bridgeSetupContractAddress
        } else {
            function = Function(
                "moveFromERC721",
                listOf(Uint256(toNetworkHex), Address(token_address), Uint256(BigInteger(token_id))),
                emptyList()
            )
            toContractAddress = nftTransferContractAddress
        }

        val regFee = getNetworkFeeAsync(network, toNetwork, type).getJSONArray("value")
            .getJSONObject(0)
            .getString("networkFee")

        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx =
            if (network == "bnb" || network == "tbnb") {
                RawTransaction.createTransaction(
                    nonce,
                    BigInteger(gasPrice), // Add 20% to the gas price
                    BigInteger(gasLimit), // Add 20% to the gas limit
                    toContractAddress,
                    BigInteger(regFee), // value
                    encodedFunction
                )
            } else {
                RawTransaction.createTransaction(
                    chainId,
                    nonce,
                    BigInteger(gasLimit), // Add 20% to the gas limit
                    toContractAddress,
                    BigInteger(regFee), // value
                    encodedFunction,
                    BigInteger(maxPriorityFeePerGas), // 35 Gwei maxPriorityFeePerGas
                    BigInteger(gasPrice) // Add 20% to the gas price
                )
            }
        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun bridgeErc1155Async(
    network: String,
    fromAddress: String,
    toNetwork: String,
    token_id: String,
    token_address: String,
    name: String,
    symbol: String,
    ownership: String,
    amount: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

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

    try {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        var toNetwork = when (toNetwork) {
            "ethereum" -> "ETHEREUM"
            "cypress" -> "KLAYTN"
            "polygon" -> "POLYGON"
            "bnb" -> "BNBMAIN"
            else -> throw IllegalArgumentException("Invalid main network type")
        }

        val hex = textToHex(toNetwork)

        // Convert hex string to BigInteger
        val toNetworkHex = BigInteger(hex, 16)

        val type = getNodeHomeAsync(network, toNetwork, token_address, "nft").getJSONArray("value")
            .getJSONObject(0)
            .getString("type")

        var function: Function
        var toContractAddress = ""
        if (type == "setup") {
            function = Function(
                "setupFromERC1155",
                listOf(
                    Uint256(toNetworkHex),
                    Utf8String(name),
                    Utf8String(symbol),
                    Address(ownership),
                    Address(token_address),
                    Uint256(BigInteger(token_id)),
                    Uint256(BigInteger(amount))
                ),
                emptyList()
            )
            toContractAddress = bridgeSetupContractAddress
        } else {
            function = Function(
                "moveFromERC1155",
                listOf(
                    Uint256(toNetworkHex),
                    Address(token_address),
                    Uint256(BigInteger(token_id)),
                    Uint256(BigInteger(amount))
                ),
                emptyList()
            )
            toContractAddress = nftTransferContractAddress
        }

        val regFee = getNetworkFeeAsync(network, toNetwork, type).getJSONArray("value")
            .getJSONObject(0)
            .getString("networkFee")

        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val gasPriceEstimate = getEstimateGasAsync(network, "baseFee")
        val gasPrice = gasPriceEstimate.getJSONArray("value")
            .getJSONObject(0)
            .getString("gas")

        val tx =
            if (network == "bnb" || network == "tbnb") {
                RawTransaction.createTransaction(
                    nonce,
                    BigInteger(gasPrice), // Add 20% to the gas price
                    BigInteger(gasLimit), // Add 20% to the gas limit
                    toContractAddress,
                    BigInteger(regFee), // value
                    encodedFunction
                )
            } else {
                RawTransaction.createTransaction(
                    chainId,
                    nonce,
                    BigInteger(gasLimit), // Add 20% to the gas limit
                    toContractAddress,
                    BigInteger(regFee), // value
                    encodedFunction,
                    BigInteger(maxPriorityFeePerGas), // 35 Gwei maxPriorityFeePerGas
                    BigInteger(gasPrice) // Add 20% to the gas price
                )
            }
        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (!transactionHash.isNullOrEmpty()) {
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            jsonData.put("error", "insufficient funds")
            jsonData.put("transaction_hash", transactionHash)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun verifyNFT(
    network: String,
    token_id: String,
    collection_id: String,
    api_key: String? = null
): JSONObject = withContext(Dispatchers.IO) {
    val result = JSONObject()
    var resultData = JSONObject()
    var resultArray = JSONArray()
    resultData.put("result", "FAIL")

    if (api_key.isNullOrEmpty()) {
        result.put("error", "API Key is NULL")
        resultArray.put(result)
        resultData.put("value", resultArray)
        return@withContext resultData
    }

    if (network == "cypress") {
        return@withContext JSONObject().apply {
            put("result", "FAIL")
            put("value", JSONArray().put(JSONObject().put("error", "cypress is not supported")))
        }
    }

    result.put("ContractVerify", false)
    result.put("TokenURIAvailable", false)
    result.put("TokenURIResponseOnTime", false)
    result.put("TokenURIDecentralized", false)
    result.put("MetadataStandard", false)
    result.put("MetadataImageAvailable", false)
    result.put("TokenURIisHTTPS", false)
    result.put("ImageURIisHTTPS", false)

    var nftType: String? = null
    var tokenURI: String? = null
    var tokenInfo: String? = null
    var imageURL: String? = null
    var query =
        "SELECT nft_type, token_uri, token_info, image_url FROM " +
                "nft_token_table " +
                "WHERE " +
                "network = '${network}' " +
                "AND " +
                "collection_id = '${collection_id}' " +
                "AND " +
                "token_id = '${token_id}' "
    try {
        val dbConnector = DBConnector()
        dbConnector.connect()
        val connection = dbConnector.getConnection()
        if (connection != null) {
            val dbQueryExector = DBQueryExector(connection)
            val getTransaction1: ResultSet? = dbQueryExector.executeQuery(query)
            if (getTransaction1 != null) {
                try {
                    while (getTransaction1.next()) {
                        nftType = getTransaction1.getString("nft_type")
                        tokenURI = getTransaction1.getString("token_uri")
                        tokenInfo = getTransaction1.getString("token_info")
                        imageURL = getTransaction1.getString("image_url")
                    }
                } catch (ex: SQLException) {
                    ex.printStackTrace()
                } finally {
                    getTransaction1.close()
                }
            }
        }
        if (nftType.isNullOrEmpty()) {
            result.put("error", "DB info NULL")
            resultArray.put(result)
            resultData.put("value", resultArray)
            return@withContext resultData
        }
        val hostUrl: String
        when (network) {
            "ethereum" -> {
                hostUrl =
                    "https://api.etherscan.com/api?module=contract&action=getabi&address=$collection_id&apikey=$api_key"
            }

            "cypress" -> {
                hostUrl = ""
            }

            "polygon" -> {
                hostUrl =
                    "https://api.polygonscan.com/api?module=contract&action=getabi&address=$collection_id&apikey=$api_key"
            }

            "bnb" -> {
                hostUrl =
                    "https://api.bscscan.com/api?module=contract&action=getabi&address=$collection_id&apikey=$api_key"
            }

            else -> {
                result.keys().forEach {
                    result.remove(it)
                }
                result.put("error", "DB info NULL")
                return@withContext resultData
            }
        }

        if (network == "cypress") {
            result.put("ContractVerify", false)
            result.put("ContractStandard", false)
        } else {
            val url = URL(hostUrl)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val responseData = InputStreamReader(connection.inputStream).readText()

                val jsonObject = JSONObject(responseData)
                val apiResult = jsonObject.optString("result")

                if (apiResult == "Contract source code not verified") {
                    result.put("ContractVerify", false)
                    result.put("ContractStandard", false)
                } else {
                    val apiResultJson = JSONArray(apiResult)
                    result.put("ContractVerify", true)

                    val bytecodeFunctions = mutableListOf<JSONObject>()

                    for (i in 0 until apiResultJson.length()) {
                        val jsonObject = apiResultJson.getJSONObject(i)
                        if (jsonObject.getString("type") == "function") {
                            bytecodeFunctions.add(jsonObject)
                        }
                    }

                    val contractStandard = JSONObject()

                    if (nftType == "erc721") {
                        val balanceOf =
                            bytecodeFunctions.any { it.getString("name") == "balanceOf" }
                        val ownerOf = bytecodeFunctions.any { it.getString("name") == "ownerOf" }
                        val transferFrom =
                            bytecodeFunctions.any { it.getString("name") == "transferFrom" }
                        val approve = bytecodeFunctions.any { it.getString("name") == "approve" }
                        val setApprovalForAll =
                            bytecodeFunctions.any { it.getString("name") == "setApprovalForAll" }
                        val getApproved =
                            bytecodeFunctions.any { it.getString("name") == "getApproved" }
                        val isApprovedForAll =
                            bytecodeFunctions.any { it.getString("name") == "isApprovedForAll" }

                        var safeTransferFromWith_data = false
                        var safeTransferFromWithout_data = false

                        for (jsonObject in bytecodeFunctions) {
                            val name = jsonObject.getString("name")
                            if (name == "safeTransferFrom") {
                                val inputs = jsonObject.getJSONArray("inputs")
                                var hasData = false
                                for (i in 0 until inputs.length()) {
                                    val input = inputs.getJSONObject(i)
                                    if (input.getString("name") == "_data") {
                                        hasData = true
                                        break
                                    }
                                }
                                if (hasData) {
                                    safeTransferFromWith_data = true
                                } else {
                                    safeTransferFromWithout_data = true
                                }
                            }
                        }

                        contractStandard.put("balanceof", balanceOf)
                        contractStandard.put("ownerOf", ownerOf)
                        contractStandard.put("transferFrom", transferFrom)
                        contractStandard.put("approve", approve)
                        contractStandard.put("setApprovalForAll", setApprovalForAll)
                        contractStandard.put("getApproved", getApproved)
                        contractStandard.put("isApprovedForAll", isApprovedForAll)
                        contractStandard.put("safeTransferFromWith_data", safeTransferFromWith_data)
                        contractStandard.put(
                            "safeTransferFromWithout_data",
                            safeTransferFromWithout_data
                        )
                    } else if (nftType == "erc1155") {
                        val balanceOf =
                            bytecodeFunctions.any { it.getString("name") == "balanceOf" }
                        val balanceOfBatch =
                            bytecodeFunctions.any { it.getString("name") == "balanceOfBatch" }
                        val setApprovalForAll =
                            bytecodeFunctions.any { it.getString("name") == "setApprovalForAll" }
                        val isApprovedForAll =
                            bytecodeFunctions.any { it.getString("name") == "isApprovedForAll" }
                        val safeTransferFrom =
                            bytecodeFunctions.any { it.getString("name") == "safeTransferFrom" }
                        val safeBatchTransferFrom =
                            bytecodeFunctions.any { it.getString("name") == "safeBatchTransferFrom" }

                        contractStandard.put("balanceof", balanceOf)
                        contractStandard.put("balanceOfBatch", balanceOfBatch)
                        contractStandard.put("setApprovalForAll", setApprovalForAll)
                        contractStandard.put("isApprovedForAll", isApprovedForAll)
                        contractStandard.put("safeTransferFrom", safeTransferFrom)
                        contractStandard.put("safeBatchTransferFrom", safeBatchTransferFrom)
                    }

                    result.put("ContractStandard", contractStandard)
                }

            } else {
                println("Invalid API response. Response code: $responseCode")
                result
            }
        }

        networkSettings(network!!)
        val web3 = Web3j.build(HttpService(rpcUrl))
        var parameter = Numeric.hexStringToByteArray("0x80ac58cd")
        if (nftType == "erc721") {
            parameter = Numeric.hexStringToByteArray("0x80ac58cd")

        } else if (nftType == "erc1155") {
            parameter = Numeric.hexStringToByteArray("0xd9b67a26")
        }
        val supportsInterface = Function(
            "supportsInterface",
            listOf(Bytes4(parameter)),
            listOf(object : TypeReference<Bool>() {})
        )
        val encodedsupportsInterface = FunctionEncoder.encode(supportsInterface)
        val supportsInterfaceResponse = web3.ethCall(
            Transaction.createEthCallTransaction(null, collection_id, encodedsupportsInterface),
            DefaultBlockParameterName.LATEST
        ).send()
        val supportsInterfaceOutput =
            FunctionReturnDecoder.decode(supportsInterfaceResponse.result, supportsInterface.outputParameters)
        val isSupported = supportsInterfaceOutput[0].value as Boolean
        if (isSupported) {
            result.put("supportsInterface", true)
        } else {
            result.put("supportsInterface", false)
        }

        if (!tokenURI.isNullOrEmpty()) {
            var uriHttp = ""
            if (tokenURI.startsWith("ipfs://ipfs/")) {
                uriHttp = "https://ipfs.io/ipfs/${tokenURI.substring(12)}"
            } else if (tokenURI.startsWith("ipfs://")) {
                uriHttp = "https://ipfs.io/ipfs/${tokenURI.substring(7)}"
            } else if (tokenURI.startsWith("ar://")) {
                uriHttp = "https://arweave.net/${tokenURI.substring(5)}"
            } else {
                uriHttp = tokenURI
            }

            if (uriHttp.contains("ipfs") || uriHttp.contains("arweave")) {
                result.put("TokenURIDecentralized", true)
            }

            val startTime = Date()
            val url = URL(uriHttp)
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val endTime = Date()

                val elapsedTime = (endTime.time - startTime.time) / 1000

                result.put("TokenURIAvailable", true)

                if (elapsedTime < 2) {
                    result.put("TokenURIResponseOnTime", true)
                }
            } else {
                println("Invalid API response. Response code: $responseCode")
            }

            if (url.protocol == "https") {
                result.put("TokenURIisHTTPS", true)
            }
        }

        if (tokenInfo != null) {
            try {
                val json = JSONObject(tokenInfo)

                var trueCount = 0

                if (json.has("name")) {
                    trueCount += 1
                }

                if (json.has("image")) {
                    trueCount += 1

                    if (imageURL!!.startsWith("ipfs://ipfs/")) {
                        imageURL = "https://ipfs.io/ipfs/${imageURL.substring(12)}"
                    } else if (imageURL.startsWith("ipfs://")) {
                        imageURL = "https://ipfs.io/ipfs/${imageURL.substring(7)}"
                    } else if (imageURL.startsWith("ar://")) {
                        imageURL = "https://arweave.net/${imageURL.substring(5)}"
                    }

                    try {
                        val imageConnection = URL(imageURL!!).openConnection() as HttpURLConnection
                        imageConnection.requestMethod = "HEAD"

                        if (imageConnection.responseCode == HttpURLConnection.HTTP_OK) {
                            result.put("MetadataImageAvailable", true)
                        }
                    } catch (e: IOException) {
                        println("Image fetch error: ${e.message}")
                    }

                    if (imageURL!!.startsWith("https://")) {
                        result.put("ImageURIisHTTPS", true)
                    }
                }

                if (json.has("description")) {
                    trueCount += 1
                }

                if (json.has("attributes")) {
                    trueCount += 1
                }

                if (trueCount == 4) {
                    result.put("MetadataStandard", true)
                }
            } catch (e: Exception) {
                println("JSON parsing error: ${e.message}")
            }
        }
        resultData.put("result", "OK")
        resultData.put("value", result)
    } catch (e: Exception) {
        val resultArray = JSONArray()
        val jsonData = JSONObject()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
        return@withContext resultData
    }
}

suspend fun chkNFTHolder(
    network: String,
    account: String,
    collection_id: String,
    token_id: String
): JSONObject = withContext(Dispatchers.IO) {
    val result = JSONObject()
    try {
        val dbConnector = DBConnector()
        dbConnector.connect()
        val connection = dbConnector.getConnection()
        var query =
            "SELECT " +
                    "network, " +
                    "collection_id, " +
                    "token_id, " +
                    "nft_type " +
                    "FROM " +
                    "nft_token_table " +
                    "WHERE " +
                    "network = '${network}' " +
                    "AND " +
                    "collection_id = '${collection_id}' " +
                    "AND " +
                    "token_id = '${token_id}' "

        var network: String? = null
        var collection_id: String? = null
        var token_id: String? = null
        var nft_type: String? = null
        if (connection != null) {
            val dbQueryExector = DBQueryExector(connection)
            val getTransaction1: ResultSet? = dbQueryExector.executeQuery(query)
            if (getTransaction1 != null) {
                try {
                    while (getTransaction1.next()) {
                        network = getTransaction1.getString("network")
                        collection_id = getTransaction1.getString("collection_id")
                        token_id = getTransaction1.getString("token_id")
                        nft_type = getTransaction1.getString("nft_type")
                    }
                } catch (ex: SQLException) {
                    ex.printStackTrace()
                } finally {
                    getTransaction1.close()
                }
            }
        }
        dbConnector.disconnect()

        if (nft_type == null) {
            result.put("result", "FAIL")
            result.put("error", "DB info is null")
            return@withContext result
        }

        networkSettings(network!!)
        val web3 = Web3j.build(HttpService(rpcUrl))
        if (nft_type == "erc721") {
            val ownerFunction = Function(
                "ownerOf",
                listOf(Uint256(BigInteger(token_id))),
                listOf(object : TypeReference<Address>() {})
            )
            val encodedOwnerFunction = FunctionEncoder.encode(ownerFunction)
            val ownerResponse = web3.ethCall(
                Transaction.createEthCallTransaction(null, collection_id, encodedOwnerFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            val ownerOutput =
                FunctionReturnDecoder.decode(ownerResponse.result, ownerFunction.outputParameters)
            val owner = ownerOutput[0].value.toString()
            if (owner.toLowerCase() == account.toLowerCase()) {
                val resultArray = JSONArray()
                val jsonData = JSONObject()
                jsonData.put("success", "OK")
                resultArray.put(jsonData)
                result.put("result", "OK")
                result.put("value", resultArray)
                return@withContext result
            } else {
                result.put("result", "FAIL")
                result.put("error", "NOT OWNER")
            }
        } else {
            val balanceOfFunction = Function(
                "balanceOf",
                listOf(Address(account), Uint256(BigInteger(token_id))),
                listOf(object : TypeReference<Uint256>() {})
            )
            val encodedbalanceOfFunction = FunctionEncoder.encode(balanceOfFunction)
            val balanceOfResponse = web3.ethCall(
                Transaction.createEthCallTransaction(null, collection_id, encodedbalanceOfFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            val balanceOfOutput =
                FunctionReturnDecoder.decode(balanceOfResponse.result, balanceOfFunction.outputParameters)
            val balance = (balanceOfOutput[0].value as BigInteger).toInt()
            if (balance >= 1) {
                val resultArray = JSONArray()
                val jsonData = JSONObject()
                jsonData.put("success", "OK")
                resultArray.put(jsonData)
                result.put("result", "OK")
                result.put("value", resultArray)
                return@withContext result
            } else {
                val resultArray = JSONArray()
                val jsonData = JSONObject()
                result.put("error", "NOT OWNER")
                resultArray.put(jsonData)
                result.put("result", "FAIL")
                result.put("value", resultArray)
                return@withContext result
            }
        }
    } catch (e: Exception) {
        val resultArray = JSONArray()
        val jsonData = JSONObject()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        result.put("result", "FAIL")
        result.put("value", resultArray)
        return@withContext result
    }
}

suspend fun getTotalSupplyAsync(network: String, collection_id: String): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)  // Assuming this method sets the network-specific settings like rpcUrl.

    val jsonData = JSONObject()
    val resultArray = JSONArray()
    val resultData = JSONObject()

    try {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val totalFunction = Function("totalSupply", emptyList(), listOf(object : TypeReference<Uint256>() {}))
        val encodedTotalFunction = FunctionEncoder.encode(totalFunction)
        val totalResponse = web3j.ethCall(
            Transaction.createEthCallTransaction(null, collection_id, encodedTotalFunction),
            DefaultBlockParameterName.LATEST
        ).send()
        val totalsOutput = FunctionReturnDecoder.decode(totalResponse.result, totalFunction.outputParameters)
        val totalSupply = totalsOutput[0].value as BigInteger

        jsonData.put("totalSupply", totalSupply.toString())
        resultData.put("result", "OK")
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultData.put("result", "FAIL")
    }

    resultArray.put(jsonData)
    resultData.put("value", resultArray)
    resultData
}


suspend fun signMessage(
    network: String,
    fromAddress: String,
    collection_id: String,
    token_id: String,
    prefix: String
): String {
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
    var message = ""
    val credentials = Credentials.create(privateKey)
    val str = prefix + network + fromAddress + collection_id + token_id
    val hash = Hash.sha3(Numeric.toHexStringNoPrefix(str.toByteArray()))
    if (network == "cypress") {
        message = """
        \x19Klaytn Signed Message:
        ${hash.length}$hash
        """.trimIndent()
    } else {
        message = """
        \x19Ethereum Signed Message:
        ${hash.length}$hash
        """.trimIndent()
    }
    val data = message.toByteArray()
    val signature = Sign.signPrefixedMessage(data, credentials.ecKeyPair)
    val r = Numeric.toHexStringNoPrefix(signature.r)
    val s = Numeric.toHexStringNoPrefix(signature.s)
    val v = Numeric.toHexStringNoPrefix(signature.v)
    return r + s + v
}

suspend fun getSignerAddressFromSignature(
    network: String,
    signature: String,
    fromAddress: String,
    collection_id: String,
    token_id: String,
    prefix: String
): String {
    var message = ""
    val str = prefix + network + fromAddress + collection_id + token_id
    val hash = Hash.sha3(Numeric.toHexStringNoPrefix(str.toByteArray()))

    if (network == "cypress") {
        message = """
        \x19Klaytn Signed Message:
        ${hash.length}$hash
        """.trimIndent()
    } else {
        message = """
        \x19Ethereum Signed Message:
        ${hash.length}$hash
        """.trimIndent()
    }
    val r = Numeric.hexStringToByteArray(signature.substring(0, 64))
    val s = Numeric.hexStringToByteArray(signature.substring(64, 128))
    val v = BigInteger(signature.substring(128), 16).toByte()

    val signData = Sign.SignatureData(v, r, s)
    val pubKey = Sign.signedPrefixedMessageToKey(message.toByteArray(Charsets.UTF_8), signData)
    return "0x" + Keys.getAddress(pubKey)
}
