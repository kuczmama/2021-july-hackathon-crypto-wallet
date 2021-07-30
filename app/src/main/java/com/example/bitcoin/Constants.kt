package com.example.bitcoin

/**
 * Created by Lynx on 4/11/2017.
 */
object Constants {
    const val WALLET_NAME = "users_wallet"
    var IS_PRODUCTION = false
    const val WEB_SOCKET_URL = "wss://ws-feed.pro.coinbase.com"

    const val UNSUBSCRIBE_COINBASE_SCRIPT = "{\n" +
            "    \"type\": \"unsubscribe\",\n" +
            "    \"channels\": [\"ticker\"]\n" +
            "}"

    const val SUBSCRIBE_COINBASE_SCRIPT = "{\n" +
            "    \"type\": \"subscribe\",\n" +
            "    \"channels\": [{ \"name\": \"ticker\", \"product_ids\": [\"BTC-USD\"] }]\n" +
            "}"


    const val BTC_IN_SATOSHIS = 100000000
}