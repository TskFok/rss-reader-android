package com.example.rssreader

import androidx.paging.PagingSource
import com.example.rssreader.core.model.Article
import com.example.rssreader.core.model.PagedResponse
import com.example.rssreader.core.network.ArticlesApi
import com.example.rssreader.core.network.ArticlesPagingSource
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ArticlesPagingSourceTest {
    @Test
    fun loadsFirstPageSuccessfully() = runTest {
        val api = object : ArticlesApi {
            override suspend fun getArticles(
                feedId: Long,
                read: Boolean?,
                page: Int,
                pageSize: Int,
            ): PagedResponse<Article> {
                return PagedResponse(
                    items = listOf(
                        Article(
                            id = 1,
                            feedId = feedId,
                            guid = "g1",
                            title = "title",
                            link = "https://a.com",
                            content = "content",
                            aiCategory = null,
                            titleTranslated = null,
                            contentTranslated = null,
                            publishedAt = null,
                            createdAt = "2026-01-01T00:00:00Z",
                            read = false,
                            favorite = false,
                            feedTitle = "feed",
                        ),
                    ),
                    total = 1,
                )
            }
        }

        val source = ArticlesPagingSource(api = api, feedId = 10, read = null)
        val result = source.load(PagingSource.LoadParams.Refresh(key = null, loadSize = 20, placeholdersEnabled = false))

        val page = result as PagingSource.LoadResult.Page
        assertEquals(1, page.data.size)
        assertEquals(2, page.nextKey)
        assertEquals(10, page.data.first().feedId)
    }
}
