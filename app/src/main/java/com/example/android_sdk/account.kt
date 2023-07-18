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
        val networkArray = arrayOf("ethereum", "klaytn", "polygon", "binance")
        val mnemonic = "ripple shrimp endorse company horror benefit boring click enter clog grab aware";
        val privateKey = "0x8d993503bb78ab5abfdad2b194bad4ae7cba9fd4590e538d232ba84c41765887";
        val token_address = "0xab40804c3da6812f41d7744fde8d6b7e8a7c30d5"
        val address = "0xDb639492E2d2A0872A6C3265163fCcC034D036b8"
        val owner = "abcuser"

        // Create accounts asynchronously
        var createAccounts = async { createAccountsAsync(networkArray) }.await()
        println(
            """
            Create Accounts:
            ${createAccounts}
            """.trimIndent()
        )
        println(
            """
            Create Accounts loaddata:
            ${loadData("ethereum")}
            """.trimIndent()
        )
        /*
        * Create Account:
        * [
        *  {network:ethereum, address:0x..},
        *  {network:klaytn, address:0x..},
        *  {network:polygon, address:0x..},
        *  {network:binace, address:0x..}
        * ]
        *
        * */

        var validAddress = async { isValidAddressAsync(address) }.await()
        println(
            """
            isValidAddress:
            ${validAddress}
            """.trimIndent()
        )
        /**
         * isValidAddress:
         * true
         */
        // Get account asynchronously to mnemonic
        var restoreAccountMnemonic = async { restoreAccountAsync("ethereum", null, mnemonic) }.await()
        println(
            """
            restoreAccountMnemonic:
            ${restoreAccountMnemonic}
            """.trimIndent()
        )
        /**
         * restoreAccountMnemonic
         * {
         *  network:ethereum,
         *  address:0x..
         * }
         */

        // Get account asynchronously to privatekey
        val restoreAccountPrivateKey = async { restoreAccountAsync("ethereum", privateKey) }.await()
        println(
            """
            getaccountPrivateKey:
            ${restoreAccountPrivateKey}
            """.trimIndent()
        )
        /**
         * restoreAccountPrivateKey
         * {
         *  network:ethereum,
         *  address:0x...
         * }
         *
         */

        // Find account info asynchronously to mainnet & address
        val getAccountInfo = async { getAccountInfoAsync("ethereum", "0xab40804c3da6812f41d7744fde8d6b7e8a7c30d5") }.await()
        println(
            """
            getAccountInfo:
            ${getAccountInfo}
            """.trimIndent()
        )
        /**
         *  getAccountInfo :
         *  {
         *      "address" : "0x",
         *      "private" : "0x.....",
         *      "mnemonic" : "MZXudDTJmKo2...",
         *      "network" : "blind nurse .."
         *  }
         */

        // Get token info asynchronously
        val tokenInfo = async {
            getTokenInfoAsync(
                networkString,
                token_address
            )
        }.await()
        println(
            """
            TokenInfo:
            ${tokenInfo}
            """.trimIndent()
        )
        /**
         * TokenInfo:
        {name : Tether, symbol : USDT, decimals: 6, total_supply : 39030615894320966}
         */

        // Get mainnet coin balance asynchronously
        val getMainnetCoinBalance = async {
            getBalanceAsync(
                networkString,
                address
            )
        }.await()
        println(
            """
            getBalance:
            ${getMainnetCoinBalance}
            """.trimIndent()
        )
        /**
         * getBalanceAsync:
        {balance : 21350.04}
         */

        // Get token balance asynchronously
        val getTokenBalance = async {
            getBalanceAsync(
                networkString,
                address,
                token_address
            )
        }.await()
        println(
            """
            getBalance:
            ${getTokenBalance}
            """.trimIndent()
        )
        /**
         * getBalanceAsync:
        {balance : 39030603.320966}
         */

        // Get token history asynchronously
        val getTokenTransferHistory = async { getTokenHistoryAsync(networkString, address, token_address) }.await()
        println(
            """
            getTokenTransferHistory:
            ${getTokenTransferHistory}
            """.trimIndent()
        )
        /**
         * [
         *   {
         *     "network": "ethereum",
         *     "token_address": "0x111111111117dC0aa78b770fA6A738034120C302",
         *     "block_number": "16500012",
         *     "timestamp": "1674844979",
         *     "transaction_hash": "0x86f518368E0d49d5916e2BD9EB162E9952b7b04d",
         *     "from": "0x788d3ea7f4acf229ca96ce3df6eade8f95ad531fa71684e06776f1976ebd4f8c",
         *     "to": "0x1111111254fb6c44bAC0beD2854e76F90643097d",
         *     "amount": "38517813190125303766",
         *     "gas_used": "152837"
         *   },
         *   {
         *     "network": "ethereum",
         *     "token_address": "0x5A98FcBEA516Cf06857215779Fd812CA3beF1B32",
         *     "block_number": "16500001",
         *     "timestamp": "1674844847",
         *     "transaction_hash": "0x7122db0Ebe4EB9B434a9F2fFE6760BC03BFbD0E0",
         *     "from": "0x703e3820c9b13b1751b47c405358401316ecc75de731d3be4c141e5d23b6e077",
         *     "to": "0x1111111254fb6c44bAC0beD2854e76F90643097d",
         *     "amount": "20115660308317876961540",
         *     "gas_used": "472518"
         *   },
         *   {
         *     "network": "ethereum",
         *     "token_address": "0xA0b86991c6218b36c1d19D4a2e9Eb0cE3606eB48",
         *     "block_number": "16500008",
         *     "timestamp": "1674844931",
         *     "transaction_hash": "0x1111111254fb6c44bAC0beD2854e76F90643097d",
         *     "from": "0xa199413aa46f4311aaa4a797ddd2e6ac7fe9a5209b7459c26e6af134b243b78f",
         *     "to": "0xcADBA199F3AC26F67f660C89d43eB1820b7f7a3b",
         *     "amount": "1413152143",
         *     "gas_used": "736607"
         *   }
         * ]
         */

        // Get User asynchronously
        val getUsers = async { getUsersAsync(owner) }.await()
        println(
            """
            getUser:
            ${getUsers}
            """.trimIndent()
        )
        /**
         * getUser:
         * [
         *   {
         *     "owner": "abcuser",
         *     "network": "ethereum",
         *     "address": "0x5Cd81e6691914557D2F74AE9A3624bfdA0de6D19",
         *     "type": "0"
         *   },
         *   {
         *     "owner": "abcuser",
         *     "network": "klaytn",
         *     "address": "0xB6a37b5d14D502c3Ab0Ae6f3a0E058BC9517786e",
         *     "type": "0"
         *   },
         *   {
         *     "owner": "abcuser",
         *     "network": "polygon",
         *     "address": "0x52101C09296E8486cCDbB7fC2d5B25b204258CCE",
         *     "type": "0"
         *   },
         *   {
         *     "owner": "abcuser",
         *     "network": "binance",
         *     "address": "0xFf32Da2b4948f0E0606D75444AC053dad590884a",
         *     "type": "0"
         *   },
         *   {
         *     "owner": "abcuser",
         *     "network": "ethereum",
         *     "address": "0x5Cd81e6691914557D2F74AE9A3624bfdA0de6D19",
         *     "type": "1"
         *   },
         *   {
         *     "owner": "abcuser",
         *     "network": "klaytn",
         *     "address": "0xB6a37b5d14D502c3Ab0Ae6f3a0E058BC9517786e",
         *     "type": "2"
         *   },
         *   {
         *     "owner": "abcuser",
         *     "network": "polygon",
         *     "address": "0x52101C09296E8486cCDbB7fC2d5B25b204258CCE",
         *     "type": "1"
         *   },
         *   {
         *     "owner": "abcuser",
         *     "network": "binance",
         *     "address": "0xFf32Da2b4948f0E0606D75444AC053dad590884a",
         *     "type": "2"
         *   }
         * ]
         */
    }
}


// Create accounts asynchronously
suspend fun createAccountsAsync(network: Array<String>): JSONArray = withContext(Dispatchers.IO) {
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

    val returnMainNet = JSONArray()

    for (network in network) {
        val saveMainNet = JSONArray()

        val returnData = JSONObject()
        returnData.put("network", network)
        returnData.put("address", credentials.address)
        val saveData = JSONObject()
        saveData.put("network", network)
        saveData.put("address", credentials.address)
        saveData.put("private", encrypt("0x${Numeric.toHexStringNoPrefix(keyPair.privateKey)}"))
        saveData.put("mnemonic", encrypt(mnemonic))

        returnMainNet.put(returnData)
        saveMainNet.put(saveData)
        saveData(network, saveMainNet.toString())
    }

    returnMainNet
}

suspend fun isValidAddressAsync(address: String): Boolean = withContext(Dispatchers.IO) {
    try {
        WalletUtils.isValidAddress(address)
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
    network: String,
    privateKey: String? = null,
    mnemonic: String? = null
): JSONObject = withContext(Dispatchers.IO) {
    val returnData = JSONObject()
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
            privateKey != null -> {
                if (!isValidPrivateKey(privateKey)) {
                    throw IllegalArgumentException("Invalid private key.")
                }
                ECKeyPair.create(Numeric.hexStringToByteArray(privateKey))
            }
            else -> throw IllegalArgumentException("Either mnemonic or privateKey must be provided.")
        }

        val credentials = Credentials.create(keyPair)

        mnemonic?.let { it } ?: ""

        returnData.put("network", network)
        returnData.put("address", credentials.address)

        if(privateKey == null){
            returnData.put("private", "")
        } else {
            returnData.put("private", encrypt(privateKey))
        }
        if(mnemonic == null){
            returnData.put("mnemonic", "")
        } else {
            returnData.put("mnemonic", encrypt(mnemonic))
        }

        val networkLoadData = JSONArray(loadData(network))
        networkLoadData.put(returnData)
        saveData(network, networkLoadData.toString())

        returnData
    }catch (e: Exception) {
        returnData.put("error", e.message)
        returnData
    }

}

suspend fun getAccountInfoAsync(network: String, address: String?): JSONObject = withContext(Dispatchers.IO) {
    val networkLoadData = JSONArray(loadData(network))
    var equalAddress: JSONObject? = null

    for (i in 0 until networkLoadData.length()) {
        val loadDataAddress = networkLoadData.getJSONObject(i)
        if (address == loadDataAddress.getString("address")) {
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
    address: String,
    token_address: String = "0x0000000000000000000000000000000000000000"
): JSONObject = withContext(Dispatchers.IO) {
    val jsonData = JSONObject()
    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    // balance, decimals 을 구하는 쿼리문
    val getBalance =
        "SELECT balance, (SELECT decimals FROM token_table WHERE token_address ='$token_address') AS decimals FROM token_owner_table WHERE network = '$network' AND account = '$address' AND token_address = '$token_address'"
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
        "SELECT network, token_address, name, symbol, decimals, total_supply FROM token_table WHERE network = '$network' AND token_address = '$token_address'"

    if (connection != null) {
        val dbQueryExector = DBQueryExector(connection)
        val getToken: ResultSet? = dbQueryExector.executeQuery(query)

        if (getToken != null) {
            try {
                while (getToken.next()) {
                    // Select data = network, token_address, block_number, timestamp, transaction_hash, from, to, amount, gas_used
                    val network = getToken.getString("network")
                    val token_address = getToken.getString("token_address")
                    val name = getToken.getString("name")
                    val symbol = getToken.getString("symbol")
                    val decimals = getToken.getString("decimals")
                    val total_supply = getToken.getString("total_supply")

                    jsonData.put("network", network)
                    jsonData.put("token_address", token_address)
                    jsonData.put("name", name)
                    jsonData.put("symbol", symbol)
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
    address: String,
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
        "SELECT network, token_address, block_number, timestamp, transaction_hash, `from`, `to`, amount, gas_used FROM token_transfer_table WHERE network = '$network' AND token_address = '$token_address' AND (`from` ='$address' OR `to` ='$address')"

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
    owner: String
) : JSONArray = withContext(Dispatchers.IO) {

    val dbConnector = DBConnector()
    dbConnector.connect()
    val connection = dbConnector.getConnection()
    val transferArray = JSONArray()

    // 매개변수 : owner
    // 테이블 : users_table
    // 조건 : owner
    val query =
        "SELECT * FROM users_table WHERE owner = '$owner'"

    if (connection != null) {
        val dbQueryExector = DBQueryExector(connection)
        val getTransfer: ResultSet? = dbQueryExector.executeQuery(query)

        if (getTransfer != null) {
            try {
                while (getTransfer.next()) {
                    val jsonData = JSONObject()
                    val owner = getTransfer.getString("owner")
                    val network = getTransfer.getString("network")
                    val address = getTransfer.getString("address")
                    val type = getTransfer.getString("type")

                    // Select data json type
                    jsonData.put("owner", owner)
                    jsonData.put("network", network)
                    jsonData.put("address", address)
                    jsonData.put("type", type)

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
