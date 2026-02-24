package com.rslab.arthaguardai.news

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.rslab.arthaguardai.network.RetrofitInstance
import com.rslab.arthaguardai.ui.components.HomeBackgroundBrush
import com.rslab.arthaguardai.ui.components.HomeStyleTopBar
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant


@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsSentimentScreen(
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val uriHandler = LocalUriHandler.current
    val listState = rememberLazyListState()

    var queryInput by remember { mutableStateOf("") }
    var activeQuery by remember { mutableStateOf("") }
    var latestToday by remember { mutableStateOf<List<NewsArticle>>(emptyList()) }
    var olderNews by remember { mutableStateOf<List<NewsArticle>>(emptyList()) }
    var currentPage by remember { mutableStateOf(1) }
    var totalResults by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var canLoadMore by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    fun loadNews(reset: Boolean) {
        if (reset) {
            if (isLoading) return
        } else {
            if (isLoading || isLoadingMore || !canLoadMore) return
        }

        scope.launch {
            if (reset) {
                isLoading = true
                currentPage = 1
                canLoadMore = true
                error = null
            } else {
                isLoadingMore = true
            }

            try {
                val pageToLoad = if (reset) 1 else currentPage + 1
                val response = RetrofitInstance.api.getNewsFeed(
                    query = activeQuery,
                    page = pageToLoad,
                    pageSize = 20
                )

                val parsedLatest = parseNewsArticles(response.array("latest_today"))
                val parsedPageItems = parseNewsArticles(response.array("items"))
                val parsedTotal = response.intValue("total_results")
                    ?: if (reset) parsedPageItems.size else totalResults

                if (reset) {
                    latestToday = parsedLatest.distinctBy { it.uniqueKey }
                    olderNews = emptyList()
                }

                val latestKeys = latestToday.map { it.uniqueKey }.toSet()
                val pageOlder = parsedPageItems.filterNot { it.uniqueKey in latestKeys }
                olderNews = (olderNews + pageOlder).distinctBy { it.uniqueKey }

                totalResults = parsedTotal
                currentPage = pageToLoad
                canLoadMore = pageOlder.isNotEmpty() &&
                    (latestToday.size + olderNews.size < totalResults)
                error = null
            } catch (e: Exception) {
                error = e.message ?: "Failed to load news."
            } finally {
                isLoading = false
                isLoadingMore = false
            }
        }
    }

    LaunchedEffect(Unit) {
        loadNews(reset = true)
    }

    LaunchedEffect(listState, canLoadMore, isLoading, isLoadingMore, olderNews.size) {
        snapshotFlow {
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            val totalCount = listState.layoutInfo.totalItemsCount
            Pair(lastVisible, totalCount)
        }
            .map { (last, total) -> total > 0 && last >= total - 3 }
            .distinctUntilChanged()
            .filter { it }
            .collect {
                loadNews(reset = false)
            }
    }

    Scaffold(
        topBar = {
            HomeStyleTopBar(
                title = "News",
                navigationIcon = Icons.AutoMirrored.Filled.ArrowBack,
                navigationContentDescription = "Back",
                onNavigationClick = onBack,
                primaryActionIcon = Icons.Default.Refresh,
                primaryActionContentDescription = "Refresh news",
                onPrimaryActionClick = { loadNews(reset = true) }
            )
        },
        containerColor = Color.Transparent
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(HomeBackgroundBrush)
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Stock Market News",
                        color = Color(0xFF102446),
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = queryInput,
                            onValueChange = { queryInput = it },
                            modifier = Modifier.weight(1f),
                            placeholder = { Text("Search any stock") },
                            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                            keyboardActions = KeyboardActions(
                                onSearch = {
                                    activeQuery = queryInput.trim()
                                    loadNews(reset = true)
                                }
                            ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = Color(0xFF2A5FFF),
                                unfocusedBorderColor = Color(0xFFADC2E8),
                                focusedContainerColor = Color(0xFFF8FAFF),
                                unfocusedContainerColor = Color(0xFFF8FAFF),
                                focusedTextColor = Color(0xFF0F172A),
                                unfocusedTextColor = Color(0xFF0F172A)
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Button(
                            onClick = {
                                activeQuery = queryInput.trim()
                                loadNews(reset = true)
                            },
                            enabled = !isLoading,
                            modifier = Modifier.height(64.dp),
                            shape = CircleShape
                        ) {
                            Icon(Icons.Default.Search, contentDescription = "")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (!error.isNullOrBlank()) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3F3)),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text(
                                text = error.orEmpty(),
                                color = Color(0xFFD94242),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                    }
                }


                if (isLoading && olderNews.isEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2A5FFF))
                        }
                    }
                } else if (latestToday.isEmpty()) {

                } else {
                    items(latestToday, key = { it.uniqueKey }) { article ->
                        NewsCard(article = article, onRead = { url ->
                            if (url.isNotBlank()) {
                                runCatching { uriHandler.openUri(url) }
                            }
                        })
                    }
                }

                item {
                    Text(
                        text = "News",
                        color = Color(0xFF102446),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                if (!isLoading && olderNews.isEmpty()) {
                    item {
                        Text(
                            text = "No older news available.",
                            color = Color(0xFF64748B),
                            fontSize = 16.sp
                        )
                    }
                } else {
                    items(olderNews, key = { it.uniqueKey }) { article ->
                        NewsCard(article = article, onRead = { url ->
                            if (url.isNotBlank()) {
                                runCatching { uriHandler.openUri(url) }
                            }
                        })
                    }
                }

                if (isLoadingMore) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(color = Color(0xFF2A5FFF), modifier = Modifier.size(22.dp))
                        }
                    }
                }

                if (!canLoadMore && olderNews.isNotEmpty()) {
                    item {
                        Text(
                            text = "You are viewing all available news.",
                            color = Color(0xFF64748B),
                            fontSize = 13.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun NewsCard(
    article: NewsArticle,
    onRead: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFF)),
        onClick = {onRead(article.url)}
    ) {
        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            if (article.imageUrl.isNotBlank()) {
                AsyncImage(
                    model = article.imageUrl,
                    contentDescription = article.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .width(120.dp)
                        .height(106.dp)
                        .clip(RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp))
                )
            } else {
                Box(
                    modifier = Modifier
                        .width(120.dp)
                        .height(106.dp)
                        .background(Color(0xFFE2E8F0))
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = article.source.ifBlank { "Unknown source" },
                        color = Color(0xFF64748B),
                        fontSize = 13.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                        Text(
                        text = article.publishedAt.toTimeAgo(),
                        color = Color(0xFF64748B),
                        fontSize = 12.sp
                    )
                }

                Text(
                    text = article.title,
                    color = Color(0xFF102446),
                    fontSize = 21.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = article.description.ifBlank { "No summary available." },
                    color = Color(0xFF475569),
                    fontSize = 13.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SentimentChip(
                        label = article.sentimentLabel,
                        score = article.sentimentScore
                    )
                }
            }
        }
    }
}


@Composable
private fun SentimentChip(
    label: String,
    score: Double?
) {
    val normalized = label.trim().uppercase()
    val color = when {
        normalized.contains("BULL") || normalized.contains("POS") -> Color(0xFF22C55E)
        normalized.contains("BEAR") || normalized.contains("NEG") -> Color(0xFFEF4444)
        else -> Color(0xFFF59E0B)
    }

    Surface(
        shape = RoundedCornerShape(999.dp),
        color = color.copy(alpha = 0.15f),
        contentColor = color
    ) {
        Text(
            text = "${if (normalized.isBlank()) "NEUTRAL" else normalized} ${((score ?: 0.0)).toInt()}%",
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

private data class NewsArticle(
    val source: String,
    val title: String,
    val description: String,
    val url: String,
    val imageUrl: String,
    val publishedAt: String,
    val sentimentLabel: String,
    val sentimentScore: Double?
) {
    val uniqueKey: String
        get() = listOf(
            url.trim(),
            title.trim(),
            publishedAt.trim(),
            source.trim()
        ).joinToString("|")
}

private fun parseNewsArticles(array: JsonArray?): List<NewsArticle> {
    if (array == null) return emptyList()
    return buildList {
        array.forEach { element ->
            val obj = element.asJsonObjectOrNull() ?: return@forEach
            val title = obj.stringValue("title").orEmpty()
            if (title.isBlank()) return@forEach

            add(
                NewsArticle(
                    source = obj.stringValue("source").orEmpty(),
                    title = title,
                    description = obj.stringValue("description").orEmpty(),
                    url = obj.stringValue("url").orEmpty(),
                    imageUrl = obj.stringValue("image_url").orEmpty(),
                    publishedAt = obj.stringValue("published_at").orEmpty(),
                    sentimentLabel = obj.stringValue("sentiment_label").orEmpty(),
                    sentimentScore = obj.doubleValue("sentiment_score")
                )
            )
        }
    }
}

private fun JsonObject.array(key: String): JsonArray? {
    val value = get(key) ?: return null
    return if (value.isJsonArray) value.asJsonArray else null
}

private fun JsonObject.intValue(key: String): Int? {
    val value = get(key) ?: return null
    return value.intOrNull()
}

private fun JsonObject.doubleValue(key: String): Double? {
    val value = get(key) ?: return null
    return value.doubleOrNull()
}

private fun JsonObject.stringValue(key: String): String? {
    val value = get(key) ?: return null
    return value.stringOrNull()
}

private fun JsonElement.asJsonObjectOrNull(): JsonObject? {
    return if (isJsonObject) asJsonObject else null
}

private fun JsonElement.stringOrNull(): String? {
    return runCatching {
        if (isJsonNull) null else if (isJsonPrimitive) asJsonPrimitive.asString else toString()
    }.getOrNull()
}

private fun JsonElement.intOrNull(): Int? {
    return runCatching {
        if (isJsonNull) null else if (isJsonPrimitive) asJsonPrimitive.asInt else null
    }.getOrNull()
}

private fun JsonElement.doubleOrNull(): Double? {
    return runCatching {
        if (isJsonNull) null else if (isJsonPrimitive) asJsonPrimitive.asDouble else null
    }.getOrNull()
}

@RequiresApi(Build.VERSION_CODES.O)
private fun String.toTimeAgo(): String {
    if (isBlank()) return "--"
    return runCatching {
        val instant = Instant.parse(replace(" ", "T"))
        val duration = Duration.between(instant, Instant.now())
        when {
            duration.toMinutes() < 60 -> "${duration.toMinutes().coerceAtLeast(1)}m ago"
            duration.toHours() < 24 -> "${duration.toHours()}h ago"
            else -> "${duration.toDays()}d ago"
        }
    }.getOrElse { "--" }
}
