package com.example.ui.trends

import androidx.compose.foundation.background
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
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import coil.compose.AsyncImage
import com.example.data.local.entity.OutfitConceptEntity
import com.example.data.local.entity.TrendEntity
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HauteButton
import com.example.ui.components.StatusBadge
import com.example.ui.theme.AccentEmerald
import com.example.ui.theme.ChampagneGold
import com.example.ui.viewmodel.FashionEngineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TrendsScreen(
  viewModel: FashionEngineViewModel,
  onNavigateToImageStudio: () -> Unit = {},
  onNavigateToContent: () -> Unit = {}
) {
  val activeTrends by viewModel.activeTrends.collectAsState()
  val allTrends by viewModel.allTrends.collectAsState()
  val allOutfits by viewModel.allOutfitConcepts.collectAsState()
  val allImages by viewModel.allImages.collectAsState()
  val activeProjects by viewModel.activeProjects.collectAsState()
  val isLoading by viewModel.isLoading.collectAsState()

  var selectedFilter by remember { mutableStateOf("All") }
  var showCreateDialog by remember { mutableStateOf(false) }

  val filterOptions = listOf("All", "Emerging", "Peaking", "Evergreen", "Niche", "Saved", "Archived")

  val displayedTrends = when (selectedFilter) {
    "All" -> activeTrends
    "Saved" -> activeTrends.filter { it.isSaved }
    "Archived" -> allTrends.filter { it.isArchived }
    else -> activeTrends.filter { it.status.equals(selectedFilter, ignoreCase = true) }
  }

  Scaffold(
    floatingActionButton = {
      FloatingActionButton(
        onClick = { showCreateDialog = true },
        modifier = Modifier.testTag("fab_new_trend"),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary
      ) {
        Icon(Icons.Default.Add, contentDescription = "New Trend")
      }
    }
  ) { paddingValues ->
    LazyColumn(
      modifier = Modifier
        .fillMaxSize()
        .padding(paddingValues)
        .padding(horizontal = 16.dp)
        .testTag("trends_screen"),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        EditorialSectionHeader(
          kicker = "FORECASTING & RADAR",
          title = "Trend Intelligence Index",
          subtitle = "Monitor evolving aesthetics, silhouettes, and micro-movements"
        )
      }

      // Filter chips
      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(filterOptions) { filter ->
            FilterChip(
              selected = selectedFilter == filter,
              onClick = { selectedFilter = filter },
              label = { Text(filter, style = MaterialTheme.typography.labelSmall) },
              modifier = Modifier.testTag("trend_filter_$filter")
            )
          }
        }
      }

      if (displayedTrends.isEmpty()) {
        item {
          EmptyStateView(
            title = if (selectedFilter == "All") "No Trends Recorded" else "No $selectedFilter Trends Found",
            description = "Track fashion movements, consumer shifts, and design aesthetics.",
            icon = Icons.Default.TrendingUp,
            actionText = "Add New Trend",
            actionTestTag = "trends_empty_state_action",
            onActionClick = { showCreateDialog = true }
          )
        }
      } else {
        items(displayedTrends) { trend ->
          val outfitsForTrend = allOutfits.filter { it.trendId == trend.id }
          TrendItemCard(
            trend = trend,
            outfits = outfitsForTrend,
            allImages = allImages,
            isLoading = isLoading,
            onToggleSave = { viewModel.toggleSaveTrend(trend) },
            onToggleArchive = { viewModel.toggleArchiveTrend(trend) },
            onDelete = { viewModel.deleteTrend(trend.id) },
            onGenerateOutfits = { viewModel.generateOutfitConceptsForTrend(trend.id) },
            onDeleteOutfit = { outfitId -> viewModel.deleteOutfitConcept(outfitId) },
            onGenerateImageForOutfit = { outfit ->
              viewModel.requestImageGeneration(
                prompt = outfit.visualPrompt.ifBlank { "Fashion look for ${outfit.title}, ${outfit.garments}, ${outfit.colorPalette} style" },
                negativePrompt = "blurry, low quality, distorted anatomy, text watermark",
                style = "Haute Couture Editorial",
                aspectRatio = "3:4",
                resolution = "1024x1024",
                numberOfImages = 1,
                projectId = outfit.projectId ?: trend.projectId,
                trendId = trend.id,
                outfitConceptId = outfit.id
              )
              onNavigateToImageStudio()
            },
            onSaveImageToPhone = { path, title ->
              viewModel.saveImageToPhone(path, title)
            },
            onDraftContentFromOutfit = { outfit ->
              viewModel.createManualContent(
                title = "Lookbook Feature: ${outfit.title}",
                contentType = "Lookbook entry",
                projectId = outfit.projectId ?: trend.projectId,
                body = "Outfit Concept: ${outfit.title}\nTrend: ${trend.name}\n\nGarments:\n${outfit.garments}\n\nAccessories:\n${outfit.accessories}\n\nColor Palette:\n${outfit.colorPalette}\n\nMaterials:\n${outfit.materials}\n\nStyling Notes:\n${outfit.stylingNotes}",
                status = "Draft",
                relatedTrends = trend.name
              )
              onNavigateToContent()
            }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(64.dp))
      }
    }
  }

  if (showCreateDialog) {
    CreateTrendDialog(
      projects = activeProjects,
      onDismiss = { showCreateDialog = false },
      onSubmit = { name, category, desc, keywords, brands, products, signals, sources, status, projectId ->
        viewModel.createTrend(
          name = name,
          category = category,
          description = desc,
          keywords = keywords,
          relatedBrands = brands,
          relatedProducts = products,
          signals = signals,
          sources = sources,
          status = status,
          projectId = projectId
        )
        showCreateDialog = false
      }
    )
  }
}

@Composable
fun TrendItemCard(
  trend: TrendEntity,
  outfits: List<OutfitConceptEntity> = emptyList(),
  allImages: List<com.example.data.local.entity.ImageGenerationEntity> = emptyList(),
  isLoading: Boolean = false,
  onToggleSave: () -> Unit,
  onToggleArchive: () -> Unit,
  onDelete: () -> Unit,
  onGenerateOutfits: () -> Unit = {},
  onDeleteOutfit: (Long) -> Unit = {},
  onGenerateImageForOutfit: (OutfitConceptEntity) -> Unit = {},
  onSaveImageToPhone: (String, String) -> Unit = { _, _ -> },
  onDraftContentFromOutfit: (OutfitConceptEntity) -> Unit = {}
) {
  val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(trend.lastUpdated))
  var showOutfitsSection by remember { mutableStateOf(false) }

  EditorialCard(testTag = "trend_item_${trend.id}") {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text(
            text = trend.category.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Bold
          )
          Spacer(modifier = Modifier.width(8.dp))
          StatusBadge(status = trend.status)
        }

        Row {
          IconButton(onClick = onToggleSave, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = if (trend.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
              contentDescription = "Save",
              tint = if (trend.isSaved) ChampagneGold else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(18.dp)
            )
          }
          IconButton(onClick = onToggleArchive, modifier = Modifier.size(32.dp)) {
            Icon(
              imageVector = if (trend.isArchived) Icons.Default.Unarchive else Icons.Default.Archive,
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

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = trend.name,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onBackground,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = trend.description,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (trend.signals.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Key Signals: ${trend.signals}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      if (trend.relatedBrands.isNotBlank() || trend.relatedProducts.isNotBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          if (trend.relatedBrands.isNotBlank()) {
            Text(
              text = "Brands: ${trend.relatedBrands}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.primary,
              modifier = Modifier.weight(1f)
            )
          }
          if (trend.relatedProducts.isNotBlank()) {
            Text(
              text = "Hero: ${trend.relatedProducts}",
              style = MaterialTheme.typography.labelSmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(8.dp))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(10.dp),
          color = AccentEmerald.copy(alpha = 0.12f)
        ) {
          Text(
            text = "${(trend.confidenceScore * 100).toInt()}% Confidence",
            style = MaterialTheme.typography.labelSmall,
            color = AccentEmerald,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
          )
        }
        Text(
          text = "Updated: $dateStr",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }

      // Pipeline Action: Outfit Concepts Section
      Spacer(modifier = Modifier.height(12.dp))
      HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
      Spacer(modifier = Modifier.height(8.dp))

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(
            Icons.Default.Checkroom,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
          )
          Spacer(modifier = Modifier.width(6.dp))
          Text(
            text = "Outfit Concepts (${outfits.size})",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.primary
          )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
          if (outfits.isNotEmpty()) {
            IconButton(
              onClick = { showOutfitsSection = !showOutfitsSection },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                imageVector = if (showOutfitsSection) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                contentDescription = if (showOutfitsSection) "Collapse" else "Expand",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }
        }
      }

      if (outfits.isEmpty()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Translate this trend into wearable styling formulas, accessories, palettes, and visual prompts.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        HauteButton(
          text = "Generate Outfit Concepts",
          onClick = onGenerateOutfits,
          icon = Icons.Default.AutoAwesome,
          isPrimary = false,
          isLoading = isLoading,
          modifier = Modifier.fillMaxWidth(),
          testTag = "btn_generate_outfits_${trend.id}"
        )
      } else {
        if (!showOutfitsSection) {
          Spacer(modifier = Modifier.height(4.dp))
          Text(
            text = "${outfits.firstOrNull()?.title ?: ""} and ${outfits.size - 1} more concept(s)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        } else {
          Spacer(modifier = Modifier.height(8.dp))
          Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            outfits.forEach { outfit ->
              val outfitImages = allImages.filter { it.outfitConceptId == outfit.id }
              Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
              ) {
                Column(modifier = Modifier.padding(12.dp)) {
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                  ) {
                    Text(
                      text = outfit.title,
                      style = MaterialTheme.typography.titleSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.onSurface,
                      modifier = Modifier.weight(1f)
                    )
                    IconButton(
                      onClick = { onDeleteOutfit(outfit.id) },
                      modifier = Modifier.size(24.dp)
                    ) {
                      Icon(
                        Icons.Default.Delete,
                        contentDescription = "Remove outfit",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                      )
                    }
                  }

                  if (outfit.occasion.isNotBlank()) {
                    Text(
                      text = "Occasion: ${outfit.occasion}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.primary
                    )
                  }

                  Spacer(modifier = Modifier.height(4.dp))
                  Text(
                    text = outfit.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                  )

                  if (outfit.garments.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                      text = "Garments: ${outfit.garments}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurface
                    )
                  }

                  if (outfit.colorPalette.isNotBlank()) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                      text = "Colors: ${outfit.colorPalette}",
                      style = MaterialTheme.typography.labelSmall,
                      color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                  }

                  // Render generated images for this outfit if any
                  if (outfitImages.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                      text = "Generated Imagery (${outfitImages.size})",
                      style = MaterialTheme.typography.labelSmall,
                      fontWeight = FontWeight.Bold,
                      color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                      outfitImages.forEach { img ->
                        val paths = img.resultImagePaths?.split(",")?.filter { it.isNotBlank() } ?: emptyList()
                        paths.forEach { imgPath ->
                          Column(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                              model = imgPath,
                              contentDescription = outfit.title,
                              contentScale = androidx.compose.ui.layout.ContentScale.Crop,
                              modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.End
                            ) {
                              HauteButton(
                                text = "Save to Phone",
                                onClick = { onSaveImageToPhone(imgPath, outfit.title) },
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

                  Spacer(modifier = Modifier.height(8.dp))
                  Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                  ) {
                    HauteButton(
                      text = "Generate Image",
                      onClick = { onGenerateImageForOutfit(outfit) },
                      icon = Icons.Default.PhotoLibrary,
                      isPrimary = true,
                      isLoading = isLoading,
                      modifier = Modifier.weight(1f),
                      testTag = "btn_outfit_image_${outfit.id}"
                    )
                    HauteButton(
                      text = "Draft Content",
                      onClick = { onDraftContentFromOutfit(outfit) },
                      icon = Icons.Default.EditNote,
                      isPrimary = false,
                      modifier = Modifier.weight(1f),
                      testTag = "btn_outfit_content_${outfit.id}"
                    )
                  }
                }
              }
            }

            HauteButton(
              text = "Re-generate Outfits",
              onClick = onGenerateOutfits,
              icon = Icons.Default.AutoAwesome,
              isPrimary = false,
              isLoading = isLoading,
              modifier = Modifier.fillMaxWidth()
            )
          }
        }
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTrendDialog(
  projects: List<com.example.data.local.entity.ProjectEntity>,
  onDismiss: () -> Unit,
  onSubmit: (
    name: String,
    category: String,
    desc: String,
    keywords: String,
    brands: String,
    products: String,
    signals: String,
    sources: String,
    status: String,
    projectId: Long?
  ) -> Unit
) {
  var name by remember { mutableStateOf("") }
  var category by remember { mutableStateOf("Luxury") }
  var desc by remember { mutableStateOf("") }
  var keywords by remember { mutableStateOf("") }
  var brands by remember { mutableStateOf("") }
  var products by remember { mutableStateOf("") }
  var signals by remember { mutableStateOf("") }
  var sources by remember { mutableStateOf("Vogue Runway, WWD") }
  var status by remember { mutableStateOf("Emerging") }
  var selectedProjectId by remember { mutableStateOf<Long?>(null) }

  val categories = listOf(
    "Streetwear", "Luxury", "Casual", "Menswear", "Womenswear",
    "Footwear", "Accessories", "Beauty", "Sustainable Fashion",
    "Sportswear", "Denim", "Designer", "Avant-Garde"
  )
  val statusList = listOf("Emerging", "Peaking", "Declining", "Evergreen", "Niche")

  var expandedCat by remember { mutableStateOf(false) }
  var expandedStatus by remember { mutableStateOf(false) }

  AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text("Log New Trend Signal", style = MaterialTheme.typography.titleLarge) },
    text = {
      LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
          OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Trend Name") },
            placeholder = { Text("e.g., Structured Leather Trench") },
            modifier = Modifier
              .fillMaxWidth()
              .testTag("input_trend_name")
          )
        }

        item {
          ExposedDropdownMenuBox(expanded = expandedCat, onExpandedChange = { expandedCat = !expandedCat }) {
            OutlinedTextField(
              value = category,
              onValueChange = {},
              readOnly = true,
              label = { Text("Category") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCat) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedCat, onDismissRequest = { expandedCat = false }) {
              categories.forEach { cat ->
                DropdownMenuItem(text = { Text(cat) }, onClick = { category = cat; expandedCat = false })
              }
            }
          }
        }

        item {
          ExposedDropdownMenuBox(expanded = expandedStatus, onExpandedChange = { expandedStatus = !expandedStatus }) {
            OutlinedTextField(
              value = status,
              onValueChange = {},
              readOnly = true,
              label = { Text("Trend Velocity / Status") },
              trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedStatus) },
              modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
            )
            ExposedDropdownMenu(expanded = expandedStatus, onDismissRequest = { expandedStatus = false }) {
              statusList.forEach { s ->
                DropdownMenuItem(text = { Text(s) }, onClick = { status = s; expandedStatus = false })
              }
            }
          }
        }

        item {
          OutlinedTextField(
            value = desc,
            onValueChange = { desc = it },
            label = { Text("Trend Description") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 2
          )
        }

        item {
          OutlinedTextField(
            value = signals,
            onValueChange = { signals = it },
            label = { Text("Signals & Drivers") },
            placeholder = { Text("e.g., Heavy runway presence, thrift demand") },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = brands,
            onValueChange = { brands = it },
            label = { Text("Associated Brands") },
            placeholder = { Text("e.g., Bottega Veneta, Prada, The Row") },
            modifier = Modifier.fillMaxWidth()
          )
        }

        item {
          OutlinedTextField(
            value = products,
            onValueChange = { products = it },
            label = { Text("Hero Products / Silhouettes") },
            modifier = Modifier.fillMaxWidth()
          )
        }
      }
    },
    confirmButton = {
      Button(
        onClick = {
          if (name.isNotBlank()) {
            onSubmit(name, category, desc, keywords, brands, products, signals, sources, status, selectedProjectId)
          }
        },
        enabled = name.isNotBlank(),
        modifier = Modifier.testTag("submit_trend_button")
      ) {
        Text("Save Trend")
      }
    },
    dismissButton = {
      TextButton(onClick = onDismiss) { Text("Cancel") }
    }
  )
}
