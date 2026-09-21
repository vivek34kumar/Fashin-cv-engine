package com.example.ui.settings

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Source
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.navigation.SubRoutes
import com.example.ui.theme.ChampagneGold
import com.example.ui.viewmodel.FashionEngineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  viewModel: FashionEngineViewModel,
  onNavigateToSubRoute: (String) -> Unit
) {
  val userSettings by viewModel.userSettings.collectAsState()
  val allProviders by viewModel.allProviders.collectAsState()
  val allSources by viewModel.allSources.collectAsState()
  val allProjects by viewModel.allProjects.collectAsState()
  val allJobs by viewModel.allJobs.collectAsState()

  val defaultProvider = allProviders.find { it.isDefault }
  val activeSourcesCount = allSources.count { it.isEnabled }

  var expandedTheme by remember { mutableStateOf(false) }
  var expandedLang by remember { mutableStateOf(false) }

  val themeOptions = listOf("Dark", "Light", "System Default")
  val languageOptions = listOf("English", "French", "Italian", "Spanish", "German", "Japanese")

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("settings_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      EditorialSectionHeader(
        kicker = "OPERATING SYSTEM",
        title = "Settings & Integrations",
        subtitle = "Manage AI providers, content sources, and system preferences"
      )
    }

    // AI Providers Hub Entry
    item {
      EditorialCard(
        onClick = { onNavigateToSubRoute(SubRoutes.AI_PROVIDERS) },
        testTag = "settings_ai_providers_entry"
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.SmartToy, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("AI Intelligence Providers", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
              Text(
                text = "Default: ${defaultProvider?.name ?: "None"} (${defaultProvider?.model ?: "Not configured"})",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    // Research Sources Entry
    item {
      EditorialCard(
        onClick = { onNavigateToSubRoute(SubRoutes.RESEARCH_SOURCES) },
        testTag = "settings_sources_entry"
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Source, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Fashion Media & Runway Sources", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
              Text(
                text = "$activeSourcesCount active intelligence feeds connected",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    // Continuous Memory Entry
    item {
      EditorialCard(
        onClick = { onNavigateToSubRoute(SubRoutes.MEMORY) },
        testTag = "settings_memory_entry"
      ) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Psychology, contentDescription = null, tint = ChampagneGold, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
              Text("Continuous Fashion Memory", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
              Text(
                text = "Editorial guidelines, voice preferences, and aesthetics",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
          Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }
    }

    // Editorial Preferences
    item {
      EditorialCard(testTag = "settings_preferences_card") {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Text("Editorial Preferences", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

          // Theme selector
          ExposedDropdownMenuBox(expanded = expandedTheme, onExpandedChange = { expandedTheme = !expandedTheme }) {
            OutlinedTextField(
              value = userSettings?.themeMode ?: "System Default",
              onValueChange = {},
              readOnly = true,
              label = { Text("Display Palette") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedTheme) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedTheme, onDismissRequest = { expandedTheme = false }) {
              themeOptions.forEach { t ->
                DropdownMenuItem(
                  text = { Text(t) },
                  onClick = {
                    viewModel.updateThemeMode(t)
                    expandedTheme = false
                  }
                )
              }
            }
          }

          // Language selector
          ExposedDropdownMenuBox(expanded = expandedLang, onExpandedChange = { expandedLang = !expandedLang }) {
            OutlinedTextField(
              value = userSettings?.defaultLanguage ?: "English",
              onValueChange = {},
              readOnly = true,
              label = { Text("Primary Language") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedLang) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedLang, onDismissRequest = { expandedLang = false }) {
              languageOptions.forEach { l ->
                DropdownMenuItem(
                  text = { Text(l) },
                  onClick = {
                    viewModel.updateLanguage(l)
                    expandedLang = false
                  }
                )
              }
            }
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text("Intelligence Notifications", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
              Text("Alert when research or syndication finishes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
              checked = userSettings?.enableNotifications ?: true,
              onCheckedChange = { viewModel.toggleNotifications(it) }
            )
          }
        }
      }
    }

    // System Telemetry & Database Diagnostics
    item {
      EditorialCard(testTag = "settings_telemetry_card") {
        Column {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Storage, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Engine Architecture & Telemetry", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text("Platform: Android OS (Jetpack Compose & Material 3)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("Persistence: Room SQLite Database v1 (Offline-First)", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("Background Workers: Android Jetpack WorkManager", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Text("Active Portfolios: ${allProjects.size} | Total Jobs Logged: ${allJobs.size}", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          Spacer(modifier = Modifier.height(8.dp))
          Text("Build: Fashion Engine Enterprise v1.0.0 (2026.09 Production)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
        }
      }
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}
