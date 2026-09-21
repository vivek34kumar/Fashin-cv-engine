package com.example.ui.projects

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryScrollableTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EditorialCard
import com.example.ui.components.EmptyStateView
import com.example.ui.components.MetricCard
import com.example.ui.components.StatusBadge
import com.example.ui.content.ContentCardItem
import com.example.ui.image.ImageGenerationCard
import com.example.ui.research.ResearchRunItem
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentCobalt
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ChampagneGold
import com.example.ui.trends.TrendItemCard
import com.example.ui.viewmodel.FashionEngineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProjectDetailScreen(
  projectId: Long,
  viewModel: FashionEngineViewModel,
  onBack: () -> Unit,
  onNavigateToResearchDetail: (Long) -> Unit
) {
  LaunchedEffect(projectId) {
    viewModel.selectProject(projectId)
  }

  val project by viewModel.selectedProject.collectAsState()
  val projectResearch by viewModel.projectResearch.collectAsState()
  val projectTrends by viewModel.projectTrends.collectAsState()
  val projectContent by viewModel.projectContent.collectAsState()
  val projectImages by viewModel.projectImages.collectAsState()
  val projectAutomations by viewModel.projectAutomations.collectAsState()
  val projectJobs by viewModel.projectJobs.collectAsState()
  val projectMemories by viewModel.projectMemories.collectAsState()

  var selectedTabIndex by remember { mutableStateOf(0) }
  val tabTitles = listOf("Overview", "Research", "Trends", "Content", "Images", "Automations", "Jobs", "Memory")

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = project?.name ?: "Project Details",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(
          containerColor = MaterialTheme.colorScheme.background
        )
      )
    }
  ) { paddingValues ->
    if (project == null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center
      ) {
        Text("Loading project...", style = MaterialTheme.typography.bodyLarge)
      }
      return@Scaffold
    }

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
    ) {
      // Sub-Tabs Row
      ScrollableTabRow(
        selectedTabIndex = selectedTabIndex,
        edgePadding = 16.dp,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.primary
      ) {
        tabTitles.forEachIndexed { index, title ->
          Tab(
            selected = selectedTabIndex == index,
            onClick = { selectedTabIndex = index },
            text = {
              Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
              )
            }
          )
        }
      }

      LazyColumn(
        modifier = Modifier
          .fillMaxSize()
          .padding(horizontal = 16.dp)
          .testTag("project_tab_content"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        item {
          Spacer(modifier = Modifier.height(8.dp))
        }

        when (selectedTabIndex) {
          0 -> { // Overview
            item {
              EditorialCard(testTag = "project_overview_brief") {
                Column {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = project!!.fashionCategory.uppercase(),
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary,
                      fontWeight = FontWeight.Bold
                    )
                    StatusBadge(status = if (project!!.isArchived) "Archived" else "Active")
                  }
                  Spacer(modifier = Modifier.height(6.dp))
                  Text(
                    text = project!!.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                  )
                  if (project!!.description.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = project!!.description,
                      style = MaterialTheme.typography.bodyMedium,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }
                  Spacer(modifier = Modifier.height(8.dp))
                  Text(
                    text = "Style Direction: ${project!!.brandStyleDirection} • Target: ${project!!.targetAudience} • Region: ${project!!.region}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                  )
                }
              }
            }

            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                MetricCard(
                  title = "Research",
                  value = projectResearch.size.toString(),
                  subtitle = "Intelligence Runs",
                  icon = Icons.Default.Explore,
                  modifier = Modifier.weight(1f),
                  accentColor = ChampagneGold
                )
                MetricCard(
                  title = "Trends",
                  value = projectTrends.size.toString(),
                  subtitle = "Category Signals",
                  icon = Icons.Default.TrendingUp,
                  modifier = Modifier.weight(1f),
                  accentColor = AccentCobalt
                )
              }
            }

            item {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
              ) {
                MetricCard(
                  title = "Content",
                  value = projectContent.size.toString(),
                  subtitle = "Drafts & Live",
                  icon = Icons.Default.EditNote,
                  modifier = Modifier.weight(1f),
                  accentColor = AccentEmerald
                )
                MetricCard(
                  title = "Visuals",
                  value = projectImages.size.toString(),
                  subtitle = "Lookbook Assets",
                  icon = Icons.Default.PhotoLibrary,
                  modifier = Modifier.weight(1f),
                  accentColor = AccentAmber
                )
              }
            }
          }

          1 -> { // Research
            if (projectResearch.isEmpty()) {
              item {
                EmptyStateView(
                  title = "No Project Research Runs",
                  description = "Launch an intelligence run linked to this project to gather focused trend data.",
                  icon = Icons.Default.Explore
                )
              }
            } else {
              items(projectResearch) { run ->
                ResearchRunItem(
                  run = run,
                  onClick = { onNavigateToResearchDetail(run.id) },
                  onExecute = { viewModel.executeResearch(run.id) }
                )
              }
            }
          }

          2 -> { // Trends
            if (projectTrends.isEmpty()) {
              item {
                EmptyStateView(
                  title = "No Project Trends Logged",
                  description = "Map fashion movements to this project in the Trends section.",
                  icon = Icons.Default.TrendingUp
                )
              }
            } else {
              items(projectTrends) { trend ->
                TrendItemCard(
                  trend = trend,
                  onToggleSave = { viewModel.toggleSaveTrend(trend) },
                  onToggleArchive = { viewModel.toggleArchiveTrend(trend) },
                  onDelete = { viewModel.deleteTrend(trend.id) }
                )
              }
            }
          }

          3 -> { // Content
            if (projectContent.isEmpty()) {
              item {
                EmptyStateView(
                  title = "No Content Drafts",
                  description = "Create editorial copy for this project in Content Studio.",
                  icon = Icons.Default.EditNote
                )
              }
            } else {
              items(projectContent) { contentItem ->
                ContentCardItem(
                  item = contentItem,
                  onClick = {},
                  onQuickPublish = {},
                  onDelete = { viewModel.deleteContent(contentItem.id) }
                )
              }
            }
          }

          4 -> { // Images
            if (projectImages.isEmpty()) {
              item {
                EmptyStateView(
                  title = "No Visual Assets",
                  description = "Generate fashion visuals and lookbooks linked to this project in Image Studio.",
                  icon = Icons.Default.PhotoLibrary
                )
              }
            } else {
              items(projectImages) { img ->
                ImageGenerationCard(imgGen = img)
              }
            }
          }

          5 -> { // Automations
            if (projectAutomations.isEmpty()) {
              item {
                EmptyStateView(
                  title = "No Automations Configured",
                  description = "Schedule automated research or trend sync for this project.",
                  icon = Icons.Default.Autorenew
                )
              }
            } else {
              items(projectAutomations) { auto ->
                EditorialCard {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Column {
                      Text(auto.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                      Text("${auto.type} • ${auto.frequency}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    StatusBadge(status = if (auto.isEnabled) "Active" else "Idle")
                  }
                }
              }
            }
          }

          6 -> { // Jobs
            if (projectJobs.isEmpty()) {
              item {
                EmptyStateView(
                  title = "No Active Background Jobs",
                  description = "All background workers for this project are complete.",
                  icon = Icons.Default.Task
                )
              }
            } else {
              items(projectJobs) { job ->
                EditorialCard {
                  Column {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween,
                      verticalAlignment = Alignment.CenterVertically
                    ) {
                      Text(job.type.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                      StatusBadge(status = job.status)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(job.details ?: "Job ID: ${job.id}", style = MaterialTheme.typography.bodyMedium)
                  }
                }
              }
            }
          }

          7 -> { // Continuous Memory
            if (projectMemories.isEmpty()) {
              item {
                EmptyStateView(
                  title = "No Brand Learning Preferences",
                  description = "Add writing styles, brand guidelines, and tone preferences for this project.",
                  icon = Icons.Default.Psychology
                )
              }
            } else {
              items(projectMemories) { mem ->
                EditorialCard {
                  Column {
                    Row(
                      modifier = Modifier.fillMaxWidth(),
                      horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                      Text(mem.category.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                      Text(mem.source, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(mem.key, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Text(mem.value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                  }
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
  }
}
