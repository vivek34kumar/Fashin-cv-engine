package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.BuildConfig
import com.example.data.local.dao.AIProviderDao
import com.example.data.local.dao.AutomationDao
import com.example.data.local.dao.ContentDao
import com.example.data.local.dao.ImageDao
import com.example.data.local.dao.JobDao
import com.example.data.local.dao.MemoryDao
import com.example.data.local.dao.OutfitConceptDao
import com.example.data.local.dao.ProjectDao
import com.example.data.local.dao.PublishingDao
import com.example.data.local.dao.ResearchDao
import com.example.data.local.dao.ResearchSourceDao
import com.example.data.local.dao.SettingsDao
import com.example.data.local.dao.TrendDao
import com.example.data.local.entity.AIProviderEntity
import com.example.data.local.entity.AutomationEntity
import com.example.data.local.entity.ContentItemEntity
import com.example.data.local.entity.ImageGenerationEntity
import com.example.data.local.entity.JobEntity
import com.example.data.local.entity.LearningMemoryEntity
import com.example.data.local.entity.OutfitConceptEntity
import com.example.data.local.entity.ProjectEntity
import com.example.data.local.entity.PublishingDestinationEntity
import com.example.data.local.entity.ResearchResultEntity
import com.example.data.local.entity.ResearchRunEntity
import com.example.data.local.entity.ResearchSourceEntity
import com.example.data.local.entity.TrendEntity
import com.example.data.local.entity.UserSettingsEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
  entities = [
    ProjectEntity::class,
    AIProviderEntity::class,
    ResearchSourceEntity::class,
    ResearchRunEntity::class,
    ResearchResultEntity::class,
    TrendEntity::class,
    OutfitConceptEntity::class,
    ContentItemEntity::class,
    ImageGenerationEntity::class,
    PublishingDestinationEntity::class,
    AutomationEntity::class,
    JobEntity::class,
    LearningMemoryEntity::class,
    UserSettingsEntity::class
  ],
  version = 2,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun projectDao(): ProjectDao
  abstract fun aiProviderDao(): AIProviderDao
  abstract fun researchSourceDao(): ResearchSourceDao
  abstract fun researchDao(): ResearchDao
  abstract fun trendDao(): TrendDao
  abstract fun outfitConceptDao(): OutfitConceptDao
  abstract fun contentDao(): ContentDao
  abstract fun imageDao(): ImageDao
  abstract fun publishingDao(): PublishingDao
  abstract fun automationDao(): AutomationDao
  abstract fun jobDao(): JobDao
  abstract fun memoryDao(): MemoryDao
  abstract fun settingsDao(): SettingsDao

  companion object {
    @Volatile
    private var INSTANCE: AppDatabase? = null

    fun getDatabase(context: Context): AppDatabase {
      return INSTANCE ?: synchronized(this) {
        val instance = Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "fashion_engine_db"
        )
          .fallbackToDestructiveMigration()
          .addCallback(object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
              super.onCreate(db)
              // Populate initial default configurations
              CoroutineScope(Dispatchers.IO).launch {
                val dbInstance = getDatabase(context)
                prepopulateDatabase(dbInstance)
              }
            }
          })
          .build()
        INSTANCE = instance
        instance
      }
    }

    private suspend fun prepopulateDatabase(db: AppDatabase) {
      // Pre-seed default settings
      db.settingsDao().insertOrUpdateSettings(
        UserSettingsEntity(
          id = 1,
          themeMode = "SYSTEM",
          defaultLanguage = "English",
          enableNotifications = true,
          dataRetentionDays = 90
        )
      )

      // Pre-seed default AI Provider templates (NOT marked connected until tested/configured)
      val geminiKey = try {
        BuildConfig.GEMINI_API_KEY
      } catch (e: Exception) {
        ""
      }
      val hasValidGeminiKey = geminiKey.isNotEmpty() && geminiKey != "MY_GEMINI_API_KEY"

      db.aiProviderDao().insertProvider(
        AIProviderEntity(
          name = "Google Gemini",
          providerType = "GEMINI",
          apiEndpoint = "https://generativelanguage.googleapis.com",
          apiKey = if (hasValidGeminiKey) geminiKey else "",
          model = "gemini-3.5-flash",
          isEnabled = true,
          isDefault = true,
          connectionStatus = "NOT_TESTED",
          statusMessage = if (hasValidGeminiKey) "API key detected in configuration" else "API key required"
        )
      )

      db.aiProviderDao().insertProvider(
        AIProviderEntity(
          name = "OpenAI",
          providerType = "OPENAI",
          apiEndpoint = "https://api.openai.com/v1",
          apiKey = "",
          model = "gpt-4o",
          isEnabled = false,
          isDefault = false,
          connectionStatus = "NOT_TESTED",
          statusMessage = "Not configured"
        )
      )

      db.aiProviderDao().insertProvider(
        AIProviderEntity(
          name = "DeepSeek",
          providerType = "DEEPSEEK",
          apiEndpoint = "https://api.deepseek.com/v1",
          apiKey = "",
          model = "deepseek-chat",
          isEnabled = false,
          isDefault = false,
          connectionStatus = "NOT_TESTED",
          statusMessage = "Not configured"
        )
      )

      db.aiProviderDao().insertProvider(
        AIProviderEntity(
          name = "NVIDIA NIM",
          providerType = "NVIDIA",
          apiEndpoint = "https://integrate.api.nvidia.com/v1",
          apiKey = "",
          model = "meta/llama-3.1-70b-instruct",
          isEnabled = false,
          isDefault = false,
          connectionStatus = "NOT_TESTED",
          statusMessage = "Not configured"
        )
      )

      // Pre-seed research sources
      db.researchSourceDao().insertSource(
        ResearchSourceEntity(
          name = "Vogue Runway Index",
          urlOrEndpoint = "https://www.vogue.com/fashion-shows",
          type = "Runway Report",
          isEnabled = true,
          status = "Active",
          notes = "Global haute-couture and ready-to-wear seasonal runways."
        )
      )

      db.researchSourceDao().insertSource(
        ResearchSourceEntity(
          name = "WWD Fashion Wire",
          urlOrEndpoint = "https://wwd.com/business-news",
          type = "Market API",
          isEnabled = true,
          status = "Active",
          notes = "Luxury executive business intelligence and retail retail data."
        )
      )

      db.researchSourceDao().insertSource(
        ResearchSourceEntity(
          name = "Highsnobiety Trend Archive",
          urlOrEndpoint = "https://www.highsnobiety.com/style",
          type = "Fashion Feed / RSS",
          isEnabled = true,
          status = "Active",
          notes = "Contemporary streetwear, drops, and youth subcultures."
        )
      )

      // Pre-seed standard publishing destination template
      db.publishingDao().insertDestination(
        PublishingDestinationEntity(
          name = "Main WordPress Editorial",
          type = "WordPress",
          endpoint = "https://editorial.fashionengine.internal/wp-json/wp/v2/posts",
          credentials = "",
          status = "Inactive",
          isEnabled = false
        )
      )
    }
  }
}
