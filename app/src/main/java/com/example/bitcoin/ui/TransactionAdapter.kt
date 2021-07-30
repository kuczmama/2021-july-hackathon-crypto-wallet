package com.example.bitcoin.ui

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.bitcoin.R
import org.bitcoinj.core.Transaction
import org.bitcoinj.wallet.Wallet


class TransactionAdapter(
    context: Context,
    private val wallet: Wallet,
    private val transactions: List<Transaction>
) : BaseAdapter() {
    private val TAG = TransactionAdapter::class.simpleName

    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return transactions.size
    }

    override fun getItem(position: Int): Transaction {
        return transactions[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        var view = convertView
        if (view == null) {
            view = inflater.inflate(R.layout.list_item_transaction, parent, false)
        }

        val transaction = transactions[position]
        Log.d(TAG, "txvalue = ${transaction.getValue(wallet)}")

        // Set date
        val txDate: TextView = view!!.findViewById(R.id.tx_date)
        txDate.text = transaction.updateTime.toString()

        // Set amount
        val txAmountTextView: TextView = view.findViewById(R.id.tx_amount_btc)
        val txAmount = transaction.getValue(wallet)
        txAmountTextView.text = txAmount.toFriendlyString()

        // Set message & Image
        val txMessage: TextView = view.findViewById(R.id.tx_status_message)
        val txImage: ImageView = view.findViewById(R.id.tx_image)

        var messageText = "Received"
        txImage.setImageResource(R.drawable.ic_arrow_up)
        if (transaction.isPending) {
            messageText = "Pending"
            txImage.setImageResource(R.drawable.ic_pending)
        } else if (txAmount.isNegative) {
            messageText = "Sent"
            txImage.setImageResource(R.drawable.ic_arrow_down)
        }

        txMessage.text = messageText

        return view
    }

}