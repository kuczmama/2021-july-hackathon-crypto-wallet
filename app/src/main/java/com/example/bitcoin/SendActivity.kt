package com.example.bitcoin

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
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

        val mScanBtn: ImageView = findViewById(R.id.scannerBtn);
        val mQRCodeScanner = findViewById<CodeScannerView>(R.id.scanner_view);

        var codeScanner: CodeScanner = CodeScanner(this, mQRCodeScanner)

        // Parameters (default values)
        codeScanner.camera = CodeScanner.CAMERA_BACK // or CAMERA_FRONT or specific camera id
        codeScanner.formats = CodeScanner.ALL_FORMATS // list of type BarcodeFormat,
        // ex. listOf(BarcodeFormat.QR_CODE)
        codeScanner.autoFocusMode = AutoFocusMode.SAFE // or CONTINUOUS
        codeScanner.scanMode = ScanMode.SINGLE // or CONTINUOUS or PREVIEW
        codeScanner.isAutoFocusEnabled = true // Whether to enable auto focus or not
        codeScanner.isFlashEnabled = false // Whether to enable flash or not

        // Callbacks
        codeScanner.decodeCallback = DecodeCallback {
            runOnUiThread {
                Toast.makeText(this, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()

                sendAddress.setText("${it.text}")

                mQRCodeScanner.visibility = View.GONE
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            runOnUiThread {
                Toast.makeText(this, "Camera initialization error: ${it.message}",
                    Toast.LENGTH_LONG).show()
            }
        }


        mScanBtn.setOnClickListener {
            codeScanner.startPreview()
            mQRCodeScanner.visibility = View.VISIBLE
        }

        mQRCodeScanner.setOnClickListener {
            codeScanner.startPreview()
            mQRCodeScanner.visibility = View.GONE
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