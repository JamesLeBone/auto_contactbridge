package io.github.jameslebone.autocontactbridge.shared.screens

import android.telephony.SmsManager
import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.CarColor
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import io.github.jameslebone.autocontactbridge.shared.data.Contact
import io.github.jameslebone.autocontactbridge.shared.data.MessageChannel
import io.github.jameslebone.autocontactbridge.shared.tts.CarTtsManager

class ConfirmSendScreen(
    carContext: CarContext,
    private val contact: Contact,
    private val message: String,
    private val ttsManager: CarTtsManager?
) : Screen(carContext) {

    init {
        val prompt = "Ready to send to ${contact.name}: \"$message\". Tap send to confirm."
        ttsManager?.speak(prompt)
    }

    override fun onGetTemplate(): Template {
        val details = buildString {
            append("\"").append(message).append("\"\n\n")
            append("Recipient: ").append(contact.name).append("\n")
            if (contact.customTitle.isNotBlank()) {
                append("Relationship: ").append(contact.customTitle).append("\n")
            }
            append("Channel: ").append(contact.channel.displayName).append("\n")
            append("Destination: ").append(contact.address)
        }

        val sendAction = Action.Builder()
            .setTitle("Send")
            .setBackgroundColor(CarColor.GREEN)
            .setOnClickListener {
                dispatchMessage()
                screenManager.push(MessageSentScreen(carContext, contact, ttsManager))
            }
            .build()

        val cancelAction = Action.Builder()
            .setTitle("Re-dictate")
            .setOnClickListener {
                ttsManager?.stop()
                screenManager.pop()
            }
            .build()

        return MessageTemplate.Builder(details)
            .setTitle("Confirm Message")
            .setHeaderAction(Action.BACK)
            .addAction(sendAction)
            .addAction(cancelAction)
            .build()
    }

    private fun dispatchMessage() {
        ttsManager?.stop()
        if (contact.channel == MessageChannel.SMS) {
            try {
                val smsManager: SmsManager = carContext.getSystemService(SmsManager::class.java)
                smsManager.sendTextMessage(contact.address, null, message, null, null)
            } catch (e: Exception) {
                // If SMS permission is not yet granted or in simulation, proceed gracefully
            }
        }
    }
}
