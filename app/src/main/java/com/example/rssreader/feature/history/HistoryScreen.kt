package com.example.rssreader.feature.history

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems

@Composable
fun HistoryScreen(viewModel: HistoryViewModel) {
    val pager = viewModel.pagingFlow.collectAsLazyPagingItems()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(count = pager.itemCount, key = { index -> pager[index]?.id ?: index }) { index ->
            val item = pager[index] ?: return@items
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(item.aiModelName, style = MaterialTheme.typography.titleMedium)
                    Text("时间范围: ${item.startTime} ~ ${item.endTime}", style = MaterialTheme.typography.labelSmall)
                    Text("文章数: ${item.articleCount}", style = MaterialTheme.typography.labelSmall)
                    if (!item.error.isNullOrBlank()) {
                        Text("错误: ${item.error}", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                    } else {
                        Text(item.content.orEmpty(), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        if (pager.loadState.refresh is LoadState.Loading || pager.loadState.append is LoadState.Loading) {
            item { Text("加载中...", modifier = Modifier.padding(16.dp)) }
        }

        if (pager.loadState.refresh is LoadState.Error || pager.loadState.append is LoadState.Error) {
            val err = (pager.loadState.refresh as? LoadState.Error)?.error
                ?: (pager.loadState.append as? LoadState.Error)?.error
            item {
                Text(
                    text = "加载失败: ${err?.message ?: "未知错误"}",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp),
                )
            }
        }
    }
}
