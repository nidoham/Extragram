package com.nidoham.extragram

import android.content.Context
import android.content.ContextWrapper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.nidoham.extragram.ui.theme.ExtragramTheme
import com.nidoham.extragram.ui.screen.SearchScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SearchActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            ExtragramTheme {
                val context = LocalContext.current
                var searchQuery by remember { mutableStateOf("") }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    Scaffold { paddingValues ->
                        SearchScreen(
                            query = searchQuery,
                            onBackPressed = {
                                context.findActivity()?.finish()
                            },
                            onQueryChange = { newQuery ->
                                searchQuery = newQuery
                            },
                            onSearch = {
                                // Handle search action
                            }
                        )
                    }
                }
            }
        }
    }
}

// Extension function to find activity from context
fun Context.findActivity(): ComponentActivity? = when (this) {
    is ComponentActivity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}