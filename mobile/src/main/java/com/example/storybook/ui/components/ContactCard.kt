package com.example.storybook.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.storybook.shared.data.Contact
import com.example.storybook.shared.data.MessageChannel

@Composable
fun ContactCard(
    contact: Contact,
    index: Int,
    totalCount: Int,
    isTopPriority: Boolean,
    isDragged: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDragHandle: Modifier,
    onToggleChannel: () -> Unit,
    onToggleEnabled: () -> Unit,
    onEdit: () -> Unit,
    onCall: () -> Unit,
    onMessage: () -> Unit,
    onDelete: () -> Unit
) {
    val elevation by animateFloatAsState(targetValue = if (isDragged) 8f else 2f, label = "elevation")
    val scale by animateFloatAsState(targetValue = if (isDragged) 1.02f else 1f, label = "scale")
    val cardAlpha = if (contact.isEnabled) 1.0f else 0.55f

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .alpha(cardAlpha),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (!contact.isEnabled) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            } else if (isTopPriority) {
                MaterialTheme.colorScheme.surface
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            }
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Drag handle icon with tactile touch gestures
                Box(
                    modifier = onDragHandle
                        .padding(end = 8.dp)
                        .padding(vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "⣿",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Avatar
                ContactAvatar(
                    name = contact.name,
                    avatarUriString = contact.avatarUri,
                    size = 46.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                // Name and Custom Title Line
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = contact.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (contact.isEnabled && isTopPriority) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "#${index + 1} Car Priority",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        } else if (!contact.isEnabled) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = "Disabled",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.outline,
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }

                    val subtitleText = if (contact.customTitle.isNotBlank()) {
                        contact.customTitle
                    } else {
                        "Contact"
                    }
                    Text(
                        text = subtitleText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Eye icon for enable/disable toggle
                IconButton(
                    onClick = onToggleEnabled,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (contact.isEnabled) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (contact.isEnabled) "Disable contact in car" else "Enable contact in car",
                        tint = if (contact.isEnabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                    )
                }

                Spacer(modifier = Modifier.width(4.dp))

                // Up / Down arrow affordances for reordering
                Column {
                    IconButton(
                        onClick = onMoveUp,
                        enabled = index > 0,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("▲", style = MaterialTheme.typography.labelSmall)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    IconButton(
                        onClick = onMoveDown,
                        enabled = index < totalCount - 1,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Text("▼", style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Action row: Preferred mode toggle + Call + Message + Edit + Delete with Icons
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FilterChip(
                    selected = true,
                    onClick = onToggleChannel,
                    leadingIcon = {
                        Icon(
                            imageVector = if (contact.preferredChannel == MessageChannel.SMS) Icons.Default.Sms else Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = {
                        Text(contact.preferredChannel.shortName)
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = if (contact.preferredChannel == MessageChannel.SMS) {
                            Color(0xFF2E7D32).copy(alpha = 0.2f)
                        } else {
                            Color(0xFF1976D2).copy(alpha = 0.2f)
                        },
                        selectedLabelColor = if (contact.preferredChannel == MessageChannel.SMS) {
                            Color(0xFF1B5E20)
                        } else {
                            Color(0xFF0D47A1)
                        }
                    )
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = onCall,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Call",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    FilledIconButton(
                        onClick = onMessage,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Message",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun ContactCardPreview() {
    MaterialTheme {
        ContactCard(
            contact = Contact(
                id = "1",
                name = "Jane Doe",
                customTitle = "Spouse",
                phoneNumber = "555-0192",
                preferredChannel = MessageChannel.SMS,
                isEnabled = true
            ),
            index = 0,
            totalCount = 4,
            isTopPriority = true,
            isDragged = false,
            onMoveUp = {},
            onMoveDown = {},
            onDragHandle = Modifier,
            onToggleChannel = {},
            onToggleEnabled = {},
            onEdit = {},
            onCall = {},
            onMessage = {},
            onDelete = {}
        )
    }
}
