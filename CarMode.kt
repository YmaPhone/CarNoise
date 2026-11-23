package com.ymaphone.carnoise

import android.app.NotificationManager
import android.app.NotificationManager.Policy
import android.content.Context

object CarMode {
    fun hasAccess(ctx: Context): Boolean =
        ctx.getSystemService(NotificationManager::class.java).isNotificationPolicyAccessGranted

    fun enter(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        if (!nm.isNotificationPolicyAccessGranted) return
        val p = Prefs(ctx)
        if (!p.inCar) {
            val old = nm.notificationPolicy
            p.savedFilter = nm.currentInterruptionFilter
            p.savedCats = old.priorityCategories
            p.savedCalls = old.priorityCallSenders
            p.savedMsgs = old.priorityMessageSenders
            p.savedEffects = old.suppressedVisualEffects
        }
        var cats = 0
        if (p.allowMedia) cats = cats or Policy.PRIORITY_CATEGORY_MEDIA
        if (p.allowCalls) cats = cats or Policy.PRIORITY_CATEGORY_CALLS
        if (p.allowMessages) cats = cats or Policy.PRIORITY_CATEGORY_MESSAGES
        if (p.allowAlarms) cats = cats or Policy.PRIORITY_CATEGORY_ALARMS
        nm.notificationPolicy = Policy(
            cats, Policy.PRIORITY_SENDERS_ANY, Policy.PRIORITY_SENDERS_ANY, p.savedEffects
        )
        nm.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        p.inCar = true
    }

    fun exit(ctx: Context) {
        val nm = ctx.getSystemService(NotificationManager::class.java)
        val p = Prefs(ctx)
        if (!p.inCar) return
        if (nm.isNotificationPolicyAccessGranted) {
            nm.notificationPolicy = Policy(p.savedCats, p.savedCalls, p.savedMsgs, p.savedEffects)
            if (p.savedFilter > 0) nm.setInterruptionFilter(p.savedFilter)
        }
        p.inCar = false
    }
}
