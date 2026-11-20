package io.github.jameslebone.autocontactbridge.ui.dialogs

import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.jameslebone.autocontactbridge.shared.data.Contact
import io.github.jameslebone.autocontactbridge.shared.data.MessageChannel
import io.github.jameslebone.autocontactbridge.ui.components.ContactAvatar
import com.facebook.AccessToken
import com.facebook.GraphRequest
import com.facebook.login.LoginManager

data class FacebookFriend(
    val id: String,
    val name: String,
    val photoUrl: String? = null
)

private fun fetchFacebookFriendsFromGraphApi(
    onResult: (List<FacebookFriend>, String?) -> Unit
) {
    val accessToken = AccessToken.getCurrentAccessToken()
    if (accessToken == null || accessToken.isExpired) {
        onResult(emptyList(), "Not logged in to Facebook.")
        return
    }

    val request = GraphRequest.newGraphPathRequest(
        accessToken,
        "/me/friends"
    ) { response ->
        val jsonObject = response.jsonObject
        val error = response.error
        if (error != null) {
            onResult(emptyList(), error.errorMessage ?: "Failed to fetch Facebook friends.")
            return@newGraphPathRequest
        }
        if (jsonObject != null) {
            val list = mutableListOf<FacebookFriend>()
            val dataArray = jsonObject.optJSONArray("data")
            if (dataArray != null) {
                for (i in 0 until dataArray.length()) {
                    val item = dataArray.getJSONObject(i)
                    val id = item.optString("id")
                    val name = item.optString("name")
                    val pictureObj = item.optJSONObject("picture")?.optJSONObject("data")
                    val photoUrl = pictureObj?.optString("url")
                    if (id.isNotBlank() && name.isNotBlank()) {
                        list.add(FacebookFriend(id, name, photoUrl))
                    }
                }
            }
            onResult(list, null)
        } else {
            onResult(emptyList(), "Empty response from Facebook.")
        }
    }

    val parameters = android.os.Bundle().apply {
        putString("fields", "id,name,picture.type(large)")
    }
    request.parameters = parameters
    request.executeAsync()
}

@Composable
fun ContactEditDialog(
    contact: Contact?,
    onDismiss: () -> Unit = {},
    onSave: (Contact) -> Unit
) {
    val context = LocalContext.current
    var name by remember { mutableStateOf(contact?.name ?: "") }
    var customTitle by remember { mutableStateOf(contact?.customTitle ?: "") }
    var phoneNumber by remember { mutableStateOf(contact?.phoneNumber ?: "") }
    var messengerHandle by remember { mutableStateOf(contact?.messengerHandle ?: "") }
    var preferredChannel by remember { mutableStateOf(contact?.preferredChannel ?: MessageChannel.SMS) }
    var avatarUri by remember { mutableStateOf(contact?.avatarUri) }
    var showFacebookDialog by remember { mutableStateOf(false) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            avatarUri = uri.toString()
        }
    }

    val contactPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val dataUri = result.data?.data
        if (dataUri != null) {
            try {
                val cursor = context.contentResolver.query(
                    dataUri,
                    arrayOf(
                        ContactsContract.CommonDataKinds.Phone.NUMBER,
                        ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
                    ),
                    null, null, null
                )
                cursor?.use { c ->
                    if (c.moveToFirst()) {
                        val numIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                        val nameIndex = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                        if (numIndex != -1) {
                            val fetchedPhone = c.getString(numIndex)
                            if (!fetchedPhone.isNullOrBlank()) {
                                phoneNumber = fetchedPhone
                            }
                        }
                        if (nameIndex != -1 && name.isBlank()) {
                            val fetchedName = c.getString(nameIndex)
                            if (!fetchedName.isNullOrBlank()) {
                                name = fetchedName
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
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
                    trailingIcon = {
                        IconButton(
                            onClick = {
                                val pickIntent = Intent(
                                    Intent.ACTION_PICK,
                                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI
                                )
                                contactPickerLauncher.launch(pickIntent)
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Contacts,
                                contentDescription = "Select from Contacts App",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = messengerHandle,
                    onValueChange = { messengerHandle = it },
                    label = { Text("Messenger Handle / Facebook ID") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(
                            onClick = { showFacebookDialog = true }
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonSearch,
                                contentDescription = "Fetch Facebook Friends Graph API",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    supportingText = {
                        Text("Tap search icon to log in with Facebook & select friends.")
                    },
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

    if (showFacebookDialog) {
        var facebookFriends by remember { mutableStateOf<List<FacebookFriend>>(emptyList()) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        var isLoading by remember { mutableStateOf(false) }
        val currentToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = currentToken != null && !currentToken.isExpired

        val loadFriends = {
            isLoading = true
            errorMessage = null
            fetchFacebookFriendsFromGraphApi { friends, err ->
                isLoading = false
                facebookFriends = friends
                errorMessage = err
            }
        }

        LaunchedEffect(isLoggedIn) {
            if (isLoggedIn) {
                loadFriends()
            }
        }

        AlertDialog(
            onDismissRequest = { showFacebookDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccountBox,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Text("Facebook Friends (App ID 1663266431863147)")
                }
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (!isLoggedIn) {
                        Text(
                            text = "Log in with Facebook to search and fetch your friends list via Meta Graph API.",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Button(
                            onClick = {
                                val activity = context as? ComponentActivity
                                if (activity != null) {
                                    LoginManager.getInstance().logInWithReadPermissions(
                                        activity,
                                        listOf("public_profile", "user_friends")
                                    )
                                } else {
                                    Toast.makeText(context, "Cannot start Facebook login", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Log In with Facebook")
                        }
                    } else {
                        Text(
                            text = "Connected with Facebook App ID: 1663266431863147",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )

                        if (isLoading) {
                            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                        }

                        errorMessage?.let { err ->
                            Text(
                                text = err,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }

                        if (facebookFriends.isNotEmpty()) {
                            LazyColumn(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(max = 240.dp),
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                items(facebookFriends) { friend ->
                                    Surface(
                                        onClick = {
                                            messengerHandle = friend.id
                                            if (name.isBlank()) name = friend.name
                                            if (avatarUri == null && friend.photoUrl != null) avatarUri = friend.photoUrl
                                            showFacebookDialog = false
                                        },
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(10.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            ContactAvatar(name = friend.name, avatarUriString = friend.photoUrl, size = 36.dp)
                                            Spacer(modifier = Modifier.width(10.dp))
                                            Column {
                                                Text(text = friend.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                                Text(text = "ID: ${friend.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                                            }
                                        }
                                    }
                                }
                            }
                        } else if (!isLoading && errorMessage == null) {
                            Text(
                                text = "No friends returned by Meta Graph API. Note: Meta restricts /me/friends to return only friends who have also installed this app.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                            Button(
                                onClick = { loadFriends() },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Refresh Friends List")
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                LoginManager.getInstance().logOut()
                                facebookFriends = emptyList()
                                errorMessage = null
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Log Out of Facebook")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = { showFacebookDialog = false }) {
                    Text("Close")
                }
            }
        )
    }
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
