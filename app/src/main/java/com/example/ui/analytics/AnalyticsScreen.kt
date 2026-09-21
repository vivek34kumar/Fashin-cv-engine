package com.example.ui.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.MetricCard
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCobalt
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ChampagneGold
import com.example.ui.viewmodel.FashionEngineViewModel

@Composable
fun AnalyticsScreen(
  viewModel: FashionEngineViewModel
) {
  val allProjects by viewModel.allProjects.collectAsState()
  val allResearch by viewModel.allResearchRuns.collectAsState()
  val allTrends by viewModel.allTrends.collectAsState()
  val allContent by viewModel.allContent.collectAsState()
  val allImages by viewModel.allImages.collectAsState()
  val allJobs by viewModel.allJobs.collectAsState()

  val completedResearchCount = allResearch.count { it.status == "Completed" }
  val publishedContentCount = allContent.count { it.status == "Published" }
  val draftContentCount = allContent.count { it.status == "Draft" }
  val approvedContentCount = allContent.count { it.status == "Approved" }

  val successfulJobs = allJobs.count { it.status == "Completed" }
  val totalCompletedOrFailed = allJobs.count { it.status == "Completed" || it.status == "Failed" }
  val jobSuccessRate = if (totalCompletedOrFailed > 0) (successfulJobs * 100) / totalCompletedOrFailed else 100

  // Category distributions computed from actual records
  val trendCategoryCounts = allTrends.groupBy { it.category }
  val trendVelocityCounts = allTrends.groupBy { it.status }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("analytics_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      EditorialSectionHeader(
        kicker = "INTELLIGENCE TELEMETRY",
        title = "Fashion Operations Analytics",
        subtitle = "Empirical operational breakdown calculated from local engine state"
      )
    }

    // High Level Metric Cards
    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricCard(
          title = "Intelligence",
          value = "$completedResearchCount / ${allResearch.size}",
          subtitle = "Dossiers Completed",
          icon = Icons.Default.Explore,
          modifier = Modifier.weight(1f),
          accentColor = AccentCobalt
        )
        MetricCard(
          title = "Syndication",
          value = "$publishedContentCount / ${allContent.size}",
          subtitle = "Pieces Published",
          icon = Icons.Default.Publish,
          modifier = Modifier.weight(1f),
          accentColor = AccentEmerald
        )
      }
    }

    item {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        MetricCard(
          title = "Trend Radar",
          value = allTrends.size.toString(),
          subtitle = "Signals Tracked",
          icon = Icons.Default.TrendingUp,
          modifier = Modifier.weight(1f),
          accentColor = AccentAmber
        )
        MetricCard(
          title = "Job Reliability",
          value = "$jobSuccessRate%",
          subtitle = "Execution Health",
          icon = Icons.Default.Autorenew,
          modifier = Modifier.weight(1f),
          accentColor = ChampagneGold
        )
      }
    }

    // Content Pipeline Velocity
    item {
      EditorialCard(testTag = "content_pipeline_analytics") {
        Column {
          Text(
            text = "Content Studio Pipeline Velocity",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(12.dp))

          val totalContent = if (allContent.isNotEmpty()) allContent.size.toFloat() else 1f

          PipelineBarRow("Drafts In Progress", draftContentCount, totalContent, AccentAmber)
          Spacer(modifier = Modifier.height(8.dp))
          PipelineBarRow("Under Review", allContent.count { it.status == "Review" }, totalContent, AccentCobalt)
          Spacer(modifier = Modifier.height(8.dp))
          PipelineBarRow("Approved for Syndication", approvedContentCount, totalContent, ChampagneGold)
          Spacer(modifier = Modifier.height(8.dp))
          PipelineBarRow("Live Published", publishedContentCount, totalContent, AccentEmerald)
        }
      }
    }

    // Trend Velocity Radar
    item {
      EditorialCard(testTag = "trend_velocity_analytics") {
        Column {
          Text(
            text = "Trend Velocity Breakdown",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(12.dp))

          if (allTrends.isEmpty()) {
            Text("No trend records in database yet.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          } else {
            val totalTrends = allTrends.size.toFloat()
            trendVelocityCounts.forEach { (status, list) ->
              PipelineBarRow(
                label = "$status Trends",
                count = list.size,
                total = totalTrends,
                color = when (status.lowercase()) {
                  "emerging" -> AccentCobalt
                  "peaking" -> AccentAmber
                  "evergreen" -> AccentEmerald
                  else -> ChampagneGold
                }
              )
              Spacer(modifier = Modifier.height(8.dp))
            }
          }
        }
      }
    }

    // Category Distribution
    item {
      EditorialCard(testTag = "category_distribution_analytics") {
        Column {
          Text(
            text = "Intelligence by Fashion Category",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
          Spacer(modifier = Modifier.height(12.dp))

          if (trendCategoryCounts.isEmpty()) {
            Text("No categorized trends recorded.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          } else {
            val total = allTrends.size.toFloat()
            trendCategoryCounts.forEach { (cat, list) ->
              PipelineBarRow(
                label = cat,
                count = list.size,
                total = total,
                color = MaterialTheme.colorScheme.primary
              )
              Spacer(modifier = Modifier.height(8.dp))
            }
          }
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}

@Composable
private fun PipelineBarRow(
  label: String,
  count: Int,
  total: Float,
  color: Color
) {
  val ratio = if (total > 0) (count / total) else 0f
  val percentage = (ratio * 100).toInt()

  Column {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      Text(label, style = MaterialTheme.typography.labelMedium)
      Text("$count ($percentage%)", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
    }
    Spacer(modifier = Modifier.height(4.dp))
    LinearProgressIndicator(
      progress = { ratio },
      modifier = Modifier
        .fillMaxWidth()
        .height(6.dp)
        .clip(RoundedCornerShape(3.dp)),
      color = color,
      trackColor = MaterialTheme.colorScheme.surfaceVariant
    )
  }
}
