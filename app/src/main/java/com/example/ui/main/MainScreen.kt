package com.example.ui.main

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Autorenew
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.NavigationDrawerItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.ui.analytics.AnalyticsScreen
import com.example.ui.automation.AutomationScreen
import com.example.ui.content.ContentStudioScreen
import com.example.ui.dashboard.DashboardScreen
import com.example.ui.image.ImageStudioScreen
import com.example.ui.jobs.JobManagerScreen
import com.example.ui.memory.ContinuousMemoryScreen
import com.example.ui.navigation.MainDestination
import com.example.ui.navigation.SubRoutes
import com.example.ui.projects.ProjectDetailScreen
import com.example.ui.projects.ProjectsScreen
import com.example.ui.publishing.PublishingScreen
import com.example.ui.research.ResearchDetailScreen
import com.example.ui.research.ResearchScreen
import com.example.ui.settings.AiProvidersScreen
import com.example.ui.settings.ResearchSourcesScreen
import com.example.ui.settings.SettingsScreen
import com.example.ui.trends.TrendsScreen
import com.example.ui.viewmodel.FashionEngineViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(viewModel: FashionEngineViewModel) {
  val navController = rememberNavController()
  val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
  val scope = rememberCoroutineScope()
  val snackbarHostState = remember { SnackbarHostState() }

  val navBackStackEntry by navController.currentBackStackEntryAsState()
  val currentRoute = navBackStackEntry?.destination?.route

  val activeJobsCount by viewModel.activeJobsCount.collectAsState()
  val uiMessage by viewModel.uiMessage.collectAsState()

  LaunchedEffect(uiMessage) {
    uiMessage?.let { msg ->
      val text = when (msg) {
        is com.example.ui.viewmodel.UiMessage.Success -> msg.message
        is com.example.ui.viewmodel.UiMessage.Error -> "Error: ${msg.message}"
      }
      snackbarHostState.showSnackbar(text)
      viewModel.clearMessage()
    }
  }

  ModalNavigationDrawer(
    drawerState = drawerState,
    drawerContent = {
      ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface
      ) {
        Column(modifier = Modifier.padding(16.dp)) {
          Text(
            text = "FASHION ENGINE",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            letterSpacing = 2.sp
          )
          Text(
            text = "Intelligence & Operating System",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
          Spacer(modifier = Modifier.height(16.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(16.dp))

          // Primary Core
          MainDestination.bottomBarItems.forEach { dest ->
            NavigationDrawerItem(
              icon = { Icon(dest.icon, contentDescription = null) },
              label = { Text(dest.label) },
              selected = currentRoute == dest.route,
              onClick = {
                scope.launch { drawerState.close() }
                navController.navigate(dest.route) {
                  popUpTo(MainDestination.DASHBOARD.route)
                  launchSingleTop = true
                }
              },
              modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
          }

          Spacer(modifier = Modifier.height(8.dp))
          HorizontalDivider()
          Spacer(modifier = Modifier.height(8.dp))

          Text(
            text = "EXTENDED STUDIOS",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
          )

          MainDestination.secondaryItems.forEach { dest ->
            NavigationDrawerItem(
              icon = { Icon(dest.icon, contentDescription = null) },
              label = { Text(dest.label) },
              selected = currentRoute == dest.route,
              onClick = {
                scope.launch { drawerState.close() }
                navController.navigate(dest.route) {
                  launchSingleTop = true
                }
              },
              modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
            )
          }

          NavigationDrawerItem(
            icon = {
              BadgedBox(badge = {
                if (activeJobsCount > 0) {
                  Badge { Text(activeJobsCount.toString()) }
                }
              }) {
                Icon(Icons.Default.Task, contentDescription = null)
              }
            },
            label = { Text("Job Queue") },
            selected = currentRoute == SubRoutes.JOB_MANAGER,
            onClick = {
              scope.launch { drawerState.close() }
              navController.navigate(SubRoutes.JOB_MANAGER)
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
          )

          NavigationDrawerItem(
            icon = { Icon(Icons.Default.Psychology, contentDescription = null) },
            label = { Text("Style Memory") },
            selected = currentRoute == SubRoutes.MEMORY,
            onClick = {
              scope.launch { drawerState.close() }
              navController.navigate(SubRoutes.MEMORY)
            },
            modifier = Modifier.padding(NavigationDrawerItemDefaults.ItemPadding)
          )
        }
      }
    }
  ) {
    Scaffold(
      snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
      topBar = {
        // Show app bar on main routes
        CenterAlignedTopAppBar(
          title = {
            Text(
              text = "FASHION ENGINE",
              style = MaterialTheme.typography.titleMedium,
              fontWeight = FontWeight.Bold,
              letterSpacing = 2.sp,
              color = MaterialTheme.colorScheme.primary
            )
          },
          navigationIcon = {
            IconButton(onClick = { scope.launch { drawerState.open() } }) {
              Icon(Icons.Default.Menu, contentDescription = "Menu")
            }
          },
          actions = {
            IconButton(onClick = { navController.navigate(SubRoutes.JOB_MANAGER) }) {
              BadgedBox(badge = {
                if (activeJobsCount > 0) {
                  Badge { Text(activeJobsCount.toString()) }
                }
              }) {
                Icon(Icons.Default.Task, contentDescription = "Jobs")
              }
            }
          },
          colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
          )
        )
      },
      bottomBar = {
        NavigationBar(
          containerColor = MaterialTheme.colorScheme.surface,
          contentColor = MaterialTheme.colorScheme.primary
        ) {
          MainDestination.bottomBarItems.forEach { dest ->
            val isSelected = currentRoute == dest.route
            NavigationBarItem(
              icon = { Icon(dest.icon, contentDescription = dest.label) },
              label = { Text(dest.label, style = MaterialTheme.typography.labelSmall) },
              selected = isSelected,
              onClick = {
                navController.navigate(dest.route) {
                  popUpTo(MainDestination.DASHBOARD.route) { saveState = true }
                  launchSingleTop = true
                  restoreState = true
                }
              },
              colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.primary,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
              )
            )
          }
        }
      }
    ) { innerPadding ->
      Box(
        modifier = Modifier
          .fillMaxSize()
          .padding(innerPadding)
      ) {
        NavHost(
          navController = navController,
          startDestination = MainDestination.DASHBOARD.route
        ) {
          composable(MainDestination.DASHBOARD.route) {
            DashboardScreen(
              viewModel = viewModel,
              onNavigateToDestination = { dest ->
                navController.navigate(dest.route) { launchSingleTop = true }
              },
              onNavigateToSubRoute = { route ->
                navController.navigate(route)
              }
            )
          }

          composable(MainDestination.RESEARCH.route) {
            ResearchScreen(
              viewModel = viewModel,
              onNavigateToDetail = { runId ->
                navController.navigate(SubRoutes.researchDetail(runId))
              }
            )
          }

          composable(MainDestination.TRENDS.route) {
            TrendsScreen(
              viewModel = viewModel,
              onNavigateToImageStudio = {
                navController.navigate(MainDestination.IMAGE.route)
              },
              onNavigateToContent = {
                navController.navigate(MainDestination.CONTENT.route)
              }
            )
          }

          composable(MainDestination.CONTENT.route) {
            ContentStudioScreen(
              viewModel = viewModel,
              onNavigateToPublishing = {
                navController.navigate(MainDestination.PUBLISHING.route)
              }
            )
          }

          composable(MainDestination.PROJECTS.route) {
            ProjectsScreen(
              viewModel = viewModel,
              onNavigateToDetail = { id ->
                navController.navigate(SubRoutes.projectDetail(id))
              }
            )
          }

          composable(MainDestination.IMAGE.route) {
            ImageStudioScreen(
              viewModel = viewModel,
              onNavigateToSettings = {
                navController.navigate(MainDestination.SETTINGS.route)
              }
            )
          }

          composable(MainDestination.PUBLISHING.route) {
            PublishingScreen(viewModel = viewModel)
          }

          composable(MainDestination.ANALYTICS.route) {
            AnalyticsScreen(viewModel = viewModel)
          }

          composable(MainDestination.AUTOMATION.route) {
            AutomationScreen(viewModel = viewModel)
          }

          composable(MainDestination.SETTINGS.route) {
            SettingsScreen(
              viewModel = viewModel,
              onNavigateToSubRoute = { route ->
                navController.navigate(route)
              }
            )
          }

          // Sub-routes
          composable(
            route = SubRoutes.PROJECT_DETAIL,
            arguments = listOf(navArgument("projectId") { type = NavType.LongType })
          ) { backStackEntry ->
            val projectId = backStackEntry.arguments?.getLong("projectId") ?: 0L
            ProjectDetailScreen(
              projectId = projectId,
              viewModel = viewModel,
              onBack = { navController.popBackStack() },
              onNavigateToResearchDetail = { runId ->
                navController.navigate(SubRoutes.researchDetail(runId))
              }
            )
          }

          composable(
            route = SubRoutes.RESEARCH_DETAIL,
            arguments = listOf(navArgument("runId") { type = NavType.LongType })
          ) { backStackEntry ->
            val runId = backStackEntry.arguments?.getLong("runId") ?: 0L
            ResearchDetailScreen(
              runId = runId,
              viewModel = viewModel,
              onBack = { navController.popBackStack() },
              onNavigateToContent = {
                navController.navigate(MainDestination.CONTENT.route)
              },
              onNavigateToSettings = {
                navController.navigate(MainDestination.SETTINGS.route)
              },
              onNavigateToTrends = {
                navController.navigate(MainDestination.TRENDS.route)
              }
            )
          }

          composable(SubRoutes.JOB_MANAGER) {
            JobManagerScreen(
              viewModel = viewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable(SubRoutes.AI_PROVIDERS) {
            AiProvidersScreen(
              viewModel = viewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable(SubRoutes.RESEARCH_SOURCES) {
            ResearchSourcesScreen(
              viewModel = viewModel,
              onBack = { navController.popBackStack() }
            )
          }

          composable(SubRoutes.MEMORY) {
            ContinuousMemoryScreen(
              viewModel = viewModel,
              onBack = { navController.popBackStack() }
            )
          }
        }
      }
    }
  }
}
