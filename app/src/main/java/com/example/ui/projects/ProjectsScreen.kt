package com.example.ui.projects

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
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Unarchive
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.entity.ProjectEntity
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.viewmodel.FashionEngineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun ProjectsScreen(
  viewModel: FashionEngineViewModel,
  onNavigateToDetail: (Long) -> Unit
) {
  val allProjects by viewModel.allProjects.collectAsState()
  var showArchived by remember { mutableStateOf(false) }
  var showCreateDialog by remember { mutableStateOf(false) }

  val displayedProjects = allProjects.filter { it.isArchived == showArchived }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showCreateDialog = true },
        modifier = Modifier.testTag("fab_new_project"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ) {
        Icon(Icons.Default.Add, contentDescription = "New Project")
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
        .testTag("projects_screen"),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        EditorialSectionHeader(
          kicker = "BRAND PORTFOLIOS",
          title = "Fashion Projects",
          subtitle = "Organize research, content, and visuals per brand or season"
        )
      }

      item {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          FilterChip(
            selected = !showArchived,
            onClick = { showArchived = false },
            label = { Text("Active Projects", style = MaterialTheme.typography.labelSmall) }
          )
          FilterChip(
            selected = showArchived,
            onClick = { showArchived = true },
            label = { Text("Archived", style = MaterialTheme.typography.labelSmall) }
          )
        }
      }

      if (displayedProjects.isEmpty()) {
        item {
          EmptyStateView(
            title = if (!showArchived) "No Active Projects" else "No Archived Projects",
            description = "Create a structured portfolio to bind trends, research, content, and style guidelines together.",
            icon = Icons.Default.FolderSpecial,
            actionText = "Create New Project",
            actionTestTag = "project_empty_state_action",
            onActionClick = { showCreateDialog = true }
          )
        }
      } else {
        items(displayedProjects) { project ->
          ProjectCardItem(
            project = project,
            onClick = { onNavigateToDetail(project.id) },
            onToggleArchive = { viewModel.toggleArchiveProject(project) },
            onDelete = { viewModel.deleteProject(project.id) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(64.dp))
      }
    }
  }

  if (showCreateDialog) {
    CreateProjectDialog(
      onDismiss = { showCreateDialog = false },
      onSubmit = { name, desc, cat, audience, reg, lang, style ->
        viewModel.createProject(name, desc, cat, audience, reg, lang, style)
        showCreateDialog = false
      }
    )
  }
}

@Composable
fun ProjectCardItem(
  project: ProjectEntity,
  onClick: () -> Unit,
  onToggleArchive: () -> Unit,
  onDelete: () -> Unit
) {
  val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(project.createdAt))

  EditorialCard(
    onClick = onClick,
    testTag = "project_card_${project.id}"
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
        ) {
          Text(
            text = project.fashionCategory.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
          )
        }

        Row {
          IconButton(onClick = onToggleArchive, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = if (project.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
              contentDescription = "Archive",
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
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

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = project.name,
        style = MaterialTheme.typography.titleLarge,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold
      )

      if (project.description.isNotBlank()) {
        Spacer(modifier = Modifier.height(4.dp))
        Text(
          text = project.description,
          style = MaterialTheme.typography.bodyMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      Spacer(modifier = Modifier.height(8.dp))
      Text(
        text = "Direction: ${project.brandStyleDirection} • Target: ${project.targetAudience}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.primary
      )

      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Text(
          text = "Region: ${project.region} (${project.language})",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
          text = "Created: $dateStr",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProjectDialog(
  onDismiss: () -> Unit,
  onSubmit: (
    name: String,
    description: String,
    category: String,
    targetAudience: String,
    region: String,
    language: String,
    brandStyleDirection: String
  ) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var description by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Streetwear") }
  var targetAudience by remember { mutableStateOf("Gen-Z & High-Street") }
  var region by remember { mutableStateOf("Global") }
  var language by remember { mutableStateOf("English") }
  var brandStyleDirection by remember { mutableStateOf("Avant-Garde Minimal") }

  val categories = listOf(
    "Streetwear", "Luxury", "Casual", "Menswear", "Womenswear",
    "Footwear", "Accessories", "Beauty", "Sustainable Fashion",
    "Sportswear", "Denim", "Designer"
  )
  var expandedCategory by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Create Fashion Portfolio", style = MaterialTheme.typography.titleLarge) },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Project / Brand Name") },
            placeholder = { Text("e.g., Aether Fall/Winter 2026") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_project_name")
          )
        }

        item {
          ExposedDropdownMenuBox(
            expanded = expandedCategory,
            onExpandedChange = { expandedCategory = !expandedCategory }
          ) {
            OutlinedTextField(
              value = category,
              onValueChange = {},
              readOnly = true,
              label = { Text("Primary Fashion Category") },
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
            value = brandStyleDirection,
            onValueChange = { brandStyleDirection = it },
            label = { Text("Brand Style Direction") },
            placeholder = { Text("e.g., Haute Minimalist, 90s Grunge") },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = targetAudience,
            onValueChange = { targetAudience = it },
            label = { Text("Target Audience") },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = region,
            onValueChange = { region = it },
            label = { Text("Primary Region") },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Portfolio Brief & Objectives") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onSubmit(name, description, category, targetAudience, region, language, brandStyleDirection)
          }
        },
        enabled = name.isNotBlank(),
        modifier = Modifier.testTag("submit_project_button")
      ) {
        Text("Create Portfolio")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
