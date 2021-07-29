package com.example.bitcoin

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.bitcoinj.core.*
import org.bitcoinj.core.listeners.DownloadProgressTracker
import org.bitcoinj.wallet.Wallet
import java.util.*


class MainActivity : AppCompatActivity() {
    val TAG = MainActivity::class.simpleName
    private var walletAddress: Address? = null
    private lateinit var sendButton: Button
    private lateinit var receiveButton: Button
    private lateinit var balanceText: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Utils.toast(this@MainActivity, "Loading...")
        sendButton = findViewById(R.id.sendButton)
        receiveButton = findViewById(R.id.receiveButton)
        balanceText = findViewById(R.id.balanceText)
        init()
    }

    private fun init() {
        sendButton.isEnabled = true
        receiveButton.isEnabled = true
        sendButton.setOnClickListener {
            val intent = Intent(this, SendActivity::class.java)
            startActivity(intent)
        }

        receiveButton.setOnClickListener {
            val intent = Intent(this, ReceiveActivity::class.java).apply {
                putExtra(Config.RECEIVE_ADDRESS_KEY.toString(), walletAddress.toString())
            }

            startActivity(intent)
        }

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
                    Utils.toast(this@MainActivity, "Permission denied to read your External storage")
                }
                return
            }
        }
    }

    private fun setText(text: TextView, value: String) {
        runOnUiThread { text.text = value }
    }

    private fun createWallet() {
        Log.d(TAG, "checking permission")
        // Download the block chain and wait until it's done.
        Log.d(TAG, "syncing blockchain")
        val walletAppKit = WalletAppKitFactory.getInstance(this)
        walletAppKit.setDownloadListener(object : DownloadProgressTracker() {
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

                runOnUiThread {
                    balanceText.text = "${wallet.balance} sats"
                    sendButton.isEnabled = true
                    receiveButton.isEnabled = true
                }

                wallet.addCoinsReceivedEventListener { wallet1: Wallet?, tx: Transaction, prevBalance: Coin?, newBalance: Coin ->
                    Log.d(TAG, "Tx received Balance: ${wallet.balance}")
                    if (tx.purpose == Transaction.Purpose.UNKNOWN) Utils.toast(this@MainActivity,
                        "Receive " + newBalance.minus(
                            prevBalance
                        ).toFriendlyString()
                    )
                }
                wallet.addCoinsSentEventListener { wallet12: Wallet?, tx: Transaction, prevBalance: Coin, newBalance: Coin? ->
                    Log.d(TAG, "Coins sent -- balance:  ${wallet.balance}")
                    Utils.toast(this@MainActivity,
                        "Sent " + prevBalance.minus(newBalance).minus(tx.fee)
                            .toFriendlyString()
                    )
                }
            }
        })
        walletAppKit.setBlockingStartup(false)
        walletAppKit.startAsync()
    }
}