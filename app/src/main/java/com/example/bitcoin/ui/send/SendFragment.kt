package com.example.bitcoin.ui.send

import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.CodeScannerView
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.example.bitcoin.*
import com.example.bitcoin.databinding.FragmentSendBinding
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.InsufficientMoneyException
import org.bitcoinj.core.NetworkParameters
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.wallet.SendRequest

class SendFragment : Fragment() {

    // This property is only valid between onCreateView and
    // onDestroyView.
    private var _binding: FragmentSendBinding? = null
    private val binding get() = _binding!!

    private val TAG: String = SendFragment::class.simpleName!!

    private lateinit var sendAddress: EditText
    private lateinit var sendAmount: EditText
    private lateinit var sendButton: Button
    private lateinit var root: View


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSendBinding.inflate(inflater, container, false)
         root = binding.root


        sendAddress = root.findViewById(R.id.sendAddress)
        sendAmount = root.findViewById(R.id.sendAmount)
        sendButton = root.findViewById(R.id.sendButton)

        sendButton.setOnClickListener {
            Log.d(TAG, "Send button clicked amount = ${sendAmount.text} address = ${sendAddress.text}")
            send(sendAmount.text.toString().toLong(), sendAddress.text.toString())
        }

        val mScanBtn: ImageView = root.findViewById(R.id.scannerBtn);
        val mQRCodeScanner = root.findViewById<CodeScannerView>(R.id.scanner_view);

        var codeScanner: CodeScanner = CodeScanner(root.context, mQRCodeScanner)

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
            getActivity()?.runOnUiThread {
                Toast.makeText(root.context, "Scan result: ${it.text}", Toast.LENGTH_LONG).show()

                sendAddress.setText("${it.text}")

                mQRCodeScanner.visibility = View.GONE
            }
        }
        codeScanner.errorCallback = ErrorCallback { // or ErrorCallback.SUPPRESS
            getActivity()?.runOnUiThread {
                Toast.makeText(root.context, "Camera initialization error: ${it.message}",
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

        return root;
    }

    fun send(amount: Long, to: String) {
        if (TextUtils.isEmpty(to)) {
            Utils.toast(root.context, "Select Recipient")
            return
        }
        if (amount <= 0) {
            Utils.toast(root.context, "Select valid amount")
            return
        }
        val walletAppKit = WalletAppKitFactory.getInstance(root.context)
        val coinAmount = Coin.valueOf(amount)
        if (walletAppKit.wallet().balance.isLessThan(coinAmount)) {
            Utils.toast(root.context, "You don't have enough bitcoin!")
            getActivity()?.runOnUiThread {
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
            Utils.toast(root.context, e.toString())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}