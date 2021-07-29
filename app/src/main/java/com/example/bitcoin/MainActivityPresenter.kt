package com.example.bitcoin

import android.os.Handler
import android.text.TextUtils
import android.util.Log
import com.example.bitcoin.MainActivityContract.MainActivityView
import org.bitcoinj.core.*
import org.bitcoinj.core.listeners.DownloadProgressTracker
import org.bitcoinj.kits.WalletAppKit
import org.bitcoinj.params.MainNetParams
import org.bitcoinj.params.TestNet3Params
import org.bitcoinj.utils.BriefLogFormatter
import org.bitcoinj.utils.Threading
import org.bitcoinj.wallet.SendRequest
import org.bitcoinj.wallet.Wallet
import java.io.File
import java.util.*
import java.util.concurrent.Executor

/**
 * Presenter where all the business logic resides
 */
class MainActivityPresenter(private val view: MainActivityView, private val walletDir: File) : MainActivityContract.MainActivityPresenter {
    private var parameters: NetworkParameters? = null
    private var walletAppKit: WalletAppKit? = null
    val TAG : String = "MainActivityPresenter"
    override fun subscribe() {
        setBtcSDKThread()
        parameters = if (Constants.IS_PRODUCTION) MainNetParams.get() else TestNet3Params.get()
        BriefLogFormatter.init()
        walletAppKit = object : WalletAppKit(parameters, walletDir, Constants.WALLET_NAME) {
            override fun onSetupCompleted() {
                if (wallet().importedKeys.size < 1) wallet().importKey(ECKey())
                wallet().allowSpendingUnconfirmedTransactions()
                view.displayWalletPath(vWalletFile.absolutePath)
                setupWalletListeners(wallet())
                Log.d(TAG, "My address = " + wallet().freshReceiveAddress())
            }
        }
        walletAppKit?.setDownloadListener(object : DownloadProgressTracker() {
             override fun progress(pct: Double, blocksSoFar: Int, date: Date) {
                super.progress(pct, blocksSoFar, date)
                val percentage = pct.toInt()
                view.displayPercentage(percentage)
                view.displayProgress(percentage)
            }

             override fun doneDownload() {
                super.doneDownload()
                view.displayDownloadContent(false)
                refresh()
            }
        })
        walletAppKit?.setBlockingStartup(false)
        walletAppKit?.startAsync()
    }

    override fun unsubscribe() {}
    override fun refresh() {
        val myAddress: String = walletAppKit?.wallet()?.freshReceiveAddress().toString()
        view.displayMyBalance(walletAppKit?.wallet()?.balance?.toFriendlyString())
        view.displayMyAddress(myAddress)
    }

    override fun pickRecipient() {
        view.displayRecipientAddress(null)
        view.startScanQR()
    }

    override fun send() {
        val recipientAddress: String = view.recipient
        val amount: String = view.amount
        if (TextUtils.isEmpty(recipientAddress) || recipientAddress == "Scan recipient QR") {
            view.showToastMessage("Select recipient")
            return
        }
        if (TextUtils.isEmpty(amount) or (amount.toDouble() <= 0)) {
            view.showToastMessage("Select valid amount")
            return
        }
        if (walletAppKit?.wallet()?.balance?.isLessThan(Coin.parseCoin(amount)) == true) {
            view.showToastMessage("You got not enough coins")
            view.clearAmount()
            return
        }
        val request: SendRequest = SendRequest.to(Address.fromString(parameters, recipientAddress), Coin.parseCoin(amount))
        try {
            walletAppKit?.wallet()?.completeTx(request)
            walletAppKit?.wallet()?.commitTx(request.tx)
            walletAppKit?.peerGroup()?.broadcastTransaction(request.tx)?.broadcast()
        } catch (e: InsufficientMoneyException) {
            e.printStackTrace()
            view.showToastMessage(e.message)
        }
    }

    override val infoDialog: Unit
        get() {
            view.displayInfoDialog(walletAppKit?.wallet()?.currentReceiveAddress()?.toString())
        }

    private fun setBtcSDKThread() {
        val handler = Handler()
        Threading.USER_THREAD = Executor { r: Runnable? -> handler.post(r!!) }
    }

    private fun setupWalletListeners(wallet: Wallet) {
        wallet.addCoinsReceivedEventListener { _: Wallet?, tx: Transaction, prevBalance: Coin?, newBalance: Coin ->
            view.displayMyBalance(wallet.balance.toFriendlyString())
            if (tx.purpose == Transaction.Purpose.UNKNOWN) view.showToastMessage(
                "Receive " + newBalance.minus(prevBalance).toFriendlyString()
            )
        }
        wallet.addCoinsSentEventListener { _: Wallet?, tx: Transaction, prevBalance: Coin, newBalance: Coin? ->
            view.displayMyBalance(wallet.balance.toFriendlyString())
            view.clearAmount()
            view.displayRecipientAddress(null)
            view.showToastMessage("Sent " + prevBalance.minus(newBalance).minus(tx.fee).toFriendlyString())
        }
    }

    init {
        view.setPresenter(this)
    }
}