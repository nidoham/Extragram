package com.nidoham.extragram.ui.screen.search

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import coil.compose.AsyncImage
import com.nidoham.extra.user.UserInfo
import com.nidoham.extra.user.UserRepository
import com.nidoham.extra.user.displayName
import com.nidoham.extra.user.fullName
import com.nidoham.extra.user.isOnline
import com.nidoham.extra.user.isPremium
import com.nidoham.extragram.R
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

// ViewModel for proper architecture
@HiltViewModel
class SearchPeopleViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow<List<UserInfo>>(emptyList())
    val searchResults: StateFlow<List<UserInfo>> = _searchResults.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.trim().isNotEmpty()) {
            searchUsers(query.trim())
        } else {
            _searchResults.value = emptyList()
        }
    }

    private fun searchUsers(query: String) {
        viewModelScope.launch {
            _isSearching.value = true
            try {
                _searchResults.value = userRepository.searchUsers(query, 30L)
            } catch (e: Exception) {
                _searchResults.value = emptyList()
                // Handle error if needed
            } finally {
                _isSearching.value = false
            }
        }
    }

    fun clearSearch() {
        _searchQuery.value = ""
        _searchResults.value = emptyList()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchPeopleScreen(
    initialQuery: String = "",
    onUserClick: (String) -> Unit = {},
    viewModel: SearchPeopleViewModel = hiltViewModel()
) {
    val focusManager = LocalFocusManager.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val isSearching by viewModel.isSearching.collectAsState()

    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotEmpty() && searchQuery.isEmpty()) {
            viewModel.updateSearchQuery(initialQuery)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        SearchBar(
            query = searchQuery,
            onQueryChange = { viewModel.updateSearchQuery(it) },
            onSearch = {
                focusManager.clearFocus()
            },
            onClear = {
                viewModel.clearSearch()
                focusManager.clearFocus()
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        when {
            isSearching -> LoadingState()
            searchResults.isEmpty() -> EmptyState(searchQuery.isNotEmpty())
            else -> SearchResults(searchResults, onUserClick)
        }
    }
}

@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    onClear: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = "Search",
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(12.dp))

            OutlinedTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.weight(1f),
                placeholder = { Text("Search by username") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { onSearch() }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color.Transparent,
                    unfocusedBorderColor = Color.Transparent,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(20.dp),
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = onClear) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "Clear search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            )
        }
    }
}

@Composable
private fun LoadingState() {
    Box(modifier = Modifier.fillMaxSize()) {
        CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center),
            color = MaterialTheme.colorScheme.primary
        )
    }
}

@Composable
private fun EmptyState(isSearchPerformed: Boolean) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Search,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = if (isSearchPerformed) "No users found" else "Search for people by username",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SearchResults(
    searchResults: List<UserInfo>,
    onUserClick: (String) -> Unit
) {
    LazyColumn {
        items(searchResults) { user ->
            UserSearchItem(
                user = user,
                onClick = { onUserClick(user.id) },
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant,
                thickness = 0.5.dp
            )
        }
    }
}

@Composable
fun UserSearchItem(
    user: UserInfo,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar
            AsyncImage(
                model = user.avatar ?: R.drawable.ic_launcher_background,
                contentDescription = "User avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape),
                error = painterResource(id = R.drawable.ic_launcher_background)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = user.username.ifEmpty { user.displayName },
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    // Badges next to username
                    UserBadges(user)
                }

                Text(
                    text = user.fullName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                UserStatusIndicator(user)
            }
        }
    }
}

@Composable
private fun UserStatusIndicator(user: UserInfo) {
    val statusColor = when {
        user.isOnline -> Color(0xFF4CAF50) // Green
        user.lastActive != null &&
                (System.currentTimeMillis() - user.lastActive!!) < 5 * 60 * 1000 -> Color(0xFFFF9800) // Orange
        else -> Color(0xFF9E9E9E) // Gray
    }

    val statusText = when {
        user.isOnline -> "Online"
        user.lastActive != null -> "Recently active"
        else -> "Offline"
    }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(statusColor)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = statusText,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UserBadges(user: UserInfo) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (user.verified) {
            Icon(
                imageVector = Icons.Default.Verified,
                contentDescription = "Verified",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFF2196F3)
            )
        }

        if (user.isPremium) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Premium",
                modifier = Modifier.size(16.dp),
                tint = Color(0xFFFFD700)
            )
        }
    }
}