package com.example.storybook.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storybook.shared.data.Contact
import com.example.storybook.shared.data.ContactRepository
import com.example.storybook.shared.data.MessageChannel
import com.example.storybook.ui.components.ContactCard
import com.example.storybook.ui.dialogs.ContactEditDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarContactsManagerScreen(repository: ContactRepository) {
    val context = LocalContext.current
    var contacts by remember { mutableStateOf(repository.getContacts()) }
    var editingContact by remember { mutableStateOf<Contact?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Car Contacts & Priority") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            // Driving safety info card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🚗 In-Car Sync & Priority",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Drag or use arrows to reorder contacts. The top 6 are prioritized on your car dashboard while driving. Tap channel badges to toggle between SMS and Messenger.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action button
            Button(
                onClick = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("+ Add Contact")
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Car Contacts (${contacts.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            val density = LocalDensity.current
            val dragThresholdPx = with(density) { 60.dp.toPx() }
            var activeDragIndex by remember { mutableIntStateOf(-1) }
            var accumulatedDrag by remember { mutableFloatStateOf(0f) }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(contacts, key = { _, contact -> contact.id }) { index, contact ->
                    val isBeingDragged = activeDragIndex == index
                    val isTopPriority = index < 6

                    ContactCard(
                        contact = contact,
                        index = index,
                        totalCount = contacts.size,
                        isTopPriority = isTopPriority,
                        isDragged = isBeingDragged,
                        onMoveUp = {
                            if (index > 0) {
                                contacts = repository.moveContact(index, index - 1)
                            }
                        },
                        onMoveDown = {
                            if (index < contacts.size - 1) {
                                contacts = repository.moveContact(index, index + 1)
                            }
                        },
                        onDragHandle = Modifier.pointerInput(contact.id, index) {
                            detectVerticalDragGestures(
                                onDragStart = {
                                    activeDragIndex = index
                                    accumulatedDrag = 0f
                                },
                                onDragEnd = {
                                    activeDragIndex = -1
                                    accumulatedDrag = 0f
                                },
                                onDragCancel = {
                                    activeDragIndex = -1
                                    accumulatedDrag = 0f
                                },
                                onVerticalDrag = { change, dragAmount ->
                                    change.consume()
                                    accumulatedDrag += dragAmount
                                    if (accumulatedDrag > dragThresholdPx && index < contacts.size - 1) {
                                        contacts = repository.moveContact(index, index + 1)
                                        activeDragIndex = index + 1
                                        accumulatedDrag = 0f
                                    } else if (accumulatedDrag < -dragThresholdPx && index > 0) {
                                        contacts = repository.moveContact(index, index - 1)
                                        activeDragIndex = index - 1
                                        accumulatedDrag = 0f
                                    }
                                }
                            )
                        },
                        onToggleChannel = {
                            repository.togglePreferredChannel(contact.id)
                            contacts = repository.getContacts()
                        },
                        onToggleEnabled = {
                            repository.toggleContactEnabled(contact.id)
                            contacts = repository.getContacts()
                        },
                        onEdit = {
                            editingContact = contact
                        },
                        onCall = {
                            if (contact.phoneNumber.isNotBlank()) {
                                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${contact.phoneNumber}")
                                }
                                context.startActivity(dialIntent)
                            } else {
                                Toast.makeText(context, "No phone number configured", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onMessage = {
                            if (contact.preferredChannel == MessageChannel.SMS) {
                                if (contact.phoneNumber.isNotBlank()) {
                                    val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                                        data = Uri.parse("smsto:${contact.phoneNumber}")
                                    }
                                    context.startActivity(smsIntent)
                                } else {
                                    Toast.makeText(context, "No phone number for SMS", Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                if (contact.messengerHandle.isNotBlank()) {
                                    try {
                                        val messengerIntent = Intent(Intent.ACTION_VIEW).apply {
                                            data = Uri.parse("fb-messenger://user-thread/${contact.messengerHandle}")
                                        }
                                        context.startActivity(messengerIntent)
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Facebook Messenger not installed", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(context, "No Messenger handle configured", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        onDelete = {
                            repository.deleteContact(contact.id)
                            contacts = repository.getContacts()
                        }
                    )
                }
            }
        }
    }

    if (showAddDialog) {
        ContactEditDialog(
            contact = null,
            onDismiss = { showAddDialog = false },
            onSave = { savedContact ->
                repository.saveContact(savedContact)
                contacts = repository.getContacts()
                showAddDialog = false
            }
        )
    }

    editingContact?.let { contactToEdit ->
        ContactEditDialog(
            contact = contactToEdit,
            onDismiss = { editingContact = null },
            onSave = { savedContact ->
                repository.saveContact(savedContact)
                contacts = repository.getContacts()
                editingContact = null
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun CarContactsManagerScreenPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Car Contacts Manager Screen", style = MaterialTheme.typography.titleLarge)
            }
        }
    }
}
