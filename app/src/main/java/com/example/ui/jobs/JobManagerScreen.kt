package com.example.ui.jobs

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.entity.JobEntity
import com.example.ui.components.EditorialCard
import com.example.ui.components.EditorialSectionHeader
import com.example.ui.components.EmptyStateView
import com.example.ui.components.HauteButton
import com.example.ui.components.StatusBadge
import com.example.ui.viewmodel.FashionEngineViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun JobManagerScreen(
  viewModel: FashionEngineViewModel,
  onBack: () -> Unit
) {
  val allJobs by viewModel.allJobs.collectAsState()
  var selectedStatus by remember { mutableStateOf("All") }

  val statusOptions = listOf("All", "Running", "Queued", "Completed", "Failed", "Cancelled")

  val filteredJobs = when (selectedStatus) {
    "All" -> allJobs
    else -> allJobs.filter { it.status.equals(selectedStatus, ignoreCase = true) }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text("Job Queue & Execution Engine", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        .testTag("job_manager_screen"),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      item {
        EditorialSectionHeader(
          kicker = "REAL-TIME WORKERS",
          title = "Background Orchestration",
          subtitle = "${allJobs.size} operational jobs logged in WorkManager & SQLite"
        )
      }

      item {
        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          items(statusOptions) { status ->
            FilterChip(
              selected = selectedStatus == status,
              onClick = { selectedStatus = status },
              label = { Text(status, style = MaterialTheme.typography.labelSmall) }
            )
          }
        }
      }

      if (filteredJobs.isEmpty()) {
        item {
          EmptyStateView(
            title = if (selectedStatus == "All") "Job Queue is Empty" else "No $selectedStatus Jobs Found",
            description = "Background tasks like research synthesis, image rendering, and sync will appear here.",
            icon = Icons.Default.Task
          )
        }
      } else {
        items(filteredJobs) { job ->
          JobDetailCard(
            job = job,
            onCancel = { viewModel.cancelJob(job.id) },
            onRetry = { viewModel.retryJob(job.id) }
          )
        }
      }

      item {
        Spacer(modifier = Modifier.height(32.dp))
      }
    }
  }
}

@Composable
fun JobDetailCard(
  job: JobEntity,
  onCancel: () -> Unit,
  onRetry: () -> Unit
) {
  val startStr = SimpleDateFormat("MMM d, yyyy • HH:mm:ss", Locale.getDefault()).format(Date(job.startedTime))
  val durationStr = if (job.finishedTime != null) {
    val diff = (job.finishedTime - job.startedTime) / 1000
    "${diff}s"
  } else "Running..."

  EditorialCard(testTag = "job_card_${job.id}") {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = job.type.uppercase(),
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.primary,
          fontWeight = FontWeight.Bold
        )
        StatusBadge(status = job.status)
      }

      Spacer(modifier = Modifier.height(6.dp))
      Text(
        text = job.details ?: "Job ID: ${job.id}",
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold
      )

      Spacer(modifier = Modifier.height(4.dp))
      Text(
        text = "Started: $startStr • Duration: $durationStr",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )

      if (!job.errorMessage.isNullOrBlank()) {
        Spacer(modifier = Modifier.height(6.dp))
        Text(
          text = "Failure Reason: ${job.errorMessage}",
          style = MaterialTheme.typography.labelSmall,
          color = MaterialTheme.colorScheme.error
        )
      }

      Spacer(modifier = Modifier.height(8.dp))
      LinearProgressIndicator(
        progress = { job.progress / 100f },
        modifier = Modifier
          .fillMaxWidth()
          .height(4.dp)
          .clip(RoundedCornerShape(2.dp)),
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.surfaceVariant
      )

      if (job.status == "Running") {
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          HauteButton(
            text = "Cancel Job",
            onClick = onCancel,
            icon = Icons.Default.Cancel,
            isPrimary = false,
            modifier = Modifier.height(34.dp)
          )
        }
      } else if (job.status == "Failed") {
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          HauteButton(
            text = "Retry Job",
            onClick = onRetry,
            icon = Icons.Default.Refresh,
            isPrimary = true,
            modifier = Modifier.height(34.dp)
          )
        }
      }
    }
  }
}
