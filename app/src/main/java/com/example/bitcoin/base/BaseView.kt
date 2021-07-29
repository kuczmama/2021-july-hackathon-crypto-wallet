package com.example.bitcoin.base

/**
 * Created by Lynx on 4/11/2017.
 */
interface BaseView<T : BasePresenter?> {
    fun setPresenter(presenter: T)
}