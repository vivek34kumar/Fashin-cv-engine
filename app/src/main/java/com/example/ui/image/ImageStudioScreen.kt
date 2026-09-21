package com.example.ui.image

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.local.entity.ImageGenerationEntity
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HauteButton
import com.example.ui.components.StatusBadge
import com.example.ui.navigation.MainDestination
import com.example.ui.theme.AccentAmber
import com.example.ui.viewmodel.FashionEngineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageStudioScreen(
  viewModel: FashionEngineViewModel,
  onNavigateToSettings: () -> Unit
) {
  val allImages by viewModel.allImages.collectAsState()
  val allProviders by viewModel.allProviders.collectAsState()
  val activeProjects by viewModel.activeProjects.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  val defaultProvider = allProviders.find { it.isDefault && it.isEnabled }
  val hasConfiguredImageProvider = defaultProvider != null && defaultProvider.apiKey.isNotBlank()

  var prompt by remember { mutableStateOf("") }
  var negativePrompt by remember { mutableStateOf("") }
  var style by remember { mutableStateOf("Haute Couture Editorial") }
  var aspectRatio by remember { mutableStateOf("1:1") }
  var resolution by remember { mutableStateOf("1024x1024") }
  var numberOfImages by remember { mutableStateOf(1) }
  var selectedProjectId by remember { mutableStateOf<Long?>(null) }

  val styles = listOf(
    "Haute Couture Editorial",
    "Streetstyle 35mm",
    "Minimal Lookbook",
    "Studio Flatlay",
    "Avant-Garde Runway",
    "Vintage Analog 1990s"
  )
  val aspectRatios = listOf("1:1", "3:4", "9:16", "16:9", "4:3")
  val resolutions = listOf("1024x1024", "1080x1350", "1920x1080", "512x512")

  var expandedStyle by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = Modifier
      .fillMaxSize()
      .padding(horizontal = 16.dp)
      .testTag("image_studio_screen"),
    verticalArrangement = Arrangement.spacedBy(14.dp)
  ) {
    item {
      EditorialSectionHeader(
        kicker = "CREATIVE DIRECTION",
        title = "Image Studio",
        subtitle = "Generate high-fashion visuals, lookbooks, and concept boards"
      )
    }

    // Provider check warning if not configured
    if (!hasConfiguredImageProvider) {
      item {
        EditorialCard(testTag = "image_provider_warning") {
          Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.Warning, contentDescription = null, tint = AccentAmber)
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "Image Provider Not Configured",
                style = MaterialTheme.typography.titleMedium,
                color = AccentAmber,
                fontWeight = FontWeight.SemiBold
              )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
              text = "An active AI Provider with image capabilities is required to render high-fashion assets. Set up a provider key in Settings.",
              style = MaterialTheme.typography.bodyMedium,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(12.dp))
            HauteButton(
              text = "Configure AI Provider",
              onClick = onNavigateToSettings,
              icon = Icons.Default.Settings,
              isPrimary = false
            )
          }
        }
      }
    }

    // Generator Form Card
    item {
      EditorialCard(testTag = "image_generator_form") {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(
            text = "Visual Generation Prompt",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
          )

          OutlinedTextField(
            value = prompt,
            onValueChange = { prompt = it },
            label = { Text("Fashion Prompt") },
            placeholder = { Text("e.g., Architectural wool trench coat, slate grey, sharp lapels, model in motion, editorial lighting") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_image_prompt"),
            minLines = 3
          )

          OutlinedTextField(
            value = negativePrompt,
            onValueChange = { negativePrompt = it },
            label = { Text("Negative Prompt (Optional)") },
            placeholder = { Text("e.g., blurry, oversaturated, deformed, cartoonish") },
            modifier = Modifier.fillMaxWidth()
          )

          // Style Dropdown
          ExposedDropdownMenuBox(
            expanded = expandedStyle,
            onExpandedChange = { expandedStyle = !expandedStyle }
          ) {
            OutlinedTextField(
              value = style,
              onValueChange = {},
              readOnly = true,
              label = { Text("Aesthetic Style") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStyle) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(
              expanded = expandedStyle,
              onDismissRequest = { expandedStyle = false }
            ) {
              styles.forEach { s ->
                DropdownMenuItem(
                  text = { Text(s) },
                  onClick = {
                    style = s
                    expandedStyle = false
                  }
                )
              }
            }
          }

          // Aspect Ratio Selector
          Column {
            Text(
              text = "Aspect Ratio:",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              aspectRatios.forEach { ar ->
                FilterChip(
                  selected = aspectRatio == ar,
                  onClick = { aspectRatio = ar },
                  label = { Text(ar, style = MaterialTheme.typography.labelSmall) }
                )
              }
            }
          }

          // Number of Images
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = "Image Count: $numberOfImages",
              style = MaterialTheme.typography.bodyMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              (1..4).forEach { count ->
                FilterChip(
                  selected = numberOfImages == count,
                  onClick = { numberOfImages = count },
                  label = { Text("$count", style = MaterialTheme.typography.labelSmall) }
                )
              }
            }
          }

          Spacer(modifier = Modifier.height(4.dp))
          HauteButton(
            text = "Generate Visuals",
            onClick = {
              viewModel.requestImageGeneration(
                prompt = prompt,
                negativePrompt = negativePrompt.ifBlank { null },
                style = style,
                aspectRatio = aspectRatio,
                resolution = resolution,
                numberOfImages = numberOfImages,
                projectId = selectedProjectId,
                relatedContentId = null
              )
            },
            icon = Icons.Default.AutoAwesome,
            isPrimary = true,
            isLoading = isLoading,
            enabled = prompt.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
            testTag = "submit_image_generation"
          )
        }
      }
    }

    // Past Generations Gallery
    item {
      EditorialSectionHeader(
        kicker = "ARCHIVE",
        title = "Generated Visual Gallery",
        subtitle = "${allImages.size} visual generation requests recorded"
      )
    }

    if (allImages.isEmpty()) {
      item {
        EmptyStateView(
          title = "No Visual Assets Generated",
          description = "Use the generator above to produce lookbook imagery and visual inspiration.",
          icon = Icons.Default.PhotoLibrary
        )
      }
    } else {
      items(allImages) { imgGen ->
        ImageGenerationCard(
          imgGen = imgGen,
          onSaveImage = { path ->
            viewModel.saveImageToPhone(path, "Look_${imgGen.id}")
          }
        )
      }
    }

    item {
      Spacer(modifier = Modifier.height(32.dp))
    }
  }
}

@Composable
fun ImageGenerationCard(
  imgGen: ImageGenerationEntity,
  onSaveImage: (String) -> Unit = {}
) {
  val dateStr = SimpleDateFormat("MMM d, yyyy • HH:mm", Locale.getDefault()).format(Date(imgGen.createdAt))
  val imagePaths = imgGen.resultImagePaths?.split(",")?.filter { it.isNotBlank() } ?: emptyList()

  EditorialCard(testTag = "image_item_${imgGen.id}") {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = imgGen.style.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )
        StatusBadge(status = imgGen.status)
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = imgGen.prompt,
        style = MaterialTheme.typography.bodyLarge,
        fontWeight = FontWeight.Medium,
        color = MaterialTheme.colorScheme.onBackground
      )

      Spacer(modifier = Modifier.height(4.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = "Ratio: ${imgGen.aspectRatio} • ${imgGen.resolution} • $dateStr",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (imgGen.trendId != null || imgGen.outfitConceptId != null) {
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
          ) {
            Text(
              text = if (imgGen.outfitConceptId != null) "Outfit Asset" else "Trend Asset",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
          }
        }
      }

      if (!imgGen.errorMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Error: ${imgGen.errorMessage}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.error
        )
      }

      // Display rendered images if completed and URLs are valid
      if (imagePaths.isNotEmpty()) {
        Spacer(modifier = Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
          imagePaths.forEach { path ->
            Column(modifier = Modifier.fillMaxWidth()) {
              AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                  .data(path)
                  .crossfade(true)
                  .build(),
                contentDescription = imgGen.prompt,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                  .fillMaxWidth()
                  .height(260.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(MaterialTheme.colorScheme.surfaceVariant)
              )
              Spacer(modifier = Modifier.height(6.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
              ) {
                HauteButton(
                  text = "Save to Phone",
                  onClick = { onSaveImage(path) },
                  icon = Icons.Default.Download,
                  isPrimary = false,
                  modifier = Modifier.height(36.dp)
                )
              }
            }
          }
        }
      }
    }
  }
}
