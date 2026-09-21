package com.example.ui.research

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.EditorialCard
import com.example.ui.components.HauteButton
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.MainDestination
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ChampagneGold
import com.example.ui.viewmodel.FashionEngineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchDetailScreen(
  runId: Long,
  viewModel: FashionEngineViewModel,
  onBack: () -> Unit,
  onNavigateToContent: () -> Unit,
  onNavigateToSettings: () -> Unit,
  onNavigateToTrends: () -> Unit = {}
) {
  val allRuns by viewModel.allResearchRuns.collectAsState()
  val run = allRuns.find { it.id == runId }
  val result by viewModel.observeResearchResult(runId).collectAsState(initial = null)
  val trendsFromThisRun by viewModel.observeTrendsByResearchRun(runId).collectAsState(initial = emptyList())
  val isLoading by viewModel.isLoading.collectAsState()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            text = "Intelligence Dossier",
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
    if (run == null) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(paddingValues),
        contentAlignment = Alignment.Center
      ) {
        Text("Research run not found.", style = MaterialTheme.typography.bodyLarge)
      }
      return@Scaffold
    }

    val dateStr = SimpleDateFormat("MMMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(run.createdAt))

    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
        .testTag("research_detail_screen"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Header Dossier Card
      item {
        EditorialCard(testTag = "research_detail_header") {
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
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.2.sp
              )
              StatusBadge(status = run.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = run.topic,
              style = MaterialTheme.typography.headlineMedium,
              color = MaterialTheme.colorScheme.onBackground,
              fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Target: ${run.targetAudience} • Region: ${run.region} • Range: ${run.timeRange}",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
              text = "Logged: $dateStr",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))
            Text(
              text = "Configured Sources: ${run.researchSources}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary
            )
          }
        }
      }

      // If Failed
      if (run.status == "Failed") {
        item {
          EditorialCard(testTag = "research_failed_card") {
            Column {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Error, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                  text = "Research Execution Failed",
                  style = MaterialTheme.typography.titleMedium,
                  color = MaterialTheme.colorScheme.error,
                  fontWeight = FontWeight.SemiBold
                )
              }
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = run.errorMessage ?: "Unknown error occurred during intelligence synthesis.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(16.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HauteButton(
                  text = "Configure Provider",
                  onClick = onNavigateToSettings,
                  icon = Icons.Default.Settings,
                  isPrimary = false
                )
                HauteButton(
                  text = "Retry Run",
                  onClick = { viewModel.executeResearch(run.id) },
                  icon = Icons.Default.Refresh,
                  isPrimary = true,
                  isLoading = isLoading
                )
              }
            }
          }
        }
      }

      // If Running or Queued
      if (run.status == "Running" || run.status == "Queued") {
        item {
          EditorialCard(testTag = "research_running_card") {
            Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically
            ) {
              CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp
              )
              Spacer(modifier = Modifier.width(16.dp))
              Column {
                Text(
                  text = if (run.status == "Running") "Synthesizing Intelligence..." else "Queued in Job Engine",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold
                )
                Text(
                  text = "Analyzing market signals, runway archives, and trend data.",
                  style = MaterialTheme.typography.bodyMedium,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }
      }

      // If Completed with Result
      if (result != null) {
        // Confidence Score Bar
        item {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "SYNTHESIZED INTELLIGENCE",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.Bold,
              letterSpacing = 1.4.sp
            )
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = AccentEmerald.copy(alpha = 0.15f)
            ) {
              Text(
                text = "${(result!!.confidenceScore * 100).toInt()}% Confidence Score",
                style = MaterialTheme.typography.labelSmall,
                color = AccentEmerald,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }
        }

        // Executive Summary
        item {
          EditorialCard(testTag = "executive_summary_card") {
            Column {
              Text(
                text = "Executive Summary",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = result!!.summary,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
              )
            }
          }
        }

        // Detailed Findings
        item {
          EditorialCard(testTag = "detailed_findings_card") {
            Column {
              Text(
                text = "Intelligence Findings & Signal Analysis",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
              )
              Spacer(modifier = Modifier.height(10.dp))
              Text(
                text = result!!.keyFindings,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 22.sp
              )
            }
          }
        }

        // Market Observations
        item {
          EditorialCard(testTag = "market_observations_card") {
            Column {
              Text(
                text = "Market Observations & Commercial Viability",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.height(8.dp))
              Text(
                text = result!!.marketObservations,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }

        // Action: Extract Fashion Trends from Research
        item {
          EditorialCard(testTag = "extract_trends_card") {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Pipeline: Extract Fashion Trends",
                  style = MaterialTheme.typography.titleMedium,
                  fontWeight = FontWeight.SemiBold
                )
                if (trendsFromThisRun.isNotEmpty()) {
                  Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = AccentEmerald.copy(alpha = 0.12f)
                  ) {
                    Text(
                      text = "${trendsFromThisRun.size} Trends Extracted",
                      style = MaterialTheme.typography.labelSmall,
                      color = AccentEmerald,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                  }
                }
              }

              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Deconstruct this research dossier into multiple structured fashion trends (aesthetic signals, hero silhouettes, brands, and commercial status).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )

              if (trendsFromThisRun.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                  trendsFromThisRun.forEach { trend ->
                    Surface(
                      modifier = Modifier.fillMaxWidth(),
                      shape = RoundedCornerShape(8.dp),
                      color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                      Row(
                        modifier = Modifier
                          .fillMaxWidth()
                          .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                      ) {
                        Column(modifier = Modifier.weight(1f)) {
                          Text(
                            text = trend.name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                          )
                          Text(
                            text = "${trend.category} • ${(trend.confidenceScore * 100).toInt()}% confidence",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                          )
                        }
                        StatusBadge(status = trend.status)
                      }
                    }
                  }
                }
              }

              Spacer(modifier = Modifier.height(12.dp))
              Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HauteButton(
                  text = if (trendsFromThisRun.isEmpty()) "Extract Trends" else "Re-Extract Trends",
                  onClick = { viewModel.extractTrendsFromResearch(run.id) },
                  icon = Icons.Default.AutoAwesome,
                  isPrimary = true,
                  isLoading = isLoading,
                  modifier = Modifier.weight(1f),
                  testTag = "btn_extract_trends_from_research"
                )
                if (trendsFromThisRun.isNotEmpty()) {
                  HauteButton(
                    text = "View in Trends",
                    onClick = onNavigateToTrends,
                    icon = Icons.Default.TrendingUp,
                    isPrimary = false,
                    modifier = Modifier.weight(1f),
                    testTag = "btn_view_in_trends"
                  )
                }
              }
            }
          }
        }

        // Action: Create Content Draft from Research
        item {
          EditorialCard(testTag = "convert_to_content_card") {
            Column {
              Text(
                text = "Convert to Editorial Content",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Transfer this intelligence dossier directly into Content Studio to begin drafting an article, trend report, or social story.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              Spacer(modifier = Modifier.height(12.dp))
              HauteButton(
                text = "Draft in Content Studio",
                onClick = {
                  viewModel.createManualContent(
                    title = "Fashion Report: ${run.topic}",
                    contentType = "Fashion report",
                    projectId = run.projectId,
                    body = result!!.keyFindings,
                    status = "Draft"
                  )
                  onNavigateToContent()
                },
                icon = Icons.Default.EditNote,
                isPrimary = true,
                modifier = Modifier.fillMaxWidth(),
                testTag = "btn_draft_in_content_studio"
              )
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
