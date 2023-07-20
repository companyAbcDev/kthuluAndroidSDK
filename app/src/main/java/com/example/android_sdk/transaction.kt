package com.example.android_sdk

import kotlinx.coroutines.*
import org.json.JSONObject
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
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
        val network = "cypress"
        val fromAddress = "0x54fbF887EdB01983DD373E79a0f37413B4565De3"
        val toAddress = "0x50515891B406cF7B8ab8D27243E0386ED06De7C8"
        val amount = "0.000001"
        val privateKey = "0x95a6bda95896978315b7b3183f79325c73ca2e9d96baa532361809bceb59ed8e"
        val contractAddress = "0x02cbe46fb8a1f579254a9b485788f2d86cad51aa"
        val decimals = 18

        // Send Coin transaction asynchronously
        val sendCoinTransaction =
            async { sendTransactionAsync(network, fromAddress, toAddress, amount, privateKey) }.await()
        if (sendCoinTransaction.getString("result") == "OK") {
            println("Transaction hash: ${sendCoinTransaction.getString("transactionHash")}")
            /**
             * Transaction hash: 0x..
             */
        } else {
            println("Error sending Coin: ${sendCoinTransaction.getString("error")}")
        }

        // Send token transaction asynchronously
        val sendErc20Transaction =
            async {
                sendTokenTransactionAsync(
                    network,
                    fromAddress,
                    toAddress,
                    amount,
                    privateKey,
                    contractAddress,
                    decimals
                )
            }.await()
        if (sendErc20Transaction.getString("result") == "OK") {
            println("Transaction hash: ${sendErc20Transaction.getString("transactionHash")}")
            /**
             * Transaction hash: 0x..
             */
        } else {
            println("Error sending Token: ${sendErc20Transaction.getString("error")}")
        }

    }
}

//Send Transaction Async
suspend fun sendTransactionAsync(
    network: String,
    fromAddress: String,
    toAddress: String,
    amount: String,
    privateKey: String,
): JSONObject = withContext(Dispatchers.IO) {
    val rpcUrl = when (network) {
        "ethereum" -> "https://mainnet.infura.io/v3/02c509fda7da4fed882ac537046cfd66"
        "cypress" -> "https://rpc.ankr.com/klaytn"
        "polygon" -> "https://rpc-mainnet.maticvigil.com/v1/96ab7849c9d3f105416383dd284c3f7e6511208c"
        "bnb" -> "https://bsc-dataseed.binance.org"
        else -> throw IllegalArgumentException("Invalid main network type")
    }
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
                BigInteger.ZERO, // maxPriorityFeePerGas
                getEstimateGas(network, "baseFee") // maxFeePerGas Add 20% to the gas price
            )
        }

        val signedTransaction = TransactionEncoder.signMessage(transaction, credentials)
        val hexValue = Numeric.toHexString(signedTransaction)

        val transactionHash = web3.ethSendRawTransaction(hexValue)
            .sendAsync()
            .get()
            .transactionHash
        jsonData.put("result", "OK")
        jsonData.put("transactionHash", transactionHash)
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
    privateKey: String,
    contractAddress: String,
    decimals: Int
): JSONObject = withContext(Dispatchers.IO) {
    val rpcUrl = when (network) {
        "ethereum" -> "https://mainnet.infura.io/v3/02c509fda7da4fed882ac537046cfd66"
        "cypress" -> "https://rpc.ankr.com/klaytn"
        "polygon" -> "https://rpc-mainnet.maticvigil.com/v1/96ab7849c9d3f105416383dd284c3f7e6511208c"
        "bnb" -> "https://bsc-dataseed.binance.org"
        else -> throw IllegalArgumentException("Invalid main network type")
    }

    val jsonData = JSONObject()
    try {
        // Ensure amount is a valid number
        if (BigDecimal(amount) <= BigDecimal.ZERO) {
            jsonData.put("error", "insufficient funds")
        }
        val web3 = Web3j.build(HttpService(rpcUrl))
        val credentials = Credentials.create(privateKey)
        val decimalMultiplier = BigDecimal.TEN.pow(decimals)
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
                    contractAddress,
                    fromAddress,
                    toAddress,
                    amount
                ), // Add 20% to the gas limit
                contractAddress, // to
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
                    contractAddress,
                    fromAddress,
                    toAddress,
                    amount
                ), // gasLimit Add 20% to the gas limit,
                contractAddress, // to
                BigInteger.ZERO, // value
                encodedFunction, // data
                BigInteger.ZERO, // maxPriorityFeePerGas
                getEstimateGas(network, "baseFee") // maxFeePerGas Add 20% to the gas price
            )
        }

        val signedTransaction = TransactionEncoder.signMessage(transaction, credentials)
        val hexValue = Numeric.toHexString(signedTransaction)

        val transactionHash = web3.ethSendRawTransaction(hexValue)
            .sendAsync()
            .get()
            .transactionHash
        jsonData.put("result", "OK")
        jsonData.put("transactionHash", transactionHash)
    } catch (e: Exception) {
        jsonData.put("result", "FAIL")
        jsonData.put("error", e.message)
    }
}
