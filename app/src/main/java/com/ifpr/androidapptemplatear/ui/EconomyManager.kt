package com.ifpr.androidapptemplatear.ui

import android.content.Context
import android.content.SharedPreferences

class EconomyManager(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("F1AppPrefs", Context.MODE_PRIVATE)

    fun getCoins(): Int {
        return prefs.getInt("USER_COINS", 0)
    }

    fun addCoins(amount: Int): Int {
        val currentCoins = getCoins() + amount
        prefs.edit().putInt("USER_COINS", currentCoins).apply()
        return currentCoins
    }

    fun spendCoins(amount: Int): Boolean {
        val current = getCoins()
        return if (current >= amount) {
            prefs.edit().putInt("USER_COINS", current - amount).apply()
            true
        } else {
            false
        }
    }
}