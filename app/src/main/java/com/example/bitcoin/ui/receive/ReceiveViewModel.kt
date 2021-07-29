package com.example.bitcoin.ui.receive

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ReceiveViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is receive Fragment"
    }
    val text: LiveData<String> = _text
}