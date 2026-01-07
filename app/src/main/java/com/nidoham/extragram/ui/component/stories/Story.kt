package com.nidoham.extragram.ui.component.stories

import androidx.compose.ui.graphics.Color

data class Story(
    val id: Int,
    val name: String,
    val avatarColor: Color,
    val hasUnread: Boolean = false
)