package io.github.jameslebone.autocontactbridge.shared.data

import android.content.Context
import android.content.SharedPreferences
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class ContactRepository(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences

    init {
        // Migrate from old prefs name if needed
        val oldPrefs = appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
        val newPrefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        if (oldPrefs.contains(KEY_CONTACTS) && !newPrefs.contains(KEY_CONTACTS)) {
            val oldData = oldPrefs.getString(KEY_CONTACTS, null)
            if (oldData != null) {
                newPrefs.edit().putString(KEY_CONTACTS, oldData).apply()
            }
            oldPrefs.edit().clear().apply()
        }
        prefs = newPrefs
    }

    fun getContacts(): List<Contact> {
        val jsonString = prefs.getString(KEY_CONTACTS, null)
        if (jsonString.isNullOrEmpty()) {
            val defaults = defaultContacts()
            saveContacts(defaults)
            return defaults
        }

        return try {
            val jsonArray = JSONArray(jsonString)
            val list = mutableListOf<Contact>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val channel = try {
                    MessageChannel.valueOf(obj.optString("preferredChannel", obj.optString("channel", MessageChannel.SMS.name)))
                } catch (e: IllegalArgumentException) {
                    MessageChannel.SMS
                }
                list.add(
                    Contact(
                        id = obj.getString("id"),
                        name = obj.getString("name"),
                        customTitle = obj.optString("customTitle", ""),
                        phoneNumber = obj.optString("phoneNumber", obj.optString("address", "")),
                        messengerHandle = obj.optString("messengerHandle", ""),
                        preferredChannel = channel,
                        avatarUri = if (obj.has("avatarUri") && !obj.isNull("avatarUri")) obj.getString("avatarUri") else null,
                        isEnabled = obj.optBoolean("isEnabled", true)
                    )
                )
            }
            list
        } catch (e: Exception) {
            defaultContacts()
        }
    }

    fun saveContact(contact: Contact) {
        val current = getContacts().toMutableList()
        val index = current.indexOfFirst { it.id == contact.id }
        if (index != -1) {
            current[index] = contact
        } else {
            current.add(contact)
        }
        saveContacts(current)
    }

    fun addContact(
        name: String,
        customTitle: String,
        phoneNumber: String,
        messengerHandle: String,
        preferredChannel: MessageChannel,
        avatarUri: String? = null
    ): Contact {
        val newContact = Contact(
            id = UUID.randomUUID().toString(),
            name = name,
            customTitle = customTitle,
            phoneNumber = phoneNumber,
            messengerHandle = messengerHandle,
            preferredChannel = preferredChannel,
            avatarUri = avatarUri
        )
        saveContact(newContact)
        return newContact
    }

    fun togglePreferredChannel(contactId: String) {
        val current = getContacts().toMutableList()
        val index = current.indexOfFirst { it.id == contactId }
        if (index != -1) {
            val old = current[index]
            val nextChannel = if (old.preferredChannel == MessageChannel.SMS) {
                MessageChannel.MESSENGER
            } else {
                MessageChannel.SMS
            }
            current[index] = old.copy(preferredChannel = nextChannel)
            saveContacts(current)
        }
    }

    fun toggleContactEnabled(contactId: String) {
        val current = getContacts().toMutableList()
        val index = current.indexOfFirst { it.id == contactId }
        if (index != -1) {
            val old = current[index]
            current[index] = old.copy(isEnabled = !old.isEnabled)
            saveContacts(current)
        }
    }

    fun deleteContact(id: String) {
        val current = getContacts().filterNot { it.id == id }
        saveContacts(current)
    }

    fun reorderContacts(newOrder: List<Contact>) {
        saveContacts(newOrder)
    }

    fun moveContact(fromIndex: Int, toIndex: Int): List<Contact> {
        val current = getContacts().toMutableList()
        if (fromIndex in current.indices && toIndex in current.indices && fromIndex != toIndex) {
            val item = current.removeAt(fromIndex)
            current.add(toIndex, item)
            saveContacts(current)
        }
        return current
    }

    fun resetToDefaults(): List<Contact> {
        val defaults = defaultContacts()
        saveContacts(defaults)
        return defaults
    }

    /**
     * Removes all saved contacts. The next call to [getContacts] will
     * return an empty list (or defaults, depending on caller intent).
     */
    fun clearAllContacts() {
        prefs.edit().remove(KEY_CONTACTS).apply()
    }

    /**
     * Wipes every key in the app's SharedPreferences file.
     * Use this for the "Erase All Data" flow.
     */
    fun clearAllData() {
        prefs.edit().clear().apply()
        // Also clear legacy prefs if they still exist
        appContext.getSharedPreferences(LEGACY_PREFS_NAME, Context.MODE_PRIVATE)
            .edit().clear().apply()
    }

    private fun saveContacts(contacts: List<Contact>) {
        val array = JSONArray()
        for (contact in contacts) {
            val obj = JSONObject().apply {
                put("id", contact.id)
                put("name", contact.name)
                put("customTitle", contact.customTitle)
                put("phoneNumber", contact.phoneNumber)
                put("messengerHandle", contact.messengerHandle)
                put("preferredChannel", contact.preferredChannel.name)
                put("isEnabled", contact.isEnabled)
                if (contact.avatarUri != null) {
                    put("avatarUri", contact.avatarUri)
                }
            }
            array.put(obj)
        }
        prefs.edit().putString(KEY_CONTACTS, array.toString()).apply()
    }

    private fun defaultContacts(): List<Contact> {
        return listOf(
            Contact(
                id = "1",
                name = "Sarah Connor",
                customTitle = "Emergency Contact",
                phoneNumber = "+1 555-0143",
                messengerHandle = "sarah.connor.resistance",
                preferredChannel = MessageChannel.SMS
            ),
            Contact(
                id = "2",
                name = "Alex Murphy",
                customTitle = "Colleague",
                phoneNumber = "+1 555-0182",
                messengerHandle = "alex.murphy.detroit",
                preferredChannel = MessageChannel.SMS
            ),
            Contact(
                id = "3",
                name = "Mom",
                customTitle = "Family",
                phoneNumber = "+1 555-0199",
                messengerHandle = "mom_chat",
                preferredChannel = MessageChannel.SMS
            ),
            Contact(
                id = "4",
                name = "Design Team",
                customTitle = "Work Group",
                phoneNumber = "+1 555-0210",
                messengerHandle = "design_squad_chat",
                preferredChannel = MessageChannel.MESSENGER
            )
        )
    }

    companion object {
        private const val PREFS_NAME = "autocontactbridge_contacts"
        private const val LEGACY_PREFS_NAME = "storybook_car_contacts"
        private const val KEY_CONTACTS = "contacts_list"
    }
}
