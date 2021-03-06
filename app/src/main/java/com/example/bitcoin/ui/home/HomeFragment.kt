package com.example.bitcoin.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.bitcoin.R
import com.example.bitcoin.Utils
import com.example.bitcoin.WalletAppKitFactory
import com.example.bitcoin.WebsocketHelper
import com.example.bitcoin.databinding.FragmentHomeBinding
import com.example.bitcoin.ui.BitcoinPriceAdapter
import com.example.bitcoin.ui.TransactionAdapter
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.bitcoinj.core.Address
import org.bitcoinj.core.Coin
import org.bitcoinj.core.Transaction
import org.bitcoinj.core.listeners.DownloadProgressTracker
import org.bitcoinj.wallet.Wallet
import org.java_websocket.client.WebSocketClient
import java.util.*

class HomeFragment : Fragment() {

    val TAG = HomeFragment::class.simpleName

    // Fragment binding
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // Waller
    private var walletAddress: Address? = null
    private lateinit var wallet: Wallet
    private var satoshis: Int = 0

    // Views
    private lateinit var balanceText: TextView
    private lateinit var balanceInUSD: TextView
    private lateinit var root: View
    private lateinit var transactionsListView: ListView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        root = binding.root
        balanceText = root.findViewById(R.id.balanceText)
        balanceInUSD = root.findViewById(R.id.balanceInUSDText)
        transactionsListView = root.findViewById(R.id.transactionsListView)

        init()
        return root
    }

    private fun init() {
        // There is some issues getting permission from fragment
        // until then we can just manually give permission
        createWallet()
        getActivity()?.let {
            ActivityCompat.requestPermissions(
                it,
                arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                1
            )
        };
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
                    //createWallet()
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    // Utils.toast(, "Permission denied to read your External storage")
                }
                return
            }
        }
    }

    private fun renderTransactions() {
        Log.d(TAG, "Rendering transactions")
        val adapter = TransactionAdapter(
            root.context,
            wallet,
            wallet.transactionsByTime
        )
        transactionsListView.adapter = adapter
    }

    val priceListener = object: WebsocketHelper.Companion.Listener {
        override fun onMessage(message: String) {
            Log.d(TAG, message)
            setUpBtcPriceText(message)
        }
    }

    private fun createWallet() {
        Log.d(TAG, "checking permission")
        // Download the block chain and wait until it's done.
        Log.d(TAG, "syncing blockchain")
        val walletAppKit = WalletAppKitFactory.getInstance(root.context)

        if (walletAppKit.isRunning) {
            Log.d(TAG, "Wallet app kit is already created and running, don't recreate")
            wallet = walletAppKit.wallet()

            satoshis = wallet.getBalance().getValue().toInt()

            balanceText.text = wallet.balance.toFriendlyString()
            renderTransactions()
            WebsocketHelper.create(priceListener)
            return
        }
        Utils.toast(root.context, "Syncing blockchain..")
        walletAppKit.setDownloadListener(object : DownloadProgressTracker() {
            override fun progress(pct: Double, blocksSoFar: Int, date: Date?) {
                super.progress(pct, blocksSoFar, date)
                val percentage = pct.toInt()
                Log.d(TAG, "percentage synced: $percentage")
            }

            override fun doneDownload() {
                super.doneDownload()
                wallet = walletAppKit.wallet()

                Log.d(TAG, "Download complete!")
                walletAddress = wallet.freshReceiveAddress()
                Log.d(TAG, "Wallet Address: $walletAddress")
                Log.d(TAG, "Balance: ${wallet.balance.toFriendlyString()}")

                activity?.runOnUiThread {
                    balanceText.text = wallet.balance.toFriendlyString()
                    renderTransactions()
                }


                satoshis = wallet.getBalance().getValue().toInt()
                WebsocketHelper.create(priceListener)

                wallet.addCoinsReceivedEventListener { wallet1: Wallet?, tx: Transaction, prevBalance: Coin?, newBalance: Coin ->
                    Log.d(TAG, "Tx received Balance: ${wallet.balance}")
                    if (tx.purpose == Transaction.Purpose.UNKNOWN) Utils.toast(
                        root.context,
                        "Receive " + newBalance.minus(
                            prevBalance
                        ).toFriendlyString()
                    )
                }
                wallet.addCoinsSentEventListener { wallet12: Wallet?, tx: Transaction, prevBalance: Coin, newBalance: Coin? ->
                    Log.d(TAG, "Coins sent -- balance:  ${wallet.balance}")
                    Utils.toast(
                        root.context,
                        "Sent " + prevBalance.minus(newBalance).minus(tx.fee)
                            .toFriendlyString()
                    )
                }


            }
        })
        walletAppKit.setBlockingStartup(false)
        walletAppKit.startAsync()

    }

    // Parsing the response and setting the value into btn
    private fun setUpBtcPriceText(message: String?) {

        // Parsing the message from the websocket using moshi and Json Adapter libraries
        message?.let {
            val moshi = Moshi.Builder().build()
            val adapter: JsonAdapter<BitcoinPriceAdapter> = moshi.adapter(BitcoinPriceAdapter::class.java)
            val bitcoin = adapter.fromJson(message)

            getActivity()?.runOnUiThread {
                balanceInUSD.text = Utils.convertBTCtoUSD( bitcoin?.price, satoshis )
                balanceInUSD.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        WebsocketHelper.destroy()
        _binding = null
    }
}