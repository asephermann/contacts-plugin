package com.github.asephermann.contacts

import android.util.Log
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.PluginMethod
import com.getcapacitor.PluginCall
import com.getcapacitor.JSObject
import com.github.asephermann.contacts.ContactsPlugin
import android.content.pm.PackageManager
import java.util.ArrayList
import android.content.ContentResolver
import android.database.Cursor
import android.provider.ContactsContract
import java.util.HashMap

class Contacts {
    fun echo(value: String?): String? {
        Log.i("Echo", value!!)
        return value
    }
}