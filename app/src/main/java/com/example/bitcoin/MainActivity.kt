package com.example.bitcoin

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.bitcoinj.core.*
import org.bitcoinj.core.listeners.DownloadProgressTracker
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.wallet.Wallet
import java.util.*


class MainActivity : AppCompatActivity() {
    final val TAG = "Bitcoin!!"
    final val IS_PRODUCTION = false
    private var parameters: NetworkParameters? = null
    private var walletAppKit: WalletAppKit? = null
    private var walletAddress: Address? = null

    // Only supports legacy addresses
    final val bitcoinAddress = "myxWv5jFrezuxM6gPvAM77jPPQfM3nLvDE"
    var mainText: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mainText = findViewById(R.id.main_text)
        toast("Loading...")

        init()
    }

    private fun init() {
        parameters = if (IS_PRODUCTION) MainNetParams.get() else TestNet3Params.get()
        val forwardingAddress: Address = LegacyAddress.fromBase58(parameters, bitcoinAddress)

        Log.d(TAG, "Forwarding address: $forwardingAddress");
        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 1);
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            1 -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults[0] === PackageManager.PERMISSION_GRANTED
                ) {
                    Log.d(TAG, "permission granted!")
                    createWallet()
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(
                        this@MainActivity,
                        "Permission denied to read your External storage",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }

    private fun toast(str: String) {
        Toast.makeText(
            this@MainActivity,
            str,
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun createWallet() {
        Log.d(TAG, "checking permission")
        parameters = if (IS_PRODUCTION) MainNetParams.get() else TestNet3Params.get()
        // Download the block chain and wait until it's done.
        Log.d(TAG, "syncing blockchain")
        walletAppKit = WalletAppKit(parameters, cacheDir, "MyWallet")
        walletAppKit?.setDownloadListener(object : DownloadProgressTracker() {
            override fun progress(pct: Double, blocksSoFar: Int, date: Date?) {
                super.progress(pct, blocksSoFar, date)
                val percentage = pct.toInt()
                Log.d(TAG, "percentage synced: $percentage")
            }

            override fun doneDownload() {
                super.doneDownload()
                val wallet: Wallet = walletAppKit?.wallet()!!
                Log.d(TAG, "Download complete!")
                walletAddress = wallet.freshReceiveAddress()
                Log.d(TAG, "Wallet Address: $walletAddress")
                Log.d(TAG, "Balance: ${wallet.balance}")
                wallet.addCoinsReceivedEventListener { wallet1: Wallet?, tx: Transaction, prevBalance: Coin?, newBalance: Coin ->
                    Log.d(TAG, "Tx received Balance: ${wallet.balance}")
                    if (tx.purpose == Transaction.Purpose.UNKNOWN) toast(
                        "Receive " + newBalance.minus(
                            prevBalance
                        ).toFriendlyString()
                    )
                }
                wallet.addCoinsSentEventListener { wallet12: Wallet?, tx: Transaction, prevBalance: Coin, newBalance: Coin? ->
                    Log.d(TAG, "Coins sent -- balance:  ${wallet.balance}")
                    toast(
                        "Sent " + prevBalance.minus(newBalance).minus(tx.fee)
                            .toFriendlyString()
                    )
                }
            }
        })
        walletAppKit?.setBlockingStartup(false)
        walletAppKit?.startAsync()


    }
}