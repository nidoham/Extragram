package com.nidoham.extragram.ui.component.stories

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nidoham.extragram.Story
import com.nidoham.extragram.ui.theme.TelegramAvatarSize

@Composable
fun StoryItem(story: Story) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(70.dp)
            .clickable { /* Open story */ }
    ) {
        Box(
            modifier = Modifier.size(TelegramAvatarSize.LARGE),
            contentAlignment = Alignment.Center
        ) {
            // Story Ring
            if (story.hasUnread) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                )
            }

            // Avatar
            Box(
                modifier = Modifier
                    .size(TelegramAvatarSize.LARGE - 4.dp)
                    .background(story.avatarColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = story.name.first().toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = story.name,
            style = MaterialTheme.typography.bodySmall,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}