package com.example.ui.settings

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Source
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
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
import com.example.data.local.entity.ResearchSourceEntity
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.HauteButton
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.FashionEngineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchSourcesScreen(
  viewModel: FashionEngineViewModel,
  onBack: () -> Unit
) {
  val sources by viewModel.allSources.collectAsState()
  var showAddDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Fashion Intelligence Sources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { showAddDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = "Add Source")
          }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
      )
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
        .testTag("research_sources_screen"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        EditorialSectionHeader(
          kicker = "SIGNAL CHANNELS",
          title = "Active Media Ingestion",
          subtitle = "Sources queried for runway archives, editorial critique, and trend signals"
        )
      }

      items(sources) { source ->
        SourceCardItem(
          source = source,
          onToggle = { viewModel.updateResearchSource(source.copy(isEnabled = !source.isEnabled)) },
          onSyncNow = { viewModel.testResearchSource(source.id) },
          onDelete = { viewModel.deleteResearchSource(source.id) }
        )
      }

      item {
        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }

  if (showAddDialog) {
    AddSourceDialog(
      onDismiss = { showAddDialog = false },
      onSubmit = { name, type, url ->
        viewModel.addResearchSource(name, url, null, type, null)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun SourceCardItem(
  source: ResearchSourceEntity,
  onToggle: () -> Unit,
  onSyncNow: () -> Unit,
  onDelete: () -> Unit
) {
  val lastSyncStr = if (source.lastSync != null) {
    SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(source.lastSync))
  } else "Not synced yet"

  EditorialCard(testTag = "source_card_${source.id}") {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = source.type.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          StatusBadge(status = if (source.isEnabled) source.status else "Inactive")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Switch(
            checked = source.isEnabled,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = source.name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )

      Text(
        text = source.urlOrEndpoint,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Last sync: $lastSyncStr",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HauteButton(
          text = "Sync / Test",
          onClick = onSyncNow,
          icon = Icons.Default.Refresh,
          isPrimary = false,
          modifier = Modifier.height(34.dp)
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSourceDialog(
  onDismiss: () -> Unit,
  onSubmit: (name: String, type: String, url: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("RSS") }
  var url by remember { mutableStateOf("") }

  val types = listOf("RSS", "Fashion Wire", "Runway Index", "Web Scraper", "API Endpoint")
  var expandedType by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Intelligence Source", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Source Name") },
          placeholder = { Text("e.g., Vogue Runway Wire") },
          modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
          OutlinedTextField(
            value = type,
            onValueChange = {},
            readOnly = true,
            label = { Text("Source Type") },
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

        OutlinedTextField(
          value = url,
          onValueChange = { url = it },
          label = { Text("URL / RSS Feed") },
          placeholder = { Text("https://example.com/feed.xml") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && url.isNotBlank()) {
            onSubmit(name, type, url)
          }
        },
        enabled = name.isNotBlank() && url.isNotBlank()
      ) {
        Text("Add Source")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
