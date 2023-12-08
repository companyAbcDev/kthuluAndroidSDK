package io.kthulu.sdk

import kotlinx.coroutines.*
import org.apache.commons.lang3.RandomUtils
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.crypto.*
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

suspend fun createAccountsAsync(
    network: Array<String>
): JSONObject = withContext(Dispatchers.IO) {
    // save data arrya
    var saveMainNet = JSONArray()
    var jsonData = JSONObject()
    var resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)
    val allowedStrings = setOf("ethereum", "cypress", "polygon", "bnb", "sepolia", "baobab", "mumbai", "tbnb")

    for (network in network) {
        if (network !in allowedStrings) {
            resultData.put("result", "FAIL")
            resultData.put("value", "Error: $network is not allowed.")
            return@withContext resultData
        }
    }

    val initialEntropy = RandomUtils.nextBytes(16)
    val mnemonic = MnemonicUtils.generateMnemonic(initialEntropy)
    val seed = MnemonicUtils.generateSeed(mnemonic, null)
    val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
    val purpose = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, intArrayOf(44 or Bip32ECKeyPair.HARDENED_BIT))
    val coinType = Bip32ECKeyPair.deriveKeyPair(purpose, intArrayOf(60 or Bip32ECKeyPair.HARDENED_BIT))
    val account = Bip32ECKeyPair.deriveKeyPair(coinType, intArrayOf(0 or Bip32ECKeyPair.HARDENED_BIT))
    val change = Bip32ECKeyPair.deriveKeyPair(account, intArrayOf(0))
    val keyPair = Bip32ECKeyPair.deriveKeyPair(change, intArrayOf(0))
    val credentials = Credentials.create(keyPair.privateKey.toString(16))



    try {
        for (network in network) {

            // add return value
            val returnData = JSONObject()
            returnData.put("network", network)
            returnData.put("account", credentials.address)
            resultArray.put(returnData)
        }

        //save
        val saveData = JSONObject()
        saveData.put("account", credentials.address)
        saveData.put("private", encrypt("0x${Numeric.toHexStringNoPrefix(keyPair.privateKey)}"))
        saveData.put("mnemonic", encrypt(mnemonic))
        saveMainNet.put(saveData)

        saveData(credentials.address.lowercase(), saveMainNet.toString())

        resultData.put("result", "OK")
        resultData.put("value", resultArray)
        resultData

    } catch (e: Exception) {
        resultArray = JSONArray()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun isValidAddressAsync(account: String): Boolean = withContext(Dispatchers.IO) {
    try {
        WalletUtils.isValidAddress(account)
    } catch (e: Exception) {
        false
    }
}

// Validation function for private key
fun isValidPrivateKey(key: String): Boolean {
    return try {
        WalletUtils.isValidPrivateKey(key)
    } catch (e: Exception) {
        false
    }
}

// Validation function for mnemonic phrase
fun isValidMnemonic(phrase: String): Boolean {
    return try {
        MnemonicUtils.validateMnemonic(phrase)
    } catch (e: Exception) {
        false
    }
}

// getAccountAsync asynchronously
suspend fun restoreAccountAsync(
    network: Array<String>? = null,
    private: String? = null,
    mnemonic: String? = null
): JSONObject = withContext(Dispatchers.IO) {

    // save data array
    var saveMainNet = JSONArray()
    var jsonData = JSONObject()
    // return array & object
    var resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    val networkArray: Array<String>

    if (network == null) {
        networkArray = arrayOf("ethereum", "cypress", "polygon", "bnb")
    } else {
        networkArray = network
    }

    try {
        val keyPair = when {
            mnemonic != null -> {
                if (!isValidMnemonic(mnemonic)) {
                    throw IllegalArgumentException("Invalid mnemonic phrase.")
                }
                val seed = MnemonicUtils.generateSeed(mnemonic, "")
                val masterKeyPair = Bip32ECKeyPair.generateKeyPair(seed)
                val purpose = Bip32ECKeyPair.deriveKeyPair(masterKeyPair, intArrayOf(44 or Bip32ECKeyPair.HARDENED_BIT))
                val coinType = Bip32ECKeyPair.deriveKeyPair(purpose, intArrayOf(60 or Bip32ECKeyPair.HARDENED_BIT))
                val account = Bip32ECKeyPair.deriveKeyPair(coinType, intArrayOf(0 or Bip32ECKeyPair.HARDENED_BIT))
                val change = Bip32ECKeyPair.deriveKeyPair(account, intArrayOf(0))
                Bip32ECKeyPair.deriveKeyPair(change, intArrayOf(0))
            }

            private != null -> {
                if (!isValidPrivateKey(private)) {
                    throw IllegalArgumentException("Invalid private key.")
                }
                ECKeyPair.create(Numeric.hexStringToByteArray(private))
            }

            else -> throw IllegalArgumentException("Either mnemonic or privateKey must be provided.")
        }

        val credentials = Credentials.create(keyPair)
        var keyPairPrivateKey = "0x${Numeric.toHexStringNoPrefix(keyPair.privateKey)}"

        mnemonic?.let { it } ?: ""

        for (network in networkArray) {
            // add return value
            val returnObject = JSONObject()
            returnObject.put("network", network)
            returnObject.put("account", credentials.address)
            resultArray.put(returnObject)
        }

        // save
        val saveObject = JSONObject()
        saveObject.put("account", credentials.address)
        saveObject.put("private", encrypt(keyPairPrivateKey))
        if (mnemonic == null) {
            saveObject.put("mnemonic", "")
        } else {
            saveObject.put("mnemonic", encrypt(mnemonic))
        }
        saveMainNet.put(saveObject)

        saveData(credentials.address.lowercase(), saveMainNet.toString())

        resultData.put("result", "OK")
        resultData.put("value", resultArray)

        resultData
    } catch (e: Exception) {
        resultArray = JSONArray()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }

}

suspend fun getAccountInfoAsync(account: String): JSONObject = withContext(Dispatchers.IO) {
    var resultArray = JSONArray()
    var jsonData = JSONObject()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("value", resultArray)
    }

    val data = loadData(account.lowercase())

    val networkLoadData = if (data != null) JSONArray(data) else JSONArray()

    var equalAddress: JSONObject? = null

    try {
        for (i in 0 until networkLoadData.length()) {
            val loadDataAddress = networkLoadData.getJSONObject(i)
            if (account.lowercase() == loadDataAddress.getString("account").lowercase()) {
                equalAddress = loadDataAddress
                break
            }
        }

        equalAddress = equalAddress?.apply {
            put("private", decrypt(getString("private")))
            getString("mnemonic")?.let { mnemonic ->
                put("mnemonic", decrypt(mnemonic))
            }
        } ?: JSONObject()

        resultArray.put(equalAddress)
        resultData.apply {
            put("result", "OK")
            put("value", resultArray)
        }
    } catch (e: Exception) {
        resultArray = JSONArray()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}


// Get token info asynchronously
suspend fun getBalanceAsync(
    network: String,
    owner_account: String,
    token_address: String? = "0x0000000000000000000000000000000000000000"
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()
    // return array & object
    var resultArray = JSONArray()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("value", resultArray)
    }
    try {
        val web3j = Web3j.build(HttpService(rpcUrl))

        if (token_address == "0x0000000000000000000000000000000000000000") {
            val ethGetBalance = web3j.ethGetBalance(owner_account, DefaultBlockParameterName.LATEST).send()
            val balance = ethGetBalance.balance
            val balanceEther = Convert.fromWei(balance.toString(), Convert.Unit.ETHER)
            jsonData.put("balance", balanceEther)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } else {
            val balanceFunction =
                Function("balanceOf", listOf(Address(owner_account)), listOf(object : TypeReference<Uint8>() {}))
            val encodedbalanceFunction = FunctionEncoder.encode(balanceFunction)
            val balanceResponse = web3j.ethCall(
                Transaction.createEthCallTransaction(null, token_address, encodedbalanceFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            val tokenBalance = BigInteger(balanceResponse.result.replace("0x", ""), 16)
            val decimalsFunction = Function("decimals", emptyList(), listOf(object : TypeReference<Uint8>() {}))
            val encodedDecimalsFunction = FunctionEncoder.encode(decimalsFunction)
            val decimalsResponse = web3j.ethCall(
                Transaction.createEthCallTransaction(null, token_address, encodedDecimalsFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            val decimalsOutput =
                FunctionReturnDecoder.decode(decimalsResponse.result, decimalsFunction.outputParameters)
            val decimals = (decimalsOutput[0].value as BigInteger).toInt()
            var newBalance =
                BigDecimal(tokenBalance.toDouble()).divide(BigDecimal.TEN.pow(decimals.toInt()))
            jsonData.put("balance", newBalance)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        }
    } catch (e: Exception) {
        resultArray = JSONArray()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun getTokenInfoAsync(
    network: String,
    token_address: String,
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()
    // return array & object
    var resultArray = JSONArray()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("value", resultArray)
    }
    try {
        val web3j = Web3j.build(HttpService(rpcUrl))

        var tokenName = "";
        var tokenSymbol = "";
        var decimals = 0;
        var adjustedTotalSupply = ""

        try {
            val nameFunction = Function("name", emptyList(), listOf(object : TypeReference<Utf8String>() {}))
            val encodedNameFunction = FunctionEncoder.encode(nameFunction)
            val nameResponse = web3j.ethCall(
                Transaction.createEthCallTransaction(null, token_address, encodedNameFunction),
                DefaultBlockParameterName.LATEST
            ).send()

            val nameOutput = FunctionReturnDecoder.decode(nameResponse.result, nameFunction.outputParameters)
            tokenName = nameOutput[0].value as String
        } catch (e: Exception) {
            println("name error")
        }


        try {
            val symbolFunction = Function("symbol", emptyList(), listOf(object : TypeReference<Utf8String>() {}))
            val encodedSymbolFunction = FunctionEncoder.encode(symbolFunction)
            val symbolResponse = web3j.ethCall(
                Transaction.createEthCallTransaction(null, token_address, encodedSymbolFunction),
                DefaultBlockParameterName.LATEST
            ).send()

            val symbolOutput = FunctionReturnDecoder.decode(symbolResponse.result, symbolFunction.outputParameters)

            tokenSymbol = symbolOutput[0].value as String

        } catch (e: Exception) {
            println("symbol error")
        }

        try {
            val decimalsFunction = Function("decimals", emptyList(), listOf(object : TypeReference<Uint8>() {}))
            val encodedDecimalsFunction = FunctionEncoder.encode(decimalsFunction)
            val decimalsResponse = web3j.ethCall(
                Transaction.createEthCallTransaction(null, token_address, encodedDecimalsFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            val decimalsOutput =
                FunctionReturnDecoder.decode(decimalsResponse.result, decimalsFunction.outputParameters)
            decimals = (decimalsOutput[0].value as BigInteger).toInt()
        } catch (e: Exception) {
            println("decimals error")
        }
        try {
            val totalFunction = Function("totalSupply", emptyList(), listOf(object : TypeReference<Uint256>() {}))
            val encodedTotalFunction = FunctionEncoder.encode(totalFunction)
            val totalResponse = web3j.ethCall(
                Transaction.createEthCallTransaction(null, token_address, encodedTotalFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            val totalsOutput =
                FunctionReturnDecoder.decode(totalResponse.result, totalFunction.outputParameters)
            val totalSupplyRaw = totalsOutput[0].value as BigInteger
            adjustedTotalSupply = totalSupplyRaw.divide(BigInteger.TEN.pow(decimals)).toString()

        } catch (e: Exception) {
            println("totalSupply error")
        }
        jsonData.put("name", tokenName)
        jsonData.put("symbol", tokenSymbol)
        jsonData.put("decimals", decimals)
        jsonData.put("total_supply", adjustedTotalSupply)
        resultArray.put(jsonData)
        resultData.put("result", "OK")
        resultData.put("value", resultArray)
    } catch (e: Exception) {
        resultArray = JSONArray()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun getTokenHistoryAsync(
    network: String,
    owner_account: String,
    token_address: String? = "0x0000000000000000000000000000000000000000",
    sort: String? = "DESC",
    size: String? = "100"
): JSONObject = withContext(Dispatchers.IO) {
    var resultArray = JSONArray()
    var jsonData = JSONObject()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("value", resultArray)
    }

    try {
        val url = URL("https://app.kthulu.io:3302/token/history/$network/$owner_account/$token_address/$sort/$size")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

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

suspend fun getUsersAsync(
    owner: String
): JSONObject = withContext(Dispatchers.IO) {
    var resultArray = JSONArray()
    var jsonData = JSONObject()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("value", resultArray)
    }

    try {
        val dbConnector = DBConnector()
        dbConnector.connect()
        val connection = dbConnector.getConnection()

        val resultArray = JSONArray()
        val resultData = JSONObject().apply {
            put("result", "FAIL")
            put("value", resultArray)
        }

        val query =
            "SELECT * FROM users_table WHERE owner_eigenvalue = '$owner'"

        connection?.use {
            val dbQueryExecutor = DBQueryExector(it)
            val resultSet = dbQueryExecutor.executeQuery(query)
            resultSet?.use {
                while (it.next()) {
                    jsonData = JSONObject().apply {
                        put("owner", it.getString("owner_eigenvalue"))
                        put("network", it.getString("network"))
                        put("account", it.getString("user_account"))
                        put("type", it.getString("user_type"))
                    }
                    resultArray.put(jsonData)
                }
                resultData.put("result", "OK")
                resultData.put("value", resultArray)
            }
        }

        dbConnector.disconnect()
        resultData
    } catch (e: Exception) {
        resultArray = JSONArray()
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

suspend fun getTokenListAsync(
    network: String,
    ownerAddress: String,
    size : Int?= 100,
    sort : String?= null,
    page_number : Int?=null
): JSONObject = withContext(Dispatchers.IO) {
    var resultArray = JSONArray()
    var jsonData = JSONObject()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("value", resultArray)
    }

    try {
        val url = URL("https://app.kthulu.io:3302/token/getTokenListAsync")
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
        jsonPayload.put("account", ownerAddress)
        jsonPayload.put("limit", size)
        jsonPayload.put("sort", sort)
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
