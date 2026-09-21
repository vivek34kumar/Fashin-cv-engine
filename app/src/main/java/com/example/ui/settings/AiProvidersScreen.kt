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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
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
import androidx.compose.material3.Surface
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.AIProviderEntity
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.HauteButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentEmerald
import com.example.ui.viewmodel.FashionEngineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiProvidersScreen(
  viewModel: FashionEngineViewModel,
  onBack: () -> Unit
) {
  val providers by viewModel.allProviders.collectAsState()
  var editingProvider by remember { mutableStateOf<AIProviderEntity?>(null) }
  var showAddDialog by remember { mutableStateOf(false) }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("AI Intelligence Providers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        actions = {
          IconButton(onClick = { showAddDialog = true }) {
            Icon(Icons.Default.Add, contentDescription = "Add Provider")
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
        .testTag("ai_providers_screen"),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      item {
        EditorialSectionHeader(
          kicker = "INFERENCE ENGINE",
          title = "Configured Providers",
          subtitle = "Credentials are stored locally in Room DB or injected from BuildConfig"
        )
      }

      items(providers) { provider ->
        ProviderCard(
          provider = provider,
          onSetDefault = { viewModel.setDefaultProvider(provider.id) },
          onToggle = { viewModel.updateProvider(provider.copy(isEnabled = !provider.isEnabled)) },
          onEdit = { editingProvider = provider },
          onTestConnection = { viewModel.testProviderConnection(provider.id) },
          onDelete = { viewModel.deleteProvider(provider.id) }
        )
      }

      item {
        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }

  editingProvider?.let { target ->
    EditProviderDialog(
      provider = target,
      onDismiss = { editingProvider = null },
      onSave = { updated ->
        viewModel.updateProvider(updated)
        editingProvider = null
      }
    )
  }

  if (showAddDialog) {
    AddProviderDialog(
      onDismiss = { showAddDialog = false },
      onSubmit = { name, type, endpoint, apiKey, model ->
        viewModel.addProvider(name, type, endpoint, apiKey, model, true)
        showAddDialog = false
      }
    )
  }
}

@Composable
fun ProviderCard(
  provider: AIProviderEntity,
  onSetDefault: () -> Unit,
  onToggle: () -> Unit,
  onEdit: () -> Unit,
  onTestConnection: () -> Unit,
  onDelete: () -> Unit
) {
  val maskedKey = if (provider.apiKey.isNotBlank()) {
    val visibleLength = minOf(4, provider.apiKey.length)
    "••••••••" + provider.apiKey.takeLast(visibleLength)
  } else "No API Key Set"

  EditorialCard(testTag = "provider_card_${provider.id}") {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = provider.name.uppercase(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
          )
          if (provider.isDefault) {
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = AccentEmerald.copy(alpha = 0.15f)
            ) {
              Text(
                text = "DEFAULT",
                style = MaterialTheme.typography.labelSmall,
                color = AccentEmerald,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          Switch(
            checked = provider.isEnabled,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(36.dp)
          )
          Spacer(modifier = Modifier.width(8.dp))
          IconButton(onClick = onEdit, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
          }
          if (!provider.isDefault) {
            IconButton(onClick = onDelete, modifier = Modifier.size(28.dp)) {
              Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Model: ${provider.model}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Text(
        text = "Endpoint: ${provider.apiEndpoint}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      Spacer(modifier = Modifier.height(6.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Key: $maskedKey",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary
        )
        StatusBadge(status = provider.connectionStatus)
      }

      Spacer(modifier = Modifier.height(10.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        if (!provider.isDefault && provider.isEnabled) {
          TextButton(onClick = onSetDefault) {
            Text("Set as Default", style = MaterialTheme.typography.labelSmall)
          }
        } else {
          Spacer(modifier = Modifier.width(8.dp))
        }

        HauteButton(
          text = "Test Connection",
          onClick = onTestConnection,
          icon = Icons.Default.Refresh,
          isPrimary = false,
          modifier = Modifier.height(34.dp),
          testTag = "test_provider_${provider.id}"
        )
      }
    }
  }
}

@Composable
fun EditProviderDialog(
  provider: AIProviderEntity,
  onDismiss: () -> Unit,
  onSave: (AIProviderEntity) -> Unit
) {
  var modelName by remember { mutableStateOf(provider.model) }
  var endpointUrl by remember { mutableStateOf(provider.apiEndpoint) }
  var apiKey by remember { mutableStateOf(provider.apiKey) }
  var showKey by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Edit ${provider.name}", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = modelName,
          onValueChange = { modelName = it },
          label = { Text("Model Name") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = endpointUrl,
          onValueChange = { endpointUrl = it },
          label = { Text("Endpoint URL") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = apiKey,
          onValueChange = { apiKey = it },
          label = { Text("API Key") },
          visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation(),
          trailingIcon = {
            IconButton(onClick = { showKey = !showKey }) {
              Icon(if (showKey) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = null)
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("input_provider_api_key")
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onSave(provider.copy(model = modelName, apiEndpoint = endpointUrl, apiKey = apiKey))
        },
        modifier = Modifier.testTag("save_provider_button")
      ) {
        Text("Save")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddProviderDialog(
  onDismiss: () -> Unit,
  onSubmit: (name: String, type: String, endpoint: String, apiKey: String, model: String) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var type by remember { mutableStateOf("GEMINI") }
  var model by remember { mutableStateOf("gemini-2.5-flash") }
  var endpoint by remember { mutableStateOf("https://generativelanguage.googleapis.com") }
  var apiKey by remember { mutableStateOf("") }

  val types = listOf("GEMINI", "OPENAI", "DEEPSEEK", "NVIDIA", "CUSTOM")
  var expandedType by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Add AI Intelligence Provider", style = MaterialTheme.typography.titleLarge) },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedTextField(
          value = name,
          onValueChange = { name = it },
          label = { Text("Provider Label") },
          placeholder = { Text("e.g., DeepSeek Reasoner") },
          modifier = Modifier.fillMaxWidth()
        )

        ExposedDropdownMenuBox(expanded = expandedType, onExpandedChange = { expandedType = !expandedType }) {
          OutlinedTextField(
            value = type,
            onValueChange = {},
            readOnly = true,
            label = { Text("Provider Protocol") },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedType) },
            modifier = Modifier.menuAnchor().fillMaxWidth()
          )
          ExposedDropdownMenu(expanded = expandedType, onDismissRequest = { expandedType = false }) {
            types.forEach { t ->
              DropdownMenuItem(text = { Text(t) }, onClick = { type = t; expandedType = false })
            }
          }
        }

        OutlinedTextField(
          value = model,
          onValueChange = { model = it },
          label = { Text("Model Identifier") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = endpoint,
          onValueChange = { endpoint = it },
          label = { Text("Endpoint Base URL") },
          modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
          value = apiKey,
          onValueChange = { apiKey = it },
          label = { Text("API Key") },
          modifier = Modifier.fillMaxWidth()
        )
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank() && endpoint.isNotBlank()) {
            onSubmit(name, type, endpoint, apiKey, model)
          }
        },
        enabled = name.isNotBlank() && endpoint.isNotBlank()
      ) {
        Text("Add Provider")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
