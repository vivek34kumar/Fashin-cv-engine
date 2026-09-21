package com.example.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainDestination(
  val route: String,
  val label: String,
  val icon: ImageVector
) {
  DASHBOARD("dashboard", "Dashboard", Icons.Default.Dashboard),
  RESEARCH("research", "Research", Icons.Default.Explore),
  TRENDS("trends", "Trends", Icons.Default.TrendingUp),
  CONTENT("content", "Content", Icons.Default.EditNote),
  PROJECTS("projects", "Projects", Icons.Default.FolderSpecial),
  IMAGE("image_studio", "Image Studio", Icons.Default.PhotoLibrary),
  PUBLISHING("publishing", "Publishing", Icons.Default.Send),
  ANALYTICS("analytics", "Analytics", Icons.Default.BarChart),
  AUTOMATION("automation", "Automation", Icons.Default.Autorenew),
  SETTINGS("settings", "Settings", Icons.Default.Settings);

  companion object {
    val bottomBarItems = listOf(DASHBOARD, RESEARCH, TRENDS, CONTENT, PROJECTS)
    val secondaryItems = listOf(IMAGE, PUBLISHING, ANALYTICS, AUTOMATION, SETTINGS)
  }
}

object SubRoutes {
  const val PROJECT_DETAIL = "project_detail/{projectId}"
  const val RESEARCH_DETAIL = "research_detail/{runId}"
  const val JOB_MANAGER = "job_manager"
  const val AI_PROVIDERS = "ai_providers"
  const val RESEARCH_SOURCES = "research_sources"
  const val MEMORY = "learning_memory"

  fun projectDetail(id: Long) = "project_detail/$id"
  fun researchDetail(id: Long) = "research_detail/$id"
}
