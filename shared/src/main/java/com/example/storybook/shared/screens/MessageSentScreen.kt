package com.example.storybook.shared.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.MessageTemplate
import androidx.car.app.model.Template
import com.example.storybook.shared.data.Contact
import com.example.storybook.shared.tts.CarTtsManager

class MessageSentScreen(
    carContext: CarContext,
    private val contact: Contact,
    private val ttsManager: CarTtsManager?
) : Screen(carContext) {

    init {
        ttsManager?.speak("Message sent to ${contact.name}.")
    }

    override fun onGetTemplate(): Template {
        return MessageTemplate.Builder("Your message was dispatched to ${contact.name} via ${contact.channel.displayName}.")
            .setTitle("Message Sent!")
            .setHeaderAction(Action.BACK)
            .addAction(
                Action.Builder()
                    .setTitle("Done")
                    .setOnClickListener {
                        // Pop back to the contact list root
                        screenManager.popToRoot()
                    }
                    .build()
            )
            .build()
    }
}
