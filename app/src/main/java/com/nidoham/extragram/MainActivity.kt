package com.nidoham.extragram

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nidoham.extragram.ui.theme.ExtragramTheme
import com.nidoham.extragram.ui.theme.TelegramAvatarSize
import com.nidoham.extragram.ui.theme.TelegramBlue
import com.nidoham.extragram.ui.theme.TelegramSpacing
import com.nidoham.extragram.ui.theme.telegram
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ExtragramTheme {
                TelegramMainScreen()
            }
        }
    }
}

// Data Models
data class Story(
    val id: Int,
    val name: String,
    val avatarColor: Color,
    val hasUnread: Boolean = false
)

data class Chat(
    val id: Int,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val avatarColor: Color,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isOnline: Boolean = false,
    val isMuted: Boolean = false,
    val hasDelivered: Boolean = false,
    val hasRead: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramMainScreen() {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // Sample Data
    val stories = remember {
        listOf(
            Story(0, "My Story", TelegramBlue, false),
            Story(1, "Alice", Color(0xFFE91E63), true),
            Story(2, "Bob", Color(0xFF9C27B0), true),
            Story(3, "Charlie", Color(0xFF3F51B5), true),
            Story(4, "Diana", Color(0xFF00BCD4), false),
            Story(5, "Eve", Color(0xFF4CAF50), true),
            Story(6, "Frank", Color(0xFFFF9800), false),
            Story(7, "Grace", Color(0xFFFF5722), true)
        )
    }

    val chats = remember {
        listOf(
            Chat(1, "Saved Messages", "You: Photo", "12:45", Color(0xFF5CA7F5), 0, true, false, false, true, true),
            Chat(2, "Alice Johnson", "Hey! How are you doing?", "Yesterday", Color(0xFFE91E63), 3, true, true, false, false, false),
            Chat(3, "Tech Group", "John: Check out this new feature", "Yesterday", Color(0xFF9C27B0), 12, true, false, false, false, false),
            Chat(4, "Bob Smith", "Thanks for the help!", "Monday", Color(0xFF3F51B5), 0, false, true, false, true, true),
            Chat(5, "Work Team", "Meeting at 3 PM tomorrow", "Monday", Color(0xFF00BCD4), 5, false, false, true, false, false),
            Chat(6, "Mom", "Don't forget to call grandma", "Sunday", Color(0xFFFF9800), 1, false, false, false, false, false),
            Chat(7, "Developers Channel", "New update released! 🚀", "Sunday", Color(0xFF4CAF50), 0, false, false, false, true, false),
            Chat(8, "Charlie Brown", "See you tomorrow!", "Saturday", Color(0xFFFF5722), 0, false, true, false, true, true),
            Chat(9, "Study Group", "Assignment due next week", "Friday", Color(0xFF795548), 2, false, false, false, false, false),
            Chat(10, "Diana Prince", "Thanks for the invite!", "Friday", Color(0xFF607D8B), 0, false, false, false, true, true)
        )
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.background
            ) {
                DrawerContent()
            }
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TelegramTopBar(
                    onMenuClick = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    onSearchClick = { /* Open search */ }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* New message */ },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = "New Message"
                    )
                }
            }
        ) { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Stories Section
                item {
                    StoriesSection(stories = stories)
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = TelegramSpacing.LARGE),
                        color = MaterialTheme.colorScheme.telegram.divider.copy(alpha = 0.3f)
                    )
                }

                // Chats Section
                items(
                    items = chats,
                    key = { it.id }
                ) { chat ->
                    ChatItem(chat = chat)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TelegramTopBar(
    onMenuClick: () -> Unit,
    onSearchClick: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Extragram",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 23.sp
                )
            )
        },
        navigationIcon = {
            IconButton(onClick = onMenuClick) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        actions = {
            IconButton(onClick = onSearchClick) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primary,
            titleContentColor = MaterialTheme.colorScheme.onPrimary,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimary
        )
    )
}

@Composable
fun StoriesSection(stories: List<Story>) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = TelegramSpacing.MEDIUM),
        contentPadding = PaddingValues(horizontal = TelegramSpacing.LARGE),
        horizontalArrangement = Arrangement.spacedBy(TelegramSpacing.MEDIUM)
    ) {
        items(stories) { story ->
            StoryItem(story = story)
        }
    }
}

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

@Composable
fun ChatItem(
    chat: Chat,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { /* Open chat */ }
            .padding(
                horizontal = TelegramSpacing.LARGE,
                vertical = TelegramSpacing.MEDIUM
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier.size(TelegramAvatarSize.LARGE),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(chat.avatarColor, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = chat.name.first().toString(),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            // Online Indicator
            if (chat.isOnline) {
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.BottomEnd)
                        .background(
                            MaterialTheme.colorScheme.telegram.onlineIndicator,
                            CircleShape
                        )
                        .padding(2.dp)
                        .background(
                            MaterialTheme.colorScheme.surface,
                            CircleShape
                        )
                )
            }
        }

        Spacer(modifier = Modifier.width(TelegramSpacing.MEDIUM))

        // Chat Content
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = chat.name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (chat.unreadCount > 0) FontWeight.SemiBold else FontWeight.Normal
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )

                Spacer(modifier = Modifier.width(TelegramSpacing.SMALL))

                // Timestamp
                Text(
                    text = chat.timestamp,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (chat.unreadCount > 0) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Read/Delivered Status
                    if (chat.hasRead || chat.hasDelivered) {
                        Text(
                            text = if (chat.hasRead) "✓✓" else "✓",
                            color = if (chat.hasRead) {
                                MaterialTheme.colorScheme.telegram.readIndicator
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            },
                            fontSize = 12.sp,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                    }

                    Text(
                        text = chat.lastMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.width(TelegramSpacing.SMALL))

                // Unread Badge or Mute Icon
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (chat.isMuted) {
                        Icon(
                            imageVector = Icons.Default.Notifications,
                            contentDescription = "Muted",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (chat.unreadCount > 0) {
                        Badge(
                            containerColor = if (chat.isMuted) {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            } else {
                                MaterialTheme.colorScheme.telegram.unreadBadge
                            }
                        ) {
                            Text(
                                text = if (chat.unreadCount > 99) "99+" else chat.unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent() {
    Column(
        modifier = Modifier
            .width(300.dp)
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Header Background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primary)
                .padding(TelegramSpacing.LARGE)
                .padding(top = TelegramSpacing.XXL, bottom = TelegramSpacing.LARGE)
        ) {
            Column {
                // Profile Avatar
                Box(
                    modifier = Modifier
                        .size(TelegramAvatarSize.PROFILE)
                        .background(Color.White.copy(alpha = 0.3f), CircleShape)
                        .clip(CircleShape)
                        .clickable { /* Open profile */ },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "U",
                        color = Color.White,
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(TelegramSpacing.LARGE))

                // User Info
                Text(
                    text = "User Name",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontSize = 22.sp
                    ),
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "+1 234 567 8900",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: String,
    title: String,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = TelegramSpacing.LARGE,
                vertical = TelegramSpacing.MEDIUM
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = icon,
            fontSize = 24.sp,
            modifier = Modifier.width(40.dp)
        )

        Spacer(modifier = Modifier.width(TelegramSpacing.LARGE))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontSize = 16.sp
            ),
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}