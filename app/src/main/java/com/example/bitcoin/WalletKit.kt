package com.example.bitcoin

import android.content.Context
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params

class WalletAppKitFactory {

    companion object {
        @Volatile private var INSTANCE: WalletAppKit? = null

        fun getInstance(context: Context): WalletAppKit = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildWalletAppKit(context).also { INSTANCE = it }
        }

        private fun buildWalletAppKit(context: Context): WalletAppKit {
            val parameters: NetworkParameters? = if (Config.IS_PRODUCTION) MainNetParams.get() else TestNet3Params.get()
            return WalletAppKit(parameters, context.cacheDir, "MyWallet")
        }
    }
}