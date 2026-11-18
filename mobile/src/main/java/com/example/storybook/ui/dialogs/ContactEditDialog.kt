package com.example.storybook.ui.dialogs

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storybook.shared.data.Contact
import com.example.storybook.shared.data.MessageChannel
import com.example.storybook.ui.components.ContactAvatar

@Composable
fun ContactEditDialog(
    contact: Contact?,
    onDismiss: () -> Unit = {},
    onSave: (Contact) -> Unit
) {
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var customTitle by remember { mutableStateOf(contact?.customTitle ?: "") }
    var phoneNumber by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var messengerHandle by remember { mutableStateOf(contact?.messengerHandle ?: "") }
    var preferredChannel by remember { mutableStateOf(contact?.preferredChannel ?: MessageChannel.SMS) }
    var avatarUri by remember { mutableStateOf(contact?.avatarUri) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUri = uri.toString()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (contact == null) "Add Car Contact" else "Edit Contact") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Avatar Picker Section
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clickable {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                    ) {
                        ContactAvatar(
                            name = name.ifBlank { "?" },
                            avatarUriString = avatarUri,
                            size = 64.dp
                        )
                    }
                    Column {
                        Text(
                            text = "Photo",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        TextButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    androidx.activity.result.PickVisualMediaRequest(
                                        ActivityResultContracts.PickVisualMedia.ImageOnly
                                    )
                                )
                            }
                        ) {
                            Text("Change Photo")
                        }
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Contact Name *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = customTitle,
                    onValueChange = { customTitle = it },
                    label = { Text("Custom Title / Relationship (e.g. Spouse)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = { phoneNumber = it },
                    label = { Text("Phone Number (for SMS & Calls)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = messengerHandle,
                    onValueChange = { messengerHandle = it },
                    label = { Text("Messenger Handle / ID") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "Preferred In-Car Channel",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = preferredChannel == MessageChannel.SMS,
                        onClick = { preferredChannel = MessageChannel.SMS },
                        label = { Text("SMS / Phone") }
                    )
                    FilterChip(
                        selected = preferredChannel == MessageChannel.MESSENGER,
                        onClick = { preferredChannel = MessageChannel.MESSENGER },
                        label = { Text("Messenger") }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val saved = (contact ?: Contact(id = java.util.UUID.randomUUID().toString(), name = "")).copy(
                            name = name.trim(),
                            customTitle = customTitle.trim(),
                            phoneNumber = phoneNumber.trim(),
                            messengerHandle = messengerHandle.trim(),
                            preferredChannel = preferredChannel,
                            avatarUri = avatarUri
                        )
                        onSave(saved)
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun ContactEditDialogPreview() {
    MaterialTheme {
        ContactEditDialog(
            contact = Contact(
                id = "1",
                name = "Jane Doe",
                customTitle = "Spouse",
                phoneNumber = "555-0192"
            ),
            onSave = {}
        )
    }
}
