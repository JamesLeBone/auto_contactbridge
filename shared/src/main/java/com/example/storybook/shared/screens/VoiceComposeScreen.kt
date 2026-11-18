package com.example.storybook.shared.screens

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ItemList
import androidx.car.app.model.Row
import androidx.car.app.model.SearchTemplate
import androidx.car.app.model.Template
import com.example.storybook.shared.data.Contact
import com.example.storybook.shared.tts.CarTtsManager

class VoiceComposeScreen(
    carContext: CarContext,
    private val contact: Contact,
    private val ttsManager: CarTtsManager?
) : Screen(carContext) {

    private val quickPresets = listOf(
        "I'm on my way",
        "Running 5 minutes late",
        "Call you when I arrive",
        "Can't talk right now, driving",
        "Almost there"
    )

    init {
        ttsManager?.speak("What message would you like to send to ${contact.name}?")
    }

    override fun onGetTemplate(): Template {
        val listBuilder = ItemList.Builder()
        for (preset in quickPresets) {
            listBuilder.addItem(
                Row.Builder()
                    .setTitle(preset)
                    .setOnClickListener {
                        proceedToConfirm(preset)
                    }
                    .build()
            )
        }

        val searchCallback = object : SearchTemplate.SearchCallback {
            override fun onSearchTextChanged(searchText: String) {}

            override fun onSearchSubmitted(searchText: String) {
                if (searchText.isNotBlank()) {
                    proceedToConfirm(searchText.trim())
                }
            }
        }

        return SearchTemplate.Builder(searchCallback)
            .setHeaderAction(Action.BACK)
            .setSearchHint("Speak message for ${contact.name}...")
            .setShowKeyboardByDefault(false)
            .setItemList(listBuilder.build())
            .build()
    }

    private fun proceedToConfirm(messageText: String) {
        ttsManager?.stop()
        screenManager.push(ConfirmSendScreen(carContext, contact, messageText, ttsManager))
    }
}
