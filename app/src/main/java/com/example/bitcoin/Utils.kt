package com.example.bitcoin

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat


class Utils {

    companion object {
        val TAG = Utils::class.simpleName

        fun toast(context: Context, str: String, duration: Int = Toast.LENGTH_SHORT) : Unit {
            Toast.makeText(
                context,
                str,
                duration
            ).show()
        }

        fun showNotification(context: Context, title: String, text: String): Unit {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val CHANNEL_ID = "btc_channel"
                val mNotificationManager =
                    context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager?
                val importance = NotificationManager.IMPORTANCE_HIGH
                val mChannel = NotificationChannel(CHANNEL_ID, "name", importance)
                mChannel.enableLights(true)
                mNotificationManager!!.createNotificationChannel(mChannel)

                val notification: Notification = NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(text)
                    .setColor(context.getResources().getColor(R.color.holo_blue_light))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setPriority(NotificationCompat.PRIORITY_MAX) // heads-up
                    .build()

                val notificationManager = NotificationManagerCompat.from(context)
                notificationManager.notify(0, notification)
            }
        }

        fun convertBTCtoUSD(price:Double?, satoshis: Int) : String{
            val balance = (price?.times(satoshis))?.div(Constants.BTC_IN_SATOSHIS)
            Log.d(TAG, "convertBTCtoUSD | $balance")
            val balanceRounded:String = String.format("%.2f", balance)
            return "$balanceRounded $"
        }
    }
}