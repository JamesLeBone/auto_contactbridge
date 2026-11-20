package io.github.jameslebone.autocontactbridge.shared

import androidx.car.app.CarContext
import androidx.car.app.Screen
import androidx.car.app.model.Action
import androidx.car.app.model.ActionStrip
import androidx.car.app.model.ItemList
import androidx.car.app.model.ListTemplate
import androidx.car.app.model.Row
import androidx.car.app.model.Template
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.github.jameslebone.autocontactbridge.shared.data.ContactRepository
import io.github.jameslebone.autocontactbridge.shared.screens.ContactActionScreen
import io.github.jameslebone.autocontactbridge.shared.tts.CarTtsManager

class MyCarAppScreen(carContext: CarContext) : Screen(carContext) {

    private val repository = ContactRepository(carContext)
    private var ttsManager: CarTtsManager? = null

    init {
        ttsManager = CarTtsManager(carContext)
        lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onDestroy(owner: LifecycleOwner) {
                ttsManager?.shutdown()
                ttsManager = null
            }
        })
    }

    override fun onGetTemplate(): Template {
        val contacts = repository.getContacts().filter { it.isEnabled }
        val listBuilder = ItemList.Builder()
            .setNoItemsMessage("No contacts configured. Add contacts in the phone app first.")

        // Enforce the standard 6-item limit for driving safety on Car App Library
        val displayContacts = contacts.take(MAX_DRIVING_ITEMS)
        for (contact in displayContacts) {
            val subtitle = if (contact.customTitle.isNotBlank()) {
                "${contact.customTitle} • [${contact.preferredChannel.shortName}]"
            } else {
                "Preferred: ${contact.preferredChannel.shortName}"
            }

            listBuilder.addItem(
                Row.Builder()
                    .setTitle(contact.name)
                    .addText(subtitle)
                    .setOnClickListener {
                        screenManager.push(ContactActionScreen(carContext, contact, ttsManager))
                    }
                    .build()
            )
        }

        val actionStrip = ActionStrip.Builder()
            .addAction(
                Action.Builder()
                    .setTitle("Refresh")
                    .setOnClickListener {
                        invalidate()
                    }
                    .build()
            )
            .build()

        return ListTemplate.Builder()
            .setTitle("Car Contacts")
            .setHeaderAction(Action.APP_ICON)
            .setActionStrip(actionStrip)
            .setSingleList(listBuilder.build())
            .build()
    }

    companion object {
        private const val MAX_DRIVING_ITEMS = 6
    }
}
