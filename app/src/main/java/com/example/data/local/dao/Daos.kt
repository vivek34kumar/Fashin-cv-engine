package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
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
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
  @Query("SELECT * FROM projects ORDER BY updatedAt DESC")
  fun getAllProjects(): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects WHERE isArchived = 0 ORDER BY updatedAt DESC")
  fun getActiveProjects(): Flow<List<ProjectEntity>>

  @Query("SELECT * FROM projects WHERE id = :id")
  suspend fun getProjectById(id: Long): ProjectEntity?

  @Query("SELECT * FROM projects WHERE id = :id")
  fun observeProjectById(id: Long): Flow<ProjectEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProject(project: ProjectEntity): Long

  @Update
  suspend fun updateProject(project: ProjectEntity)

  @Query("DELETE FROM projects WHERE id = :id")
  suspend fun deleteProjectById(id: Long)

  @Query("SELECT COUNT(*) FROM projects WHERE isArchived = 0")
  fun countActiveProjects(): Flow<Int>
}

@Dao
interface AIProviderDao {
  @Query("SELECT * FROM ai_providers ORDER BY id ASC")
  fun getAllProviders(): Flow<List<AIProviderEntity>>

  @Query("SELECT * FROM ai_providers WHERE isEnabled = 1")
  fun getEnabledProviders(): Flow<List<AIProviderEntity>>

  @Query("SELECT * FROM ai_providers WHERE isDefault = 1 AND isEnabled = 1 LIMIT 1")
  suspend fun getDefaultProvider(): AIProviderEntity?

  @Query("SELECT * FROM ai_providers WHERE id = :id")
  suspend fun getProviderById(id: Long): AIProviderEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProvider(provider: AIProviderEntity): Long

  @Update
  suspend fun updateProvider(provider: AIProviderEntity)

  @Query("UPDATE ai_providers SET isDefault = 0 WHERE id != :providerId")
  suspend fun clearOtherDefaults(providerId: Long)

  @Query("DELETE FROM ai_providers WHERE id = :id")
  suspend fun deleteProviderById(id: Long)

  @Query("SELECT COUNT(*) FROM ai_providers WHERE isEnabled = 1")
  fun countEnabledProviders(): Flow<Int>
}

@Dao
interface ResearchSourceDao {
  @Query("SELECT * FROM research_sources ORDER BY id ASC")
  fun getAllSources(): Flow<List<ResearchSourceEntity>>

  @Query("SELECT * FROM research_sources WHERE isEnabled = 1")
  fun getEnabledSources(): Flow<List<ResearchSourceEntity>>

  @Query("SELECT * FROM research_sources WHERE id = :id")
  suspend fun getSourceById(id: Long): ResearchSourceEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertSource(source: ResearchSourceEntity): Long

  @Update
  suspend fun updateSource(source: ResearchSourceEntity)

  @Query("DELETE FROM research_sources WHERE id = :id")
  suspend fun deleteSourceById(id: Long)
}

@Dao
interface ResearchDao {
  @Query("SELECT * FROM research_runs ORDER BY createdAt DESC")
  fun getAllResearchRuns(): Flow<List<ResearchRunEntity>>

  @Query("SELECT * FROM research_runs WHERE projectId = :projectId ORDER BY createdAt DESC")
  fun getResearchRunsByProject(projectId: Long): Flow<List<ResearchRunEntity>>

  @Query("SELECT * FROM research_runs WHERE id = :id")
  suspend fun getResearchRunById(id: Long): ResearchRunEntity?

  @Query("SELECT * FROM research_runs WHERE id = :id")
  fun observeResearchRunById(id: Long): Flow<ResearchRunEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertResearchRun(run: ResearchRunEntity): Long

  @Update
  suspend fun updateResearchRun(run: ResearchRunEntity)

  @Query("DELETE FROM research_runs WHERE id = :id")
  suspend fun deleteResearchRunById(id: Long)

  @Query("SELECT * FROM research_results WHERE researchRunId = :runId LIMIT 1")
  suspend fun getResultByRunId(runId: Long): ResearchResultEntity?

  @Query("SELECT * FROM research_results WHERE researchRunId = :runId LIMIT 1")
  fun observeResultByRunId(runId: Long): Flow<ResearchResultEntity?>

  @Query("SELECT * FROM research_results WHERE projectId = :projectId ORDER BY createdAt DESC")
  fun getResultsByProject(projectId: Long): Flow<List<ResearchResultEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertResearchResult(result: ResearchResultEntity): Long

  @Query("SELECT COUNT(*) FROM research_runs")
  fun countAllRuns(): Flow<Int>

  @Query("SELECT COUNT(*) FROM research_runs WHERE status = 'Completed'")
  fun countCompletedRuns(): Flow<Int>
}

@Dao
interface TrendDao {
  @Query("SELECT * FROM trends WHERE isArchived = 0 ORDER BY lastUpdated DESC")
  fun getActiveTrends(): Flow<List<TrendEntity>>

  @Query("SELECT * FROM trends ORDER BY lastUpdated DESC")
  fun getAllTrends(): Flow<List<TrendEntity>>

  @Query("SELECT * FROM trends WHERE projectId = :projectId AND isArchived = 0 ORDER BY lastUpdated DESC")
  fun getTrendsByProject(projectId: Long): Flow<List<TrendEntity>>

  @Query("SELECT * FROM trends WHERE id = :id")
  suspend fun getTrendById(id: Long): TrendEntity?

  @Query("SELECT * FROM trends WHERE id = :id")
  fun observeTrendById(id: Long): Flow<TrendEntity?>

  @Query("SELECT * FROM trends WHERE researchRunId = :runId ORDER BY lastUpdated DESC")
  fun getTrendsByResearchRun(runId: Long): Flow<List<TrendEntity>>

  @Query("SELECT * FROM trends WHERE researchRunId = :runId ORDER BY lastUpdated DESC")
  suspend fun getTrendsListByResearchRun(runId: Long): List<TrendEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrend(trend: TrendEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertTrends(trends: List<TrendEntity>): List<Long>

  @Update
  suspend fun updateTrend(trend: TrendEntity)

  @Query("DELETE FROM trends WHERE id = :id")
  suspend fun deleteTrendById(id: Long)

  @Query("SELECT COUNT(*) FROM trends WHERE isSaved = 1 AND isArchived = 0")
  fun countSavedTrends(): Flow<Int>

  @Query("SELECT COUNT(*) FROM trends")
  fun countTotalTrends(): Flow<Int>
}

@Dao
interface ContentDao {
  @Query("SELECT * FROM content_items ORDER BY updatedAt DESC")
  fun getAllContent(): Flow<List<ContentItemEntity>>

  @Query("SELECT * FROM content_items WHERE projectId = :projectId ORDER BY updatedAt DESC")
  fun getContentByProject(projectId: Long): Flow<List<ContentItemEntity>>

  @Query("SELECT * FROM content_items WHERE id = :id")
  suspend fun getContentById(id: Long): ContentItemEntity?

  @Query("SELECT * FROM content_items WHERE id = :id")
  fun observeContentById(id: Long): Flow<ContentItemEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertContent(content: ContentItemEntity): Long

  @Update
  suspend fun updateContent(content: ContentItemEntity)

  @Query("DELETE FROM content_items WHERE id = :id")
  suspend fun deleteContentById(id: Long)

  @Query("SELECT COUNT(*) FROM content_items WHERE status = 'Draft'")
  fun countDraftContent(): Flow<Int>

  @Query("SELECT COUNT(*) FROM content_items WHERE status = 'Published'")
  fun countPublishedContent(): Flow<Int>

  @Query("SELECT COUNT(*) FROM content_items WHERE status = 'Approved'")
  fun countApprovedContent(): Flow<Int>

  @Query("SELECT COUNT(*) FROM content_items")
  fun countTotalContent(): Flow<Int>
}

@Dao
interface OutfitConceptDao {
  @Query("SELECT * FROM outfit_concepts ORDER BY createdAt DESC")
  fun getAllOutfitConcepts(): Flow<List<OutfitConceptEntity>>

  @Query("SELECT * FROM outfit_concepts WHERE trendId = :trendId ORDER BY createdAt DESC")
  fun getOutfitsByTrend(trendId: Long): Flow<List<OutfitConceptEntity>>

  @Query("SELECT * FROM outfit_concepts WHERE trendId = :trendId ORDER BY createdAt DESC")
  suspend fun getOutfitsListByTrend(trendId: Long): List<OutfitConceptEntity>

  @Query("SELECT * FROM outfit_concepts WHERE projectId = :projectId ORDER BY createdAt DESC")
  fun getOutfitsByProject(projectId: Long): Flow<List<OutfitConceptEntity>>

  @Query("SELECT * FROM outfit_concepts WHERE id = :id")
  suspend fun getOutfitById(id: Long): OutfitConceptEntity?

  @Query("SELECT * FROM outfit_concepts WHERE id = :id")
  fun observeOutfitById(id: Long): Flow<OutfitConceptEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOutfit(outfit: OutfitConceptEntity): Long

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOutfits(outfits: List<OutfitConceptEntity>): List<Long>

  @Update
  suspend fun updateOutfit(outfit: OutfitConceptEntity)

  @Query("DELETE FROM outfit_concepts WHERE id = :id")
  suspend fun deleteOutfitById(id: Long)

  @Query("SELECT COUNT(*) FROM outfit_concepts")
  fun countTotalOutfits(): Flow<Int>
}

@Dao
interface ImageDao {
  @Query("SELECT * FROM image_generations ORDER BY createdAt DESC")
  fun getAllImageGenerations(): Flow<List<ImageGenerationEntity>>

  @Query("SELECT * FROM image_generations WHERE projectId = :projectId ORDER BY createdAt DESC")
  fun getImageGenerationsByProject(projectId: Long): Flow<List<ImageGenerationEntity>>

  @Query("SELECT * FROM image_generations WHERE outfitConceptId = :outfitId ORDER BY createdAt DESC")
  fun getImageGenerationsByOutfit(outfitId: Long): Flow<List<ImageGenerationEntity>>

  @Query("SELECT * FROM image_generations WHERE trendId = :trendId ORDER BY createdAt DESC")
  fun getImageGenerationsByTrend(trendId: Long): Flow<List<ImageGenerationEntity>>

  @Query("SELECT * FROM image_generations WHERE id = :id")
  suspend fun getImageGenerationById(id: Long): ImageGenerationEntity?

  @Query("SELECT * FROM image_generations WHERE id = :id")
  fun observeImageGenerationById(id: Long): Flow<ImageGenerationEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertImageGeneration(item: ImageGenerationEntity): Long

  @Update
  suspend fun updateImageGeneration(item: ImageGenerationEntity)

  @Query("DELETE FROM image_generations WHERE id = :id")
  suspend fun deleteImageGenerationById(id: Long)

  @Query("SELECT COUNT(*) FROM image_generations")
  fun countTotalImageGenerations(): Flow<Int>
}

@Dao
interface PublishingDao {
  @Query("SELECT * FROM publishing_destinations ORDER BY id ASC")
  fun getAllDestinations(): Flow<List<PublishingDestinationEntity>>

  @Query("SELECT * FROM publishing_destinations WHERE isEnabled = 1")
  fun getEnabledDestinations(): Flow<List<PublishingDestinationEntity>>

  @Query("SELECT * FROM publishing_destinations WHERE id = :id")
  suspend fun getDestinationById(id: Long): PublishingDestinationEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertDestination(destination: PublishingDestinationEntity): Long

  @Update
  suspend fun updateDestination(destination: PublishingDestinationEntity)

  @Query("DELETE FROM publishing_destinations WHERE id = :id")
  suspend fun deleteDestinationById(id: Long)
}

@Dao
interface AutomationDao {
  @Query("SELECT * FROM automations ORDER BY id ASC")
  fun getAllAutomations(): Flow<List<AutomationEntity>>

  @Query("SELECT * FROM automations WHERE projectId = :projectId")
  fun getAutomationsByProject(projectId: Long): Flow<List<AutomationEntity>>

  @Query("SELECT * FROM automations WHERE isEnabled = 1")
  fun getEnabledAutomations(): Flow<List<AutomationEntity>>

  @Query("SELECT * FROM automations WHERE id = :id")
  suspend fun getAutomationById(id: Long): AutomationEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertAutomation(automation: AutomationEntity): Long

  @Update
  suspend fun updateAutomation(automation: AutomationEntity)

  @Query("DELETE FROM automations WHERE id = :id")
  suspend fun deleteAutomationById(id: Long)

  @Query("SELECT COUNT(*) FROM automations WHERE isEnabled = 1")
  fun countEnabledAutomations(): Flow<Int>

  @Query("SELECT COUNT(*) FROM automations")
  fun countTotalAutomations(): Flow<Int>
}

@Dao
interface JobDao {
  @Query("SELECT * FROM jobs ORDER BY startedTime DESC")
  fun getAllJobs(): Flow<List<JobEntity>>

  @Query("SELECT * FROM jobs WHERE projectId = :projectId ORDER BY startedTime DESC")
  fun getJobsByProject(projectId: Long): Flow<List<JobEntity>>

  @Query("SELECT * FROM jobs WHERE status IN ('Queued', 'Running') ORDER BY startedTime DESC")
  fun getActiveJobs(): Flow<List<JobEntity>>

  @Query("SELECT * FROM jobs WHERE id = :id")
  suspend fun getJobById(id: String): JobEntity?

  @Query("SELECT * FROM jobs WHERE id = :id")
  fun observeJobById(id: String): Flow<JobEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertJob(job: JobEntity)

  @Update
  suspend fun updateJob(job: JobEntity)

  @Query("DELETE FROM jobs WHERE id = :id")
  suspend fun deleteJobById(id: String)

  @Query("SELECT COUNT(*) FROM jobs WHERE status IN ('Queued', 'Running')")
  fun countActiveJobs(): Flow<Int>

  @Query("SELECT COUNT(*) FROM jobs WHERE status = 'Completed'")
  fun countCompletedJobs(): Flow<Int>

  @Query("SELECT COUNT(*) FROM jobs WHERE status = 'Failed'")
  fun countFailedJobs(): Flow<Int>
}

@Dao
interface MemoryDao {
  @Query("SELECT * FROM learning_memory ORDER BY createdAt DESC")
  fun getAllMemories(): Flow<List<LearningMemoryEntity>>

  @Query("SELECT * FROM learning_memory WHERE projectId = :projectId ORDER BY createdAt DESC")
  fun getMemoriesByProject(projectId: Long): Flow<List<LearningMemoryEntity>>

  @Query("SELECT * FROM learning_memory WHERE projectId IS NULL OR projectId = :projectId ORDER BY createdAt DESC")
  fun getApplicableMemories(projectId: Long): Flow<List<LearningMemoryEntity>>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMemory(memory: LearningMemoryEntity): Long

  @Query("DELETE FROM learning_memory WHERE id = :id")
  suspend fun deleteMemoryById(id: Long)

  @Query("DELETE FROM learning_memory WHERE projectId = :projectId")
  suspend fun resetProjectMemories(projectId: Long)
}

@Dao
interface SettingsDao {
  @Query("SELECT * FROM user_settings WHERE id = 1")
  fun getSettings(): Flow<UserSettingsEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertOrUpdateSettings(settings: UserSettingsEntity)
}
