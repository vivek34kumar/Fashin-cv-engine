package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val description: String = "",
  val fashionCategory: String = "Streetwear",
  val targetAudience: String = "Gen-Z & Millennials",
  val region: String = "Global",
  val language: String = "English",
  val brandStyleDirection: String = "Haute Minimalist",
  val isArchived: Boolean = false,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "ai_providers")
data class AIProviderEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val providerType: String = "GEMINI", // GEMINI, OPENAI, DEEPSEEK, NVIDIA, CUSTOM
  val apiEndpoint: String = "",
  val apiKey: String = "",
  val model: String = "gemini-3.5-flash",
  val isEnabled: Boolean = true,
  val isDefault: Boolean = false,
  val lastTested: Long? = null,
  val connectionStatus: String = "NOT_TESTED", // NOT_TESTED, CONNECTED, FAILED
  val statusMessage: String? = null
)

@Entity(tableName = "research_sources")
data class ResearchSourceEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val urlOrEndpoint: String,
  val apiKey: String? = null,
  val type: String = "Fashion Feed / RSS", // Fashion Feed / RSS, Trend Archive, Market API, Runway Report
  val isEnabled: Boolean = true,
  val status: String = "Active", // Active, Inactive, Error, Syncing
  val lastSync: Long? = null,
  val notes: String? = null
)

@Entity(tableName = "research_runs")
data class ResearchRunEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val projectId: Long? = null,
  val topic: String,
  val category: String = "Luxury",
  val targetAudience: String = "Contemporary Consumer",
  val region: String = "North America & Europe",
  val language: String = "English",
  val timeRange: String = "Past 30 Days",
  val researchSources: String = "Fashion Feed, Trend Index",
  val status: String = "Draft", // Draft, Queued, Running, Completed, Failed, Cancelled
  val errorMessage: String? = null,
  val startedAt: Long? = null,
  val completedAt: Long? = null,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "research_results")
data class ResearchResultEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val researchRunId: Long,
  val projectId: Long? = null,
  val title: String,
  val summary: String,
  val keyFindings: String,
  val sources: String,
  val trendSignals: String,
  val keywords: String,
  val brands: String,
  val products: String,
  val marketObservations: String,
  val confidenceScore: Float = 0.88f,
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "trends")
data class TrendEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val projectId: Long? = null,
  val researchRunId: Long? = null,
  val name: String,
  val category: String = "Streetwear",
  val description: String,
  val keywords: String = "",
  val relatedBrands: String = "",
  val relatedProducts: String = "",
  val signals: String = "",
  val sources: String = "",
  val discoveryDate: Long = System.currentTimeMillis(),
  val lastUpdated: Long = System.currentTimeMillis(),
  val confidenceScore: Float = 0.85f,
  val status: String = "Emerging", // Emerging, Peaking, Declining, Evergreen, Niche
  val isSaved: Boolean = true,
  val isArchived: Boolean = false
)

@Entity(tableName = "outfit_concepts")
data class OutfitConceptEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val trendId: Long,
  val projectId: Long? = null,
  val title: String,
  val description: String,
  val garments: String = "", // Comma/line separated list of clothing items
  val accessories: String = "", // Footwear, bags, jewelry, eyewear
  val colorPalette: String = "", // Key tones, hex or names
  val materials: String = "", // Fabrics, textures (e.g., Raw silk, distressed denim)
  val stylingNotes: String = "", // Silhouette, layering, proportion
  val visualPrompt: String = "", // Tailored prompt for Image Studio
  val occasion: String = "Editorial / Streetwear",
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "content_items")
data class ContentItemEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val projectId: Long? = null,
  val title: String,
  val contentType: String = "Article", // Article, Blog, Fashion report, Trend report, Social media post, Social caption, Product description, Newsletter, Editorial, Campaign content
  val researchSourceId: Long? = null,
  val researchSourceName: String? = null,
  val relatedTrends: String = "",
  val outfitConceptId: Long? = null,
  val body: String = "",
  val status: String = "Draft", // Idea, Research, Outline, Draft, Review, Approved, Published, Archived
  val author: String = "Editorial Team",
  val aiProviderUsed: String? = null,
  val aiModelUsed: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "image_generations")
data class ImageGenerationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val projectId: Long? = null,
  val trendId: Long? = null,
  val outfitConceptId: Long? = null,
  val relatedContentId: Long? = null,
  val prompt: String,
  val negativePrompt: String? = null,
  val style: String = "Haute Couture Editorial",
  val aspectRatio: String = "1:1", // 1:1, 3:4, 9:16, 16:9, 4:3
  val resolution: String = "1024x1024",
  val numberOfImages: Int = 1, // 1 to 5
  val referenceImageUrl: String? = null,
  val status: String = "Queued", // Queued, Generating, Completed, Failed
  val resultImagePaths: String? = null,
  val errorMessage: String? = null,
  val createdAt: Long = System.currentTimeMillis(),
  val completedAt: Long? = null
)

@Entity(tableName = "publishing_destinations")
data class PublishingDestinationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val type: String = "WordPress", // WordPress, Headless CMS, Other
  val endpoint: String,
  val credentials: String = "",
  val status: String = "Active", // Active, Inactive, Connection Error
  val isEnabled: Boolean = true,
  val lastPublishedAt: Long? = null
)

@Entity(tableName = "automations")
data class AutomationEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val name: String,
  val description: String = "",
  val type: String = "Periodic research", // Periodic research, Trend synchronization, Content workflow jobs, Analytics updates, Publishing jobs
  val isEnabled: Boolean = false,
  val frequency: String = "Daily at 08:00",
  val lastRun: Long? = null,
  val nextRun: Long? = null,
  val status: String = "Idle", // Idle, Running, Failed, Paused
  val projectId: Long? = null
)

@Entity(tableName = "jobs")
data class JobEntity(
  @PrimaryKey val id: String, // e.g. "JOB-2026-XXXX"
  val type: String, // Research, Trend Sync, Content AI Gen, Image Gen, Publishing, Automation Sync
  val projectId: Long? = null,
  val projectName: String? = null,
  val status: String = "Queued", // Queued, Running, Completed, Failed, Cancelled
  val progress: Int = 0,
  val startedTime: Long = System.currentTimeMillis(),
  val finishedTime: Long? = null,
  val errorMessage: String? = null,
  val retryCount: Int = 0,
  val details: String? = null
)

@Entity(tableName = "learning_memory")
data class LearningMemoryEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val projectId: Long? = null,
  val category: String, // Preferred writing style, Preferred content format, Preferred fashion categories, Frequently used keywords, Content performance observations, User-approved style preferences, Project-specific preferences
  val key: String,
  val value: String,
  val source: String = "Explicit Preference", // Explicit Preference, Content Analysis, User Selection
  val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_settings")
data class UserSettingsEntity(
  @PrimaryKey val id: Int = 1,
  val themeMode: String = "SYSTEM", // SYSTEM, LIGHT, DARK
  val defaultLanguage: String = "English",
  val enableNotifications: Boolean = true,
  val dataRetentionDays: Int = 90
)
