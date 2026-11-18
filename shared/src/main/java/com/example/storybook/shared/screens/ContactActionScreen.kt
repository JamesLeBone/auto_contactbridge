package com.example.storybook.shared.screens

import android.content.Intent
import android.net.Uri
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import com.example.storybook.shared.data.Contact
import com.example.storybook.shared.data.ContactRepository
import com.example.storybook.shared.data.MessageChannel
import com.example.storybook.shared.tts.CarTtsManager

class ContactActionScreen(
    carContext: CarContext,
    private var contact: Contact,
    private val ttsManager: CarTtsManager?
) : Screen(carContext) {

    private val repository = ContactRepository(carContext)

    init {
        ttsManager?.speak("What would you like to do with ${contact.name}? Call or send a message?")
    }

    override fun onGetTemplate(): Template {
        // Refresh contact in case channel changed
        val current = repository.getContacts().find { it.id == contact.id } ?: contact
        contact = current

        val listBuilder = ItemList.Builder()

        // 1. Option to Make a Call
        val callSubtitle = if (contact.phoneNumber.isNotBlank()) "Hands-free voice call" else "No phone number available"
        listBuilder.addItem(
            Row.Builder()
                .setTitle("Call ${contact.name}")
                .addText(callSubtitle)
                .setEnabled(contact.phoneNumber.isNotBlank())
                .setOnClickListener {
                    ttsManager?.stop()
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${contact.phoneNumber}")
                    }
                    try {
                        carContext.startCarApp(dialIntent)
                    } catch (e: Exception) {
                        ttsManager?.speak("Unable to open car dialer.")
                    }
                }
                .build()
        )

        // 2. Option to Send a Message
        val messageSubtitle = "Via ${contact.preferredChannel.displayName}"
        listBuilder.addItem(
            Row.Builder()
                .setTitle("Send Message")
                .addText(messageSubtitle)
                .setOnClickListener {
                    ttsManager?.stop()
                    screenManager.push(VoiceComposeScreen(carContext, contact, ttsManager))
                }
                .build()
        )

        // 3. Option to Switch Channel (e.g. when parked)
        val alternateChannel = if (contact.preferredChannel == MessageChannel.SMS) {
            MessageChannel.MESSENGER
        } else {
            MessageChannel.SMS
        }
        listBuilder.addItem(
            Row.Builder()
                .setTitle("Switch to ${alternateChannel.displayName}")
                .addText("Current active: ${contact.preferredChannel.displayName}")
                .setOnClickListener {
                    repository.togglePreferredChannel(contact.id)
                    ttsManager?.speak("Switched preferred mode to ${alternateChannel.shortName}.")
                    invalidate()
                }
                .build()
        )

        val headerTitle = if (contact.customTitle.isNotBlank()) {
            "${contact.name} (${contact.customTitle})"
        } else {
            contact.name
        }

        return ListTemplate.Builder()
            .setTitle(headerTitle)
            .setHeaderAction(Action.BACK)
            .setSingleList(listBuilder.build())
            .build()
    }
}
