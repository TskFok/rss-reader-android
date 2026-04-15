package com.example.rssreader.feature.feeds

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.rssreader.core.model.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedsScreen(
    viewModel: FeedsViewModel,
    onFeedClick: (Long, String) -> Unit,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val feeds = (state as? UiState.Success)?.data.orEmpty()

    when (state) {
        is UiState.Error -> {
            val message = (state as UiState.Error).message
            Column(modifier = Modifier.padding(16.dp)) {
                Text(message, color = MaterialTheme.colorScheme.error)
            }
        }
        else -> {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                if (state is UiState.Loading) {
                    item {
                        Text("加载中...", modifier = Modifier.padding(16.dp))
                    }
                }
                items(feeds, key = { it.id }) { feed ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp)
                            .clickable { onFeedClick(feed.id, feed.title) },
                    ) {
                        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(feed.title, style = MaterialTheme.typography.titleMedium)
                            Text(feed.url, style = MaterialTheme.typography.bodySmall)
                            Text(
                                text = "最近抓取: ${feed.lastFetchedAt ?: "-"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.secondary,
                            )
                        }
                    }
                }
            }
        }
    }
}
