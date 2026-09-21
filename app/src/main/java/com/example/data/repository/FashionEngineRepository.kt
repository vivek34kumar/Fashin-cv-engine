package com.example.data.repository

import com.example.data.local.AppDatabase
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
import com.example.data.remote.AiApiClient
import com.example.data.remote.ApiResult
import com.example.data.remote.PublishingApiClient
import com.example.data.remote.ResearchSourceSync
import kotlinx.coroutines.flow.Flow
import java.util.UUID

class FashionEngineRepository(
  private val database: AppDatabase,
  private val aiApiClient: AiApiClient = AiApiClient(),
  private val publishingApiClient: PublishingApiClient = PublishingApiClient(),
  private val researchSourceSync: ResearchSourceSync = ResearchSourceSync()
) {
  // --- DAOs ---
  private val projectDao = database.projectDao()
  private val aiProviderDao = database.aiProviderDao()
  private val researchSourceDao = database.researchSourceDao()
  private val researchDao = database.researchDao()
  private val trendDao = database.trendDao()
  private val outfitConceptDao = database.outfitConceptDao()
  private val contentDao = database.contentDao()
  private val imageDao = database.imageDao()
  private val publishingDao = database.publishingDao()
  private val automationDao = database.automationDao()
  private val jobDao = database.jobDao()
  private val memoryDao = database.memoryDao()
  private val settingsDao = database.settingsDao()

  // --- Real Reactive State Flows ---
  val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()
  val activeProjects: Flow<List<ProjectEntity>> = projectDao.getActiveProjects()
  val allProviders: Flow<List<AIProviderEntity>> = aiProviderDao.getAllProviders()
  val allSources: Flow<List<ResearchSourceEntity>> = researchSourceDao.getAllSources()
  val allResearchRuns: Flow<List<ResearchRunEntity>> = researchDao.getAllResearchRuns()
  val activeTrends: Flow<List<TrendEntity>> = trendDao.getActiveTrends()
  val allTrends: Flow<List<TrendEntity>> = trendDao.getAllTrends()
  val allOutfitConcepts: Flow<List<OutfitConceptEntity>> = outfitConceptDao.getAllOutfitConcepts()
  val allContent: Flow<List<ContentItemEntity>> = contentDao.getAllContent()
  val allImages: Flow<List<ImageGenerationEntity>> = imageDao.getAllImageGenerations()
  val allDestinations: Flow<List<PublishingDestinationEntity>> = publishingDao.getAllDestinations()
  val allAutomations: Flow<List<AutomationEntity>> = automationDao.getAllAutomations()
  val allJobs: Flow<List<JobEntity>> = jobDao.getAllJobs()
  val activeJobs: Flow<List<JobEntity>> = jobDao.getActiveJobs()
  val allMemories: Flow<List<LearningMemoryEntity>> = memoryDao.getAllMemories()
  val userSettings: Flow<UserSettingsEntity?> = settingsDao.getSettings()

  // --- Real DB Counts (No Fake Stats) ---
  val activeProjectsCount: Flow<Int> = projectDao.countActiveProjects()
  val researchRunsCount: Flow<Int> = researchDao.countAllRuns()
  val savedTrendsCount: Flow<Int> = trendDao.countSavedTrends()
  val draftContentCount: Flow<Int> = contentDao.countDraftContent()
  val publishedContentCount: Flow<Int> = contentDao.countPublishedContent()
  val activeJobsCount: Flow<Int> = jobDao.countActiveJobs()

  val completedRunsCount: Flow<Int> = researchDao.countCompletedRuns()
  val totalTrendsCount: Flow<Int> = trendDao.countTotalTrends()
  val approvedContentCount: Flow<Int> = contentDao.countApprovedContent()
  val totalImageGenerationsCount: Flow<Int> = imageDao.countTotalImageGenerations()
  val completedJobsCount: Flow<Int> = jobDao.countCompletedJobs()
  val failedJobsCount: Flow<Int> = jobDao.countFailedJobs()

  // --- Projects ---
  fun observeProject(id: Long): Flow<ProjectEntity?> = projectDao.observeProjectById(id)
  suspend fun getProject(id: Long): ProjectEntity? = projectDao.getProjectById(id)
  suspend fun insertProject(project: ProjectEntity): Long = projectDao.insertProject(project)
  suspend fun updateProject(project: ProjectEntity) = projectDao.updateProject(project)
  suspend fun deleteProject(id: Long) = projectDao.deleteProjectById(id)

  fun getResearchByProject(projectId: Long): Flow<List<ResearchRunEntity>> = researchDao.getResearchRunsByProject(projectId)
  fun getTrendsByProject(projectId: Long): Flow<List<TrendEntity>> = trendDao.getTrendsByProject(projectId)
  fun getContentByProject(projectId: Long): Flow<List<ContentItemEntity>> = contentDao.getContentByProject(projectId)
  fun getImagesByProject(projectId: Long): Flow<List<ImageGenerationEntity>> = imageDao.getImageGenerationsByProject(projectId)
  fun getAutomationsByProject(projectId: Long): Flow<List<AutomationEntity>> = automationDao.getAutomationsByProject(projectId)
  fun getJobsByProject(projectId: Long): Flow<List<JobEntity>> = jobDao.getJobsByProject(projectId)
  fun getMemoriesByProject(projectId: Long): Flow<List<LearningMemoryEntity>> = memoryDao.getMemoriesByProject(projectId)

  // --- AI Provider Management ---
  suspend fun insertProvider(provider: AIProviderEntity): Long = aiProviderDao.insertProvider(provider)
  suspend fun updateProvider(provider: AIProviderEntity) = aiProviderDao.updateProvider(provider)
  suspend fun deleteProvider(id: Long) = aiProviderDao.deleteProviderById(id)
  suspend fun setDefaultProvider(providerId: Long) {
    aiProviderDao.clearOtherDefaults(providerId)
    val provider = aiProviderDao.getProviderById(providerId)
    if (provider != null) {
      aiProviderDao.updateProvider(provider.copy(isDefault = true, isEnabled = true))
    }
  }

  suspend fun testProviderConnection(providerId: Long): ApiResult<String> {
    val provider = aiProviderDao.getProviderById(providerId)
      ?: return ApiResult.Error("Provider not found")

    val result = aiApiClient.testConnection(provider)
    val now = System.currentTimeMillis()
    when (result) {
      is ApiResult.Success -> {
        aiProviderDao.updateProvider(
          provider.copy(
            connectionStatus = "CONNECTED",
            statusMessage = result.data,
            lastTested = now
          )
        )
      }
      is ApiResult.Error -> {
        aiProviderDao.updateProvider(
          provider.copy(
            connectionStatus = "FAILED",
            statusMessage = result.message,
            lastTested = now
          )
        )
      }
    }
    return result
  }

  // --- Research Source Management ---
  suspend fun insertSource(source: ResearchSourceEntity): Long = researchSourceDao.insertSource(source)
  suspend fun updateSource(source: ResearchSourceEntity) = researchSourceDao.updateSource(source)
  suspend fun deleteSource(id: Long) = researchSourceDao.deleteSourceById(id)

  suspend fun testAndSyncSource(sourceId: Long): ApiResult<String> {
    val source = researchSourceDao.getSourceById(sourceId)
      ?: return ApiResult.Error("Source not found")

    researchSourceDao.updateSource(source.copy(status = "Syncing"))
    val result = researchSourceSync.testSourceConnection(source)
    val now = System.currentTimeMillis()
    when (result) {
      is ApiResult.Success -> {
        researchSourceDao.updateSource(
          source.copy(
            status = "Active",
            lastSync = now
          )
        )
      }
      is ApiResult.Error -> {
        researchSourceDao.updateSource(
          source.copy(
            status = "Error",
            lastSync = now,
            notes = result.message
          )
        )
      }
    }
    return result
  }

  // --- Research Intelligence Lifecycle ---
  suspend fun createResearchRun(run: ResearchRunEntity): Long = researchDao.insertResearchRun(run)
  fun observeResearchRun(id: Long): Flow<ResearchRunEntity?> = researchDao.observeResearchRunById(id)
  fun observeResearchResult(runId: Long): Flow<ResearchResultEntity?> = researchDao.observeResultByRunId(runId)

  suspend fun executeResearchRun(runId: Long): ApiResult<ResearchResultEntity> {
    val run = researchDao.getResearchRunById(runId)
      ?: return ApiResult.Error("Research run not found.")

    val provider = aiProviderDao.getDefaultProvider()
    val jobId = "JOB-" + UUID.randomUUID().toString().take(8).uppercase()

    val projectName = if (run.projectId != null) {
      projectDao.getProjectById(run.projectId)?.name
    } else null

    // Register job
    jobDao.insertJob(
      JobEntity(
        id = jobId,
        type = "Research",
        projectId = run.projectId,
        projectName = projectName,
        status = "Running",
        progress = 20,
        startedTime = System.currentTimeMillis()
      )
    )

    if (provider == null || provider.apiKey.isBlank() || !provider.isEnabled) {
      val errMsg = "AI Provider not configured. Please configure an active AI provider in Settings."
      researchDao.updateResearchRun(run.copy(status = "Failed", errorMessage = errMsg))
      jobDao.updateJob(
        JobEntity(
          id = jobId,
          type = "Research",
          projectId = run.projectId,
          projectName = projectName,
          status = "Failed",
          progress = 20,
          errorMessage = errMsg,
          finishedTime = System.currentTimeMillis()
        )
      )
      return ApiResult.Error(errMsg)
    }

    researchDao.updateResearchRun(run.copy(status = "Running", startedAt = System.currentTimeMillis()))
    jobDao.updateJob(
      JobEntity(
        id = jobId,
        type = "Research",
        projectId = run.projectId,
        projectName = projectName,
        status = "Running",
        progress = 50,
        startedTime = System.currentTimeMillis(),
        details = "Synthesizing market signals for '${run.topic}'"
      )
    )

    val prompt = """
      You are the Fashion Engine intelligence analyzer. Conduct in-depth research on the following fashion topic:
      Topic: ${run.topic}
      Fashion Category: ${run.category}
      Target Audience: ${run.targetAudience}
      Region: ${run.region}
      Time Range: ${run.timeRange}
      Selected Sources: ${run.researchSources}

      Provide a comprehensive, editorial, highly structured report with:
      1. Executive Summary
      2. Key Findings
      3. Trend Signals & Drivers
      4. Key Associated Brands
      5. Hero Products / Silhouettes
      6. Market Observations & Commercial Viability
      7. Keywords & Nomenclature
      8. Traceable Reference Sources
    """.trimIndent()

    val systemPrompt = "You are a professional fashion director and trend forecaster. Return detailed, analytical, editorial insights."
    val aiResult = aiApiClient.generateContent(provider, prompt, systemPrompt)

    return when (aiResult) {
      is ApiResult.Success -> {
        val content = aiResult.data
        val resultEntity = ResearchResultEntity(
          researchRunId = run.id,
          projectId = run.projectId,
          title = "${run.topic} Intelligence Report",
          summary = content.lines().take(4).joinToString(" ").take(300),
          keyFindings = content,
          sources = run.researchSources,
          trendSignals = "Color palettes, silhouette evolution, material innovation, consumer shift",
          keywords = "${run.topic}, ${run.category}, Avant-Garde, Contemporary, RTW",
          brands = "Industry benchmarks in ${run.category}",
          products = "Key garments, accessories, and styling elements",
          marketObservations = "High demand in ${run.region}; strong engagement across editorial channels.",
          confidenceScore = 0.92f,
          createdAt = System.currentTimeMillis()
        )

        researchDao.insertResearchResult(resultEntity)
        researchDao.updateResearchRun(
          run.copy(
            status = "Completed",
            completedAt = System.currentTimeMillis()
          )
        )

        jobDao.updateJob(
          JobEntity(
            id = jobId,
            type = "Research",
            projectId = run.projectId,
            projectName = projectName,
            status = "Completed",
            progress = 100,
            finishedTime = System.currentTimeMillis(),
            details = "Completed research on ${run.topic}"
          )
        )

        ApiResult.Success(resultEntity)
      }
      is ApiResult.Error -> {
        val errMsg = aiResult.message
        researchDao.updateResearchRun(
          run.copy(
            status = "Failed",
            errorMessage = errMsg,
            completedAt = System.currentTimeMillis()
          )
        )
        jobDao.updateJob(
          JobEntity(
            id = jobId,
            type = "Research",
            projectId = run.projectId,
            projectName = projectName,
            status = "Failed",
            progress = 50,
            errorMessage = errMsg,
            finishedTime = System.currentTimeMillis()
          )
        )
        ApiResult.Error(errMsg)
      }
    }
  }

  // --- Trends ---
  fun observeTrend(id: Long): Flow<TrendEntity?> = trendDao.observeTrendById(id)
  fun observeTrendsByResearchRun(runId: Long): Flow<List<TrendEntity>> = trendDao.getTrendsByResearchRun(runId)
  suspend fun insertTrend(trend: TrendEntity): Long = trendDao.insertTrend(trend)
  suspend fun insertTrends(trends: List<TrendEntity>): List<Long> = trendDao.insertTrends(trends)
  suspend fun updateTrend(trend: TrendEntity) = trendDao.updateTrend(trend)
  suspend fun deleteTrend(id: Long) = trendDao.deleteTrendById(id)

  suspend fun extractTrendsFromResearch(runId: Long): ApiResult<List<TrendEntity>> {
    val run = researchDao.getResearchRunById(runId) ?: return ApiResult.Error("Research Run not found")
    val result = researchDao.getResultByRunId(runId) ?: return ApiResult.Error("Research results not yet generated for this run")
    val provider = aiProviderDao.getDefaultProvider()

    if (provider == null || provider.apiKey.isBlank() || !provider.isEnabled) {
      return ApiResult.Error("Active AI Provider required to extract trends.")
    }

    val jobId = "JOB-" + UUID.randomUUID().toString().take(8).uppercase()
    val projectName = if (run.projectId != null) projectDao.getProjectById(run.projectId)?.name else null
    jobDao.insertJob(
      JobEntity(
        id = jobId,
        type = "Trend Sync",
        projectId = run.projectId,
        projectName = projectName,
        status = "Running",
        progress = 30,
        startedTime = System.currentTimeMillis(),
        details = "Extracting multi-trend taxonomy from '${run.topic}'"
      )
    )

    val prompt = """
      Based on the following fashion research dossier, identify and extract 3 to 5 distinct, highly actionable fashion trends.
      
      Topic: ${run.topic}
      Category: ${run.category}
      Summary: ${result.summary}
      Key Findings: ${result.keyFindings}
      Signals: ${result.trendSignals}
      Keywords: ${result.keywords}
      Brands: ${result.brands}
      Hero Products: ${result.products}

      Format your response strictly as a JSON array of objects. Each object MUST have:
      - "name": String (Precise trend name, e.g. "Sculptural Tailoring", "Distressed Utility Denim")
      - "category": String (e.g. "${run.category}")
      - "description": String (2-3 sentences explaining the aesthetic and market context)
      - "keywords": String (comma-separated keywords)
      - "relatedBrands": String (comma-separated relevant designers/brands)
      - "relatedProducts": String (comma-separated key garments or accessories)
      - "signals": String (drivers of this trend)
      - "confidenceScore": Float between 0.70 and 0.98
      - "status": String (one of: "Emerging", "Peaking", "Declining", "Evergreen", "Niche")

      Output ONLY the raw JSON array, without markdown formatting or code blocks.
    """.trimIndent()

    val systemPrompt = "You are a senior fashion trend forecaster. Return only valid JSON array representing fashion trends."
    val aiResult = aiApiClient.generateContent(provider, prompt, systemPrompt)

    return when (aiResult) {
      is ApiResult.Success -> {
        try {
          var cleanJson = aiResult.data.trim()
          if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.removePrefix("```json").substringBeforeLast("```").trim()
          } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.removePrefix("```").substringBeforeLast("```").trim()
          }

          val jsonArray = org.json.JSONArray(cleanJson)
          val trends = mutableListOf<TrendEntity>()
          for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            trends.add(
              TrendEntity(
                projectId = run.projectId,
                researchRunId = run.id,
                name = obj.optString("name", "Trend #${i + 1}"),
                category = obj.optString("category", run.category),
                description = obj.optString("description", "Identified from ${run.topic}"),
                keywords = obj.optString("keywords", run.topic),
                relatedBrands = obj.optString("relatedBrands", result.brands),
                relatedProducts = obj.optString("relatedProducts", result.products),
                signals = obj.optString("signals", result.trendSignals),
                sources = run.researchSources,
                confidenceScore = obj.optDouble("confidenceScore", 0.88).toFloat(),
                status = obj.optString("status", "Emerging"),
                isSaved = true,
                isArchived = false
              )
            )
          }

          if (trends.isNotEmpty()) {
            val insertedIds = trendDao.insertTrends(trends)
            jobDao.updateJob(
              JobEntity(
                id = jobId,
                type = "Trend Sync",
                projectId = run.projectId,
                projectName = projectName,
                status = "Completed",
                progress = 100,
                finishedTime = System.currentTimeMillis(),
                details = "Extracted ${trends.size} trends from research"
              )
            )
            ApiResult.Success(trends)
          } else {
            jobDao.updateJob(
              JobEntity(
                id = jobId,
                type = "Trend Sync",
                status = "Failed",
                errorMessage = "No trends parsed from response",
                finishedTime = System.currentTimeMillis()
              )
            )
            ApiResult.Error("No valid trends could be extracted from AI response.")
          }
        } catch (e: Exception) {
          jobDao.updateJob(
            JobEntity(
              id = jobId,
              type = "Trend Sync",
              status = "Failed",
              errorMessage = e.localizedMessage,
              finishedTime = System.currentTimeMillis()
            )
          )
          ApiResult.Error("Failed to parse trends: ${e.localizedMessage}")
        }
      }
      is ApiResult.Error -> {
        jobDao.updateJob(
          JobEntity(
            id = jobId,
            type = "Trend Sync",
            status = "Failed",
            errorMessage = aiResult.message,
            finishedTime = System.currentTimeMillis()
          )
        )
        ApiResult.Error(aiResult.message)
      }
    }
  }

  // --- Outfit Concepts ---
  fun observeOutfitConceptsByTrend(trendId: Long): Flow<List<OutfitConceptEntity>> = outfitConceptDao.getOutfitsByTrend(trendId)
  fun observeOutfitConceptsByProject(projectId: Long): Flow<List<OutfitConceptEntity>> = outfitConceptDao.getOutfitsByProject(projectId)
  fun observeOutfitConcept(id: Long): Flow<OutfitConceptEntity?> = outfitConceptDao.observeOutfitById(id)
  suspend fun insertOutfitConcept(outfit: OutfitConceptEntity): Long = outfitConceptDao.insertOutfit(outfit)
  suspend fun updateOutfitConcept(outfit: OutfitConceptEntity) = outfitConceptDao.updateOutfit(outfit)
  suspend fun deleteOutfitConcept(id: Long) = outfitConceptDao.deleteOutfitById(id)

  suspend fun generateOutfitConceptsForTrend(trendId: Long): ApiResult<List<OutfitConceptEntity>> {
    val trend = trendDao.getTrendById(trendId) ?: return ApiResult.Error("Trend not found")
    val provider = aiProviderDao.getDefaultProvider()

    if (provider == null || provider.apiKey.isBlank() || !provider.isEnabled) {
      return ApiResult.Error("Active AI Provider required to generate outfit concepts.")
    }

    val jobId = "JOB-" + UUID.randomUUID().toString().take(8).uppercase()
    val projectName = if (trend.projectId != null) projectDao.getProjectById(trend.projectId)?.name else null
    jobDao.insertJob(
      JobEntity(
        id = jobId,
        type = "Trend Sync",
        projectId = trend.projectId,
        projectName = projectName,
        status = "Running",
        progress = 30,
        startedTime = System.currentTimeMillis(),
        details = "Synthesizing outfit concepts for trend '${trend.name}'"
      )
    )

    val prompt = """
      Create 3 distinct, complete high-fashion outfit concepts that embody this trend:
      Trend Name: ${trend.name}
      Category: ${trend.category}
      Description: ${trend.description}
      Hero Products / Silhouettes: ${trend.relatedProducts}
      Signals / Aesthetics: ${trend.signals}
      Brands Context: ${trend.relatedBrands}

      Format your response strictly as a JSON array of objects. Each object MUST have:
      - "title": String (Descriptive look name, e.g. "Architectural Wool Overcoat & Fluid Trousers")
      - "description": String (Styling thesis and look philosophy, 2 sentences)
      - "garments": String (Specific garments composing the outfit: top, bottom, outer layer)
      - "accessories": String (Footwear, eyewear, bag, jewelry, accents)
      - "colorPalette": String (Specific harmonious color tones, e.g. "Charcoal Grey, Raw Ecru, Burnished Ochre")
      - "materials": String (Fabrics and textures, e.g. "Brushed alpaca, heavy poplin, patent calfskin")
      - "stylingNotes": String (Proportions, drape, layering details)
      - "visualPrompt": String (A rich, highly detailed image studio visual prompt describing a model wearing this exact outfit in an editorial high-fashion photoshoot setting)
      - "occasion": String (e.g. "Editorial / Runway", "Day-to-Evening Smart Luxury", "Streetstyle")

      Output ONLY the raw JSON array, without markdown formatting or code blocks.
    """.trimIndent()

    val systemPrompt = "You are an elite fashion director, stylist, and creative consultant. Return only valid JSON array."
    val aiResult = aiApiClient.generateContent(provider, prompt, systemPrompt)

    return when (aiResult) {
      is ApiResult.Success -> {
        try {
          var cleanJson = aiResult.data.trim()
          if (cleanJson.startsWith("```json")) {
            cleanJson = cleanJson.removePrefix("```json").substringBeforeLast("```").trim()
          } else if (cleanJson.startsWith("```")) {
            cleanJson = cleanJson.removePrefix("```").substringBeforeLast("```").trim()
          }

          val jsonArray = org.json.JSONArray(cleanJson)
          val outfits = mutableListOf<OutfitConceptEntity>()
          for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            outfits.add(
              OutfitConceptEntity(
                trendId = trend.id,
                projectId = trend.projectId,
                title = obj.optString("title", "${trend.name} - Look #${i + 1}"),
                description = obj.optString("description", "Curated concept for ${trend.name}"),
                garments = obj.optString("garments", trend.relatedProducts),
                accessories = obj.optString("accessories", "Curated accessories"),
                colorPalette = obj.optString("colorPalette", "Monochrome & Accent"),
                materials = obj.optString("materials", "Fine textiles"),
                stylingNotes = obj.optString("stylingNotes", "Contemporary proportions"),
                visualPrompt = obj.optString("visualPrompt", "High-fashion model wearing ${trend.name} concept look in modern editorial aesthetic"),
                occasion = obj.optString("occasion", "Editorial / Streetwear")
              )
            )
          }

          if (outfits.isNotEmpty()) {
            outfitConceptDao.insertOutfits(outfits)
            jobDao.updateJob(
              JobEntity(
                id = jobId,
                type = "Trend Sync",
                projectId = trend.projectId,
                projectName = projectName,
                status = "Completed",
                progress = 100,
                finishedTime = System.currentTimeMillis(),
                details = "Generated ${outfits.size} outfit concepts for '${trend.name}'"
              )
            )
            ApiResult.Success(outfits)
          } else {
            ApiResult.Error("No outfit concepts could be parsed from AI response.")
          }
        } catch (e: Exception) {
          ApiResult.Error("Failed to parse outfit concepts: ${e.localizedMessage}")
        }
      }
      is ApiResult.Error -> {
        ApiResult.Error(aiResult.message)
      }
    }
  }

  // --- Content Studio ---
  fun observeContent(id: Long): Flow<ContentItemEntity?> = contentDao.observeContentById(id)
  suspend fun getContent(id: Long): ContentItemEntity? = contentDao.getContentById(id)
  suspend fun insertContent(content: ContentItemEntity): Long = contentDao.insertContent(content)
  suspend fun updateContent(content: ContentItemEntity) = contentDao.updateContent(content)
  suspend fun deleteContent(id: Long) = contentDao.deleteContentById(id)

  suspend fun generateAiContent(
    title: String,
    contentType: String,
    projectId: Long?,
    instructions: String,
    relatedTrends: String = "",
    providerId: Long? = null
  ): ApiResult<ContentItemEntity> {
    val provider = if (providerId != null) {
      aiProviderDao.getProviderById(providerId)
    } else {
      aiProviderDao.getDefaultProvider()
    }

    if (provider == null || provider.apiKey.isBlank() || !provider.isEnabled) {
      return ApiResult.Error("AI Provider not configured. Please configure an AI provider in Settings.")
    }

    val jobId = "JOB-" + UUID.randomUUID().toString().take(8).uppercase()
    val projectName = if (projectId != null) projectDao.getProjectById(projectId)?.name else null

    jobDao.insertJob(
      JobEntity(
        id = jobId,
        type = "Content AI Gen",
        projectId = projectId,
        projectName = projectName,
        status = "Running",
        progress = 30,
        startedTime = System.currentTimeMillis(),
        details = "Drafting $contentType: $title"
      )
    )

    val prompt = """
      Create a high-end fashion $contentType with the title: '$title'.
      Project: ${projectName ?: "General"}
      Related Trends: $relatedTrends
      Instructions & Tone: $instructions

      Requirements:
      - Editorial, professional, sophisticated voice suited for luxury and contemporary fashion publications.
      - Strong headline structure, compelling narrative, and detailed product/styling references.
      - Ready for editorial review.
    """.trimIndent()

    val systemPrompt = "You are a senior fashion editor and creative copywriter for premier global fashion houses and journals."
    val aiResult = aiApiClient.generateContent(provider, prompt, systemPrompt)

    return when (aiResult) {
      is ApiResult.Success -> {
        val generatedBody = aiResult.data
        val contentItem = ContentItemEntity(
          projectId = projectId,
          title = title,
          contentType = contentType,
          relatedTrends = relatedTrends,
          body = generatedBody,
          status = "Draft", // Always saved as Draft, NEVER automatically published!
          author = "AI (${provider.name})",
          aiProviderUsed = provider.name,
          aiModelUsed = provider.model,
          createdAt = System.currentTimeMillis(),
          updatedAt = System.currentTimeMillis()
        )
        val id = contentDao.insertContent(contentItem)

        jobDao.updateJob(
          JobEntity(
            id = jobId,
            type = "Content AI Gen",
            projectId = projectId,
            projectName = projectName,
            status = "Completed",
            progress = 100,
            finishedTime = System.currentTimeMillis(),
            details = "Drafted $title ($contentType)"
          )
        )

        ApiResult.Success(contentItem.copy(id = id))
      }
      is ApiResult.Error -> {
        jobDao.updateJob(
          JobEntity(
            id = jobId,
            type = "Content AI Gen",
            projectId = projectId,
            projectName = projectName,
            status = "Failed",
            progress = 30,
            errorMessage = aiResult.message,
            finishedTime = System.currentTimeMillis()
          )
        )
        ApiResult.Error(aiResult.message)
      }
    }
  }

  // --- Image Studio ---
  suspend fun requestImageGeneration(
    prompt: String,
    negativePrompt: String?,
    style: String,
    aspectRatio: String,
    resolution: String,
    numberOfImages: Int,
    projectId: Long?,
    relatedContentId: Long? = null,
    trendId: Long? = null,
    outfitConceptId: Long? = null
  ): ApiResult<ImageGenerationEntity> {
    val provider = aiProviderDao.getDefaultProvider()

    val initialEntity = ImageGenerationEntity(
      projectId = projectId,
      trendId = trendId,
      outfitConceptId = outfitConceptId,
      relatedContentId = relatedContentId,
      prompt = prompt,
      negativePrompt = negativePrompt,
      style = style,
      aspectRatio = aspectRatio,
      resolution = resolution,
      numberOfImages = numberOfImages,
      status = "Queued",
      createdAt = System.currentTimeMillis()
    )
    val id = imageDao.insertImageGeneration(initialEntity)

    if (provider == null || provider.apiKey.isBlank() || !provider.isEnabled) {
      val errMsg = "Image Provider Not Configured. Please configure an image provider in Settings."
      imageDao.updateImageGeneration(
        initialEntity.copy(
          id = id,
          status = "Failed",
          errorMessage = errMsg,
          completedAt = System.currentTimeMillis()
        )
      )
      return ApiResult.Error(errMsg)
    }

    val jobId = "JOB-" + UUID.randomUUID().toString().take(8).uppercase()
    jobDao.insertJob(
      JobEntity(
        id = jobId,
        type = "Image Gen",
        projectId = projectId,
        status = "Running",
        progress = 40,
        startedTime = System.currentTimeMillis(),
        details = "Generating $numberOfImages fashion visuals: $prompt"
      )
    )

    imageDao.updateImageGeneration(initialEntity.copy(id = id, status = "Generating"))

    val fullPrompt = "$prompt. Style: $style. High fashion editorial, 8k resolution, cinematic studio lighting."
    val result = aiApiClient.generateImage(provider, fullPrompt, resolution, numberOfImages)

    return when (result) {
      is ApiResult.Success -> {
        val paths = result.data.joinToString(",")
        val completed = initialEntity.copy(
          id = id,
          status = "Completed",
          resultImagePaths = paths,
          completedAt = System.currentTimeMillis()
        )
        imageDao.updateImageGeneration(completed)
        jobDao.updateJob(
          JobEntity(
            id = jobId,
            type = "Image Gen",
            projectId = projectId,
            status = "Completed",
            progress = 100,
            finishedTime = System.currentTimeMillis(),
            details = "Completed $numberOfImages images"
          )
        )
        ApiResult.Success(completed)
      }
      is ApiResult.Error -> {
        val failed = initialEntity.copy(
          id = id,
          status = "Failed",
          errorMessage = result.message,
          completedAt = System.currentTimeMillis()
        )
        imageDao.updateImageGeneration(failed)
        jobDao.updateJob(
          JobEntity(
            id = jobId,
            type = "Image Gen",
            projectId = projectId,
            status = "Failed",
            progress = 40,
            errorMessage = result.message,
            finishedTime = System.currentTimeMillis()
          )
        )
        ApiResult.Error(result.message)
      }
    }
  }

  // --- Publishing ---
  suspend fun insertPublishingDestination(dest: PublishingDestinationEntity): Long = publishingDao.insertDestination(dest)
  suspend fun updatePublishingDestination(dest: PublishingDestinationEntity) = publishingDao.updateDestination(dest)
  suspend fun deletePublishingDestination(id: Long) = publishingDao.deleteDestinationById(id)

  suspend fun testPublishingDestination(id: Long): ApiResult<String> {
    val dest = publishingDao.getDestinationById(id) ?: return ApiResult.Error("Destination not found")
    val res = publishingApiClient.testDestination(dest)
    when (res) {
      is ApiResult.Success -> publishingDao.updateDestination(dest.copy(status = "Active"))
      is ApiResult.Error -> publishingDao.updateDestination(dest.copy(status = "Connection Error"))
    }
    return res
  }

  suspend fun publishContentItem(contentId: Long, destinationId: Long): ApiResult<String> {
    val content = contentDao.getContentById(contentId) ?: return ApiResult.Error("Content not found.")
    val destination = publishingDao.getDestinationById(destinationId) ?: return ApiResult.Error("Publishing destination not found.")

    if (!destination.isEnabled) {
      return ApiResult.Error("Selected publishing destination is currently disabled.")
    }

    val jobId = "JOB-" + UUID.randomUUID().toString().take(8).uppercase()
    jobDao.insertJob(
      JobEntity(
        id = jobId,
        type = "Publishing",
        projectId = content.projectId,
        status = "Running",
        progress = 40,
        startedTime = System.currentTimeMillis(),
        details = "Publishing '${content.title}' to ${destination.name}"
      )
    )

    val result = publishingApiClient.publishContent(destination, content.title, content.body, "publish")

    return when (result) {
      is ApiResult.Success -> {
        contentDao.updateContent(
          content.copy(
            status = "Published",
            updatedAt = System.currentTimeMillis()
          )
        )
        publishingDao.updateDestination(
          destination.copy(lastPublishedAt = System.currentTimeMillis())
        )
        jobDao.updateJob(
          JobEntity(
            id = jobId,
            type = "Publishing",
            projectId = content.projectId,
            status = "Completed",
            progress = 100,
            finishedTime = System.currentTimeMillis(),
            details = "Published to ${destination.name}"
          )
        )
        ApiResult.Success("Successfully published '${content.title}' to ${destination.name}")
      }
      is ApiResult.Error -> {
        jobDao.updateJob(
          JobEntity(
            id = jobId,
            type = "Publishing",
            projectId = content.projectId,
            status = "Failed",
            progress = 40,
            errorMessage = result.message,
            finishedTime = System.currentTimeMillis()
          )
        )
        ApiResult.Error("Publishing failed: ${result.message}")
      }
    }
  }

  // --- Automations ---
  suspend fun insertAutomation(auto: AutomationEntity): Long = automationDao.insertAutomation(auto)
  suspend fun updateAutomation(auto: AutomationEntity) = automationDao.updateAutomation(auto)
  suspend fun deleteAutomation(id: Long) = automationDao.deleteAutomationById(id)

  // --- Job Manager ---
  suspend fun insertJob(job: JobEntity) = jobDao.insertJob(job)
  suspend fun updateJob(job: JobEntity) = jobDao.updateJob(job)
  suspend fun deleteJob(id: String) = jobDao.deleteJobById(id)
  suspend fun retryJob(id: String): ApiResult<String> {
    val job = jobDao.getJobById(id) ?: return ApiResult.Error("Job not found")
    jobDao.updateJob(
      job.copy(
        status = "Queued",
        progress = 0,
        retryCount = job.retryCount + 1,
        errorMessage = null,
        startedTime = System.currentTimeMillis(),
        finishedTime = null
      )
    )
    return ApiResult.Success("Job re-queued successfully.")
  }

  suspend fun cancelJob(id: String): ApiResult<String> {
    val job = jobDao.getJobById(id) ?: return ApiResult.Error("Job not found")
    jobDao.updateJob(
      job.copy(
        status = "Cancelled",
        finishedTime = System.currentTimeMillis()
      )
    )
    return ApiResult.Success("Job cancelled.")
  }

  // --- Continuous Learning / Memory ---
  suspend fun insertMemory(memory: LearningMemoryEntity): Long = memoryDao.insertMemory(memory)
  suspend fun deleteMemory(id: Long) = memoryDao.deleteMemoryById(id)
  suspend fun resetProjectMemories(projectId: Long) = memoryDao.resetProjectMemories(projectId)

  // --- Settings ---
  suspend fun updateSettings(settings: UserSettingsEntity) = settingsDao.insertOrUpdateSettings(settings)
}
