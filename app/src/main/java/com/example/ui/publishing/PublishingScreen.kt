package com.example.ui.publishing

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
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
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.PublishingDestinationEntity
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HauteButton
import com.example.ui.components.StatusBadge
import com.example.ui.content.PublishDestinationDialog
import com.example.ui.viewmodel.FashionEngineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PublishingScreen(
  viewModel: FashionEngineViewModel
) {
  val destinations by viewModel.allDestinations.collectAsState()
  val allContent by viewModel.allContent.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  val publishedItems = allContent.filter { it.status == "Published" }
  val approvedItems = allContent.filter { it.status == "Approved" || it.status == "Draft" }

  var showAddDialog by remember { mutableStateOf(false) }
  var publishingItem by remember { mutableStateOf<com.example.data.local.entity.ContentItemEntity?>(null) }
  var showApprovedPicker by remember { mutableStateOf(false) }

  Scaffold(
    floatingActionButton = {
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        if (approvedItems.isNotEmpty()) {
          HauteButton(
            text = "Publish Draft",
            onClick = { showApprovedPicker = true },
            icon = Icons.Default.Send,
            isPrimary = false,
            testTag = "btn_publish_approved_draft"
          )
        }
        FloatingActionButton(
          onClick = { showAddDialog = true },
          modifier = Modifier.testTag("fab_add_destination"),
          containerColor = MaterialTheme.colorScheme.primary,
          contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
          Icon(Icons.Default.Add, contentDescription = "Add Channel")
        }
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
        .testTag("publishing_screen"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        EditorialSectionHeader(
          kicker = "DISTRIBUTION CHANNELS",
          title = "Publishing & Syndication",
          subtitle = "Configure CMS webhooks, WordPress integrations, and publishing pipelines"
        )
      }

      // Channels section
      item {
        Text(
          text = "Connected Endpoints",
          style = MaterialTheme.typography.titleMedium,
          fontWeight = FontWeight.SemiBold
        )
      }

      if (destinations.isEmpty()) {
        item {
          EmptyStateView(
            title = "No Publishing Channels",
            description = "Add a WordPress REST API or Headless CMS endpoint to syndicate articles directly.",
            icon = Icons.Default.Public,
            actionText = "Add Channel",
            actionTestTag = "publishing_empty_state_action",
            onActionClick = { showAddDialog = true }
          )
        }
      } else {
        items(destinations) { dest ->
          DestinationCard(
            dest = dest,
            onTestConnection = { viewModel.testPublishingDestination(dest.id) },
            onToggle = { viewModel.updatePublishingDestination(dest.copy(isEnabled = !dest.isEnabled)) },
            onDelete = { viewModel.deletePublishingDestination(dest.id) }
          )
        }
      }

      // Published History
      item {
        Spacer(modifier = Modifier.height(10.dp))
        EditorialSectionHeader(
          kicker = "AUDIT TRAIL",
          title = "Syndicated Content History",
          subtitle = "${publishedItems.size} live published pieces verified in database"
        )
      }

      if (publishedItems.isEmpty()) {
        item {
          EmptyStateView(
            title = "No Content Published Yet",
            description = "When articles in Content Studio are approved and transmitted, their audit record appears here.",
            icon = Icons.Default.CheckCircle
          )
        }
      } else {
        items(publishedItems) { item ->
          val dateStr = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(item.updatedAt))
          EditorialCard {
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
                StatusBadge(status = "Published")
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
              )
              Spacer(modifier = Modifier.height(4.dp))
              Text(
                text = "Author: ${item.author} • Published: $dateStr",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
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
    AddDestinationDialog(
      onDismiss = { showAddDialog = false },
      onSubmit = { name, type, endpoint, credentials, isEnabled ->
        viewModel.addPublishingDestination(name, type, endpoint, credentials, isEnabled)
        showAddDialog = false
      }
    )
  }

  if (showApprovedPicker) {
    ApprovedDraftsPickerDialog(
      items = approvedItems,
      onDismiss = { showApprovedPicker = false },
      onItemSelected = { item ->
        publishingItem = item
        showApprovedPicker = false
      }
    )
  }

  if (publishingItem != null) {
    PublishDestinationDialog(
      item = publishingItem!!,
      destinations = destinations,
      onDismiss = { publishingItem = null },
      onConfirmPublish = { destId ->
        viewModel.publishContent(publishingItem!!.id, destId)
        publishingItem = null
      }
    )
  }
}

@Composable
fun DestinationCard(
  dest: PublishingDestinationEntity,
  onTestConnection: () -> Unit,
  onToggle: () -> Unit,
  onDelete: () -> Unit
) {
  val lastPubStr = if (dest.lastPublishedAt != null) {
    SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dest.lastPublishedAt))
  } else "Never"

  EditorialCard(testTag = "destination_card_${dest.id}") {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = dest.type.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          StatusBadge(status = if (dest.isEnabled) dest.status else "Inactive")
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Switch(
            checked = dest.isEnabled,
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
        text = dest.name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = dest.endpoint,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Last Syndication: $lastPubStr",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        HauteButton(
          text = "Test Endpoint",
          onClick = onTestConnection,
          icon = Icons.Default.Refresh,
          isPrimary = false,
          modifier = Modifier.height(36.dp),
          testTag = "test_dest_${dest.id}"
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDestinationDialog(
  onDismiss: () -> Unit,
  onSubmit: (name: String, type: String, endpoint: String, credentials: String, isEnabled: Boolean) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("WordPress") }
  var endpoint by remember { mutableStateOf("") }
  var credentials by remember { mutableStateOf("") }
  var isEnabled by remember { mutableStateOf(true) }

  val types = listOf("WordPress", "Headless CMS", "Shopify Blog", "Ghost", "Custom Webhook")
  var expandedType by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add Publishing Destination", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Channel Name") },
          placeholder = { Text("e.g., Vogue Editorial CMS") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_dest_name")
        )

        ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
          OutlinedTextField(
            value = type,
            onValueChange = {},
            readOnly = true,
            label = { Text("CMS Architecture") },
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
          value = endpoint,
          onValueChange = { endpoint = it },
          label = { Text("API Endpoint URL") },
          placeholder = { Text("https://example.com/wp-json/wp/v2/posts") },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_dest_endpoint")
        )

        OutlinedTextField(
          value = credentials,
          onValueChange = { credentials = it },
          label = { Text("Bearer Token / App Password") },
          placeholder = { Text("Optional authorization token") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && endpoint.isNotBlank()) {
            onSubmit(name, type, endpoint, credentials, isEnabled)
          }
        },
        enabled = name.isNotBlank() && endpoint.isNotBlank(),
        modifier = Modifier.testTag("submit_destination_button")
      ) {
        Text("Add Channel")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@Composable
fun ApprovedDraftsPickerDialog(
  items: List<com.example.data.local.entity.ContentItemEntity>,
  onDismiss: () -> Unit,
  onItemSelected: (com.example.data.local.entity.ContentItemEntity) -> Unit
) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Select Content to Publish", style = MaterialTheme.typography.titleLarge) },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(items) { item ->
          EditorialCard(
            onClick = { onItemSelected(item) }
          ) {
            Column {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
              ) {
                Text(item.contentType.uppercase(), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                StatusBadge(status = item.status)
              }
              Spacer(modifier = Modifier.height(4.dp))
              Text(item.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
          }
        }
      }
    },
    confirmButton = {},
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Close") }
    }
  )
}
