import com.example.android_sdk.*
import kotlinx.coroutines.*
import org.apache.commons.lang3.RandomUtils
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.crypto.*
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.sql.ResultSet
import java.sql.SQLException

suspend fun account() = runBlocking<Unit> {
    // Initialize the coroutine context
    coroutineScope {
        val networkString = "bnb";
        val networkArray = arrayOf("ethereum", "cypress", "polygon", "bnb")
        val network = arrayOf("ethereum","cypress")
        val mnemonic = "ripple shrimp endorse company horror benefit boring click enter clog grab aware";
        val privateKey = "0x8d993503bb78ab5abfdad2b194bad4ae7cba9fd4590e538d232ba84c41765887";
        val token_address = "0xab40804c3da6812f41d7744fde8d6b7e8a7c30d5"
        val account = "0xDb639492E2d2A0872A6C3265163fCcC034D036b8"
        val owner_eigenvalue = "abcuser"

//        // Create accounts asynchronously
//        var createAccounts = async { createAccountsAsync(networkArray) }.await()
//        println(
//            """
//            Create Accounts:
//            ${createAccounts}
//            """.trimIndent()
//        )
//        println(
//            """
//            Create Accounts loaddata:
//            ${loadData("ethereum")}
//            """.trimIndent()
//        )
//        /**
//         * Create Account:
//        [
//        {"network":"ethereum", "user_account":"0x.."},
//        {"network":"klaytn", "user_account":"0x..""},
//        {"network":"polygon", "user_account":"0x.."},
//        {"network":"binace", "user_account":"0x.."}
//        ]
//         */
//
//        var validAddress = async { isValidAddressAsync(account) }.await()
//        println(
//            """
//            isValidAddress:
//            ${validAddress}
//            """.trimIndent()
//        )
//        /**
//        isValidAddress : true
//         */
//
//        // Get account asynchronously to mnemonic
//        var restoreAccountMnemonic = async { restoreAccountAsync(network, null, mnemonic) }.await()
//        println(
//            """
//            restoreAccountMnemonic:
//            ${restoreAccountMnemonic}
//            """.trimIndent()
//        )
//        /**
//        restoreAccountMnemonic
//            {
//                "network":"ethereum",
//                "account":"0x..."
//            }
//         */
//
        // Get account asynchronously to privatekey
        val restoreAccountPrivateKey = async { restoreAccountAsync(network, privateKey) }.await()
        println(
            """
            getaccountPrivateKey:
            ${restoreAccountPrivateKey}
            """.trimIndent()
        )
        /**
        restoreAccountPrivateKey
        {
        "network":"ethereum",
        "account":"0x..."
        }
         */
//
//        // Find account info asynchronously to network & account
//        val getAccountInfo = async { getAccountInfoAsync("ethereum", "0xab40804c3da6812f41d7744fde8d6b7e8a7c30d5") }.await()
//        println(
//            """
//            getAccountInfo:
//            ${getAccountInfo}
//            """.trimIndent()
//        )
//        /**
//         * getAccountInfo :
//        {
//        "account" : "0x...",
//        "private" : "0x...",
//        "mnemonic" : "blind nurse ..",
//        "network" : "ethereum"
//        }
//         */
//
//        // Get token info asynchronously
//        val tokenInfo = async {
//            getTokenInfoAsync(
//                networkString,
//                token_address
//            )
//        }.await()
//        println(
//            """
//            TokenInfo:
//            ${tokenInfo}
//            """.trimIndent()
//        )
//        /**
//         * TokenInfo:
//        {
//        "token_name" : "Tether",
//        "token_symbol" : "USDT",
//        "decimals": "6",
//        "total_supply" : "39030615894320966"
//        }
//         */
//
//        // Get mainnet coin balance asynchronously
//        val getMainnetCoinBalance = async {
//            getBalanceAsync(
//                networkString,
//                account
//            )
//        }.await()
//        println(
//            """
//            getBalance:
//            ${getMainnetCoinBalance}
//            """.trimIndent()
//        )
//        /**
//         * getBalanceAsync:
//        {
//        "balance" : "21350.04"
//        }
//         */
//
//        // Get token balance asynchronously
//        val getTokenBalance = async {
//            getBalanceAsync(
//                networkString,
//                address,
//                token_address
//            )
//        }.await()
//        println(
//            """
//            getBalance:
//            ${getTokenBalance}
//            """.trimIndent()
//        )
//        /**
//         * getBalanceAsync:
//        {
//        "balance" : "39030603.320966"
//        }
//         */
//
//        // Get token history asynchronously
//        val getTokenTransferHistory = async { getTokenHistoryAsync(networkString, account, token_address) }.await()
//        println(
//            """
//            getTokenTransferHistory:
//            ${getTokenTransferHistory}
//            """.trimIndent()
//        )
//        /**
//         * getTokenHistoryAsync
//        [
//        {
//        "network": "ethereum",
//        "token_address": "0x111111111117dC0aa78b770fA6A738034120C302",
//        "block_number": "16500012",
//        "timestamp": "1674844979",
//        "transaction_hash": "0x86f518368E0d49d5916e2BD9EB162E9952b7b04d",
//        "from": "0x788d3ea7f4acf229ca96ce3df6eade8f95ad531fa71684e06776f1976ebd4f8c",
//        "to": "0x1111111254fb6c44bAC0beD2854e76F90643097d",
//        "amount": "38517813190125303766",
//        "gas_used": "152837"
//        },
//        {
//        "network": "ethereum",
//        "token_address": "0x5A98FcBEA516Cf06857215779Fd812CA3beF1B32",
//        "block_number": "16500001",
//        "timestamp": "1674844847",
//        "transaction_hash": "0x7122db0Ebe4EB9B434a9F2fFE6760BC03BFbD0E0",
//        "from": "0x703e3820c9b13b1751b47c405358401316ecc75de731d3be4c141e5d23b6e077",
//        "to": "0x1111111254fb6c44bAC0beD2854e76F90643097d",
//        "amount": "20115660308317876961540",
//        "gas_used": "472518"
//        },
//        {
//        "network": "ethereum",
//        "token_address": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
//        "block_number": "16500008",
//        "timestamp": "1674844931",
//        "transaction_hash": "0x1111111254fb6c44bAC0beD2854e76F90643097d",
//        "from": "0xa199413aa46f4311aaa4a797ddd2e6ac7fe9a5209b7459c26e6af134b243b78f",
//        "to": "0xcADBA199F3AC26F67f660C89d43eB1820b7f7a3b",
//        "amount": "1413152143",
//        "gas_used": "736607"
//        }
//        ]
//         */
//
//        // Get User asynchronously
//        val getUsers = async { getUsersAsync(owner_eigenvalue) }.await()
//        println(
//            """
//            getUser:
//            ${getUsers}
//            """.trimIndent()
//        )
//        /**
//         * getUser:
//        [
//        {
//        "owner_eigenvalue": "abcuser",
//        "network": "ethereum",
//        "user_account": "0x5Cd81e6691914557D2F74AE9A3624bfdA0de6D19",
//        "user_type": "0"
//        },
//        {
//        "owner_eigenvalue": "abcuser",
//        "network": "klaytn",
//        "user_account": "0xB6a37b5d14D502c3Ab0Ae6f3a0E058BC9517786e",
//        "user_type": "0"
//        },
//        {
//        "owner_eigenvalue": "abcuser",
//        "network": "polygon",
//        "user_account": "0x52101C09296E8486cCDbB7fC2d5B25b204258CCE",
//        "user_type": "0"
//        },
//        {
//        "owner_eigenvalue": "abcuser",
//        "network": "binance",
//        "user_account": "0xFf32Da2b4948f0E0606D75444AC053dad590884a",
//        "user_type": "0"
//        },
//        {
//        "owner_eigenvalue": "abcuser",
//        "network": "ethereum",
//        "user_account": "0x5Cd81e6691914557D2F74AE9A3624bfdA0de6D19",
//        "user_type": "1"
//        },
//        {
//        "owner_eigenvalue": "abcuser",
//        "network": "klaytn",
//        "user_account": "0xB6a37b5d14D502c3Ab0Ae6f3a0E058BC9517786e",
//        "user_type": "2"
//        },
//        {
//        "owner_eigenvalue": "abcuser",
//        "network": "polygon",
//        "user_account": "0x52101C09296E8486cCDbB7fC2d5B25b204258CCE",
//        "user_type": "1"
//        },
//        {
//        "owner_eigenvalue": "abcuser",
//        "network": "binance",
//        "user_account": "0xFf32Da2b4948f0E0606D75444AC053dad590884a",
//        "user_type": "2"
//        }
//        ]
//         */
    }
}


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

    var saveMainNet = JSONArray()
    val data = loadData(network[0])
    if (data != null) {
        // 기존에 있는 계정 정보 추가
        val networkLoadData = JSONArray(data)
        if (networkLoadData.length() != 0) {
            saveMainNet = networkLoadData
        }
    }

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

            //save
            val saveData = JSONObject()
            saveData.put("network", network)
            saveData.put("account", credentials.address)
            saveData.put("private", encrypt("0x${Numeric.toHexStringNoPrefix(keyPair.privateKey)}"))
            saveData.put("mnemonic", encrypt(mnemonic))
            saveMainNet.put(saveData)

            saveData(network, saveMainNet.toString())
        }

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
    network: Array<String>,
    private: String? = null,
    mnemonic: String? = null
): JSONObject = withContext(Dispatchers.IO) {

    // save array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

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

        var saveMainNet = JSONArray()
        val data = loadData(network[0])
        if (data != null) {
            // 기존에 있는 계정 정보 추가
            val networkLoadData = JSONArray(data)
            if (networkLoadData.length() != 0) {
                saveMainNet = networkLoadData
            }
        }

        for (network in network) {

            // add return value
            val returnObject = JSONObject()
            returnObject.put("network", network)
            returnObject.put("account", credentials.address)
            resultArray.put(returnObject)

            // save
            val saveObject = JSONObject()
            saveObject.put("network", network)
            saveObject.put("account", credentials.address)
            saveObject.put("private", encrypt(keyPairPrivateKey))
            if (mnemonic == null) {
                saveObject.put("mnemonic", "")
            } else {
                saveObject.put("mnemonic", encrypt(mnemonic))
            }

            saveMainNet.put(saveObject)
            saveData(network, saveMainNet.toString())
        }

        resultData.put("result", "OK")
        resultData.put("value", resultArray)

        resultData
    }catch (e: Exception) {
        resultData.put("error", e.message)
        resultData
    }

}

suspend fun getAccountInfoAsync(network: String, account: String?): JSONObject = withContext(Dispatchers.IO) {
    val networkLoadData = JSONArray(loadData(network))
    var equalAddress: JSONObject? = null

    for (i in 0 until networkLoadData.length()) {
        val loadDataAddress = networkLoadData.getJSONObject(i)
        if (account == loadDataAddress.getString("account")) {
            equalAddress = loadDataAddress
            break
        }
    }

    if (equalAddress == null) {
        equalAddress = JSONObject()
    } else {
        equalAddress.put("private", decrypt((equalAddress.getString("private"))))
        equalAddress.put("mnemonic", decrypt(equalAddress.getString("mnemonic")))
    }

    equalAddress ?: JSONObject()
}

// Get token info asynchronously
suspend fun getBalanceAsync(
    network: String,
    owner_account: String,
    token_address: String = "0x0000000000000000000000000000000000000000"
): JSONObject = withContext(Dispatchers.IO) {
    val jsonData = JSONObject()
    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    // balance, decimals 을 구하는 쿼리문
    val getBalance =
        "SELECT balance, (SELECT decimals FROM token_table WHERE token_address ='$token_address') AS decimals FROM token_owner_table WHERE network = '$network' AND owner_account = '$owner_account' AND token_address = '$token_address'"
    if (connection != null) {
        val dbQueryExector = DBQueryExector(connection)
        val dbData: ResultSet? = dbQueryExector.executeQuery(getBalance)

        if (dbData != null) {
            try {
                while (dbData.next()) {
                    val balance = dbData.getString("balance")
                    val decimals = dbData.getString("decimals")
                    // balance / 10 ^ decimals
                    var newBalance =
                        BigDecimal(balance.toDouble()).divide(BigDecimal.TEN.pow(decimals.toInt()))
                    jsonData.put("balance", newBalance)

                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                dbData.close() //
            }
        }
    }
    dbConnector.disconnect()
    jsonData
}

suspend fun getTokenInfoAsync(
    network: String,
    token_address: String,
) : JSONObject = withContext(Dispatchers.IO) {
    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val jsonData = JSONObject()
    val query =
        "SELECT network, token_address, token_name, token_symbol, decimals, total_supply FROM token_table WHERE network = '$network' AND token_address = '$token_address'"

    if (connection != null) {
        val dbQueryExector = DBQueryExector(connection)
        val getToken: ResultSet? = dbQueryExector.executeQuery(query)

        if (getToken != null) {
            try {
                while (getToken.next()) {
                    val network = getToken.getString("network")
                    val token_address = getToken.getString("token_address")
                    val token_name = getToken.getString("token_name")
                    val token_symbol = getToken.getString("token_symbol")
                    val decimals = getToken.getString("decimals")
                    val total_supply = getToken.getString("total_supply")

                    jsonData.put("network", network)
                    jsonData.put("token_address", token_address)
                    jsonData.put("token_name", token_name)
                    jsonData.put("token_symbol", token_symbol)
                    jsonData.put("decimals", decimals)
                    jsonData.put("total_supply", total_supply)

                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                getToken.close() //
            }
        }
    }
    dbConnector.disconnect()
    jsonData
}

suspend fun getTokenHistoryAsync(
    network: String,
    owner_account: String,
    token_address: String = "0x0000000000000000000000000000000000000000"
) : JSONArray = withContext(Dispatchers.IO) {

    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val jsonData = JSONObject()
    val transferArray = JSONArray()

    // 매개변수 : network, token_address, account
    // 테이블 : token_transfer_table
    // 조건 : network & (from == account || to == account)
    val query =
        "SELECT network, token_address, block_number, timestamp, transaction_hash, `from`, `to`, amount, gas_used FROM token_transfer_table WHERE network = '$network' AND token_address = '$token_address' AND (`from` ='$owner_account' OR `to` ='$owner_account')"

    if (connection != null) {
        val dbQueryExector = DBQueryExector(connection)
        val getTransfer: ResultSet? = dbQueryExector.executeQuery(query)

        if (getTransfer != null) {
            try {
                while (getTransfer.next()) {

                    // Select data = network, token_address, block_number, timestamp, transaction_hash, from, to, amount, gas_used
                    val network = getTransfer.getString("network")
                    val token_address = getTransfer.getString("token_address")
                    val block_number = getTransfer.getString("block_number")
                    val timestamp = getTransfer.getString("timestamp")
                    val transaction_hash = getTransfer.getString("from")
                    val from = getTransfer.getString("transaction_hash")
                    val to = getTransfer.getString("to")
                    val amount = getTransfer.getString("amount")
                    val gas_used = getTransfer.getString("gas_used")

                    // Select data json type
                    jsonData.put("network", network)
                    jsonData.put("token_address", token_address)
                    jsonData.put("block_number", block_number)
                    jsonData.put("timestamp", timestamp)
                    jsonData.put("transaction_hash", transaction_hash)
                    jsonData.put("from", from)
                    jsonData.put("to", to)
                    jsonData.put("amount", amount)
                    jsonData.put("gas_used", gas_used)

                    transferArray.put(jsonData)
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                getTransfer.close() //
            }
        }
    }
    dbConnector.disconnect()
    transferArray
}

suspend fun getUsersAsync(
    owner_eigenvalue: String
) : JSONArray = withContext(Dispatchers.IO) {

    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val transferArray = JSONArray()

    val query =
        "SELECT * FROM users_table WHERE owner_eigenvalue = '$owner_eigenvalue'"

    if (connection != null) {
        val dbQueryExector = DBQueryExector(connection)
        val getTransfer: ResultSet? = dbQueryExector.executeQuery(query)

        if (getTransfer != null) {
            try {
                while (getTransfer.next()) {
                    val jsonData = JSONObject()
                    val owner_eigenvalue = getTransfer.getString("owner_eigenvalue")
                    val network = getTransfer.getString("network")
                    val user_account = getTransfer.getString("user_account")
                    val user_type = getTransfer.getString("user_type")

                    // Select data json type
                    jsonData.put("owner_eigenvalue", owner_eigenvalue)
                    jsonData.put("network", network)
                    jsonData.put("user_account", user_account)
                    jsonData.put("user_type", user_type)

                    transferArray.put(jsonData)
                }
            } catch (ex: SQLException) {
                ex.printStackTrace()
            } finally {
                getTransfer.close() //
            }
        }
    }
    dbConnector.disconnect()
    transferArray
}

