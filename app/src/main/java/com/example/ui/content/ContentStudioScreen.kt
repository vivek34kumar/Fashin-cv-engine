package com.example.ui.content

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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.material3.Surface
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
import com.example.data.local.entity.ContentItemEntity
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
fun ContentStudioScreen(
  viewModel: FashionEngineViewModel,
  onNavigateToPublishing: () -> Unit
) {
  val contentItems by viewModel.allContent.collectAsState()
  val activeProjects by viewModel.activeProjects.collectAsState()
  val publishingDestinations by viewModel.allDestinations.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  var selectedStatusFilter by remember { mutableStateOf("All") }
  var showAiGenDialog by remember { mutableStateOf(false) }
  var showManualDialog by remember { mutableStateOf(false) }
  var editingItem by remember { mutableStateOf<ContentItemEntity?>(null) }
  var publishingItem by remember { mutableStateOf<ContentItemEntity?>(null) }

  val statusTabs = listOf("All", "Draft", "Review", "Approved", "Published", "Idea", "Outline", "Archived")

  val filteredItems = when (selectedStatusFilter) {
    "All" -> contentItems
    else -> contentItems.filter { it.status.equals(selectedStatusFilter, ignoreCase = true) }
  }

  Scaffold(
    floatingActionButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        HauteButton(
          text = "AI Generate",
          onClick = { showAiGenDialog = true },
          icon = Icons.Default.AutoAwesome,
          isPrimary = false,
          testTag = "btn_ai_generate_content"
        )
        FloatingActionButton(
          onClick = { showManualDialog = true },
          modifier = Modifier.testTag("fab_manual_content"),
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
          Icon(Icons.Default.Add, contentDescription = "Manual Content")
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
        .testTag("content_studio_screen"),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        EditorialSectionHeader(
          kicker = "EDITORIAL OPERATING SYSTEM",
          title = "Content Studio",
          subtitle = "Draft, refine, and orchestrate fashion publications"
        )
      }

      // Status filter chips
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(statusTabs) { tab ->
            FilterChip(
              selected = selectedStatusFilter == tab,
              onClick = { selectedStatusFilter = tab },
              label = { Text(tab, style = MaterialTheme.typography.labelSmall) },
              modifier = Modifier.testTag("content_tab_$tab")
            )
          }
        }
      }

      if (filteredItems.isEmpty()) {
        item {
          EmptyStateView(
            title = if (selectedStatusFilter == "All") "Content Studio is Empty" else "No $selectedStatusFilter Content",
            description = "Create editorial articles, trend reports, newsletters, or social posts.",
            icon = Icons.Default.EditNote,
            actionText = "Create First Draft",
            actionTestTag = "content_empty_state_action",
            onActionClick = { showAiGenDialog = true }
          )
        }
      } else {
        items(filteredItems) { item ->
          ContentCardItem(
            item = item,
            onClick = { editingItem = item },
            onQuickPublish = { publishingItem = item },
            onDelete = { viewModel.deleteContent(item.id) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(72.dp))
      }
    }
  }

  // AI Content Generator Dialog
  if (showAiGenDialog) {
    AiContentGenerationDialog(
      projects = activeProjects,
      onDismiss = { showAiGenDialog = false },
      onSubmit = { title, type, projectId, instructions, relatedTrends ->
        viewModel.generateAiContent(
          title = title,
          contentType = type,
          projectId = projectId,
          instructions = instructions,
          relatedTrends = relatedTrends
        )
        showAiGenDialog = false
      }
    )
  }

  // Manual Draft Dialog
  if (showManualDialog) {
    ManualDraftDialog(
      projects = activeProjects,
      onDismiss = { showManualDialog = false },
      onSubmit = { title, type, projectId, body ->
        viewModel.createManualContent(
          title = title,
          contentType = type,
          projectId = projectId,
          body = body
        )
        showManualDialog = false
      }
    )
  }

  // Content Editor Sheet / Dialog
  if (editingItem != null) {
    EditContentDialog(
      item = editingItem!!,
      onDismiss = { editingItem = null },
      onSave = { updated ->
        viewModel.updateContent(updated)
        editingItem = null
      },
      onPublish = {
        publishingItem = editingItem
        editingItem = null
      }
    )
  }

  // Quick Publish Destination Picker
  if (publishingItem != null) {
    PublishDestinationDialog(
      item = publishingItem!!,
      destinations = publishingDestinations,
      onDismiss = { publishingItem = null },
      onConfirmPublish = { destId ->
        viewModel.publishContent(publishingItem!!.id, destId)
        publishingItem = null
      }
    )
  }
}

@Composable
fun ContentCardItem(
  item: ContentItemEntity,
  onClick: () -> Unit,
  onQuickPublish: () -> Unit,
  onDelete: () -> Unit
) {
  val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(item.updatedAt))

  EditorialCard(
    onClick = onClick,
    testTag = "content_card_${item.id}"
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = item.contentType.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          StatusBadge(status = item.status)
        }

        Row {
          if (item.status == "Approved" || item.status == "Draft") {
            IconButton(onClick = onQuickPublish, modifier = Modifier.size(32.dp)) {
              Icon(
                imageVector = Icons.Default.Send,
                contentDescription = "Publish",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
          IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = Icons.Default.Delete,
              contentDescription = "Delete",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = item.title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = item.body.ifBlank { "No body copy drafted yet." },
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        maxLines = 3,
        overflow = TextOverflow.Ellipsis
      )

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "By ${item.author}",
          style = MaterialTheme.typography.labelSmall,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiContentGenerationDialog(
  projects: List<com.example.data.local.entity.ProjectEntity>,
  onDismiss: () -> Unit,
  onSubmit: (
    title: String,
    type: String,
    projectId: Long?,
    instructions: String,
    relatedTrends: String
  ) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("Fashion report") }
  var instructions by remember { mutableStateOf("") }
  var relatedTrends by remember { mutableStateOf("") }
  var selectedProjectId by remember { mutableStateOf<Long?>(null) }

  val contentTypes = listOf(
    "Article", "Blog", "Fashion report", "Trend report",
    "Social media post", "Social caption", "Product description",
    "Newsletter", "Editorial", "Campaign content"
  )

  var expandedType by remember { mutableStateOf(false) }
  var expandedProject by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Generate AI Fashion Draft", style = MaterialTheme.typography.titleLarge) },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Article / Headline Title") },
            placeholder = { Text("e.g., The Resurgence of Tailored Suiting") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_ai_content_title")
          )
        }

        item {
          ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
            OutlinedTextField(
              value = type,
              onValueChange = {},
              readOnly = true,
              label = { Text("Content Format") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
              contentTypes.forEach { ct ->
                DropdownMenuItem(text = { Text(ct) }, onClick = { type = ct; expandedType = false })
              }
            }
          }
        }

        if (projects.isNotEmpty()) {
          item {
            val selectedProjectName = projects.find { it.id == selectedProjectId }?.name ?: "Global (No Project)"
            ExposedDropdownMenuBox(expanded = expandedProject, onExpandedChange = { expandedProject = !expandedProject }) {
              OutlinedTextField(
                value = selectedProjectName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Associate with Project") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedProject) },
                modifier = Modifier
                  .menuAnchor()
                  .fillMaxWidth()
              )
              ExposedDropdownMenu(expanded = expandedProject, onDismissRequest = { expandedProject = false }) {
                DropdownMenuItem(text = { Text("Global (No Project)") }, onClick = { selectedProjectId = null; expandedProject = false })
                projects.forEach { p ->
                  DropdownMenuItem(text = { Text(p.name) }, onClick = { selectedProjectId = p.id; expandedProject = false })
                }
              }
            }
          }
        }

        item {
          OutlinedTextField(
            value = relatedTrends,
            onValueChange = { relatedTrends = it },
            label = { Text("Related Trend Signals (Optional)") },
            placeholder = { Text("e.g., Post-Minimalism, Quiet Luxury") },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = instructions,
            onValueChange = { instructions = it },
            label = { Text("Creative Direction & Tone") },
            placeholder = { Text("e.g., Sophisticated, analytical voice for high-fashion readership.") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onSubmit(title, type, selectedProjectId, instructions, relatedTrends)
          }
        },
        enabled = title.isNotBlank(),
        modifier = Modifier.testTag("submit_ai_content")
      ) {
        Text("Generate Draft")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManualDraftDialog(
  projects: List<com.example.data.local.entity.ProjectEntity>,
  onDismiss: () -> Unit,
  onSubmit: (title: String, type: String, projectId: Long?, body: String) -> Unit
) {
  var title by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("Article") }
  var body by remember { mutableStateOf("") }
  var selectedProjectId by remember { mutableStateOf<Long?>(null) }

  val contentTypes = listOf(
    "Article", "Blog", "Fashion report", "Trend report",
    "Social media post", "Social caption", "Product description",
    "Newsletter", "Editorial", "Campaign content"
  )
  var expandedType by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("New Editorial Draft", style = MaterialTheme.typography.titleLarge) },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_manual_title")
          )
        }
        item {
          ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
            OutlinedTextField(
              value = type,
              onValueChange = {},
              readOnly = true,
              label = { Text("Content Type") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
              contentTypes.forEach { ct ->
                DropdownMenuItem(text = { Text(ct) }, onClick = { type = ct; expandedType = false })
              }
            }
          }
        }
        item {
          OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Body Text") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 4
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (title.isNotBlank()) {
            onSubmit(title, type, selectedProjectId, body)
          }
        },
        enabled = title.isNotBlank()
      ) {
        Text("Save Draft")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditContentDialog(
  item: ContentItemEntity,
  onDismiss: () -> Unit,
  onSave: (ContentItemEntity) -> Unit,
  onPublish: () -> Unit
) {
  var title by remember { mutableStateOf(item.title) }
  var body by remember { mutableStateOf(item.body) }
  var status by remember { mutableStateOf(item.status) }

  val statuses = listOf("Draft", "Review", "Approved", "Published", "Archived")
  var expandedStatus by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit Content Piece", style = MaterialTheme.typography.titleLarge) },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
          OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Title") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_content_title")
          )
        }

        item {
          ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = !expandedStatus }) {
            OutlinedTextField(
              value = status,
              onValueChange = {},
              readOnly = true,
              label = { Text("Publication Lifecycle Status") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
              statuses.forEach { st ->
                DropdownMenuItem(text = { Text(st) }, onClick = { status = st; expandedStatus = false })
              }
            }
          }
        }

        item {
          OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Content Body") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("edit_content_body"),
            minLines = 8
          )
        }
      }
    },
    confirmButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = {
            onSave(item.copy(title = title, body = body, status = status))
          },
          modifier = Modifier.testTag("save_content_button")
        ) {
          Text("Save")
        }
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    }
  )
}

@Composable
fun PublishDestinationDialog(
  item: ContentItemEntity,
  destinations: List<com.example.data.local.entity.PublishingDestinationEntity>,
  onDismiss: () -> Unit,
  onConfirmPublish: (destinationId: Long) -> Unit
) {
  var selectedDestId by remember { mutableStateOf(destinations.firstOrNull()?.id) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Publish to Distribution Channel", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
          text = "Select an active distribution endpoint for: '${item.title}'",
          style = MaterialTheme.typography.bodyMedium
        )

        if (destinations.isEmpty()) {
          Text(
            text = "No publishing destinations configured. Add a destination in the Publishing section.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.error
          )
        } else {
          destinations.forEach { dest ->
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.fillMaxWidth()
            ) {
              androidx.compose.material3.RadioButton(
                selected = selectedDestId == dest.id,
                onClick = { selectedDestId = dest.id }
              )
              Spacer(modifier = Modifier.width(8.dp))
              Column {
                Text(dest.name, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text("${dest.type} • ${dest.endpoint}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (selectedDestId != null) {
            onConfirmPublish(selectedDestId!!)
          }
        },
        enabled = selectedDestId != null,
        modifier = Modifier.testTag("confirm_publish_button")
      ) {
        Text("Publish Now")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
