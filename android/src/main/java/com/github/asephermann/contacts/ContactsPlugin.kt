package com.github.asephermann.contacts

import android.Manifest
import android.provider.ContactsContract
import com.getcapacitor.*
import com.getcapacitor.annotation.CapacitorPlugin
import com.getcapacitor.annotation.Permission
import com.getcapacitor.annotation.PermissionCallback
import org.json.JSONArray


@CapacitorPlugin(name = "Contacts", permissions = [
    Permission(
        alias = "contacts",
        strings = [Manifest.permission.READ_CONTACTS]
    )
])
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
        contactsPermsCallback(call)
    }

    @PermissionCallback
    private fun contactsPermsCallback(call: PluginCall) {
        if (getPermissionState("contacts") == PermissionState.GRANTED) {
            loadContacts(call)
        } else {
            call.reject("Permission is required to read contacts")
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
                val id = cur.getString(
                    cur.getColumnIndex(ContactsContract.Contacts._ID)
                )
                val name = cur.getString(
                    cur.getColumnIndex(
                        ContactsContract.Contacts.DISPLAY_NAME
                    )
                )
                map["displayName"] = name
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
                    //                     Log.i("phoneNumber", "The phone number is "+ contactNumber);
                }
                if (contactNumber !== "") {
                    contactNumber = contactNumber.replace("\\s".toRegex(), "")
                    contactNumber = contactNumber.replace("-".toRegex(), "")
                    map["phoneNumber"] = contactNumber
                    contactList.add(map)
                }
            }
        }
        cur?.close()
        val jsonArray = JSONArray(contactList)
        val ret = JSObject()
        ret.put("results", jsonArray)
        call.resolve(ret)
    }
}