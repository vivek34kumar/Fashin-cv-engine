package com.example.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.local.AppDatabase
import com.example.data.local.entity.JobEntity
import kotlinx.coroutines.delay
import java.util.UUID

class FashionAutomationWorker(
  appContext: Context,
  workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

  override suspend fun doWork(): Result {
    val database = AppDatabase.getDatabase(applicationContext)
    val automationDao = database.automationDao()
    val jobDao = database.jobDao()

    val automationId = inputData.getLong("AUTOMATION_ID", -1L)
    val automationName = inputData.getString("AUTOMATION_NAME") ?: "System Automation"

    val jobId = "AUTO-" + UUID.randomUUID().toString().take(8).uppercase()

    // Register active job in database
    jobDao.insertJob(
      JobEntity(
        id = jobId,
        type = "Automation Sync",
        status = "Running",
        progress = 10,
        startedTime = System.currentTimeMillis(),
        details = "Executing background automation: $automationName"
      )
    )

    return try {
      setProgress(workDataOf("PROGRESS" to 25))
      jobDao.updateJob(
        JobEntity(
          id = jobId,
          type = "Automation Sync",
          status = "Running",
          progress = 50,
          startedTime = System.currentTimeMillis(),
          details = "Running pipeline checks for $automationName"
        )
      )

      // Simulate step-wise pipeline execution with safety delay
      delay(1000)

      setProgress(workDataOf("PROGRESS" to 100))

      if (automationId != -1L) {
        val auto = automationDao.getAutomationById(automationId)
        if (auto != null) {
          automationDao.updateAutomation(
            auto.copy(
              lastRun = System.currentTimeMillis(),
              status = "Idle"
            )
          )
        }
      }

      jobDao.updateJob(
        JobEntity(
          id = jobId,
          type = "Automation Sync",
          status = "Completed",
          progress = 100,
          finishedTime = System.currentTimeMillis(),
          details = "Successfully executed $automationName"
        )
      )

      Result.success()
    } catch (e: Exception) {
      jobDao.updateJob(
        JobEntity(
          id = jobId,
          type = "Automation Sync",
          status = "Failed",
          progress = 50,
          errorMessage = e.localizedMessage ?: "Automation pipeline error",
          finishedTime = System.currentTimeMillis()
        )
      )
      if (runAttemptCount < 3) {
        Result.retry()
      } else {
        Result.failure()
      }
    }
  }
}
