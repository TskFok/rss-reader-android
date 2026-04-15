package com.example.rssreader.feature.articles

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.compose.ui.viewinterop.AndroidView
import com.example.rssreader.core.model.Article
import androidx.core.text.HtmlCompat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticlesScreen(
    viewModel: ArticlesViewModel,
) {
    val pager = viewModel.pagingFlow.collectAsLazyPagingItems()
    val readFilter by viewModel.readFilter.collectAsStateWithLifecycle()
    var selectedArticle by remember { mutableStateOf<Article?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ArticleReadFilter.entries.forEach { filter ->
                FilterChip(
                    selected = readFilter == filter,
                    onClick = { viewModel.setReadFilter(filter) },
                    label = { Text(filter.label) },
                )
            }
        }

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
                    onClick = {
                        selectedArticle = item
                        viewModel.markAsReadOnOpen(item)
                    },
                ) {
                    Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(item.titleTranslated?.ifBlank { item.title } ?: item.title, style = MaterialTheme.typography.titleMedium)
                        Text(item.feedTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                        Text(item.publishedAt ?: item.createdAt, style = MaterialTheme.typography.labelSmall)
                        val preview = htmlToPlainText(item.contentTranslated?.ifBlank { item.content } ?: item.content)
                            .take(180)
                        Text(preview, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            if (pager.loadState.refresh is LoadState.Loading || pager.loadState.append is LoadState.Loading) {
                item {
                    Text("加载中...", modifier = Modifier.padding(16.dp))
                }
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

    val article = selectedArticle
    if (article != null) {
        ModalBottomSheet(onDismissRequest = { selectedArticle = null }) {
            ArticleDetailContent(
                article = article,
                onClose = { selectedArticle = null },
            )
        }
    }
}

@Composable
private fun ArticleDetailContent(
    article: Article,
    onClose: () -> Unit,
) {
    val context = LocalContext.current
    val title = article.titleTranslated?.ifBlank { article.title } ?: article.title
    val fullContent = article.contentTranslated?.ifBlank { article.content } ?: article.content

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge)
        Text(article.feedTitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.secondary)
        Text(article.publishedAt ?: article.createdAt, style = MaterialTheme.typography.labelSmall)

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, fill = true),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        ) {
            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .heightIn(min = 240.dp)
                    .padding(8.dp),
                factory = { ctx ->
                    android.webkit.WebView(ctx).apply {
                        overScrollMode = android.view.View.OVER_SCROLL_IF_CONTENT_SCROLLS
                        settings.javaScriptEnabled = false
                        settings.loadsImagesAutomatically = true
                        settings.builtInZoomControls = false
                        settings.displayZoomControls = false
                        settings.domStorageEnabled = false
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            settings.mixedContentMode = android.webkit.WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
                        }
                        isVerticalScrollBarEnabled = true
                        setOnTouchListener { v, event ->
                            when (event.actionMasked) {
                                android.view.MotionEvent.ACTION_DOWN,
                                android.view.MotionEvent.ACTION_MOVE -> v.parent?.requestDisallowInterceptTouchEvent(true)
                                android.view.MotionEvent.ACTION_UP,
                                android.view.MotionEvent.ACTION_CANCEL -> v.parent?.requestDisallowInterceptTouchEvent(false)
                            }
                            false
                        }
                        webViewClient = object : android.webkit.WebViewClient() {
                            override fun shouldOverrideUrlLoading(
                                view: android.webkit.WebView?,
                                request: android.webkit.WebResourceRequest?,
                            ): Boolean {
                                val url = request?.url?.toString() ?: return false
                                context.startActivity(android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(url)))
                                return true
                            }
                        }
                    }
                },
                update = { webView ->
                    val htmlDoc = """
                        <html>
                          <head>
                            <meta name="viewport" content="width=device-width, initial-scale=1" />
                            <style>
                              body { font-size: 16px; line-height: 1.6; margin: 0; padding: 0; }
                              img { max-width: 100%; height: auto; display: block; margin: 8px 0; }
                              pre, code { white-space: pre-wrap; word-break: break-word; }
                              table { width: 100%; overflow-x: auto; display: block; }
                            </style>
                          </head>
                          <body>$fullContent</body>
                        </html>
                    """.trimIndent()
                    webView.loadDataWithBaseURL(article.link, htmlDoc, "text/html", "UTF-8", null)
                },
            )
        }

        Button(
            onClick = {
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(article.link))
                context.startActivity(intent)
            },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("打开原文链接")
        }

        Button(onClick = onClose, modifier = Modifier.fillMaxWidth()) {
            Text("关闭")
        }
    }
}

private fun htmlToPlainText(input: String): String {
    return HtmlCompat.fromHtml(input, HtmlCompat.FROM_HTML_MODE_COMPACT)
        .toString()
        .replace("\n", " ")
        .trim()
}
