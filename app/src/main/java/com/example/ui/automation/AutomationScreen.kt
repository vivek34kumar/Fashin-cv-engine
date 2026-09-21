package com.example.ui.automation

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.AutomationEntity
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HauteButton
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.FashionEngineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AutomationScreen(
  viewModel: FashionEngineViewModel
) {
  val automations by viewModel.allAutomations.collectAsState()
  val activeProjects by viewModel.activeProjects.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        modifier = Modifier.testTag("fab_add_automation"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Automation")
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
        .testTag("automation_screen"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        EditorialSectionHeader(
          kicker = "AUTONOMOUS OPERATING SYSTEM",
          title = "Background Pipelines",
          subtitle = "Configure automated intelligence gathering and content syndication"
        )
      }

      if (automations.isEmpty()) {
        item {
          EmptyStateView(
            title = "No Active Automations",
            description = "Automate periodic research runs, trend ingestion, or scheduled syndications.",
            icon = Icons.Default.Autorenew,
            actionText = "Create Pipeline",
            actionTestTag = "automation_empty_state_action",
            onActionClick = { showAddDialog = true }
          )
        }
      } else {
        items(automations) { auto ->
          AutomationCardItem(
            auto = auto,
            onToggle = { viewModel.toggleAutomation(auto) },
            onRunNow = { viewModel.runAutomationNow(auto) },
            onDelete = { viewModel.deleteAutomation(auto.id) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(64.dp))
      }
    }
  }

  if (showAddDialog) {
    AddAutomationDialog(
      projects = activeProjects,
      onDismiss = { showAddDialog = false },
      onSubmit = { name, type, freq, projectId, params ->
        viewModel.addAutomation(name, params, type, freq, projectId, true)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun AutomationCardItem(
  auto: AutomationEntity,
  onToggle: () -> Unit,
  onRunNow: () -> Unit,
  onDelete: () -> Unit
) {
  val lastRunStr = if (auto.lastRun != null) {
    SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(auto.lastRun))
  } else "Never run yet"

  EditorialCard(testTag = "automation_card_${auto.id}") {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = auto.type.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          StatusBadge(status = if (auto.isEnabled) "Active" else "Idle")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Switch(
            checked = auto.isEnabled,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = auto.name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Cadence: ${auto.frequency} • Last run: $lastRunStr",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
      ) {
        HauteButton(
          text = "Execute Now",
          onClick = onRunNow,
          icon = Icons.Default.PlayArrow,
          isPrimary = true,
          modifier = Modifier.height(36.dp),
          testTag = "run_automation_${auto.id}"
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAutomationDialog(
  projects: List<com.example.data.local.entity.ProjectEntity>,
  onDismiss: () -> Unit,
  onSubmit: (name: String, type: String, frequency: String, projectId: Long?, parameters: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("Periodic research") }
  var frequency by remember { mutableStateOf("Daily") }
  var selectedProjectId by remember { mutableStateOf<Long?>(null) }
  var parameters by remember { mutableStateOf("") }

  val types = listOf("Periodic research", "Trend sync", "Content workflow", "Analytics updates", "Publishing jobs")
  val frequencies = listOf("Hourly", "Daily", "Weekly", "Custom")

  var expandedType by remember { mutableStateOf(false) }
  var expandedFreq by remember { mutableStateOf(false) }
  var expandedProject by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Configure Automation Pipeline", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Pipeline Name") },
          placeholder = { Text("e.g., Daily Runway & Trend Digest") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_automation_name")
        )

        ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
          OutlinedTextField(
            value = type,
            onValueChange = {},
            readOnly = true,
            label = { Text("Automation Type") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
            types.forEach { t ->
              DropdownMenuItem(text = { Text(t) }, onClick = { type = t; expandedType = false })
            }
          }
        }

        ExposedDropdownMenuBox(expanded = expandedFreq, onExpandedChange = { expandedFreq = !expandedFreq }) {
          OutlinedTextField(
            value = frequency,
            onValueChange = {},
            readOnly = true,
            label = { Text("Execution Frequency") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFreq) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = expandedFreq, onDismissRequest = { expandedFreq = false }) {
            frequencies.forEach { f ->
              DropdownMenuItem(text = { Text(f) }, onClick = { frequency = f; expandedFreq = false })
            }
          }
        }

        if (projects.isNotEmpty()) {
          val selectedProjectName = projects.find { it.id == selectedProjectId }?.name ?: "All Projects"
          ExposedDropdownMenuBox(expanded = expandedProject, onExpandedChange = { expandedProject = !expandedProject }) {
            OutlinedTextField(
              value = selectedProjectName,
              onValueChange = {},
              readOnly = true,
              label = { Text("Project Scope") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProject) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedProject, onDismissRequest = { expandedProject = false }) {
              DropdownMenuItem(text = { Text("All Projects") }, onClick = { selectedProjectId = null; expandedProject = false })
              projects.forEach { p ->
                DropdownMenuItem(text = { Text(p.name) }, onClick = { selectedProjectId = p.id; expandedProject = false })
              }
            }
          }
        }

        OutlinedTextField(
          value = parameters,
          onValueChange = { parameters = it },
          label = { Text("Runtime Parameters (Optional)") },
          placeholder = { Text("e.g., categories=Luxury,Streetwear") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onSubmit(name, type, frequency, selectedProjectId, parameters)
          }
        },
        enabled = name.isNotBlank(),
        modifier = Modifier.testTag("submit_automation_button")
      ) {
        Text("Save Pipeline")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
