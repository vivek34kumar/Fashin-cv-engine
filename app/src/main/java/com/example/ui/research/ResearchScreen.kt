package com.example.ui.research

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ResearchRunEntity
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HauteButton
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.SubRoutes
import com.example.ui.viewmodel.FashionEngineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ResearchScreen(
  viewModel: FashionEngineViewModel,
  onNavigateToDetail: (Long) -> Unit
) {
  val researchRuns by viewModel.allResearchRuns.collectAsState()
  val activeProjects by viewModel.activeProjects.collectAsState()
  val allSources by viewModel.allSources.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  var showCreateDialog by remember { mutableStateOf(false) }
  var selectedFilter by remember { mutableStateOf("All") }

  val filterOptions = listOf("All", "Completed", "Running", "Queued", "Draft", "Failed")

  val filteredRuns = researchRuns.filter { run ->
    when (selectedFilter) {
      "All" -> true
      else -> run.status.equals(selectedFilter, ignoreCase = true)
    }
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showCreateDialog = true },
        modifier = Modifier.testTag("fab_new_research"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ) {
        Icon(Icons.Default.Add, contentDescription = "New Research Run")
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
        .testTag("research_screen"),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        EditorialSectionHeader(
          kicker = "FASHION INTELLIGENCE",
          title = "Research Pipeline",
          subtitle = "Multi-source market signal analysis and trend discovery"
        )
      }

      // Filter Chips
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(filterOptions) { filter ->
            FilterChip(
              selected = selectedFilter == filter,
              onClick = { selectedFilter = filter },
              label = { Text(filter, style = MaterialTheme.typography.labelSmall) },
              modifier = Modifier.testTag("filter_$filter")
            )
          }
        }
      }

      if (filteredRuns.isEmpty()) {
        item {
          EmptyStateView(
            title = if (selectedFilter == "All") "No Research Runs Yet" else "No $selectedFilter Research Runs",
            description = "Launch an in-depth fashion intelligence run across your configured sources.",
            icon = Icons.Default.Explore,
            actionText = "New Research Run",
            actionTestTag = "research_empty_state_action",
            onActionClick = { showCreateDialog = true }
          )
        }
      } else {
        items(filteredRuns) { run ->
          ResearchRunItem(
            run = run,
            onClick = { onNavigateToDetail(run.id) },
            onExecute = { viewModel.executeResearch(run.id) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(64.dp))
      }
    }
  }

  if (showCreateDialog) {
    CreateResearchRunDialog(
      projects = activeProjects,
      sources = allSources.map { it.name },
      onDismiss = { showCreateDialog = false },
      onSubmit = { topic, category, audience, region, language, timeRange, selectedSources, projectId, executeImmediately ->
        viewModel.createResearchRun(
          topic = topic,
          category = category,
          targetAudience = audience,
          region = region,
          language = language,
          timeRange = timeRange,
          sources = selectedSources.joinToString(", "),
          projectId = projectId,
          executeImmediately = executeImmediately
        )
        showCreateDialog = false
      }
    )
  }
}

@Composable
fun ResearchRunItem(
  run: ResearchRunEntity,
  onClick: () -> Unit,
  onExecute: () -> Unit
) {
  val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(run.createdAt))

  EditorialCard(
    onClick = onClick,
    testTag = "research_run_item_${run.id}"
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

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = run.topic,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = "Audience: ${run.targetAudience} • Region: ${run.region}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Sources: ${run.researchSources}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )

      if (!run.errorMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = "Error: ${run.errorMessage}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.error
        )
      }

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = dateStr,
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (run.status == "Draft" || run.status == "Failed") {
          HauteButton(
            text = if (run.status == "Failed") "Retry" else "Execute",
            onClick = onExecute,
            icon = Icons.Default.PlayArrow,
            isPrimary = true,
            modifier = Modifier.height(36.dp),
            testTag = "execute_research_${run.id}"
          )
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateResearchRunDialog(
  projects: List<com.example.data.local.entity.ProjectEntity>,
  sources: List<String>,
  onDismiss: () -> Unit,
  onSubmit: (
    topic: String,
    category: String,
    audience: String,
    region: String,
    language: String,
    timeRange: String,
    sources: List<String>,
    projectId: Long?,
    executeImmediately: Boolean
  ) -> Unit
) {
  var topic by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Streetwear") }
  var audience by remember { mutableStateOf("Contemporary Consumer") }
  var region by remember { mutableStateOf("Global") }
  var language by remember { mutableStateOf("English") }
  var timeRange by remember { mutableStateOf("Past 30 Days") }
  var selectedProjectId by remember { mutableStateOf<Long?>(null) }
  var executeImmediately by remember { mutableStateOf(true) }

  val categories = listOf(
    "Streetwear", "Luxury", "Casual", "Menswear", "Womenswear",
    "Footwear", "Accessories", "Beauty", "Sustainable Fashion",
    "Sportswear", "Denim", "Designer", "Avant-Garde"
  )
  val timeRanges = listOf("Past 7 Days", "Past 30 Days", "Past 90 Days", "Past 6 Months", "Past 1 Year")

  var expandedCategory by remember { mutableStateOf(false) }
  var expandedTimeRange by remember { mutableStateOf(false) }
  var expandedProject by remember { mutableStateOf(false) }

  val defaultSources = if (sources.isNotEmpty()) sources else listOf("Vogue Runway Index", "WWD Fashion Wire", "Highsnobiety Archive")
  var chosenSources by remember { mutableStateOf(defaultSources.toSet()) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Text("New Fashion Research Run", style = MaterialTheme.typography.titleLarge)
    },
    text = {
      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(vertical = 4.dp)
      ) {
        item {
          OutlinedTextField(
            value = topic,
            onValueChange = { topic = it },
            label = { Text("Research Topic") },
            placeholder = { Text("e.g., Post-Minimalism & Cashmere Outerwear") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_research_topic"),
            singleLine = true
          )
        }

        // Category Selector
        item {
          ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory }
          ) {
            OutlinedTextField(
              value = category,
              onValueChange = {},
              readOnly = true,
              label = { Text("Fashion Category") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = expandedCategory,
              onDismissRequest = { expandedCategory = false }
            ) {
              categories.forEach { cat ->
                DropdownMenuItem(
                  text = { Text(cat) },
                  onClick = {
                    category = cat
                    expandedCategory = false
                  }
                )
              }
            }
          }
        }

        item {
          OutlinedTextField(
            value = audience,
            onValueChange = { audience = it },
            label = { Text("Target Audience") },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text("Market Region") },
            modifier = Modifier.fillMaxWidth()
          )
        }

        // Time Range
        item {
          ExposedDropdownMenuBox(
            expanded = expandedTimeRange,
            onExpandedChange = { expandedTimeRange = !expandedTimeRange }
          ) {
            OutlinedTextField(
              value = timeRange,
              onValueChange = {},
              readOnly = true,
              label = { Text("Time Range") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTimeRange) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = expandedTimeRange,
              onDismissRequest = { expandedTimeRange = false }
            ) {
              timeRanges.forEach { tr ->
                DropdownMenuItem(
                  text = { Text(tr) },
                  onClick = {
                    timeRange = tr
                    expandedTimeRange = false
                  }
                )
              }
            }
          }
        }

        // Project selection (optional)
        if (projects.isNotEmpty()) {
          item {
            val selectedProjectName = projects.find { it.id == selectedProjectId }?.name ?: "None (Global Research)"
            ExposedDropdownMenuBox(
              expanded = expandedProject,
              onExpandedChange = { expandedProject = !expandedProject }
            ) {
              OutlinedTextField(
                value = selectedProjectName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Link to Project (Optional)") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProject) },
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth()
              )
              ExposedDropdownMenu(
                expanded = expandedProject,
                onDismissRequest = { expandedProject = false }
              ) {
                DropdownMenuItem(
                  text = { Text("None (Global Research)") },
                  onClick = {
                    selectedProjectId = null
                    expandedProject = false
                  }
                )
                projects.forEach { proj ->
                  DropdownMenuItem(
                    text = { Text(proj.name) },
                    onClick = {
                      selectedProjectId = proj.id
                      expandedProject = false
                    }
                  )
                }
              }
            }
          }
        }

        // Research sources selection
        item {
          Text(
            text = "Select Intelligence Sources:",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onBackground
          )
        }

        items(defaultSources) { src ->
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Checkbox(
              checked = chosenSources.contains(src),
              onCheckedChange = { checked ->
                chosenSources = if (checked) chosenSources + src else chosenSources - src
              }
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(src, style = MaterialTheme.typography.bodyMedium)
          }
        }

        item {
          Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
          ) {
            Checkbox(
              checked = executeImmediately,
              onCheckedChange = { executeImmediately = it },
              modifier = Modifier.testTag("checkbox_execute_immediately")
            )
            Spacer(modifier = Modifier.width(6.dp))
            Column {
              Text("Execute Intelligence Run Now", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              Text("Synthesizes signals with AI provider immediately", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (topic.isNotBlank()) {
            onSubmit(
              topic,
              category,
              audience,
              region,
              language,
              timeRange,
              chosenSources.toList(),
              selectedProjectId,
              executeImmediately
            )
          }
        },
        enabled = topic.isNotBlank(),
        modifier = Modifier.testTag("submit_research_run")
      ) {
        Text("Create Run")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) {
        Text("Cancel")
      }
    }
  )
}
