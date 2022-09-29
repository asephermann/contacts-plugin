package com.github.asephermann.contacts

import android.Manifest
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
import com.getcapacitor.Plugin
import org.json.JSONArray
import java.util.HashMap

@CapacitorPlugin(name = "Contacts")
class ContactsPlugin : Plugin() {
    private val implementation = Contacts()
    @PluginMethod
    fun echo(call: PluginCall) {
        val value = call.getString("value")
        val ret = JSObject()
        ret.put("value", implementation.echo(value))
        call.resolve(ret)
    }

    @PluginMethod
    fun getContacts(call: PluginCall) {
        val value = call.getString("filter")
        // Filter based on the value if want
        saveCall(call)
        pluginRequestPermission(Manifest.permission.READ_CONTACTS, REQUEST_CONTACTS)
    }

    override fun handleRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.handleRequestPermissionsResult(requestCode, permissions, grantResults)
        val savedCall = savedCall
        if (savedCall == null) {
            Log.d("Test", "No stored plugin call for permissions request result")
            return
        }
        for (result in grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                Log.d("Test", "User denied permission")
                return
            }
        }
        if (requestCode == REQUEST_CONTACTS) {
            // We got the permission!
            loadContacts(savedCall)
        }
    }

    fun loadContacts(call: PluginCall) {
        val contactList = ArrayList<Map<*, *>?>()
        val cr = this.context.contentResolver
        val cur = cr.query(
            ContactsContract.Contacts.CONTENT_URI,
            null, null, null, null
        )
        if (cur?.count ?: 0 > 0) {
            while (cur != null && cur.moveToNext()) {
                val map: MutableMap<String, String> = HashMap()
                val id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID))
                val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))
                map["firstName"] = name
                map["lastName"] = ""
                var contactNumber = ""
                if (cur.getInt(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER)) > 0) {
                    val pCur = cr.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        arrayOf(id),
                        null
                    )
                    pCur!!.moveToFirst()
                    contactNumber =
                        pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                    Log.i("phoneNUmber", "The phone number is $contactNumber")
                }
                map["telephone"] = contactNumber
                contactList.add(map)
            }
        }
        cur?.close()
        val jsonArray = JSONArray(contactList)
        val ret = JSObject()
        ret.put("results", jsonArray)
        call.success(ret)
    }

    companion object {
        protected const val REQUEST_CONTACTS = 12345 // Unique request code
    }
}