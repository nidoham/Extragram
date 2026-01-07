package com.nidoham.extragram.ui.component.chat

import androidx.compose.ui.graphics.Color

data class Chat(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val avatarColor: Color,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isOnline: Boolean = false,
    val isMuted: Boolean = false
)