package com.ymaphone.carnoise

import android.content.Context

class Prefs(ctx: Context) {
    private val sp = ctx.applicationContext.getSharedPreferences("carnoise", Context.MODE_PRIVATE)

    private fun b(k: String, d: Boolean) = sp.getBoolean(k, d)
    private fun b(k: String, v: Boolean, @Suppress("UNUSED_PARAMETER") x: Unit = Unit) = sp.edit().putBoolean(k, v).apply()

    var carAddress: String?
        get() = sp.getString("addr", null)
        set(v) = sp.edit().putString("addr", v).apply()
    var btName: String
        get() = sp.getString("btName", "") ?: ""
        set(v) = sp.edit().putString("btName", v).apply()
    var nickname: String
        get() = sp.getString("nick", "") ?: ""
        set(v) = sp.edit().putString("nick", v).apply()
    var model: String
        get() = sp.getString("model", "") ?: ""
        set(v) = sp.edit().putString("model", v).apply()
    var year: String
        get() = sp.getString("year", "") ?: ""
        set(v) = sp.edit().putString("year", v).apply()
    var plate: String
        get() = sp.getString("plate", "") ?: ""
        set(v) = sp.edit().putString("plate", v).apply()

    var autoEnable: Boolean
        get() = b("auto", true); set(v) = b("auto", v, Unit)
    var allowMedia: Boolean
        get() = b("media", true); set(v) = b("media", v, Unit)
    var allowCalls: Boolean
        get() = b("calls", false); set(v) = b("calls", v, Unit)
    var allowMessages: Boolean
        get() = b("msgs", false); set(v) = b("msgs", v, Unit)
    var allowAlarms: Boolean
        get() = b("alarms", true); set(v) = b("alarms", v, Unit)
    var inCar: Boolean
        get() = b("inCar", false); set(v) = b("inCar", v, Unit)

    // מצב ה"נא לא להפריע" המקורי, כדי להחזיר אותו אחרי הנסיעה
    var savedFilter: Int
        get() = sp.getInt("sFilter", -1); set(v) = sp.edit().putInt("sFilter", v).apply()
    var savedCats: Int
        get() = sp.getInt("sCats", 0); set(v) = sp.edit().putInt("sCats", v).apply()
    var savedCalls: Int
        get() = sp.getInt("sCalls", 0); set(v) = sp.edit().putInt("sCalls", v).apply()
    var savedMsgs: Int
        get() = sp.getInt("sMsgs", 0); set(v) = sp.edit().putInt("sMsgs", v).apply()
    var savedEffects: Int
        get() = sp.getInt("sEff", 0); set(v) = sp.edit().putInt("sEff", v).apply()

    val displayName: String
        get() = nickname.ifBlank { btName.ifBlank { "לא נבחר רכב" } }
}
