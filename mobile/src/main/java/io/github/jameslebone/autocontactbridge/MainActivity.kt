package io.github.jameslebone.autocontactbridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.jameslebone.autocontactbridge.shared.data.ContactRepository
import io.github.jameslebone.autocontactbridge.ui.screens.CarContactsManagerScreen
import io.github.jameslebone.autocontactbridge.ui.theme.StorybookTheme

class MainActivity : ComponentActivity() {
    private lateinit var repository: ContactRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        repository = ContactRepository(this)
        setContent {
            StorybookTheme {
                CarContactsManagerScreen(repository = repository)
            }
        }
    }
}
