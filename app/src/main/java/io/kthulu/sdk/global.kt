package io.kthulu.sdk

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint32
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.security.KeyFactory
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.PrivateKey
import java.security.PublicKey
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.time.Instant
import java.util.Base64
import javax.crypto.Cipher

suspend fun kthuluSdkVersion() {
//    println("SDK version:1.0.0, Connect OK")
    val resultArray = JSONArray()
    var resultData = JSONObject()
    val jsonData = JSONObject()
    jsonData.put("version", "SDK version:1.0.0, Connect OK")
    resultArray.put(jsonData)
    resultData.put("result", "OK")
    resultData.put("value", resultArray)

    println(resultData)
}

var rpcUrl = "";
var bridgeConfigContractAddress = "";
var bridgeContractAddress = "";
var nftTransferContractAddress = "";
var bridgeSetupContractAddress = "";
var uniswapV2RouterAddress = "";
var uniswapV2FactoryAddress = "";
var maxPriorityFeePerGas = "";
var gasLimit = "";

fun networkSettings(network: String) {
    rpcUrl = when (network) {
        "ethereum" -> "https://mainnet.infura.io/v3/02c509fda7da4fed882ac537046cfd66"
        "cypress" -> "https://rpc.ankr.com/klaytn"
        "polygon" -> "https://rpc-mainnet.maticvigil.com/v1/96ab7849c9d3f105416383dd284c3f7e6511208c"
        "bnb" -> "https://bsc-dataseed.binance.org"
        "sepolia" -> "https://rpc.sepolia.org"
        "baobab" -> "https://api.baobab.klaytn.net:8651"
        "mumbai" -> "https://polygon-mumbai.infura.io/v3/4458cf4d1689497b9a38b1d6bbf05e78"
        "tbnb" -> "https://data-seed-prebsc-1-s1.binance.org:8545"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    maxPriorityFeePerGas = when (network) {
        "ethereum" -> "2000000000"
        "cypress" -> "0"
        "polygon" -> "50000000000"
        "bnb" -> "0"
        "sepolia" -> "10"
        "baobab" -> "0"
        "mumbai" -> "1500000000"
        "tbnb" -> "0"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    bridgeConfigContractAddress = when (network) {
        "ethereum" -> "0xf643a4fb01cbbfb561cc906c1f37d5718ef3bba3"
        "cypress" -> "0x33fcf21e795447cc1668ef2ca06dbf78eb180763"
        "polygon" -> "0xf643a4fb01cbbfb561cc906c1f37d5718ef3bba3"
        "bnb" -> "0xf643a4fb01cbbfb561cc906c1f37d5718ef3bba3"
        "sepolia" -> "0x9b0125085ccfec4e697b2e0d4ce0e3fe5f5eaf53"
        "baobab" -> "0xd3cb20c7476d544d9b34878ea352cd460125b34c"
        "mumbai" -> "0xd6012f44f054a811ffae39c69660d3a759dae7e8"
        "tbnb" -> ""
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    bridgeContractAddress = when (network) {
        "ethereum" -> "0x7362fa30ada8ccf2130017f2a8f0b6be78aa38de"
        "cypress" -> "0xb7e2b748364c7d38311444a62a57d76dd697e99b"
        "polygon" -> "0x7362fa30ada8ccf2130017f2a8f0b6be78aa38de"
        "bnb" -> "0x873caf09b6668db216191a0121bc481e261643b3"
        "sepolia" -> "0x99c0c3fff607ea8c1c6e7ef66a7410bf9ab5db94"
        "baobab" -> "0x68bfcdfd034f050e65ae1de11103c94dc2e7747d"
        "mumbai" -> "0x9eab88e535d6676649c4b0b7f783ee53a29624df"
        "tbnb" -> "0x808EE7147d91EAe0f658164248402ac380EB5F17"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    nftTransferContractAddress = when (network) {
        "ethereum" -> "0x9a1c0ef3989f944e692232d491fe5395927be9bd"
        "cypress" -> "0x534d102f2bf1bcad450c8a5da6e1cfb6cdb93b2f"
        "polygon" -> "0x9a1c0ef3989f944e692232d491fe5395927be9bd"
        "bnb" -> "0x718e40874dac43d840f1e9bb135c3c098174e832"
        "sepolia" -> "0xcab822accb20114c715aad8ca3315204f6f81d90"
        "baobab" -> "0x7ae6b96456d8ba526c8615b75e3c22f7d955b30b"
        "mumbai" -> "0xe3b3e095c50e3e0c202e496e5bc94df2ec59eef5"
        "tbnb" -> ""
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    bridgeSetupContractAddress = when (network) {
        "ethereum" -> "0x3cf93d43251324c527346abf3e0559f4c7a713d1"
        "cypress" -> "0x41ec118425e4d13b509382e97cdc3f09dbba8fd9"
        "polygon" -> "0x4f5d095ccda117e168ea58bcccffafb9c3617491"
        "bnb" -> "0x35baced894af326573a85565c1cf3aed54394b60"
        "sepolia" -> "0x65c3ff55f9d32e2144b25f20b3f833056d22e97c"
        "baobab" -> "0xa2b3ab85d49caed1b125532d4e6ed8f1153d7929"
        "mumbai" -> "0x09d51e8bbe25a114e8e1c6f280153dcc041eb1ef"
        "tbnb" -> ""
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    uniswapV2RouterAddress = when (network) {
        "ethereum" -> "0x7a250d5630B4cF539739dF2C5dAcb4c659F2488D"
        "cypress" -> ""
        "polygon" -> "0xa5E0829CaCEd8fFDD4De3c43696c57F7D7A678ff"
        "bnb" -> "0x05fF2B0DB69458A0750badebc4f9e13aDd608C7F"
        "sepolia" -> ""
        "baobab" -> ""
        "mumbai" -> ""
        "tbnb" -> ""
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    uniswapV2FactoryAddress = when (network) {
        "ethereum" -> "0x5C69bEe701ef814a2B6a3EDD4B1652CB9cc5aA6f"
        "cypress" -> ""
        "polygon" -> "0x5757371414417b8C6CAad45bAeF941aBc7d3Ab32"
        "bnb" -> "0xBCfCcbde45cE874adCB698cC183deBcF17952812"
        "sepolia" -> ""
        "baobab" -> ""
        "mumbai" -> ""
        "tbnb" -> ""
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    gasLimit = when (network) {
        "ethereum" -> "200000"
        "cypress" -> "2000000"
        "polygon" -> "500000"
        "bnb" -> "2000000"
        "sepolia" -> "0"
        "baobab" -> "5000000"
        "mumbai" -> "5500000"
        "tbnb" -> "0"
        else -> throw IllegalArgumentException("Invalid main network type")
    }

}

// Create RSA key
fun generateRSAKeyPair(): KeyPair {
    val keyGen = KeyPairGenerator.getInstance("RSA")
    keyGen.initialize(2048)
    return keyGen.generateKeyPair()
}

// PublicKey와 PrivateKey 초기화 및 저장
fun initializeKeyPair() {
    val keyPair = generateRSAKeyPair()
    val publicKey = keyPair.public
    val privateKey = keyPair.private
    saveData("public_key", android.util.Base64.encodeToString(publicKey.encoded, android.util.Base64.DEFAULT))
    saveData("private_key", android.util.Base64.encodeToString(privateKey.encoded, android.util.Base64.DEFAULT))
}

// Encrypting
fun encrypt(input: String): String {
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.ENCRYPT_MODE, getPublicKey())
    val encrypt = cipher.doFinal(input.toByteArray())
    return Base64.getEncoder().encodeToString(encrypt)
}

// Decrypting
fun decrypt(input: String): String {
    var byteEncrypt: ByteArray = Base64.getDecoder().decode(input)
    val cipher = Cipher.getInstance("RSA")
    cipher.init(Cipher.DECRYPT_MODE, getPrivateKey())
    val decrypt = cipher.doFinal(byteEncrypt)
    return String(decrypt)
}

// PublicKey 불러오기
fun getPublicKey(): PublicKey? {
    val encodedKey = loadData("public_key")
    if (encodedKey == null) {
        initializeKeyPair()
    }
    return loadData("public_key")?.let {
        val keyBytes = android.util.Base64.decode(it, android.util.Base64.DEFAULT)
        val keySpec = X509EncodedKeySpec(keyBytes)
        KeyFactory.getInstance("RSA").generatePublic(keySpec)
    }
}

// PrivateKey 불러오기
fun getPrivateKey(): PrivateKey? {
    val encodedKey = loadData("private_key")
    val keyBytes = android.util.Base64.decode(encodedKey, android.util.Base64.DEFAULT)
    val keySpec = PKCS8EncodedKeySpec(keyBytes)
    val keyFactory = KeyFactory.getInstance("RSA")
    return keyFactory.generatePrivate(keySpec)
}

// 데이터 저장
fun saveData(key: String, value: String) {
    val sharedPreferences = MyContext.context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.putString(key, value)
    editor.apply()
}

// 데이터 불러오기
fun loadData(key: String): String? {
    val sharedPreferences = MyContext.context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    return sharedPreferences.getString(key, null)
}

// 데이터 삭제
fun removeData(key: String) {
    val sharedPreferences = MyContext.context.getSharedPreferences("my_preferences", Context.MODE_PRIVATE)
    val editor = sharedPreferences.edit()
    editor.remove(key)
    editor.apply()
}

suspend fun getEstimateGasAsync(
    network: String,
    tx_type: String,
    token_address: String? = null,
    from: String? = null,
    to: String? = null,
    amount: String? = null,
    token_id: String? = null,
    to_token_address: String? = null,
    to_network: String? = null,
    batch_token_id: Array<String>? = null,
    batch_token_amount: Array<String>? = null,
    name: String? = null,
    symbol: String? = null,
    base_uri: String? = null,
    uri_type: String? = null,
    token_uri: String? = null,
    batch_token_uri: Array<String>? = null,
    start_id: String? = null,
    end_id: String? = null
): JSONObject = withContext(Dispatchers.IO) {

    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()
    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val web3 = Web3j.build(HttpService(rpcUrl))
        val gasPrice = web3.ethGasPrice().sendAsync().get().gasPrice
        var result = BigInteger.ZERO;
        when (tx_type) {
            "baseFee" -> result = gasPrice
            "transferCoin" ->
                result = web3.ethEstimateGas(
                    Transaction.createEtherTransaction(
                        from,
                        BigInteger.ONE,
                        gasPrice,
                        BigInteger.ZERO, // temporary gasLimit
                        to,
                        Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger() // value
                    )
                ).send().amountUsed

            "transferERC20" ->
                if (token_address != null && to != null && from != null && amount != null) {
                    // Ensure amount is a valid number
                    if (BigDecimal(amount) <= BigDecimal.ZERO) BigInteger.ZERO

                    val decimalsFunction = Function("decimals", emptyList(), listOf(object : TypeReference<Uint8>() {}))
                    val encodedDecimalsFunction = FunctionEncoder.encode(decimalsFunction)
                    val decimalsResponse = web3.ethCall(
                        Transaction.createEthCallTransaction(null, token_address, encodedDecimalsFunction),
                        DefaultBlockParameterName.LATEST
                    ).send()
                    val decimalsOutput =
                        FunctionReturnDecoder.decode(decimalsResponse.result, decimalsFunction.outputParameters)
                    val decimals = (decimalsOutput[0].value as BigInteger).toInt()
                    val decimalMultiplier = BigDecimal.TEN.pow(decimals.toInt())
                    val tokenAmount = BigDecimal(amount).multiply(decimalMultiplier).toBigInteger()

                    val function = Function(
                        "transfer",
                        listOf(Address(to), Uint256(tokenAmount)),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            token_address,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "deployERC20" ->
                if (name != null && symbol != null && from != null && amount != null) {
                    val decimals = "18"
                    val decimalMultiplier = BigDecimal.TEN.pow(decimals.toInt())
                    val tokenAmount = BigDecimal(amount).multiply(decimalMultiplier).toBigInteger()
                    val function = Function(
                        "deployWrapped20",
                        listOf(Utf8String(name), Utf8String(symbol), Uint8(BigInteger(decimals)), Uint256(tokenAmount)),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            bridgeContractAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed

                }

            "bridgeToken" ->
                if (from != null && amount != null) {
                    val function = Function(
                        "moveFromETHER",
                        listOf(Uint256("KLAYTNs".toBigInteger())),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            bridgeContractAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "swapToken" ->
                if (from != null && token_address != null && amount != null && to_token_address != null) {
                    val path =
                        DynamicArray(Address::class.java, listOf(Address(token_address), Address(to_token_address)))
                    // Deadline is the current time + 10 minutes in seconds
                    val deadline = Instant.now().epochSecond + 600
                    val function = Function(
                        "swapExactTokensForTokens",
                        listOf(
                            Uint256(BigInteger(amount)),
                            Uint256(BigInteger.ZERO),
                            path,
                            Address(from),
                            Uint256(deadline)
                        ),
                        emptyList()
                    )

                    val encodedFunction = FunctionEncoder.encode(function)
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            uniswapV2RouterAddress,
                            BigInteger.ZERO, // value
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "transferERC721" ->
                if (token_id != null && to != null && from != null && token_address != null) {
                    val function = Function(
                        "safeTransferFrom",
                        listOf(Address(from), Address(to), Uint256(BigInteger(token_id))),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            token_address,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "transferERC1155" ->
                if (token_id != null && to != null && from != null && token_address != null && amount != null) {
                    val function = Function(
                        "safeTransferFrom",
                        listOf(
                            Address(from), Address(to), Uint256(BigInteger(token_id)),
                            Uint256(BigInteger(amount)), DynamicBytes(byteArrayOf(0))
                        ),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            token_address,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "batchTransferERC721" ->
                if (token_address != null && to != null && from != null && batch_token_id != null) {
                    val batchTokenId = batch_token_id.map { Uint256(BigInteger(it)) }
                    val function = Function(
                        "transferFromBatch",
                        listOf(
                            Address(from), Address(to), DynamicArray(batchTokenId)
                        ),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            token_address,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "batchTransferERC1155" ->
                if (token_address != null && to != null && from != null && batch_token_id != null && batch_token_amount != null) {
                    val batchTokenId = batch_token_id.map { Uint256(BigInteger(it)) }
                    val batchAmount = batch_token_amount.map { Uint256(BigInteger(it)) }
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

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            token_address,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "deployERC721" ->
                if (name != null && symbol != null && from != null && base_uri != null && uri_type != null) {
                    val function = Function(
                        "deployWrapped721",
                        listOf(Utf8String(name), Utf8String(symbol), Utf8String(base_uri), Uint8(BigInteger(uri_type))),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            nftTransferContractAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "deployERC1155" ->
                if (name != null && symbol != null && from != null && base_uri != null && uri_type != null) {
                    val function = Function(
                        "deployWrapped1155",
                        listOf(Utf8String(name), Utf8String(symbol), Utf8String(base_uri), Uint8(BigInteger(uri_type))),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            nftTransferContractAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "mintERC721" ->
                if (from != null && to != null && token_uri != null && token_id != null && token_address != null) {
                    val function = Function(
                        "mint",
                        listOf(Address(to), Uint256(BigInteger(token_id)), Utf8String(token_uri)),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            token_address,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "mintERC1155" ->
                if (from != null && to != null && token_uri != null && token_id != null && token_address != null && amount != null) {
                    val function = Function(
                        "mint",
                        listOf(
                            Address(to),
                            Uint256(BigInteger(token_id)),
                            Uint256(BigInteger(amount)),
                            Utf8String(token_uri)
                        ),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            token_address,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "batchMintERC721" ->
                if (from != null && to != null && batch_token_uri != null && start_id != null && end_id != null && token_address != null) {

                    val b = batch_token_uri.map { Utf8String(it) }

                    val function = Function(
                        "mintBatch",
                        listOf(
                            Address(to),
                            Uint256(BigInteger(start_id)),
                            Uint256(BigInteger(end_id)),
                            DynamicArray(b)
                        ),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            token_address,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "batchMintERC1155" ->
                if (from != null && to != null && batch_token_uri != null && batch_token_id != null && token_address != null && batch_token_amount != null) {
                    val a = batch_token_id.map { Uint256(BigInteger(it)) }
                    val b = batch_token_amount.map { Uint256(BigInteger(it)) }
                    val c = batch_token_uri.map { Utf8String(it) }

                    val function = Function(
                        "mintBatch",
                        listOf(Address(to), DynamicArray(a), DynamicArray(b), DynamicArray(c)),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            token_address,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "burnERC721" ->
                if (from != null && token_id != null && token_address != null) {
                    val function = Function(
                        "burn",
                        listOf(Uint256(BigInteger(token_id))),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            token_address,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }

            "burnERC1155" ->
                if (from != null && token_id != null && token_address != null && amount != null) {
                    val function = Function(
                        "burn",
                        listOf(Address(from), Uint256(BigInteger(token_id)), Uint256(BigInteger(amount))),
                        emptyList()
                    )
                    val encodedFunction = FunctionEncoder.encode(function)

                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            from,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            token_address,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                }
        }
        result = BigDecimal(result).multiply(BigDecimal(1.2)).setScale(0, RoundingMode.DOWN).toBigInteger()

        jsonData.put("gas", result)
        resultArray.put(jsonData)
        resultData.put("result", "OK")
        resultData.put("value", resultArray)
    } catch (e: Exception) {
        jsonData.put("error", e.message)
        resultArray.put(jsonData)
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
    }
}

fun textToHex(text: String): String {
    if (text.isEmpty()) return "0x00"
    return text.map { it.toInt().toString(16).padStart(2, '0') }.joinToString("")
}

suspend fun getNetworkFeeAsync(network: String, toNetwork: String, type: String): JSONObject =
    withContext(Dispatchers.IO) {
        networkSettings(network)
        val jsonData = JSONObject()
        // return array & object
        var resultArray = JSONArray()
        var resultData = JSONObject()
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)
        try {
            val web3j = Web3j.build(HttpService(rpcUrl))
            val hex = textToHex(toNetwork)
            // Convert hex string to BigInteger
            val toNetworkHex = BigInteger(hex, 16)
            val networkFeeIdxFunction = Function("getNetworkFeeIdxByName", listOf(Uint256(toNetworkHex)), emptyList())
            val encodedNetworkFeeIdxFunction = FunctionEncoder.encode(networkFeeIdxFunction)
            val networkFeeIdxResponse = web3j.ethCall(
                Transaction.createEthCallTransaction(null, bridgeConfigContractAddress, encodedNetworkFeeIdxFunction),
                DefaultBlockParameterName.LATEST
            ).send()

            val networkFeeIdx = BigInteger(networkFeeIdxResponse.result.replace("0x", ""), 16)

            println("networkFeeIdx $networkFeeIdx")

            val networkFeeFunction = Function("getNetworkFeeByIdx", listOf(Uint32(networkFeeIdx)), emptyList())
            val encodedNetworkFeeFunction = FunctionEncoder.encode(networkFeeFunction)
            val networkFeeResponse = web3j.ethCall(
                Transaction.createEthCallTransaction(null, bridgeConfigContractAddress, encodedNetworkFeeFunction),
                DefaultBlockParameterName.LATEST
            ).send()

            // Assuming each value is of length 64 characters (32 bytes, which is standard for Ethereum)
            //    val networkHex = networkFeeResponse.result.substring(2, 66)
            val tokenFeeHex = networkFeeResponse.result.substring(66, 130)
            val nftFeeHex = networkFeeResponse.result.substring(130, 194)
            val regFeeHex = networkFeeResponse.result.substring(194, 258)

            //    val network = String(BigInteger(networkHex, 16).toByteArray())
            val tokenFee = BigInteger(tokenFeeHex, 16)
            val nftFee = BigInteger(nftFeeHex, 16)
            val regFee = BigInteger(regFeeHex, 16)

            val resultFee = when (type) {
                "nft" -> nftFee
                "token" -> tokenFee
                "setup" -> regFee
                else -> throw IllegalArgumentException("Invalid main network type")
            }

            jsonData.put("networkFee", resultFee)
            resultArray.put(jsonData)
            resultData.put("result", "OK")
            resultData.put("value", resultArray)
        } catch (e: Exception) {
            jsonData.put("error", e.message)
            resultArray.put(jsonData)
            resultData.put("result", "FAIL")
            resultData.put("value", resultArray)
        }
    }

suspend fun getNodeHomeAsync(network: String, toNetwork: String, token_address: String, typed: String): JSONObject =
    withContext(Dispatchers.IO) {
        networkSettings(network)
        var jsonData = JSONObject()
        // return array & object
        var resultArray = JSONArray()
        var resultData = JSONObject()
        resultData.put("result", "FAIL")
        resultData.put("value", resultArray)

        try {
            val web3j = Web3j.build(HttpService(rpcUrl))
            val hex = textToHex(toNetwork)
            // Convert hex string to BigInteger
            val toNetworkHex = BigInteger(hex, 16)

            val customNhidFunction = Function("customNhid", listOf(Address(token_address)), emptyList())
            val encodedCustomNhidFunction = FunctionEncoder.encode(customNhidFunction)
            val customNhidResponse = web3j.ethCall(
                Transaction.createEthCallTransaction(null, bridgeConfigContractAddress, encodedCustomNhidFunction),
                DefaultBlockParameterName.LATEST
            ).send()

            val customNhid = BigInteger(customNhidResponse.result.replace("0x", ""), 16)

            if (customNhid != BigInteger.ZERO) {
                val getTokNetworkFunction =
                    Function("getToNetwork", listOf(Address(token_address), Uint256(toNetworkHex)), emptyList())
                val encodedGetTokNetworkFunction = FunctionEncoder.encode(getTokNetworkFunction)
                val getTokNetworkResponse = web3j.ethCall(
                    Transaction.createEthCallTransaction(
                        null,
                        bridgeConfigContractAddress,
                        encodedGetTokNetworkFunction
                    ),
                    DefaultBlockParameterName.LATEST
                ).send()

                val getTokNetwork = BigInteger(getTokNetworkResponse.result.replace("0x", ""), 16)

                if (getTokNetwork != BigInteger.ZERO) {
                    resultArray = JSONArray()
                    jsonData.put("type", typed)
                    resultArray.put(jsonData)
                    resultData.put("result", "OK")
                    resultData.put("value", resultArray)
                } else {
                    resultArray = JSONArray()
                    jsonData.put("type", "setup")
                    resultArray.put(jsonData)
                    resultData.put("result", "OK")
                    resultData.put("value", resultArray)
                }
            } else {
                resultArray = JSONArray()
                jsonData.put("type", "setup")
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
