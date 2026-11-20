package io.github.jameslebone.autocontactbridge.shared.data

enum class MessageChannel(val displayName: String, val shortName: String) {
    SMS("SMS / Messages", "SMS"),
    MESSENGER("Facebook Messenger", "Messenger")
}

data class Contact(
    val id: String,
    val name: String,
    val customTitle: String = "",
    val phoneNumber: String = "",
    val messengerHandle: String = "",
    val preferredChannel: MessageChannel = MessageChannel.SMS,
    val avatarUri: String? = null,
    val isEnabled: Boolean = true
) {
    val address: String
        get() = if (preferredChannel == MessageChannel.SMS) phoneNumber else messengerHandle

    val channel: MessageChannel
        get() = preferredChannel
}
