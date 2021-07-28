package com.example.bitcoin

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.InsufficientMoneyException
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.wallet.SendRequest


class SendActivity : AppCompatActivity() {
    private val TAG: String = SendActivity::class.simpleName!!

    private lateinit var sendAddress: EditText
    private lateinit var sendAmount: EditText
    private lateinit var sendButton: Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_send)

        sendAddress = findViewById(R.id.sendAddress)
        sendAmount = findViewById(R.id.sendAmount)
        sendButton = findViewById(R.id.sendButton)

        sendButton.setOnClickListener {
            Log.d(TAG, "Send button clicked amount = ${sendAmount.text} address = ${sendAddress.text}")
            send(sendAmount.text.toString().toLong(), sendAddress.text.toString())
        }
    }

    fun send(amount: Long, to: String) {
        if (TextUtils.isEmpty(to)) {
            Utils.toast(this@SendActivity, "Select Recipient")
            return
        }
        if (amount <= 0) {
            Utils.toast(this@SendActivity, "Select valid amount")
            return
        }
        val walletAppKit = WalletAppKitFactory.getInstance(this)
        val coinAmount = Coin.valueOf(amount)
        if (walletAppKit.wallet().balance.isLessThan(coinAmount)) {
            Utils.toast(this@SendActivity, "You don't have enough bitcoin!")
            runOnUiThread {
                sendAmount.text.clear()
            }
            return
        }
        // todo set a minimum transaction amount

        val parameters: NetworkParameters? = if (Config.IS_PRODUCTION) MainNetParams.get() else TestNet3Params.get()

        val toAddress: Address = Address.fromString(parameters, sendAddress.text.toString())
        val request =
            SendRequest.to(toAddress, coinAmount)
        try {
            walletAppKit.wallet().completeTx(request)
            walletAppKit.wallet().commitTx(request.tx)
            walletAppKit.peerGroup().broadcastTransaction(request.tx).broadcast()
        } catch (e: InsufficientMoneyException) {
            e.printStackTrace()
            Utils.toast(this@SendActivity, e.toString())
        }
    }
}