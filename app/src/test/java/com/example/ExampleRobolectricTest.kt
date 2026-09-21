package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.entity.ProjectEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Fashion Engine", appName)
  }

  @Test
  fun `database stores and retrieves fashion project`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val db = AppDatabase.getDatabase(context)
    val testProject = ProjectEntity(
      name = "Test Haute Couture",
      description = "Automated test portfolio",
      fashionCategory = "Haute Couture",
      targetAudience = "Global Luxury",
      region = "Paris",
      language = "French",
      brandStyleDirection = "Avant-Garde"
    )
    val id = db.projectDao().insertProject(testProject)
    val retrieved = db.projectDao().getProjectById(id)
    assertNotNull(retrieved)
    assertEquals("Test Haute Couture", retrieved?.name)
  }
}

