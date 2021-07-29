package com.example.bitcoin

import com.example.bitcoin.base.BasePresenter
import com.example.bitcoin.base.BaseView

/**
 * Created by Lynx on 4/11/2017.
 */
interface MainActivityContract {
    interface MainActivityView : BaseView<MainActivityPresenter> {
        fun displayDownloadContent(isShown: Boolean)
        fun displayProgress(percent: Int)
        fun displayPercentage(percent: Int)
        fun displayMyBalance(myBalance: String?)
        fun displayWalletPath(walletPath: String?)
        fun displayMyAddress(myAddress: String?)
        fun displayRecipientAddress(recipientAddress: String?)
        fun showToastMessage(message: String?)
        val recipient: String
        val amount: String
        fun clearAmount()
        fun startScanQR()
        fun displayInfoDialog(myAddress: String?)
    }

    interface MainActivityPresenter : BasePresenter {
        fun refresh()
        fun pickRecipient()
        fun send()
        val infoDialog: Unit
    }

    interface MainActivityModel
}