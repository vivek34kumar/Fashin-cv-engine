package com.example.ui.dashboard

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ContentItemEntity
import com.example.data.local.entity.JobEntity
import com.example.data.local.entity.ResearchRunEntity
import com.example.data.local.entity.TrendEntity
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.MainDestination
import com.example.ui.navigation.SubRoutes
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCobalt
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ChampagneGold
import com.example.ui.viewmodel.FashionEngineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DashboardScreen(
  viewModel: FashionEngineViewModel,
  onNavigateToDestination: (MainDestination) -> Unit,
  onNavigateToSubRoute: (String) -> Unit
) {
  val activeProjectsCount by viewModel.activeProjectsCount.collectAsState()
  val researchRunsCount by viewModel.researchRunsCount.collectAsState()
  val savedTrendsCount by viewModel.savedTrendsCount.collectAsState()
  val draftContentCount by viewModel.draftContentCount.collectAsState()
  val publishedContentCount by viewModel.publishedContentCount.collectAsState()
  val activeJobsCount by viewModel.activeJobsCount.collectAsState()

  val researchRuns by viewModel.allResearchRuns.collectAsState()
  val trends by viewModel.activeTrends.collectAsState()
  val contentItems by viewModel.allContent.collectAsState()
  val activeJobs by viewModel.activeJobs.collectAsState()
  val selectedProject by viewModel.selectedProject.collectAsState()

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("dashboard_screen"),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    item {
      Spacer(modifier = Modifier.height(8.dp))
      // Brand Header
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "FASHION INTELLIGENCE OS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.8.sp
          )
          Text(
            text = "Editorial Dashboard",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            fontWeight = FontWeight.SemiBold
          )
        }

        // Active project pill if selected
        if (selectedProject != null) {
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.clickable { onNavigateToSubRoute(SubRoutes.projectDetail(selectedProject!!.id)) }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Icon(
                imageVector = Icons.Default.FolderSpecial,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(14.dp)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = selectedProject!!.name,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1
              )
            }
          }
        }
      }
    }

    // Quick System Navigation Ribbon
    item {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        AssistChip(
          onClick = { onNavigateToDestination(MainDestination.IMAGE) },
          label = { Text("Image Studio", style = MaterialTheme.typography.labelSmall) },
          leadingIcon = { Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        AssistChip(
          onClick = { onNavigateToDestination(MainDestination.PUBLISHING) },
          label = { Text("Publishing", style = MaterialTheme.typography.labelSmall) },
          leadingIcon = { Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        AssistChip(
          onClick = { onNavigateToDestination(MainDestination.ANALYTICS) },
          label = { Text("Analytics", style = MaterialTheme.typography.labelSmall) },
          leadingIcon = { Icon(Icons.Default.BarChart, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        AssistChip(
          onClick = { onNavigateToDestination(MainDestination.AUTOMATION) },
          label = { Text("Automation", style = MaterialTheme.typography.labelSmall) },
          leadingIcon = { Icon(Icons.Default.Autorenew, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        AssistChip(
          onClick = { onNavigateToSubRoute(SubRoutes.JOB_MANAGER) },
          label = { Text("Job Queue ($activeJobsCount)", style = MaterialTheme.typography.labelSmall) },
          leadingIcon = { Icon(Icons.Default.Task, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        AssistChip(
          onClick = { onNavigateToSubRoute(SubRoutes.MEMORY) },
          label = { Text("Memory", style = MaterialTheme.typography.labelSmall) },
          leadingIcon = { Icon(Icons.Default.Psychology, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
      }
    }

    // Metric Cards Grid (2x3)
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricCard(
            title = "Projects",
            value = activeProjectsCount.toString(),
            subtitle = "Active Portfolios",
            icon = Icons.Default.FolderSpecial,
            modifier = Modifier.weight(1f),
            accentColor = ChampagneGold,
            testTag = "metric_active_projects",
            onClick = { onNavigateToDestination(MainDestination.PROJECTS) }
          )
          MetricCard(
            title = "Research",
            value = researchRunsCount.toString(),
            subtitle = "Intelligence Runs",
            icon = Icons.Default.Explore,
            modifier = Modifier.weight(1f),
            accentColor = AccentCobalt,
            testTag = "metric_research_runs",
            onClick = { onNavigateToDestination(MainDestination.RESEARCH) }
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricCard(
            title = "Trends",
            value = savedTrendsCount.toString(),
            subtitle = "Signals Tracked",
            icon = Icons.Default.TrendingUp,
            modifier = Modifier.weight(1f),
            accentColor = AccentAmber,
            testTag = "metric_saved_trends",
            onClick = { onNavigateToDestination(MainDestination.TRENDS) }
          )
          MetricCard(
            title = "Drafts",
            value = draftContentCount.toString(),
            subtitle = "In Pipeline",
            icon = Icons.Default.EditNote,
            modifier = Modifier.weight(1f),
            accentColor = MaterialTheme.colorScheme.onSurfaceVariant,
            testTag = "metric_draft_content",
            onClick = { onNavigateToDestination(MainDestination.CONTENT) }
          )
        }

        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          MetricCard(
            title = "Published",
            value = publishedContentCount.toString(),
            subtitle = "Live Articles",
            icon = Icons.Default.Publish,
            modifier = Modifier.weight(1f),
            accentColor = AccentEmerald,
            testTag = "metric_published_content",
            onClick = { onNavigateToDestination(MainDestination.PUBLISHING) }
          )
          MetricCard(
            title = "Background",
            value = activeJobsCount.toString(),
            subtitle = "Active Workers",
            icon = Icons.Default.Task,
            modifier = Modifier.weight(1f),
            accentColor = if (activeJobsCount > 0) AccentCobalt else MaterialTheme.colorScheme.outline,
            testTag = "metric_active_jobs",
            onClick = { onNavigateToSubRoute(SubRoutes.JOB_MANAGER) }
          )
        }
      }
    }

    // Active Jobs Section (if any jobs running)
    if (activeJobs.isNotEmpty()) {
      item {
        EditorialSectionHeader(
          kicker = "REAL-TIME TASKS",
          title = "Active WorkManager Pipeline",
          subtitle = "${activeJobs.size} operational jobs currently running"
        )
      }

      items(activeJobs) { job ->
        EditorialCard(
          onClick = { onNavigateToSubRoute(SubRoutes.JOB_MANAGER) },
          testTag = "dashboard_job_${job.id}"
        ) {
          Column {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(
                text = job.type.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
              )
              StatusBadge(status = job.status)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = job.details ?: "Job ID: ${job.id}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onBackground,
              fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
              progress = { job.progress / 100f },
              modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp)),
              color = MaterialTheme.colorScheme.primary,
              trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
          }
        }
      }
    }

    // Recent Research Runs
    item {
      EditorialSectionHeader(
        kicker = "INTELLIGENCE",
        title = "Recent Research Runs",
        actionButton = {
          IconButton(onClick = { onNavigateToDestination(MainDestination.RESEARCH) }) {
            Icon(Icons.Default.Add, contentDescription = "New Research", tint = MaterialTheme.colorScheme.primary)
          }
        }
      )
    }

    if (researchRuns.isEmpty()) {
      item {
        EmptyStateView(
          title = "No Research Runs Recorded",
          description = "Initiate your first fashion intelligence run to discover category signals and competitive trends.",
          icon = Icons.Default.Explore,
          actionText = "Start Research Run",
          actionTestTag = "dashboard_start_research_action",
          onActionClick = { onNavigateToDestination(MainDestination.RESEARCH) }
        )
      }
    } else {
      items(researchRuns.take(3)) { run ->
        DashboardResearchItem(
          run = run,
          onClick = { onNavigateToSubRoute(SubRoutes.researchDetail(run.id)) }
        )
      }
    }

    // Active Trends
    item {
      EditorialSectionHeader(
        kicker = "FORECASTING",
        title = "Active Trend Radar",
        actionButton = {
          IconButton(onClick = { onNavigateToDestination(MainDestination.TRENDS) }) {
            Icon(Icons.Default.Add, contentDescription = "New Trend", tint = MaterialTheme.colorScheme.primary)
          }
        }
      )
    }

    if (trends.isEmpty()) {
      item {
        EmptyStateView(
          title = "No Active Trends",
          description = "Create or extract trend signals from research to monitor emergence and peaking cycles.",
          icon = Icons.Default.TrendingUp,
          actionText = "Create Trend",
          actionTestTag = "dashboard_create_trend_action",
          onActionClick = { onNavigateToDestination(MainDestination.TRENDS) }
        )
      }
    } else {
      items(trends.take(3)) { trend ->
        DashboardTrendItem(
          trend = trend,
          onClick = { onNavigateToDestination(MainDestination.TRENDS) }
        )
      }
    }

    // Latest Content Items
    item {
      EditorialSectionHeader(
        kicker = "EDITORIAL STUDIO",
        title = "Latest Content Drafts",
        actionButton = {
          IconButton(onClick = { onNavigateToDestination(MainDestination.CONTENT) }) {
            Icon(Icons.Default.Add, contentDescription = "New Content", tint = MaterialTheme.colorScheme.primary)
          }
        }
      )
    }

    if (contentItems.isEmpty()) {
      item {
        EmptyStateView(
          title = "No Content Created",
          description = "Use Content Studio to draft articles, fashion reports, and newsletters with AI or manually.",
          icon = Icons.Default.EditNote,
          actionText = "Draft Content",
          actionTestTag = "dashboard_draft_content_action",
          onActionClick = { onNavigateToDestination(MainDestination.CONTENT) }
        )
      }
    } else {
      items(contentItems.take(3)) { item ->
        DashboardContentItem(
          item = item,
          onClick = { onNavigateToDestination(MainDestination.CONTENT) }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(24.dp))
    }
  }
}

@Composable
fun DashboardResearchItem(
  run: ResearchRunEntity,
  onClick: () -> Unit
) {
  val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(run.createdAt))

  EditorialCard(
    onClick = onClick,
    testTag = "dashboard_research_item_${run.id}"
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = run.category.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )
        StatusBadge(status = run.status)
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = run.topic,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "${run.region} • ${run.timeRange}",
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = dateStr,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun DashboardTrendItem(
  trend: TrendEntity,
  onClick: () -> Unit
) {
  EditorialCard(
    onClick = onClick,
    testTag = "dashboard_trend_item_${trend.id}"
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = trend.category.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )
        StatusBadge(status = trend.status)
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = trend.name,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = trend.description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
      if (trend.relatedBrands.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Key Brands: ${trend.relatedBrands}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary
        )
      }
    }
  }
}

@Composable
fun DashboardContentItem(
  item: ContentItemEntity,
  onClick: () -> Unit
) {
  EditorialCard(
    onClick = onClick,
    testTag = "dashboard_content_item_${item.id}"
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = item.contentType.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )
        StatusBadge(status = item.status)
      }
      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = item.title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = item.body.ifBlank { "No content body drafted yet." },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis
      )
    }
  }
}
