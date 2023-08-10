package com.example.android_sdk

import android.content.Context
import getTokenInfoAsync
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.DynamicBytes
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
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
import java.util.Base64
import javax.crypto.Cipher

var rpcUrl ="";
var erc20DeployContractAddress = "";
var erc721DeployContractAddress = "";
var erc1155DeployContractAddress = "";

fun networkSettings(network: String) {
    rpcUrl = when (network) {
        "ethereum" -> "https://mainnet.infura.io/v3/02c509fda7da4fed882ac537046cfd66"
        "cypress" -> "https://rpc.ankr.com/klaytn"
        "polygon" -> "https://rpc-mainnet.maticvigil.com/v1/96ab7849c9d3f105416383dd284c3f7e6511208c"
        "bnb" -> "https://bsc-dataseed.binance.org"
        "goerli" -> "https://goerli.infura.io/v3/9aa3d95b3bc440fa88ea12eaa4456161"
        "baobab" -> "https://api.baobab.klaytn.net:8651"
        "mumbai" -> "https://polygon-mumbai.infura.io/v3/4458cf4d1689497b9a38b1d6bbf05e78"
        "tbnb" -> "https://data-seed-prebsc-1-s1.binance.org:8545"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    erc20DeployContractAddress = when (network) {
        "ethereum" -> ""
        "cypress" -> ""
        "polygon" -> "0x96856126a6bb4870cDD3e179004CD18cEf569044"
        "bnb" -> ""
        "goerli" -> "0xc11735Ce3c155E755bC9839A5B5d06dEa0482306"
        "baobab" -> "0x808ee7147d91eae0f658164248402ac380eb5f17"
        "mumbai" -> "0x95f34cD3FE7ca6273f7EaFcA35E65A36aa8894cC"
        "tbnb" -> "0x808EE7147d91EAe0f658164248402ac380EB5F17"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    erc721DeployContractAddress = when (network) {
        "ethereum" -> ""
        "cypress" -> ""
        "polygon" -> "0x780A19638D126d59f4Ed048Ae1e0DC77DAf39a77"
        "bnb" -> ""
        "goerli" -> "0x4F6b53a83c71EF127FE6e3f76f666A064116E201"
        "baobab" -> "0x780A19638D126d59f4Ed048Ae1e0DC77DAf39a77"
        "mumbai" -> "0xE00838B7948833cf14935489bAF52F2d8d0c2d23"
        "tbnb" -> "0xB668Bd1358442ba36eb9f2E00B2E79b2c6F1bD98"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
    erc1155DeployContractAddress = when (network) {
        "ethereum" -> ""
        "cypress" -> ""
        "polygon" -> "0x7E055Cb85FBE64da619865Df8a392d12f009aD81"
        "bnb" -> ""
        "goerli" -> "0xFEA394a312369b7772513cF856ce4424C1756F2C"
        "baobab" -> "0x96856126a6bb4870cdd3e179004cd18cef569044"
        "mumbai" -> "0x57040e8b36AD23BB766572cED73A1daC6596d375"
        "tbnb" -> "0x23205635BcFAEeb236360D35731d708415246DAC"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
}

// Create RSA key
fun generateRSAKeyPair() : KeyPair {
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
    return  Base64.getEncoder().encodeToString(encrypt)
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
    txType: String,
    tokenAddress: String? = null,
    fromAddress: String? = null,
    toAddress: String? = null,
    tokenAmount: String? = null,
    tokenId: String? = null,
    toTokenAddress: String? = null,
    toNetwork: String? = null,
    decimals: Int? = null,
    batchTokenId: Array<String>? = null,
    batchTokenAmount: Array<String>? = null,
    name: String? = null,
    symbol: String? = null,
    owner: String? = null,
    baseURI: String? = null,
    uriType: String? = null,
    tokenURI: String? = null,
    batchTokenURI: Array<String>? = null,
): BigInteger = withContext(Dispatchers.IO) {
    networkSettings(network)
    var result = BigInteger.ZERO;
    val web3 = Web3j.build(HttpService(rpcUrl))
    val gasPrice = web3.ethGasPrice().sendAsync().get().gasPrice

    when(txType) {
        "baseFee" -> result = gasPrice
        "transferCoin" ->
            try {
                result = web3.ethEstimateGas(
                    Transaction.createEtherTransaction(
                        fromAddress,
                        BigInteger.ONE,
                        gasPrice,
                        BigInteger.ZERO, // temporary gasLimit
                        toAddress,
                        Convert.toWei(tokenAmount, Convert.Unit.ETHER).toBigInteger() // value
                    )
                ).send().amountUsed
            } catch (ex: Exception) {
                // Handle the exception appropriately
                result = BigInteger.ZERO
            }
        "transferERC20" ->
            if (tokenAddress != null && toAddress != null && fromAddress != null && tokenAmount != null) {
                // Ensure amount is a valid number
                if (BigDecimal(tokenAmount) <= BigDecimal.ZERO) BigInteger.ZERO

                val decimalsFunction = Function("decimals", emptyList(), listOf(object : TypeReference<Uint8>() {}))
                val encodedDecimalsFunction = FunctionEncoder.encode(decimalsFunction)
                val decimalsResponse = web3.ethCall(
                    Transaction.createEthCallTransaction(null, tokenAddress, encodedDecimalsFunction),
                    DefaultBlockParameterName.LATEST
                ).send()
                val decimalsOutput =
                    FunctionReturnDecoder.decode(decimalsResponse.result, decimalsFunction.outputParameters)
                val decimals = (decimalsOutput[0].value as BigInteger).toInt()
                val decimalMultiplier = BigDecimal.TEN.pow(decimals.toInt())
                val tokenAmount = BigDecimal(tokenAmount).multiply(decimalMultiplier).toBigInteger()

                val function = Function(
                    "transfer",
                    listOf(Address(toAddress), Uint256(tokenAmount)),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            tokenAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "deployERC20" ->
            if (name != null && symbol != null && fromAddress != null && tokenAmount != null) {
                val function = Function(
                    "deployedERC20",
                    listOf(Utf8String(name), Utf8String(symbol), Uint256(BigInteger(tokenAmount)), Address(fromAddress)),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)
                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            erc20DeployContractAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "transferERC721" ->
            if (tokenAddress != null && toAddress != null && fromAddress != null && tokenId != null) {
                val function = Function(
                    "safeTransferFrom",
                    listOf(Address(fromAddress), Address(toAddress), Uint256(BigInteger(tokenId))),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            tokenAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "transferERC1155" ->
            if (tokenAddress != null && toAddress != null && fromAddress != null && tokenId != null && tokenAmount != null) {
                val function = Function(
                    "safeTransferFrom",
                    listOf(
                        Address(fromAddress), Address(toAddress), Uint256(BigInteger(tokenId)),
                        Uint256(BigInteger(tokenAmount)), DynamicBytes(byteArrayOf(0))
                    ),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            tokenAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "batchTransferERC721" ->
            if (tokenAddress != null && toAddress != null && fromAddress != null && batchTokenId != null) {
                val batchTokenId = batchTokenId.map { Uint256(BigInteger(it)) }
                val function = Function(
                    "safeBatchTransferFrom",
                    listOf(
                        Address(fromAddress), Address(toAddress), DynamicArray(batchTokenId)
                    ),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            tokenAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "batchTransferERC1155" ->
            if (tokenAddress != null && toAddress != null && fromAddress != null && batchTokenId != null && batchTokenAmount != null) {
                val batchTokenId = batchTokenId.map { Uint256(BigInteger(it)) }
                val batchAmount = batchTokenAmount.map { Uint256(BigInteger(it)) }
                val function = Function(
                    "safeBatchTransferFrom",
                    listOf(
                        Address(fromAddress), Address(toAddress), DynamicArray(batchTokenId), DynamicArray(batchAmount), DynamicBytes(byteArrayOf(0))
                    ),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            tokenAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "deployERC721" ->
            if (name != null && symbol != null && fromAddress != null && owner != null && baseURI != null && uriType != null) {
                val function = Function(
                    "deployedERC721",
                    listOf(Utf8String(name), Utf8String(symbol), Utf8String(baseURI), Uint8(BigInteger(uriType)), Address(owner)),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            erc721DeployContractAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "deployERC1155" ->
            if (name != null && symbol != null && fromAddress != null && owner != null && baseURI != null && uriType != null) {
                val function = Function(
                    "deployedERC1155",
                    listOf(Utf8String(name), Utf8String(symbol), Utf8String(baseURI), Uint8(BigInteger(uriType)), Address(owner)),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            erc1155DeployContractAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "mintERC721" ->
            if (fromAddress != null && toAddress != null && tokenURI != null && tokenId != null && tokenAddress != null) {
                val function = Function(
                    "mint",
                    listOf(Address(toAddress), Uint256(BigInteger(tokenId)), Utf8String(tokenURI)),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            tokenAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "mintERC1155" ->
            if (fromAddress != null && toAddress != null && tokenURI != null && tokenId != null && tokenAddress != null && tokenAmount!= null) {
                val function = Function(
                    "mint",
                    listOf(Address(toAddress), Uint256(BigInteger(tokenId)), Uint256(BigInteger(tokenAmount)), Utf8String(tokenURI), DynamicBytes(byteArrayOf(0))),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            tokenAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "batchMintERC721" ->
            if (fromAddress != null && toAddress != null && batchTokenURI != null && batchTokenId != null && tokenAddress != null) {
                val a = batchTokenId.map { Uint256(BigInteger(it)) }
                val b = batchTokenURI.map { Utf8String(it) }

                val function = Function(
                    "mintBatch",
                    listOf(Address(toAddress), DynamicArray(a), DynamicArray(b)),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            tokenAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "batchMintERC1155" ->
            if (fromAddress != null && toAddress != null && batchTokenURI != null && batchTokenId != null && tokenAddress != null && batchTokenAmount!= null) {
                val a = batchTokenId.map { Uint256(BigInteger(it)) }
                val b = batchTokenAmount.map { Uint256(BigInteger(it)) }
                val c = batchTokenURI.map { Utf8String(it) }

                val function = Function(
                    "mintBatch",
                    listOf(Address(toAddress), DynamicArray(a), DynamicArray(b), DynamicArray(c), DynamicBytes(byteArrayOf(0))),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            tokenAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "burnERC721" ->
            if (fromAddress != null && tokenId != null && tokenAddress != null) {
                val function = Function(
                    "burn",
                    listOf(Uint256(BigInteger(tokenId))),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            tokenAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
        "burnERC1155" ->
            if (fromAddress != null && tokenId != null && tokenAddress != null && tokenAmount != null) {
                val function = Function(
                    "burn",
                    listOf(Address(fromAddress), Uint256(BigInteger(tokenId)), Uint256(BigInteger(tokenAmount))),
                    emptyList()
                )
                val encodedFunction = FunctionEncoder.encode(function)

                try {
                    result = web3.ethEstimateGas(
                        Transaction.createFunctionCallTransaction(
                            fromAddress,
                            BigInteger.ONE,
                            gasPrice,
                            BigInteger.ZERO, // temporary gasLimit
                            tokenAddress,
                            encodedFunction // data
                        )
                    ).send().amountUsed
                } catch (ex: Exception) {
                    // Handle the exception appropriately
                    result = BigInteger.ZERO
                }
            }
    }
    BigDecimal(result).multiply(BigDecimal(1.2)).setScale(0, RoundingMode.DOWN).toBigInteger()
}
