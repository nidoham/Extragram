package com.nidoham.extragram.ui.component.chat

data class Chat(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isOnline: Boolean = false,
    val isMuted: Boolean = false
)