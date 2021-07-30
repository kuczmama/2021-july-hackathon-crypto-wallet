package com.example.bitcoin.ui

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class BitcoinPriceAdapter(val price: Double?)