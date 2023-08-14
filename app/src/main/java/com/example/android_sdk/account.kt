import com.example.android_sdk.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.apache.commons.lang3.RandomUtils
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.crypto.*
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.nio.charset.StandardCharsets

// Create accounts asynchronously
suspend fun createAccountsAsync(
    network: Array<String>
): JSONObject = withContext(Dispatchers.IO) {
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

    // save data arrya
    var saveMainNet = JSONArray()

    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try{
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

    } catch(e: Exception){
        resultData
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

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    val networkArray: Array<String>

    if(network == null){
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
        var keyPairPrivateKey= "0x${Numeric.toHexStringNoPrefix(keyPair.privateKey)}"

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
    }catch (e: Exception) {
        resultData.put("error", e.message)
        resultData
    }

}

suspend fun getAccountInfoAsync(account: String): JSONObject = withContext(Dispatchers.IO) {
    val resultArray = JSONArray()
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
        resultData
    }
}



// Get token info asynchronously
suspend fun getBalanceAsync(
    network: String,
    owner_account: String,
    token_address: String? = "0x0000000000000000000000000000000000000000"
): JSONObject = withContext(Dispatchers.IO) {
    val jsonData = JSONObject()
    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()

    // return array & object
    val resultArray = JSONArray()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("value", resultArray)
    }

    val query =
        "SELECT balance, (SELECT decimals FROM token_table WHERE t.network ='$network' AND t.t.token_address ='$token_address' LIMIT 1) FROM token_owner_table t WHERE network = '$network' AND owner_account = '$owner_account' AND token_address = '$token_address'"

    connection?.use {
        val dbQueryExecutor = DBQueryExector(it)
        val resultSet = dbQueryExecutor.executeQuery(query)
        resultSet?.use {
            while (it.next()) {
                val jsonData = JSONObject().apply {
                    var balance = it.getString("balance")
                    var decimals = it.getString("decimals")
                    var newBalance =
                        BigDecimal(balance.toDouble()).divide(BigDecimal.TEN.pow(decimals.toInt()))
                    put("balance", newBalance.toString())

                }
                resultArray.put(jsonData)
            }
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        }
    }
    dbConnector.disconnect()
    resultData
}

suspend fun getTokenInfoAsync(
    network: String,
    token_address: String,
) : JSONObject = withContext(Dispatchers.IO) {
    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()

    val resultArray = JSONArray()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("value", resultArray)
    }

    val query =
        "SELECT network, token_address, token_name, token_symbol, decimals, total_supply FROM token_table WHERE network = '$network' AND token_address = '$token_address'"

    connection?.use {
        val dbQueryExecutor = DBQueryExector(it)
        val resultSet = dbQueryExecutor.executeQuery(query)
        resultSet?.use {
            while (it.next()) {
                val jsonData = JSONObject().apply {
                    put("network", it.getString("network"))
                    put("token_id", it.getString("token_address"))
                    put("name", it.getString("token_name"))
                    put("symbol", it.getString("token_symbol"))
                    put("decimals", it.getString("decimals"))
                    put("total_supply", it.getString("total_supply"))
                }
                resultArray.put(jsonData)
            }
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        }
    }
    dbConnector.disconnect()
    resultData
}

suspend fun getTokenHistoryAsync(
    network: String,
    owner_account: String,
    token_address: String = "0x0000000000000000000000000000000000000000"
) : JSONObject = withContext(Dispatchers.IO) {

    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()

    val resultArray = JSONArray()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("value", resultArray)
    }

    val query =
        "SELECT " +
        " network," +
        " token_address," +
        " block_number," +
        " timestamp," +
        " transaction_hash," +
        " `from`," +
        " `to`," +
        " amount," +
        " gas_used, " +
        " (SELECT token_symbol FROM token_table WHERE network ='$network' AND token_address ='$token_address' LIMIT 1) AS symbol, " +
        " (SELECT decimals FROM token_table WHERE network ='$network' AND token_address ='$token_address' LIMIT 1) AS decimals " +
        "FROM " +
        " token_transfer_table " +
        "WHERE " +
        " network = '$network' AND token_address = '$token_address' AND (`from` ='$owner_account' OR `to` ='$owner_account')"

    connection?.use {
        val dbQueryExecutor = DBQueryExector(it)
        val resultSet = dbQueryExecutor.executeQuery(query)
        resultSet?.use {
            while (it.next()) {
                val jsonData = JSONObject().apply {
                    put("network", it.getString("network"))
                    put("token_id", it.getString("token_address"))
                    put("block_number", it.getString("block_number"))
                    put("timestamp", it.getString("timestamp"))
                    put("transaction_hash", it.getString("transaction_hash"))
                    put("from", it.getString("from"))
                    put("to", it.getString("to"))
                    put("amount", it.getString("amount"))
                    put("gas_used", it.getString("gas_used"))
                    put("symbol", it.getString("symbol"))
                    put("decimals", it.getString("decimals"))

                }
                resultArray.put(jsonData)
            }
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        }
    }

    dbConnector.disconnect()
    resultData
}

suspend fun getUsersAsync(
    owner: String
) : JSONObject = withContext(Dispatchers.IO) {

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
                val jsonData = JSONObject().apply {
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
}

suspend fun getTokenListAsync(
    network: String,
    ownerAddress: String,
    sort: String? = "DESC",
    limit: Int? = null,
    page_number: Int? = null): JSONObject = withContext(Dispatchers.IO) {
    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()

    val resultArray = JSONArray()
    val resultData = JSONObject().apply {
        put("result", "FAIL")
        put("sum", 0)
        put("value", resultArray)
    }
    var sum = 0;
    val offset = limit?.let { lim -> page_number?.minus(1)?.times(lim) } ?: 0

    var query =
    " SELECT" +
    " idx AS idx," +
    " network AS network," +
    " token_address AS token_id," +
    " owner_account AS owner," +
    " balance AS balance," +
    " (SELECT decimals FROM token_table WHERE network = t.network AND token_address = t.token_address LIMIT 1) AS decimals," +
    " (SELECT token_symbol FROM token_table WHERE network = t.network AND  token_address = t.token_address LIMIT 1) AS symbol," +
    " (SELECT token_name FROM token_table WHERE network = t.network AND  token_address = t.token_address LIMIT 1) AS name," +
    " (SELECT COUNT(*) FROM token_owner_table WHERE network = '$network' AND owner_account = '$ownerAddress') AS sum " +
    " FROM" +
    " token_owner_table t" +
    " WHERE" +
    " network = '$network' AND owner_account = '$ownerAddress'" +
    " ORDER BY" +
            " idx $sort";

    if(offset != 0) {
        query += " LIMIT $limit OFFSET $offset";
    }

    connection?.use {
        val dbQueryExecutor = DBQueryExector(it)
        val resultSet = dbQueryExecutor.executeQuery(query)
        resultSet?.use {
            while (it.next()) {
                val jsonData = JSONObject().apply {
                    put("network", it.getString("network"))
                    put("token_id", it.getString("token_id"))
                    put("owner", it.getString("owner"))
                    put("balance", it.getString("balance"))
                    put("decimals", it.getString("decimals"))
                    put("symbol", it.getString("symbol"))
                    put("name", it.getString("name"))
                }
                resultArray.put(jsonData)
                sum = it.getInt("sum")
            }
            resultData.put("result", "OK")
            resultData.put("sum", sum)
            resultData.put("value", resultArray)
        }
    }
    dbConnector.disconnect()
    resultData
}

fun signMessage(): String {
    val privateKey = ""
    val credentials = Credentials.create(privateKey)
    val hash = Hash.sha3(Numeric.toHexStringNoPrefix("TEST".toByteArray()))
    println("Hash$hash")

    val message = """
        \x19Ethereum Signed Message:
        ${hash.length}$hash
        """.trimIndent()
    val data = message.toByteArray()
    val signature = Sign.signPrefixedMessage(data, credentials.ecKeyPair)
    val r = Numeric.toHexStringNoPrefix(signature.r)
    val s = Numeric.toHexStringNoPrefix(signature.s)
    val v = Numeric.toHexStringNoPrefix(signature.v)
    return r + s + v
}

fun getSignerAddressFromSignature(signature: String): String {
    val hash = Hash.sha3(Numeric.toHexStringNoPrefix("TEST".toByteArray()))
    println("Hash$hash")

    val message = """
        \x19Ethereum Signed Message:
        ${hash.length}$hash
        """.trimIndent()
    val r = Numeric.hexStringToByteArray(signature.substring(0, 64))
    val s = Numeric.hexStringToByteArray(signature.substring(64, 128))
    val v = BigInteger(signature.substring(128), 16).toByte()

    val signData = Sign.SignatureData(v, r, s)
    val pubKey = Sign.signedPrefixedMessageToKey(message.toByteArray(Charsets.UTF_8), signData)
    return "0x" + Keys.getAddress(pubKey)
}




