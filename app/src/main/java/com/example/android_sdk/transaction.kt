package com.example.android_sdk

import com.klaytn.caver.Caver
import com.klaytn.caver.contract.SendOptions
import com.klaytn.caver.kct.kip7.KIP7
import com.klaytn.caver.methods.request.CallObject
import com.klaytn.caver.transaction.type.SmartContractExecution
import com.klaytn.caver.transaction.type.ValueTransfer
import com.klaytn.caver.wallet.keyring.KeyringFactory
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.DynamicArray
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Utf8String
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint32
import org.web3j.abi.datatypes.generated.Uint8
import org.web3j.crypto.Credentials
import org.web3j.crypto.RawTransaction
import org.web3j.crypto.TransactionEncoder
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.protocol.http.HttpService
import org.web3j.utils.Convert
import org.web3j.utils.Numeric
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.time.Instant

suspend fun transaction() = runBlocking<Unit> {
    // Initialize the coroutine context
    coroutineScope {
//        val network = "ethereum"
//        val fromAddress = "0x54fbF887EdB01983DD373E79a0f37413B4565De3"
        // test
        val fromAddress = "0xeC4eC414c1f6a0759e5d184E17dB45cCd87E09FD"
        val toAddress = "0x0eae45485F2D14FDEB3dAa1143E5170752D5EAe8"
        val amount = "0.000001"
        val contractAddress = "0x02cbe46fb8a1f579254a9b485788f2d86cad51aa"
        val decimals = 18

        // Send Coin transaction asynchronously
//        val sendCoinTransaction =
//            async { sendTransactionAsync("polygon", fromAddress, toAddress, amount) }.await()
//
//        if (sendCoinTransaction.getString("result") == "OK") {
//            println("result : ${sendCoinTransaction.getString("result")}")
//            println(
//                "transactionHash : ${
//                    sendCoinTransaction.getJSONArray("value").getJSONObject(0).getString("transaction_hash")
//                }"
//            )
//        } else {
//            println("result : ${sendCoinTransaction.getString("result")}")
//            println(
//                "transactionHash : ${
//                    sendCoinTransaction.getJSONArray("value").getJSONObject(0).getString("error")
//                }"
//            )
//        }

        // Send token transaction asynchronously
//        val sendErc20Transaction =
//            async {
//                sendTokenTransactionAsync(
//                    "cypress",
//                    fromAddress,
//                    toAddress,
//                    "0.00001",
//                    "0x02cbe46fb8a1f579254a9b485788f2d86cad51aa"
//                )
//            }.await()
//        if (sendErc20Transaction.getString("result") == "OK") {
//            println("result : ${sendErc20Transaction.getString("result")}")
//            println(
//                "transactionHash : ${
//                    sendErc20Transaction.getJSONArray("value").getJSONObject(0).getString("transaction_hash")
//                }"
//            )
//        } else {
//            println("result : ${sendErc20Transaction.getString("result")}")
//            println(
//                "transactionHash : ${
//                    sendErc20Transaction.getJSONArray("value").getJSONObject(0).getString("error")
//                }"
//            )
//        }

//        val deployErc20 =
//            async {
//                deployErc20Async(
//                    "cypress",
//                    fromAddress,
//                    "YGToken",
//                    "YGT",
//                    "5000000"
//                )
//            }.await()
//        if (deployErc20.getString("result") == "OK") {
//            println("result : ${deployErc20.getString("result")}")
//            println(
//                "transactionHash : ${
//                    deployErc20.getJSONArray("value").getJSONObject(0).getString("transaction_hash")
//                }"
//            )
//        } else {
//            println("result : ${deployErc20.getString("result")}")
//            println(
//                "transactionHash : ${
//                    deployErc20.getJSONArray("value").getJSONObject(0).getString("error")
//                }"
//            )
//        }

//         val bridgeToken =
//             async {
//                 bridgeTokenAsync(
//                     "polygon",
//                     fromAddress,
//                     "ETHEREUM",
//                     "11"
//                 )
//             }.await()
//         println("Transaction hash: ${bridgeToken}")
//         /**
//          * Transaction hash: 0x..
//          */

//          val bridgeToken =
//             async {
//                 bridgeTokenAsync(
//                     "cypress",
//                     fromAddress,
//                     "POLYGON",
//                     "10000000000000000",
//                     "0x085AB24e511bEa905bDe815FA38a11eEB507E206"
//                 )
//             }.await()
//         println("Transaction hash: ${bridgeToken}")
//         /**
//          * Transaction hash: 0x..
//          */

//          val coinForTokenswap =
//          async {
//             coinForTokenswapAsync( "polygon", fromAddress,"0xD6DF932A45C0f255f85145f286eA0b292B21C90B", "20")
//         }.await()
//        if (coinForTokenswap.getString("result") == "OK") {
//            println("result : ${coinForTokenswap.getString("result")}")
//            println(
//                "transactionHash : ${
//                    coinForTokenswap.getJSONArray("value").getJSONObject(0).getString("transaction_hash")
//                }"
//            )
//        } else {
//            println("result : ${coinForTokenswap.getString("result")}")
//            println(
//                "transactionHash : ${
//                    coinForTokenswap.getJSONArray("value").getJSONObject(0).getString("error")
//                }"
//            )
//        }

//        val tokenSwapAppove =
//            async {
//                tokenSwapAppoveAsync( "polygon", fromAddress,"0xc2132D05D31c914a87C6611C10748AEb04B58e8F","0xD6DF932A45C0f255f85145f286eA0b292B21C90B", "1")
//            }.await()
//        println("Transaction hash: ${tokenSwapAppove}")
//        /**
//         * Transaction hash: 0x..
//         */

//     val tokenForTokenswap =
//         async {
//             tokenForTokenswapAsync( "polygon", fromAddress,"0xD6DF932A45C0f255f85145f286eA0b292B21C90B","0xc2132D05D31c914a87C6611C10748AEb04B58e8F", "0.04")
//         }.await()
//        println("Transaction hash: ${tokenForTokenswap}")
//        /**
//         * Transaction hash: 0x..
//         */

//        val tokenForCoinswap =
//            async {
//                tokenForCoinswapAsync( "polygon", fromAddress,"0xc2132D05D31c914a87C6611C10748AEb04B58e8F","1")
//            }.await()
//        println("Transaction hash: ${tokenForCoinswap}")
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
        // Ensure amount is a valid number
        if (BigDecimal(amount) <= BigDecimal.ZERO) {
            jsonData.put("result", "FAIL")
            jsonData.put("error", "insufficient funds")
        }

        var transactionHash = "";

        val weiAmount = Convert.toWei(amount, Convert.Unit.ETHER).toBigInteger()

        if (network == "cypress") {
            val caver = Caver(rpcUrl)
            val keyring = KeyringFactory.createFromPrivateKey(privateKey)
            caver.wallet.add(keyring)

            // Estimate gas
            var gasLimit: BigInteger
            try {
                val callObject = CallObject.createCallObject(
                    fromAddress,
                    toAddress,
                    BigInteger.ZERO,
                    BigInteger.ONE,
                    weiAmount
                )
                gasLimit = caver.rpc.klay.estimateGas(callObject).send().value
            } catch (ex: Exception) {
                // Handle the exception appropriately
                gasLimit = BigInteger.ZERO
            }

            //Create a value transfer transaction
            val valueTransfer: ValueTransfer = ValueTransfer.Builder()
                .setKlaytnCall(caver.rpc.getKlay())
                .setFrom(keyring.address)
                .setTo(toAddress)
                .setValue(weiAmount)
                .setGas(gasLimit)
                .build()

            //Sign to the transaction
            valueTransfer.sign(keyring)

            //Send a transaction to the klaytn blockchain platform (Klaytn)
            transactionHash = caver.rpc.klay.sendRawTransaction(valueTransfer.rawTransaction).send().result

        } else {
            val web3 = Web3j.build(HttpService(rpcUrl))
            val credentials = Credentials.create(privateKey)
            val nonce = web3.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.LATEST)
                .sendAsync()
                .get()
                .transactionCount

            val chainId = web3.ethChainId().sendAsync().get().chainId.toLong()

            val gasLimit = getEstimateGasAsync(
                network,
                "transferCoin",
                null,
                fromAddress,
                toAddress,
                amount
            )
            val transaction = if (network == "bnb") {
                RawTransaction.createEtherTransaction(
                    nonce,
                    getEstimateGasAsync(network, "baseFee"), // Add 20% to the gas price ,
                    gasLimit, // Add 20% to the gas price ,
                    toAddress,
                    weiAmount as BigInteger? // value
                )

            } else {
                RawTransaction.createTransaction(
                    chainId,
                    nonce,
                    gasLimit, // gasLimit Add 20% to the gas limit,
                    toAddress, // to
                    weiAmount, // value
                    "0x", // data
                    BigInteger(maxPriorityFeePerGas), // 35 Gwei maxPriorityFeePerGas
                    getEstimateGasAsync(network, "baseFee") // maxFeePerGas Add 20% to the gas price
                )
            }

            val signedTransaction = TransactionEncoder.signMessage(transaction, credentials)
            val hexValue = Numeric.toHexString(signedTransaction)

            transactionHash = web3.ethSendRawTransaction(hexValue)
                .sendAsync()
                .get()
                .transactionHash
        }

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

//Send Token Transaction Async
suspend fun sendTokenTransactionAsync(
    network: String,
    fromAddress: String,
    toAddress: String,
    amount: String,
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
            println("Error while fetching the private key: ${it.message}")
            null
        }

        var transactionHash = "";

        if (network == "cypress") {
            val caver = Caver(rpcUrl)
            val keyring = KeyringFactory.createFromPrivateKey(privateKey)
            caver.wallet.add(keyring)

            // Send KRC20 tokens
            val clone = KIP7(caver, token_address)
            val decimals = clone.decimals()
            val decimalMultiplier = BigDecimal.TEN.pow(decimals)
            val tokenAmount = BigDecimal(amount).multiply(decimalMultiplier).toBigInteger()

            // Set the send options
            val sendOptions = SendOptions()
            sendOptions.setFrom(fromAddress)

            // Encode the function
            val function = Function(
                "transfer",
                listOf(Address(toAddress), Uint256(tokenAmount)),
                emptyList()
            )
            val encodedFunction = FunctionEncoder.encode(function)

            // Estimate gas
            var gasLimit: BigInteger
            try {
                val callObject = CallObject.createCallObject(
                    fromAddress,
                    token_address,
                    null,
                    null,
                    null,
                    encodedFunction
                )
                gasLimit = caver.rpc.klay.estimateGas(callObject).send().value
            } catch (ex: Exception) {
                // Handle the exception appropriately
                gasLimit = BigInteger.ZERO
            }
            sendOptions.setGas(gasLimit)  // Set the estimated gas value to sendOptions

            transactionHash = clone.transfer(toAddress, tokenAmount, sendOptions).transactionHash
        } else {
            val web3 = Web3j.build(HttpService(rpcUrl))
            if (BigDecimal(amount) <= BigDecimal.ZERO) {
                jsonData.put("result", "FAIL")
                jsonData.put("error", "insufficient funds")
            }
            val decimalsFunction = Function("decimals", emptyList(), listOf(object : TypeReference<Uint8>() {}))
            val encodedDecimalsFunction = FunctionEncoder.encode(decimalsFunction)
            val decimalsResponse = web3.ethCall(
                Transaction.createEthCallTransaction(null, token_address, encodedDecimalsFunction),
                DefaultBlockParameterName.LATEST
            ).send()
            val decimalsOutput =
                FunctionReturnDecoder.decode(decimalsResponse.result, decimalsFunction.outputParameters)
            val decimals = (decimalsOutput[0].value as BigInteger).toInt()
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

            val gasLimit = getEstimateGasAsync(
                network,
                "transferERC20",
                token_address,
                fromAddress,
                toAddress,
                amount
            )
            val transaction = if (network == "bnb") {
                RawTransaction.createTransaction(
                    nonce,
                    getEstimateGasAsync(network, "baseFee"), // Add 20% to the gas price ,
                    gasLimit, // Add 20% to the gas limit
                    token_address, // to
                    tokenAmount, // value
                    encodedFunction // data
                )
            } else {
                RawTransaction.createTransaction(
                    chainId,
                    nonce,
                    gasLimit, // gasLimit Add 20% to the gas limit,
                    token_address, // to
                    BigInteger.ZERO, // value
                    encodedFunction, // data
                    BigInteger(maxPriorityFeePerGas), // 35 Gwei maxPriorityFeePerGas
                    getEstimateGasAsync(network, "baseFee") // maxFeePerGas Add 20% to the gas price
                )
            }

            val signedTransaction = TransactionEncoder.signMessage(transaction, credentials)
            val hexValue = Numeric.toHexString(signedTransaction)

            transactionHash = web3.ethSendRawTransaction(hexValue)
                .sendAsync()
                .get()
                .transactionHash
        }

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

suspend fun deployErc20Async(
    network: String,
    ownerAddress: String,
    name: String,
    symbol: String,
    totalSupply: String
): JSONObject = withContext(Dispatchers.IO) {
    networkSettings(network)
    val jsonData = JSONObject()

    // return array & object
    val resultArray = JSONArray()
    var resultData = JSONObject()

    resultData.put("result", "FAIL")
    resultData.put("value", resultArray)

    try {
        val getAddressInfo = getAccountInfoAsync(ownerAddress)
        val privateKey = runCatching {
            getAddressInfo.getJSONArray("value")
                .getJSONObject(0)
                .getString("private")
        }.getOrElse {
            // handle error here
            println("Error while fetching the private key: ${it.message}")
            null
        }
        var transactionHash = "";
        val decimals = "18"
        val decimalMultiplier = BigDecimal.TEN.pow(decimals.toInt())
        val tokenAmount = BigDecimal(totalSupply).multiply(decimalMultiplier).toBigInteger()
        if (network == "cypress") {
            val caver = Caver(rpcUrl)
            val keyring = KeyringFactory.createFromPrivateKey(privateKey)
            caver.wallet.add(keyring)

            val function = Function(
                "deployWrapped20",
                listOf(Utf8String(name), Utf8String(symbol), Uint8(BigInteger(decimals)), Uint256(tokenAmount)),
                emptyList()
            )
            val encodedFunction = FunctionEncoder.encode(function)

            // Estimate gas
            var gasLimit: BigInteger
            try {
                val callObject = CallObject.createCallObject(
                    ownerAddress,
                    bridgeContractAddress,
                    null,
                    null,
                    null,
                    encodedFunction
                )
                gasLimit = caver.rpc.klay.estimateGas(callObject).send().value
            } catch (ex: Exception) {
                // Handle the exception appropriately
                gasLimit = BigInteger.ZERO
            }

            // Create a smart contract execution transaction
            val smartContractExecution = SmartContractExecution.Builder()
                .setKlaytnCall(caver.rpc.getKlay())
                .setFrom(keyring.address)
                .setTo(bridgeContractAddress)
                .setValue(BigInteger.ZERO)
                .setInput(encodedFunction)
                .setGas(gasLimit)
                .build()

            //Sign to the transaction
            smartContractExecution.sign(keyring)

            //Send a transaction to the klaytn blockchain platform (Klaytn)
            transactionHash = caver.rpc.klay.sendRawTransaction(smartContractExecution.rawTransaction).send().result

        } else {
            val web3j = Web3j.build(HttpService(rpcUrl))
            val credentials =
                Credentials.create(privateKey)


            val function = Function(
                "deployWrapped20",
                listOf(Utf8String(name), Utf8String(symbol), Uint8(BigInteger(decimals)), Uint256(tokenAmount)),
                emptyList()
            )

            val encodedFunction = FunctionEncoder.encode(function)

            val nonce = web3j.ethGetTransactionCount(ownerAddress, DefaultBlockParameterName.PENDING)
                .sendAsync()
                .get()
                .transactionCount

            val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

            val gasLimit = getEstimateGasAsync(
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
            )

            val tx = if (network == "bnb" || network == "bnbTest") {
                RawTransaction.createTransaction(
                    nonce,
                    getEstimateGasAsync(network, "baseFee"), // Add 20% to the gas price
                    gasLimit, // Add 20% to the gas limit
                    bridgeContractAddress,
                    encodedFunction
                )
            } else {
                RawTransaction.createTransaction(
                    chainId,
                    nonce,
                    gasLimit, // Add 20% to the gas limit
                    bridgeContractAddress,
                    BigInteger.ZERO,
                    encodedFunction,
                    BigInteger(maxPriorityFeePerGas), // 35 Gwei maxPriorityFeePerGas
                    getEstimateGasAsync(network, "baseFee") // Add 20% to the gas price
                )
            }
            val signedMessage = TransactionEncoder.signMessage(tx, credentials)
            val signedTx = Numeric.toHexString(signedMessage)

            transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        }

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

suspend fun bridgeTokenAsync(
    network: String,
    fromAddress: String,
    toNetwork: String,
    amount: String,
): JSONObject = withContext(Dispatchers.IO) {
    val jsonData = JSONObject()
    networkSettings(network)
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

        val hex = textToHex(toNetwork)

        // Convert hex string to BigInteger
        val bigIntValue = BigInteger(hex, 16)

        val function = Function(
            "moveFromETHER",
            listOf(Uint256(bigIntValue)),
            emptyList()
        )

        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigIntegerExact()

        val tx =
            if (network == "bnb" || network == "bnbTest") {
                RawTransaction.createTransaction(
                    nonce,
                    getEstimateGasAsync(network, "baseFee"), // Add 20% to the gas price
                    BigInteger.valueOf(200000), // Add 20% to the gas limit
                    bridgeContractAddress,
                    encodedFunction
                )
            } else {
                RawTransaction.createTransaction(
                    chainId,
                    nonce,
                    BigInteger.valueOf(200000), // Add 20% to the gas limit
                    bridgeContractAddress,
                    amountInWei,
                    encodedFunction,
                    BigInteger(maxPriorityFeePerGas), // 35 Gwei maxPriorityFeePerGas
                    getEstimateGasAsync(network, "baseFee") // Add 20% to the gas price
                )
            }
        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val txHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (txHash != null) {
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

suspend fun bridgeTokenAsync(
    network: String,
    fromAddress: String,
    toNetwork: String,
    amount: String,
    token_address: String
): JSONObject = withContext(Dispatchers.IO) {
    val jsonData = JSONObject()
    networkSettings(network)
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

        val hex = textToHex(toNetwork)

        // Convert hex string to BigInteger
        val toNetworkHex = BigInteger(hex, 16)

        val function = Function(
            "moveFromERC20",
            listOf(Uint256(toNetworkHex), Address(token_address), Uint256(BigInteger(amount))),
            emptyList()
        )

        val networkFeeIdxFunction = Function("getNetworkFeeIdxByName", listOf(Uint256(toNetworkHex)), emptyList())
        val encodedNetworkFeeIdxFunction = FunctionEncoder.encode(networkFeeIdxFunction)
        val networkFeeIdxResponse = web3j.ethCall(
            Transaction.createEthCallTransaction(null, bridgeConfigContractAddress, encodedNetworkFeeIdxFunction),
            DefaultBlockParameterName.LATEST
        ).send()

        val networkFeeIdx = BigInteger(networkFeeIdxResponse.result.replace("0x", ""), 16)

        val networkFeeFunction = Function("getNetworkFeeByIdx", listOf(Uint32(networkFeeIdx)), emptyList())
        val encodedNetworkFeeFunction = FunctionEncoder.encode(networkFeeFunction)
        val networkFeeResponse = web3j.ethCall(
            Transaction.createEthCallTransaction(null, bridgeConfigContractAddress, encodedNetworkFeeFunction),
            DefaultBlockParameterName.LATEST
        ).send()

        // Assuming each value is of length 64 characters (32 bytes, which is standard for Ethereum)
//        val networkHex = networkFeeResponse.result.substring(2, 66)
        val tokenFeeHex = networkFeeResponse.result.substring(66, 130)
//        val nftFeeHex = networkFeeResponse.result.substring(130, 194)
//        val regFeeHex = networkFeeResponse.result.substring(194, 258)

//        val network = String(BigInteger(networkHex, 16).toByteArray())
        val tokenFee = BigInteger(tokenFeeHex, 16)
//        val nftFee = BigInteger(nftFeeHex, 16)
//        val regFee = BigInteger(regFeeHex, 16)

        val encodedFunction = FunctionEncoder.encode(function)

        val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
            .sendAsync()
            .get()
            .transactionCount

        val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

        val tx =
            if (network == "bnb" || network == "bnbTest") {
                RawTransaction.createTransaction(
                    nonce,
                    getEstimateGasAsync(network, "baseFee"), // Add 20% to the gas price
                    BigInteger.valueOf(200000), // Add 20% to the gas limit
                    bridgeContractAddress,
                    encodedFunction
                )
            } else {
                RawTransaction.createTransaction(
                    chainId,
                    nonce,
                    BigInteger.valueOf(200000), // Add 20% to the gas limit
                    bridgeContractAddress,
                    tokenFee, // value
                    encodedFunction,
                    BigInteger(maxPriorityFeePerGas), // 35 Gwei maxPriorityFeePerGas
                    getEstimateGasAsync(network, "baseFee") // Add 20% to the gas price
                )
            }
        val signedMessage = TransactionEncoder.signMessage(tx, credentials)
        val signedTx = Numeric.toHexString(signedMessage)

        val txHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
        if (txHash != null) {
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

suspend fun tokenSwapAppoveAsync(
    network: String,
    fromAddress: String,
    fromTokenId: String,
    toTokenId: String? = null,
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
        var transactionHash = "";
        var toTokenId = toTokenId

        if (toTokenId == null) {
            toTokenId = when (network) {
                "ethereum" -> "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2"
                "cypress" -> "0"
                "polygon" -> "0x0d500B1d8E8eF31E21C99d1Db9A6444d3ADf1270"
                "bnb" -> "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c"
                else -> throw IllegalArgumentException("Invalid main network type")
            }
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)

        val decimalsFunction = Function("decimals", emptyList(), listOf(object : TypeReference<Uint8>() {}))
        val encodedDecimalsFunction = FunctionEncoder.encode(decimalsFunction)
        val decimalsResponse = web3j.ethCall(
            Transaction.createEthCallTransaction(null, fromTokenId, encodedDecimalsFunction),
            DefaultBlockParameterName.LATEST
        ).send()
        val decimalsOutput =
            FunctionReturnDecoder.decode(decimalsResponse.result, decimalsFunction.outputParameters)
        val decimals = (decimalsOutput[0].value as BigInteger).toInt()
        val decimalMultiplier = BigDecimal.TEN.pow(decimals)
        var amountInWei = BigDecimal(amount).multiply(decimalMultiplier).toBigInteger()

        amountInWei = BigDecimal(amountInWei).multiply(BigDecimal(1.2)).setScale(0, RoundingMode.DOWN).toBigInteger()

        val getPairFunction = Function("getPair", listOf(Address(fromTokenId), Address(toTokenId)), emptyList())

        val encodedGetPairFunction = FunctionEncoder.encode(getPairFunction)

        val getPairResponse = web3j.ethCall(
            Transaction.createEthCallTransaction(null, uniswapV2FactoryAddress, encodedGetPairFunction),
            DefaultBlockParameterName.LATEST
        ).send()

        val getPair = BigInteger(getPairResponse.result.replace("0x", ""), 16)

        if (getPair != BigInteger.ZERO) {
            val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()
            val approveTokenFunction =
                Function("approve", listOf(Address(uniswapV2RouterAddress), Uint256(amountInWei)), emptyList())
            val arroveTokenEncodedFunction = FunctionEncoder.encode(approveTokenFunction)
            val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
                .sendAsync()
                .get()
                .transactionCount

            val tx =
                if (network == "bnb" || network == "bnbTest") {
                    RawTransaction.createTransaction(
                        nonce,
                        getEstimateGasAsync(network, "baseFee"), // Add 20% to the gas price
                        BigInteger("200000"), // Add 20% to the gas limit
                        fromTokenId,
                        arroveTokenEncodedFunction
                    )
                } else {
                    RawTransaction.createTransaction(
                        chainId,
                        nonce,
                        BigInteger("200000"), // Add 20% to the gas limit
                        fromTokenId,
                        BigInteger.ZERO, // value
                        arroveTokenEncodedFunction,
                        BigInteger(maxPriorityFeePerGas), // maxPriorityFeePerGas
                        getEstimateGasAsync(network, "baseFee") // Add 20% to the gas price
                    )
                }
            val signedMessage = TransactionEncoder.signMessage(tx, credentials)
            val signedTx = Numeric.toHexString(signedMessage)

            transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
            if(transactionHash != "") {
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

        } else {
            jsonData.put("error", "pair not found")
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

// coin to token swap
suspend fun coinForTokenswapAsync(
    network: String,
    fromAddress: String,
    toTokenId: String,
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

        val fromTokenId = when (network) {
            "ethereum" -> "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2"
            "cypress" -> "0"
            "polygon" -> "0x0d500B1d8E8eF31E21C99d1Db9A6444d3ADf1270"
            "bnb" -> "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c"
            else -> throw IllegalArgumentException("Invalid main network type")
        }
        val web3j = Web3j.build(HttpService(rpcUrl))
        val credentials =
            Credentials.create(privateKey)
        var transactionHash = "";
        val amountInWei = Convert.toWei(amount, Convert.Unit.ETHER).toBigIntegerExact()

        // Deadline is the current time + 10 minutes in seconds
        val deadline = Instant.now().epochSecond + 600

        val path = DynamicArray(Address::class.java, listOf(Address(fromTokenId), Address(toTokenId)))

        val getPairFunction = Function("getPair", listOf(Address(fromTokenId), Address(toTokenId)), emptyList())

        val encodedGetPairFunction = FunctionEncoder.encode(getPairFunction)

        val getPairResponse = web3j.ethCall(
            Transaction.createEthCallTransaction(null, uniswapV2FactoryAddress, encodedGetPairFunction),
            DefaultBlockParameterName.LATEST
        ).send()

        val getPair = BigInteger(getPairResponse.result.replace("0x", ""), 16)

        if (getPair != BigInteger.ZERO) {
            val swapExactETHForTokensFunction = Function(
                "swapExactETHForTokens",
                listOf(Uint256(BigInteger.ZERO), path, Address(fromAddress), Uint256(deadline)),
                emptyList()
            )

            val encodedFunction = FunctionEncoder.encode(swapExactETHForTokensFunction)

            val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
                .sendAsync()
                .get()
                .transactionCount

            val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

            val tx =
                if (network == "bnb" || network == "bnbTest") {
                    RawTransaction.createTransaction(
                        nonce,
                        getEstimateGasAsync(network, "baseFee"), // Add 20% to the gas price
                        BigInteger("200000"), // Add 20% to the gas limit
                        uniswapV2RouterAddress,
                        encodedFunction
                    )
                } else {
                    RawTransaction.createTransaction(
                        chainId,
                        nonce,
                        BigInteger("200000"), // Add 20% to the gas limit
                        uniswapV2RouterAddress,
                        amountInWei, // value
                        encodedFunction,
                        BigInteger(maxPriorityFeePerGas), // maxPriorityFeePerGas
                        getEstimateGasAsync(network, "baseFee") // Add 20% to the gas price
                    )
                }
            val signedMessage = TransactionEncoder.signMessage(tx, credentials)
            val signedTx = Numeric.toHexString(signedMessage)

            transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
            if(transactionHash != "") {
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

        } else {
            jsonData.put("error", "pair not found")
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

// token to token swap
suspend fun tokenForTokenswapAsync(
    network: String,
    fromAddress: String,
    fromTokenId: String,
    toTokenId: String,
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

        var transactionHash = "";

        val decimalsFunction = Function("decimals", emptyList(), listOf(object : TypeReference<Uint8>() {}))
        val encodedDecimalsFunction = FunctionEncoder.encode(decimalsFunction)
        val decimalsResponse = web3j.ethCall(
            Transaction.createEthCallTransaction(null, fromTokenId, encodedDecimalsFunction),
            DefaultBlockParameterName.LATEST
        ).send()
        val decimalsOutput =
            FunctionReturnDecoder.decode(decimalsResponse.result, decimalsFunction.outputParameters)
        val decimals = (decimalsOutput[0].value as BigInteger).toInt()
        val decimalMultiplier = BigDecimal.TEN.pow(decimals)
        val amountInWei = BigDecimal(amount).multiply(decimalMultiplier).toBigInteger()

        // Deadline is the current time + 10 minutes in seconds
        val deadline = Instant.now().epochSecond + 600

        val path = DynamicArray(Address::class.java, listOf(Address(fromTokenId), Address(toTokenId)))

        val getPairFunction = Function("getPair", listOf(Address(fromTokenId), Address(toTokenId)), emptyList())

        val encodedGetPairFunction = FunctionEncoder.encode(getPairFunction)

        val getPairResponse = web3j.ethCall(
            Transaction.createEthCallTransaction(null, uniswapV2FactoryAddress, encodedGetPairFunction),
            DefaultBlockParameterName.LATEST
        ).send()

        val getPair = BigInteger(getPairResponse.result.replace("0x", ""), 16)

        if (getPair != BigInteger.ZERO) {
            val swapExactETHForTokensFunction = Function(
                "swapExactTokensForTokens",
                listOf(Uint256(amountInWei), Uint256(BigInteger.ZERO), path, Address(fromAddress), Uint256(deadline)),
                emptyList()
            )

            val encodedFunction = FunctionEncoder.encode(swapExactETHForTokensFunction)

            val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
                .sendAsync()
                .get()
                .transactionCount

            val gasLimit = getEstimateGasAsync(
                network,
                "swapToken",
                fromTokenId,
                fromAddress,
                "",
                amountInWei.toString(),
                "",
                toTokenId
            )

            val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

            val tx =
                if (network == "bnb" || network == "bnbTest") {
                    RawTransaction.createTransaction(
                        nonce,
                        getEstimateGasAsync(network, "baseFee"), // Add 20% to the gas price
                        gasLimit, // Add 20% to the gas limit
                        uniswapV2RouterAddress,
                        encodedFunction
                    )
                } else {
                    RawTransaction.createTransaction(
                        chainId,
                        nonce,
                        gasLimit, // Add 20% to the gas limit,
                        uniswapV2RouterAddress,
                        BigInteger.ZERO, // value
                        encodedFunction,
                        BigInteger(maxPriorityFeePerGas), // maxPriorityFeePerGas
                        getEstimateGasAsync(network, "baseFee") // Add 20% to the gas price
                    )
                }
            val signedMessage = TransactionEncoder.signMessage(tx, credentials)
            val signedTx = Numeric.toHexString(signedMessage)

            transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
            if(transactionHash != "") {
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

        } else {
            jsonData.put("error", "pair not found")
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

// token to coin swap
suspend fun tokenForCoinswapAsync(
    network: String,
    fromAddress: String,
    fromTokenId: String,
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

        var transactionHash = "";

        val decimalsFunction = Function("decimals", emptyList(), listOf(object : TypeReference<Uint8>() {}))
        val encodedDecimalsFunction = FunctionEncoder.encode(decimalsFunction)
        val decimalsResponse = web3j.ethCall(
            Transaction.createEthCallTransaction(null, fromTokenId, encodedDecimalsFunction),
            DefaultBlockParameterName.LATEST
        ).send()
        val decimalsOutput =
            FunctionReturnDecoder.decode(decimalsResponse.result, decimalsFunction.outputParameters)
        val decimals = (decimalsOutput[0].value as BigInteger).toInt()
        val decimalMultiplier = BigDecimal.TEN.pow(decimals)
        val amountInWei = BigDecimal(amount).multiply(decimalMultiplier).toBigInteger()

        val toTokenId = when (network) {
            "ethereum" -> "0xC02aaA39b223FE8D0A0e5C4F27eAD9083C756Cc2"
            "cypress" -> "0"
            "polygon" -> "0x0d500B1d8E8eF31E21C99d1Db9A6444d3ADf1270"
            "bnb" -> "0xbb4CdB9CBd36B01bD1cBaEBF2De08d9173bc095c"
            else -> throw IllegalArgumentException("Invalid main network type")
        }

        // Deadline is the current time + 10 minutes in seconds
        val deadline = Instant.now().epochSecond + 600

        val path = DynamicArray(Address::class.java, listOf(Address(fromTokenId), Address(toTokenId)))

        val getPairFunction = Function("getPair", listOf(Address(fromTokenId), Address(toTokenId)), emptyList())

        val encodedGetPairFunction = FunctionEncoder.encode(getPairFunction)

        val getPairResponse = web3j.ethCall(
            Transaction.createEthCallTransaction(null, uniswapV2FactoryAddress, encodedGetPairFunction),
            DefaultBlockParameterName.LATEST
        ).send()

        val getPair = BigInteger(getPairResponse.result.replace("0x", ""), 16)

        if (getPair != BigInteger.ZERO) {
            val swapExactETHForTokensFunction = Function(
                "swapExactTokensForETH",
                listOf(Uint256(amountInWei), Uint256(BigInteger.ZERO), path, Address(fromAddress), Uint256(deadline)),
                emptyList()
            )

            val encodedFunction = FunctionEncoder.encode(swapExactETHForTokensFunction)

            val nonce = web3j.ethGetTransactionCount(fromAddress, DefaultBlockParameterName.PENDING)
                .sendAsync()
                .get()
                .transactionCount

            val chainId = web3j.ethChainId().sendAsync().get().chainId.toLong()

            val gasLimit = getEstimateGasAsync(
                network,
                "swapToken",
                fromTokenId,
                fromAddress,
                "",
                amountInWei.toString(),
                "",
                toTokenId
            )

            val tx =
                if (network == "bnb" || network == "bnbTest") {
                    RawTransaction.createTransaction(
                        nonce,
                        getEstimateGasAsync(network, "baseFee"), // Add 20% to the gas price
                        gasLimit, // Add 20% to the gas limit
                        uniswapV2RouterAddress,
                        encodedFunction
                    )
                } else {
                    RawTransaction.createTransaction(
                        chainId,
                        nonce,
                        BigInteger("200000"), // Add 20% to the gas limit,
                        uniswapV2RouterAddress,
                        BigInteger.ZERO, // value
                        encodedFunction,
                        BigInteger(maxPriorityFeePerGas), // maxPriorityFeePerGas
                        getEstimateGasAsync(network, "baseFee") // Add 20% to the gas price
                    )
                }
            val signedMessage = TransactionEncoder.signMessage(tx, credentials)
            val signedTx = Numeric.toHexString(signedMessage)

            transactionHash = web3j.ethSendRawTransaction(signedTx).sendAsync().get().transactionHash
            if(transactionHash != "") {
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

        } else {
            jsonData.put("error", "pair not found")
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

suspend fun checkTransactionStatusAsync(network: String, txHash: String): String? {
    networkSettings(network)
    val web3j = Web3j.build(HttpService(rpcUrl))
    val receipt = web3j.ethGetTransactionReceipt(txHash).send()

    if (receipt.transactionReceipt.isPresent) {
        val status = receipt.transactionReceipt.get().status
        return if (status == "0x1" || status == "0x01") {
            "Transaction Successful"
        } else if (status == "0x0" || status == "0x00") {
            "Transaction Failed"
        } else {
            "Unknown Status: $status"
        }
    } else {
        return "Transaction not yet mined"
    }
}