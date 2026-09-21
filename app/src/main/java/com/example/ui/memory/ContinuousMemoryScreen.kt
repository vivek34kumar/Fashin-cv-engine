package com.example.ui.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.example.data.local.entity.LearningMemoryEntity
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.viewmodel.FashionEngineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContinuousMemoryScreen(
  viewModel: FashionEngineViewModel,
  onBack: () -> Unit
) {
  val allMemories by viewModel.allMemories.collectAsState()
  var selectedCategoryFilter by remember { mutableStateOf("All") }
  var showAddDialog by remember { mutableStateOf(false) }

  val categories = listOf("All", "Writing Style", "Brand Guidelines", "Audience Constraints", "Tone", "Aesthetics", "Keywords")

  val filteredMemories = when (selectedCategoryFilter) {
    "All" -> allMemories
    else -> allMemories.filter { it.category.equals(selectedCategoryFilter, ignoreCase = true) }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Continuous Fashion Learning Memory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showAddDialog = true },
        modifier = Modifier.testTag("fab_add_memory"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ) {
        Icon(Icons.Default.Add, contentDescription = "Add Learning Rule")
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
        .testTag("continuous_memory_screen"),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        EditorialSectionHeader(
          kicker = "ADAPTIVE INTELLIGENCE",
          title = "Fashion Knowledge & Style Memory",
          subtitle = "Injected into AI prompts to preserve your editorial voice and brand DNA"
        )
      }

      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(categories) { cat ->
            FilterChip(
              selected = selectedCategoryFilter == cat,
              onClick = { selectedCategoryFilter = cat },
              label = { Text(cat, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }
      }

      if (filteredMemories.isEmpty()) {
        item {
          EmptyStateView(
            title = "No Memory Entries Recorded",
            description = "Save brand guidelines, tone rules, or vocabulary preferences.",
            icon = Icons.Default.Psychology,
            actionText = "Add Preference",
            actionTestTag = "memory_empty_state_action",
            onActionClick = { showAddDialog = true }
          )
        }
      } else {
        items(filteredMemories) { mem ->
          EditorialCard(testTag = "memory_card_${mem.id}") {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = mem.category.uppercase(),
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.primary,
                  fontWeight = FontWeight.Bold
                )
                IconButton(onClick = { viewModel.deleteLearningMemory(mem.id) }, modifier = Modifier.size(28.dp)) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
                }
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(mem.key, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
              Spacer(modifier = Modifier.height(2.dp))
              Text(mem.value, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
          }
        }
      }

      item {
        Spacer(modifier = Modifier.height(64.dp))
      }
    }
  }

  if (showAddDialog) {
    AddMemoryDialog(
      onDismiss = { showAddDialog = false },
      onSubmit = { category, key, value ->
        viewModel.addLearningMemory(null, category, key, value)
        showAddDialog = false
      }
    )
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMemoryDialog(
  onDismiss: () -> Unit,
  onSubmit: (category: String, key: String, value: String) -> Unit
) {
  var category by remember { mutableStateOf("Writing Style") }
  var key by remember { mutableStateOf("") }
  var value by remember { mutableStateOf("") }

  val categories = listOf("Writing Style", "Brand Guidelines", "Audience Constraints", "Tone", "Aesthetics", "Keywords")
  var expandedCat by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Continuous Learning Rule", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        ExposedDropdownMenuBox(expanded = expandedCat, onExpandedChange = { expandedCat = !expandedCat }) {
          OutlinedTextField(
            value = category,
            onValueChange = {},
            readOnly = true,
            label = { Text("Rule Category") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
            modifier = Modifier
              .menuAnchor()
              .fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
            categories.forEach { c ->
              DropdownMenuItem(text = { Text(c) }, onClick = { category = c; expandedCat = false })
            }
          }
        }

        OutlinedTextField(
          value = key,
          onValueChange = { key = it },
          label = { Text("Rule Title / Guideline Key") },
          placeholder = { Text("e.g., Tone of Voice") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_memory_key")
        )

        OutlinedTextField(
          value = value,
          onValueChange = { value = it },
          label = { Text("Rule Specification / Value") },
          placeholder = { Text("e.g., Use concise, elevated language. Avoid colloquialisms and hype.") },
          modifier = Modifier.fillMaxWidth(),
          minLines = 3
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (key.isNotBlank() && value.isNotBlank()) {
            onSubmit(category, key, value)
          }
        },
        enabled = key.isNotBlank() && value.isNotBlank(),
        modifier = Modifier.testTag("submit_memory_button")
      ) {
        Text("Save Rule")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
