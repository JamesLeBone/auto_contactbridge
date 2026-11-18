package com.example.storybook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.storybook.shared.data.ContactRepository
import com.example.storybook.ui.screens.CarContactsManagerScreen
import com.example.storybook.ui.theme.StorybookTheme

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
