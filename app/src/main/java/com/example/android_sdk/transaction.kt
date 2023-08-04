package com.example.android_sdk

import getAccountInfoAsync
import getTokenInfoAsync
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import org.json.JSONObject
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
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

suspend fun transaction() = runBlocking<Unit> {
    // Initialize the coroutine context
    coroutineScope {
//        val network = "ethereum"
//        val fromAddress = "0x54fbF887EdB01983DD373E79a0f37413B4565De3"
        // test
        val fromAddress = "0xeC4eC414c1f6a0759e5d184E17dB45cCd87E09FD"
        val toAddress = "0x50515891B406cF7B8ab8D27243E0386ED06De7C8"
        val amount = "0.000001"
        val contractAddress = "0x02cbe46fb8a1f579254a9b485788f2d86cad51aa"
        val decimals = 18

//        // Send Coin transaction asynchronously
//        val sendCoinTransaction =
//            async { sendTransactionAsync(network, fromAddress, toAddress, amount) }.await()
//        if (sendCoinTransaction.getString("result") == "OK") {
//            println("Transaction hash: ${sendCoinTransaction.getString("transactionHash")}")
//            /**
//              Transaction hash: 0x..
//             */
//        } else {
//            println("Error sending Coin: ${sendCoinTransaction.getString("error")}")
//        }
//
//        // Send token transaction asynchronously
//        val sendErc20Transaction =
//            async {
//                sendTokenTransactionAsync(
//                    "goerli",
//                    fromAddress,
//                    toAddress,
//                    amount,
//                    contractAddress,
//                    decimals
//                )
//            }.await()
//        if (sendErc20Transaction.getString("result") == "OK") {
//            println("Transaction hash: ${sendErc20Transaction.getString("transactionHash")}")
//            /**
//             * Transaction hash: 0x..
//             */
//        } else {
//            println("Error sending Token: ${sendErc20Transaction.getString("error")}")
//        }
//
        val deployErc20 =
            async {
                deployErc20Async(
                    "polygon",
                    fromAddress,
                    "MainAbcToken",
                    "MAT",
                    "5000000000"
                )
            }.await()
        println("Transaction hash: ${deployErc20}")
        /**
         * Transaction hash: 0x..
         */
//        val mintErc20 =
//            async {
//                mintErc20Async(
//                    "goerli",
//                    fromAddress,
//                    "1000000000000000000",
//                )
//            }.await()
//        println("Transaction hash: ${mintErc20}")
//        /**
//         * Transaction hash: 0x..
//         */

    }
}

//Send Transaction Async
suspend fun sendTransactionAsync(
    network: String,
    fromAddress: String,
    toAddress: String,
    amount: String,
): JSONObject = withContext(Dispatchers.IO) {
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
    val getAddressInfo = getAccountInfoAsync(fromAddress.lowercase())
    val result = getAddressInfo.getJSONArray("value")
    val value = result.getJSONObject(0)
    val privateKey = value.getString("private")

    val jsonData = JSONObject()
    try {
        // Ensure amount is a valid number
        if (BigDecimal(amount) <= BigDecimal.ZERO) {
            jsonData.put("error", "insufficient funds")
        }
        val web3 = Web3j.build(HttpService(rpcUrl))
        val credentials = Credentials.create(privateKey)
        val weiAmount = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()
        val nonce = web3.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3.ethChainId().sendAsync().get().chainId.toLong()

        val transaction = if (network == "bnb") {
            RawTransaction.createEtherTransaction(
                nonce,
                getEstimateGas(network, "baseFee"), // Add 20% to the gas price ,
                getEstimateGas(
                    network,
                    "transferCoin",
                    null,
                    fromAddress,
                    toAddress,
                    amount
                ), // Add 20% to the gas price ,
                toAddress,
                weiAmount as BigInteger? // value
            )

        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                getEstimateGas(
                    network,
                    "transferCoin",
                    null,
                    fromAddress,
                    toAddress,
                    amount
                ), // gasLimit Add 20% to the gas limit,
                toAddress, // to
                weiAmount, // value
                "0x", // data
                BigInteger("1000000000"), // maxPriorityFeePerGas
                getEstimateGas(network, "baseFee") // maxFeePerGas Add 20% to the gas price
            )
        }

        val signedTransaction = TransactionEncoder.signMessage(transaction, credentials)
        val hexValue = Numeric.toHexString(signedTransaction)

        val transactionHash = web3.ethSendRawTransaction(hexValue)
            .sendAsync()
            .get()
            .transactionHash
        if(transactionHash != null) {
            jsonData.put("result", "OK")
            jsonData.put("transactionHash", transactionHash)
        } else {
            jsonData.put("result", "FAIL")
            jsonData.put("error", "insufficient funds")
        }
    } catch (e: Exception) {
        jsonData.put("result", "FAIL")
        jsonData.put("error", e.message)
    }
}

//Send Token Transaction Async
suspend fun sendTokenTransactionAsync(
    network: String,
    fromAddress: String,
    toAddress: String,
    amount: String,
    token_address: String
): JSONObject = withContext(Dispatchers.IO) {
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
    val getAddressInfo = getAccountInfoAsync(fromAddress.lowercase())
    val result = getAddressInfo.getJSONArray("value")
    val value = result.getJSONObject(0)
    val privateKey = value.getString("private")

    val jsonData = JSONObject()
    try {
        val decimals = getTokenInfoAsync(network, token_address).getString("decimals")
        // Ensure amount is a valid number
        if (BigDecimal(amount) <= BigDecimal.ZERO) {
            jsonData.put("error", "insufficient funds")
        }
        val web3 = Web3j.build(HttpService(rpcUrl))
        val credentials = Credentials.create(privateKey)
        val decimalMultiplier = BigDecimal.TEN.pow(decimals.toInt())
        val tokenAmount = BigDecimal(amount).multiply(decimalMultiplier).toBigInteger()
        val function = Function(
            "transfer",
            listOf(Address(toAddress), Uint256(tokenAmount)),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce: BigInteger = web3.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3.ethChainId().sendAsync().get().chainId.toLong()

        val transaction = if (network == "bnb") {
            RawTransaction.createTransaction(
                nonce,
                getEstimateGas(network, "baseFee"), // Add 20% to the gas price ,
                getEstimateGas(
                    network,
                    "transferERC20",
                    token_address,
                    fromAddress,
                    toAddress,
                    amount
                ), // Add 20% to the gas limit
                token_address, // to
                tokenAmount, // value
                encodedFunction // data
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                getEstimateGas(
                    network,
                    "transferERC20",
                    token_address,
                    fromAddress,
                    toAddress,
                    amount
                ), // gasLimit Add 20% to the gas limit,
                token_address, // to
                BigInteger.ZERO, // value
                encodedFunction, // data
                BigInteger("30000000000"), // 30 Gwei maxPriorityFeePerGas
                getEstimateGas(network, "baseFee") // maxFeePerGas Add 20% to the gas price
            )
        }

        val signedTransaction = TransactionEncoder.signMessage(transaction, credentials)
        val hexValue = Numeric.toHexString(signedTransaction)

        val transactionHash = web3.ethSendRawTransaction(hexValue)
            .sendAsync()
            .get()
            .transactionHash
        if(transactionHash != null) {
            jsonData.put("result", "OK")
            jsonData.put("transactionHash", transactionHash)
        } else {
            jsonData.put("result", "FAIL")
            jsonData.put("error", "insufficient funds")
        }
    } catch (e: Exception) {
        jsonData.put("result", "FAIL")
        jsonData.put("error", e.message)
    }
}


suspend fun deployErc20Async(
    network: String,
    ownerAddress: String,
    name: String,
    symbol: String,
    totalSupply: String
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
    val getAddressInfo = getAccountInfoAsync(ownerAddress.lowercase())
    val result = getAddressInfo.getJSONArray("value")
    val value = result.getJSONObject(0)
    val privateKey = value.getString("private")

    val jsonData = JSONObject()

    try {
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)
        val function = Function(
            "deployedERC20",
            listOf(Utf8String(name), Utf8String(symbol), Uint256(BigInteger(totalSupply)), Address(ownerAddress)),
            emptyList()
        )

        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(ownerAddress, DefaultBlockParameterName.PENDING)
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
                    "deployERC20",
                    null,
                    ownerAddress,
                    null,
                    totalSupply,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    name, symbol
                ), // Add 20% to the gas limit
                erc20DeployGoerli,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                getEstimateGas(
                    network,
                    "deployERC20",
                    null,
                    ownerAddress,
                    null,
                    totalSupply,
                    null,
                    null,
                    null,
                    null,
                    null,
                    null,
                    name, symbol
                ), // Add 20% to the gas limit
                erc20DeployPolygon,
                BigInteger.ZERO,
                encodedFunction,
                BigInteger("33000000000"), // 33 Gwei maxPriorityFeePerGas
                getEstimateGas(network, "baseFee") // Add 20% to the gas price
            )
        }
        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val txHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if(txHash != null) {
            jsonData.put("result", "OK")
            jsonData.put("transactionHash", txHash)
        } else {
            jsonData.put("result", "FAIL")
            jsonData.put("error", "insufficient funds")
        }
    } catch (e: Exception) {
        jsonData.put("result", "FAIL")
        jsonData.put("error", e.message)
    }
}

suspend fun mintErc20Async(
    network: String,
    publisherAddress: String,
    amount: String,
    ownerAddress: String? = publisherAddress
): JSONObject = withContext(Dispatchers.IO){
    val rpcUrl = when (network) {
        "ethereum" -> "https://mainnet.infura.io/v3/02c509fda7da4fed882ac537046cfd66"
        "cypress" -> "https://rpc.ankr.com/klaytn"
        "matic" -> "https://rpc-mainnet.maticvigil.com/v1/96ab7849c9d3f105416383dd284c3f7e6511208c"
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
            "mint",
            listOf(Address(ownerAddress), Uint256(BigInteger(amount))),
            emptyList()
        )
        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(ownerAddress, DefaultBlockParameterName.PENDING)
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
                    null
                ), // Add 20% to the gas limit
                addrBridgeGoerli,
                encodedFunction
            )
        } else {
            RawTransaction.createTransaction(
                chainId,
                nonce,
                BigInteger("4000000"),
                addrBridgeGoerli,
                BigInteger.ZERO,
                encodedFunction,
                //1gwei
                BigInteger("1000000000"), // maxPriorityFeePerGas
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
