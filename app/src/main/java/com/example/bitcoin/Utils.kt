package com.example.bitcoin

import android.content.Context
import android.widget.Toast

class Utils {
    companion object {
        fun toast(context: Context, str: String, duration: Int = Toast.LENGTH_SHORT) : Unit {
            Toast.makeText(
                context,
                str,
                duration
            ).show()
        }
    }
}