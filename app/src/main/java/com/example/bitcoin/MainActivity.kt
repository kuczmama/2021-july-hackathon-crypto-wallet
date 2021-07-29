package com.example.bitcoin

import android.content.ClipData
import android.content.ClipboardManager
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidmads.library.qrgenearator.QRGContents
import androidmads.library.qrgenearator.QRGEncoder
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.example.bitcoin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(), MainActivityContract.MainActivityView {
    private lateinit var presenter: MainActivityContract.MainActivityPresenter

    private lateinit var activityMainBinding: ActivityMainBinding

//    @SystemService
//    protected var clipboardManager: ClipboardManager? = null

    protected var strScanRecipientQRCode: String? = null
    protected var strAbout: String? = null

    private fun initData() {
        MainActivityPresenter(this, cacheDir)
    }

    private fun initUI() {
        setListeners()
        presenter.subscribe()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
        //setSupportActionBar(findViewById(R.id.toolbar_AT))
        supportActionBar?.title = "Wallet"
        initData()
        initUI()
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
                    Log.d("MainActivity", "permission granted!")
                }
                return
            }
        }
    }

    override fun setPresenter(presenter: MainActivityContract.MainActivityPresenter) {
        this.presenter = presenter
    }

    override fun displayDownloadContent(isShown: Boolean) {
        runOnUiThread {
            activityMainBinding.flDownloadContentLDP.visibility = if (isShown) View.VISIBLE else View.GONE
        }
    }

    override fun displayProgress(percent: Int) {
        runOnUiThread {
            if (activityMainBinding.pbProgressLDP.isIndeterminate()) activityMainBinding.pbProgressLDP.setIndeterminate(false)
            activityMainBinding.pbProgressLDP.progress = percent
        }
    }

    override fun displayPercentage(percent: Int) {
        runOnUiThread {
            activityMainBinding.tvPercentageLDP.text = "$percent %"
        }
    }

    override fun displayMyBalance(myBalance: String?) {
        runOnUiThread {
            activityMainBinding.tvMyBalanceAM.text = myBalance
        }
    }

    override fun displayWalletPath(walletPath: String?) {
        activityMainBinding.tvWalletFilePathAM.text = walletPath
    }

    override fun displayMyAddress(myAddress: String?) {
        runOnUiThread {
            activityMainBinding.tvMyAddressAM.text = myAddress
            // TODO Make them dynamic size instead of static
            val width: Int = 500
            val height: Int = 500
            var dimension = if (width < height) width else height
            val bitmapMyQR = QRGEncoder(myAddress, null, QRGContents.Type.TEXT, dimension)
            activityMainBinding.ivMyQRAddressAM.setImageBitmap(bitmapMyQR.bitmap)
            if (activityMainBinding.srlContentAM.isRefreshing) activityMainBinding.srlContentAM.isRefreshing = false
        }
    }

    override fun displayRecipientAddress(recipientAddress: String?) {
        activityMainBinding.tvRecipientAddressAM.text = if (TextUtils.isEmpty(recipientAddress)) strScanRecipientQRCode else recipientAddress
        activityMainBinding.tvRecipientAddressAM
            .setTextColor(
                if (TextUtils.isEmpty(recipientAddress))
                ResourcesCompat.getColor(resources, android.R.color.darker_gray, null)
            else ResourcesCompat.getColor(resources, android.R.color.holo_green_dark, null))
    }

    override fun showToastMessage(message: String?) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override val recipient: String
        get() = activityMainBinding.tvRecipientAddressAM.getText().toString().trim { it <= ' ' }
    override val amount: String
        get() = activityMainBinding.etAmountAM.getText().toString()

    override fun clearAmount() {
        activityMainBinding.etAmountAM.setText(null)
    }

    override fun startScanQR() {
        //IntentIntegrator(this).initiateScan()
    }

    override fun displayInfoDialog(myAddress: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("About")
        builder.setMessage(Html.fromHtml(strAbout))
        builder.setCancelable(true)
        builder.setPositiveButton("GOT IT", DialogInterface.OnClickListener { dialog: DialogInterface, which: Int -> dialog.dismiss() })
        val alertDialog = builder.create()
        alertDialog.show()
        val msgTxt: TextView? = alertDialog.findViewById<View>(android.R.id.message) as TextView?
        msgTxt?.setMovementMethod(LinkMovementMethod.getInstance())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
//        val scanResult: IntentResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
//        if (scanResult != null) {
//            displayRecipientAddress(scanResult.getContents())
//        }
    }

    private fun setListeners() {
        activityMainBinding.srlContentAM.setOnRefreshListener { presenter!!.refresh() }
        activityMainBinding.tvRecipientAddressAM.setOnClickListener(View.OnClickListener { v: View? -> presenter!!.pickRecipient() })
        activityMainBinding.btnSendAM!!.setOnClickListener { v: View? -> presenter!!.send() }
        activityMainBinding.etAmountAM.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                if (s.toString().trim { it <= ' ' }.isEmpty()) activityMainBinding.etAmountAM.setText("0.00")
            }
        })
        activityMainBinding.ivCopyAM.setOnClickListener { v: View? ->
            val clip: ClipData = ClipData.newPlainText("My wallet address", activityMainBinding.tvMyAddressAM.text.toString())
            //clipboardManager?.setPrimaryClip(clip)
            Toast.makeText(this@MainActivity, "Copied", Toast.LENGTH_SHORT).show()
        }
    }
}