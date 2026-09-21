package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
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
import com.example.data.local.entity.ResearchRunEntity
import com.example.data.local.entity.ResearchSourceEntity
import com.example.data.local.entity.TrendEntity
import com.example.data.local.entity.UserSettingsEntity
import com.example.data.remote.ApiResult
import com.example.data.repository.FashionEngineRepository
import com.example.util.ImageStorageUtil
import com.example.worker.FashionAutomationWorker
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

sealed class UiMessage {
  data class Success(val message: String) : UiMessage()
  data class Error(val message: String) : UiMessage()
}

class FashionEngineViewModel(application: Application) : AndroidViewModel(application) {
  private val database = AppDatabase.getDatabase(application)
  private val repository = FashionEngineRepository(database)
  private val workManager = WorkManager.getInstance(application)

  // Status message for Snackbars/Banners
  private val _uiMessage = MutableStateFlow<UiMessage?>(null)
  val uiMessage: StateFlow<UiMessage?> = _uiMessage.asStateFlow()

  private val _isLoading = MutableStateFlow(false)
  val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

  fun clearMessage() {
    _uiMessage.value = null
  }

  // Reactive Data
  val activeProjects: StateFlow<List<ProjectEntity>> = repository.activeProjects
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allProjects: StateFlow<List<ProjectEntity>> = repository.allProjects
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allProviders: StateFlow<List<AIProviderEntity>> = repository.allProviders
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allSources: StateFlow<List<ResearchSourceEntity>> = repository.allSources
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allResearchRuns: StateFlow<List<ResearchRunEntity>> = repository.allResearchRuns
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activeTrends: StateFlow<List<TrendEntity>> = repository.activeTrends
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allTrends: StateFlow<List<TrendEntity>> = repository.allTrends
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allOutfitConcepts: StateFlow<List<OutfitConceptEntity>> = repository.allOutfitConcepts
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allContent: StateFlow<List<ContentItemEntity>> = repository.allContent
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allImages: StateFlow<List<ImageGenerationEntity>> = repository.allImages
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allDestinations: StateFlow<List<PublishingDestinationEntity>> = repository.allDestinations
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allAutomations: StateFlow<List<AutomationEntity>> = repository.allAutomations
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allJobs: StateFlow<List<JobEntity>> = repository.allJobs
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val activeJobs: StateFlow<List<JobEntity>> = repository.activeJobs
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val allMemories: StateFlow<List<LearningMemoryEntity>> = repository.allMemories
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  val userSettings: StateFlow<UserSettingsEntity?> = repository.userSettings
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  // Real Counts for Dashboard Cards (Never fake values!)
  val activeProjectsCount: StateFlow<Int> = repository.activeProjectsCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val researchRunsCount: StateFlow<Int> = repository.researchRunsCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val savedTrendsCount: StateFlow<Int> = repository.savedTrendsCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val draftContentCount: StateFlow<Int> = repository.draftContentCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val publishedContentCount: StateFlow<Int> = repository.publishedContentCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val activeJobsCount: StateFlow<Int> = repository.activeJobsCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  // Real Counts for Analytics
  val completedRunsCount: StateFlow<Int> = repository.completedRunsCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val totalTrendsCount: StateFlow<Int> = repository.totalTrendsCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val approvedContentCount: StateFlow<Int> = repository.approvedContentCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val totalImageGenerationsCount: StateFlow<Int> = repository.totalImageGenerationsCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val completedJobsCount: StateFlow<Int> = repository.completedJobsCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  val failedJobsCount: StateFlow<Int> = repository.failedJobsCount
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

  // Currently Selected Project for Project Details View
  private val _selectedProjectId = MutableStateFlow<Long?>(null)
  val selectedProjectId: StateFlow<Long?> = _selectedProjectId.asStateFlow()

  fun selectProject(id: Long?) {
    _selectedProjectId.value = id
  }

  @OptIn(ExperimentalCoroutinesApi::class)
  val selectedProject: StateFlow<ProjectEntity?> = _selectedProjectId.flatMapLatest { id ->
    if (id != null) repository.observeProject(id) else flowOf(null)
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

  @OptIn(ExperimentalCoroutinesApi::class)
  val projectResearch = _selectedProjectId.flatMapLatest { id ->
    if (id != null) repository.getResearchByProject(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(ExperimentalCoroutinesApi::class)
  val projectTrends = _selectedProjectId.flatMapLatest { id ->
    if (id != null) repository.getTrendsByProject(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(ExperimentalCoroutinesApi::class)
  val projectContent = _selectedProjectId.flatMapLatest { id ->
    if (id != null) repository.getContentByProject(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(ExperimentalCoroutinesApi::class)
  val projectImages = _selectedProjectId.flatMapLatest { id ->
    if (id != null) repository.getImagesByProject(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(ExperimentalCoroutinesApi::class)
  val projectAutomations = _selectedProjectId.flatMapLatest { id ->
    if (id != null) repository.getAutomationsByProject(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(ExperimentalCoroutinesApi::class)
  val projectJobs = _selectedProjectId.flatMapLatest { id ->
    if (id != null) repository.getJobsByProject(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  @OptIn(ExperimentalCoroutinesApi::class)
  val projectMemories = _selectedProjectId.flatMapLatest { id ->
    if (id != null) repository.getMemoriesByProject(id) else flowOf(emptyList())
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  // --- Project Actions ---
  fun createProject(
    name: String,
    description: String,
    category: String,
    targetAudience: String,
    region: String,
    language: String,
    brandStyleDirection: String
  ) {
    viewModelScope.launch {
      val project = ProjectEntity(
        name = name,
        description = description,
        fashionCategory = category,
        targetAudience = targetAudience,
        region = region,
        language = language,
        brandStyleDirection = brandStyleDirection
      )
      val id = repository.insertProject(project)
      _uiMessage.value = UiMessage.Success("Created project: $name")
      _selectedProjectId.value = id
    }
  }

  fun updateProject(project: ProjectEntity) {
    viewModelScope.launch {
      repository.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
      _uiMessage.value = UiMessage.Success("Project updated: ${project.name}")
    }
  }

  fun deleteProject(id: Long) {
    viewModelScope.launch {
      repository.deleteProject(id)
      if (_selectedProjectId.value == id) {
        _selectedProjectId.value = null
      }
      _uiMessage.value = UiMessage.Success("Project deleted.")
    }
  }

  fun toggleArchiveProject(project: ProjectEntity) {
    viewModelScope.launch {
      val updated = project.copy(isArchived = !project.isArchived, updatedAt = System.currentTimeMillis())
      repository.updateProject(updated)
      _uiMessage.value = UiMessage.Success(
        if (updated.isArchived) "Archived project: ${project.name}" else "Unarchived project: ${project.name}"
      )
    }
  }

  // --- AI Provider Actions ---
  fun addProvider(
    name: String,
    type: String,
    endpoint: String,
    key: String,
    model: String,
    enabled: Boolean
  ) {
    viewModelScope.launch {
      val provider = AIProviderEntity(
        name = name,
        providerType = type,
        apiEndpoint = endpoint,
        apiKey = key,
        model = model,
        isEnabled = enabled,
        isDefault = false,
        connectionStatus = "NOT_TESTED",
        statusMessage = "Added provider. Test connection to verify."
      )
      repository.insertProvider(provider)
      _uiMessage.value = UiMessage.Success("Added AI Provider: $name")
    }
  }

  fun updateProvider(provider: AIProviderEntity) {
    viewModelScope.launch {
      repository.updateProvider(provider)
      _uiMessage.value = UiMessage.Success("Updated provider: ${provider.name}")
    }
  }

  fun deleteProvider(id: Long) {
    viewModelScope.launch {
      repository.deleteProvider(id)
      _uiMessage.value = UiMessage.Success("Provider deleted.")
    }
  }

  fun setDefaultProvider(providerId: Long) {
    viewModelScope.launch {
      repository.setDefaultProvider(providerId)
      _uiMessage.value = UiMessage.Success("Set default AI provider.")
    }
  }

  fun testProviderConnection(providerId: Long) {
    viewModelScope.launch {
      _isLoading.value = true
      val res = repository.testProviderConnection(providerId)
      _isLoading.value = false
      when (res) {
        is ApiResult.Success -> _uiMessage.value = UiMessage.Success(res.data)
        is ApiResult.Error -> _uiMessage.value = UiMessage.Error(res.message)
      }
    }
  }

  // --- Research Source Actions ---
  fun addResearchSource(
    name: String,
    url: String,
    apiKey: String?,
    type: String,
    notes: String?
  ) {
    viewModelScope.launch {
      val source = ResearchSourceEntity(
        name = name,
        urlOrEndpoint = url,
        apiKey = apiKey,
        type = type,
        notes = notes
      )
      repository.insertSource(source)
      _uiMessage.value = UiMessage.Success("Added research source: $name")
    }
  }

  fun updateResearchSource(source: ResearchSourceEntity) {
    viewModelScope.launch {
      repository.updateSource(source)
      _uiMessage.value = UiMessage.Success("Updated research source.")
    }
  }

  fun deleteResearchSource(id: Long) {
    viewModelScope.launch {
      repository.deleteSource(id)
      _uiMessage.value = UiMessage.Success("Research source deleted.")
    }
  }

  fun testResearchSource(sourceId: Long) {
    viewModelScope.launch {
      _isLoading.value = true
      val res = repository.testAndSyncSource(sourceId)
      _isLoading.value = false
      when (res) {
        is ApiResult.Success -> _uiMessage.value = UiMessage.Success(res.data)
        is ApiResult.Error -> _uiMessage.value = UiMessage.Error(res.message)
      }
    }
  }

  // --- Research Actions ---
  fun createResearchRun(
    topic: String,
    category: String,
    targetAudience: String,
    region: String,
    language: String,
    timeRange: String,
    sources: String,
    projectId: Long?,
    executeImmediately: Boolean
  ) {
    viewModelScope.launch {
      val run = ResearchRunEntity(
        topic = topic,
        category = category,
        targetAudience = targetAudience,
        region = region,
        language = language,
        timeRange = timeRange,
        researchSources = sources,
        projectId = projectId,
        status = if (executeImmediately) "Queued" else "Draft"
      )
      val runId = repository.createResearchRun(run)
      _uiMessage.value = UiMessage.Success("Created research: $topic")

      if (executeImmediately) {
        _isLoading.value = true
        val res = repository.executeResearchRun(runId)
        _isLoading.value = false
        when (res) {
          is ApiResult.Success -> _uiMessage.value = UiMessage.Success("Research completed for: $topic")
          is ApiResult.Error -> _uiMessage.value = UiMessage.Error(res.message)
        }
      }
    }
  }

  fun executeResearch(runId: Long) {
    viewModelScope.launch {
      _isLoading.value = true
      val res = repository.executeResearchRun(runId)
      _isLoading.value = false
      when (res) {
        is ApiResult.Success -> _uiMessage.value = UiMessage.Success("Research run finished.")
        is ApiResult.Error -> _uiMessage.value = UiMessage.Error(res.message)
      }
    }
  }

  fun observeResearchResult(runId: Long) = repository.observeResearchResult(runId)

  // --- Trend Actions ---
  fun createTrend(
    name: String,
    category: String,
    description: String,
    keywords: String,
    relatedBrands: String,
    relatedProducts: String,
    signals: String,
    sources: String,
    status: String,
    projectId: Long?
  ) {
    viewModelScope.launch {
      val trend = TrendEntity(
        name = name,
        category = category,
        description = description,
        keywords = keywords,
        relatedBrands = relatedBrands,
        relatedProducts = relatedProducts,
        signals = signals,
        sources = sources,
        status = status,
        projectId = projectId
      )
      repository.insertTrend(trend)
      _uiMessage.value = UiMessage.Success("Created trend: $name")
    }
  }

  fun updateTrend(trend: TrendEntity) {
    viewModelScope.launch {
      repository.updateTrend(trend.copy(lastUpdated = System.currentTimeMillis()))
      _uiMessage.value = UiMessage.Success("Trend updated: ${trend.name}")
    }
  }

  fun deleteTrend(id: Long) {
    viewModelScope.launch {
      repository.deleteTrend(id)
      _uiMessage.value = UiMessage.Success("Trend deleted.")
    }
  }

  fun toggleSaveTrend(trend: TrendEntity) {
    viewModelScope.launch {
      val updated = trend.copy(isSaved = !trend.isSaved, lastUpdated = System.currentTimeMillis())
      repository.updateTrend(updated)
      _uiMessage.value = UiMessage.Success(if (updated.isSaved) "Trend saved." else "Trend unsaved.")
    }
  }

  fun toggleArchiveTrend(trend: TrendEntity) {
    viewModelScope.launch {
      val updated = trend.copy(isArchived = !trend.isArchived, lastUpdated = System.currentTimeMillis())
      repository.updateTrend(updated)
      _uiMessage.value = UiMessage.Success(if (updated.isArchived) "Trend archived." else "Trend restored.")
    }
  }

  fun extractTrendsFromResearch(runId: Long) {
    viewModelScope.launch {
      _isLoading.value = true
      val result = repository.extractTrendsFromResearch(runId)
      _isLoading.value = false
      when (result) {
        is ApiResult.Success -> _uiMessage.value = UiMessage.Success("Extracted ${result.data.size} fashion trends from research.")
        is ApiResult.Error -> _uiMessage.value = UiMessage.Error(result.message)
      }
    }
  }

  fun observeTrendsByResearchRun(runId: Long) = repository.observeTrendsByResearchRun(runId)

  // --- Outfit Concept Actions ---
  fun observeOutfitConceptsByTrend(trendId: Long) = repository.observeOutfitConceptsByTrend(trendId)
  fun observeOutfitConceptsByProject(projectId: Long) = repository.observeOutfitConceptsByProject(projectId)
  fun observeOutfitConcept(id: Long) = repository.observeOutfitConcept(id)

  fun generateOutfitConceptsForTrend(trendId: Long) {
    viewModelScope.launch {
      _isLoading.value = true
      val result = repository.generateOutfitConceptsForTrend(trendId)
      _isLoading.value = false
      when (result) {
        is ApiResult.Success -> _uiMessage.value = UiMessage.Success("Generated ${result.data.size} outfit concepts.")
        is ApiResult.Error -> _uiMessage.value = UiMessage.Error(result.message)
      }
    }
  }

  fun createOutfitConcept(
    trendId: Long,
    projectId: Long?,
    title: String,
    description: String,
    garments: String,
    accessories: String,
    colorPalette: String,
    materials: String,
    stylingNotes: String,
    visualPrompt: String,
    occasion: String
  ) {
    viewModelScope.launch {
      val outfit = OutfitConceptEntity(
        trendId = trendId,
        projectId = projectId,
        title = title,
        description = description,
        garments = garments,
        accessories = accessories,
        colorPalette = colorPalette,
        materials = materials,
        stylingNotes = stylingNotes,
        visualPrompt = visualPrompt,
        occasion = occasion
      )
      repository.insertOutfitConcept(outfit)
      _uiMessage.value = UiMessage.Success("Created outfit concept: $title")
    }
  }

  fun deleteOutfitConcept(id: Long) {
    viewModelScope.launch {
      repository.deleteOutfitConcept(id)
      _uiMessage.value = UiMessage.Success("Outfit concept removed.")
    }
  }

  // --- Content Studio Actions ---
  fun createManualContent(
    title: String,
    contentType: String,
    projectId: Long?,
    body: String,
    status: String = "Draft",
    relatedTrends: String = ""
  ) {
    viewModelScope.launch {
      val item = ContentItemEntity(
        projectId = projectId,
        title = title,
        contentType = contentType,
        body = body,
        status = status,
        relatedTrends = relatedTrends
      )
      repository.insertContent(item)
      _uiMessage.value = UiMessage.Success("Saved content draft: $title")
    }
  }

  fun updateContent(item: ContentItemEntity) {
    viewModelScope.launch {
      repository.updateContent(item.copy(updatedAt = System.currentTimeMillis()))
      _uiMessage.value = UiMessage.Success("Content saved.")
    }
  }

  fun deleteContent(id: Long) {
    viewModelScope.launch {
      repository.deleteContent(id)
      _uiMessage.value = UiMessage.Success("Content deleted.")
    }
  }

  fun generateAiContent(
    title: String,
    contentType: String,
    projectId: Long?,
    instructions: String,
    relatedTrends: String = "",
    providerId: Long? = null
  ) {
    viewModelScope.launch {
      _isLoading.value = true
      val result = repository.generateAiContent(
        title = title,
        contentType = contentType,
        projectId = projectId,
        instructions = instructions,
        relatedTrends = relatedTrends,
        providerId = providerId
      )
      _isLoading.value = false
      when (result) {
        is ApiResult.Success -> _uiMessage.value = UiMessage.Success("Generated draft: ${result.data.title}")
        is ApiResult.Error -> _uiMessage.value = UiMessage.Error(result.message)
      }
    }
  }

  // --- Image Studio Actions ---
  fun requestImageGeneration(
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
  ) {
    viewModelScope.launch {
      _isLoading.value = true
      val res = repository.requestImageGeneration(
        prompt = prompt,
        negativePrompt = negativePrompt,
        style = style,
        aspectRatio = aspectRatio,
        resolution = resolution,
        numberOfImages = numberOfImages,
        projectId = projectId,
        relatedContentId = relatedContentId,
        trendId = trendId,
        outfitConceptId = outfitConceptId
      )
      _isLoading.value = false
      when (res) {
        is ApiResult.Success -> _uiMessage.value = UiMessage.Success("Generated image request completed.")
        is ApiResult.Error -> _uiMessage.value = UiMessage.Error(res.message)
      }
    }
  }

  fun saveImageToPhone(imageSource: String, title: String = "Fashion_Look") {
    viewModelScope.launch {
      _isLoading.value = true
      val app = getApplication<Application>()
      val result = ImageStorageUtil.saveImageToGallery(app, imageSource, title)
      _isLoading.value = false
      result.onSuccess {
        _uiMessage.value = UiMessage.Success("Image saved to phone Pictures/FashionEngine!")
      }.onFailure { err ->
        _uiMessage.value = UiMessage.Error("Failed to save image: ${err.localizedMessage}")
      }
    }
  }

  // --- Publishing Actions ---
  fun addPublishingDestination(
    name: String,
    type: String,
    endpoint: String,
    credentials: String,
    isEnabled: Boolean
  ) {
    viewModelScope.launch {
      val dest = PublishingDestinationEntity(
        name = name,
        type = type,
        endpoint = endpoint,
        credentials = credentials,
        isEnabled = isEnabled,
        status = "Active"
      )
      repository.insertPublishingDestination(dest)
      _uiMessage.value = UiMessage.Success("Added publishing destination: $name")
    }
  }

  fun updatePublishingDestination(dest: PublishingDestinationEntity) {
    viewModelScope.launch {
      repository.updatePublishingDestination(dest)
      _uiMessage.value = UiMessage.Success("Publishing destination updated.")
    }
  }

  fun deletePublishingDestination(id: Long) {
    viewModelScope.launch {
      repository.deletePublishingDestination(id)
      _uiMessage.value = UiMessage.Success("Destination deleted.")
    }
  }

  fun testPublishingDestination(id: Long) {
    viewModelScope.launch {
      _isLoading.value = true
      val res = repository.testPublishingDestination(id)
      _isLoading.value = false
      when (res) {
        is ApiResult.Success -> _uiMessage.value = UiMessage.Success(res.data)
        is ApiResult.Error -> _uiMessage.value = UiMessage.Error(res.message)
      }
    }
  }

  fun publishContent(contentId: Long, destinationId: Long) {
    viewModelScope.launch {
      _isLoading.value = true
      val res = repository.publishContentItem(contentId, destinationId)
      _isLoading.value = false
      when (res) {
        is ApiResult.Success -> _uiMessage.value = UiMessage.Success(res.data)
        is ApiResult.Error -> _uiMessage.value = UiMessage.Error(res.message)
      }
    }
  }

  // --- Automation Actions ---
  fun addAutomation(
    name: String,
    description: String,
    type: String,
    frequency: String,
    projectId: Long?,
    isEnabled: Boolean
  ) {
    viewModelScope.launch {
      val auto = AutomationEntity(
        name = name,
        description = description,
        type = type,
        frequency = frequency,
        projectId = projectId,
        isEnabled = isEnabled
      )
      val id = repository.insertAutomation(auto)
      if (isEnabled) {
        scheduleWorkManagerJob(id, name)
      }
      _uiMessage.value = UiMessage.Success("Automation saved: $name")
    }
  }

  fun toggleAutomation(auto: AutomationEntity) {
    viewModelScope.launch {
      val newEnabled = !auto.isEnabled
      val updated = auto.copy(isEnabled = newEnabled)
      repository.updateAutomation(updated)
      if (newEnabled) {
        scheduleWorkManagerJob(auto.id, auto.name)
        _uiMessage.value = UiMessage.Success("Automation '${auto.name}' enabled & scheduled.")
      } else {
        _uiMessage.value = UiMessage.Success("Automation '${auto.name}' disabled.")
      }
    }
  }

  fun runAutomationNow(auto: AutomationEntity) {
    scheduleWorkManagerJob(auto.id, auto.name)
    _uiMessage.value = UiMessage.Success("Queued background automation: ${auto.name}")
  }

  fun deleteAutomation(id: Long) {
    viewModelScope.launch {
      repository.deleteAutomation(id)
      _uiMessage.value = UiMessage.Success("Automation deleted.")
    }
  }

  private fun scheduleWorkManagerJob(automationId: Long, name: String) {
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .build()

    val workRequest = OneTimeWorkRequestBuilder<FashionAutomationWorker>()
      .setConstraints(constraints)
      .setInputData(
        workDataOf(
          "AUTOMATION_ID" to automationId,
          "AUTOMATION_NAME" to name
        )
      )
      .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 15, TimeUnit.SECONDS)
      .build()

    workManager.enqueue(workRequest)
  }

  // --- Job Manager Actions ---
  fun retryJob(id: String) {
    viewModelScope.launch {
      val res = repository.retryJob(id)
      when (res) {
        is ApiResult.Success -> _uiMessage.value = UiMessage.Success(res.data)
        is ApiResult.Error -> _uiMessage.value = UiMessage.Error(res.message)
      }
    }
  }

  fun cancelJob(id: String) {
    viewModelScope.launch {
      val res = repository.cancelJob(id)
      when (res) {
        is ApiResult.Success -> _uiMessage.value = UiMessage.Success(res.data)
        is ApiResult.Error -> _uiMessage.value = UiMessage.Error(res.message)
      }
    }
  }

  fun deleteJob(id: String) {
    viewModelScope.launch {
      repository.deleteJob(id)
      _uiMessage.value = UiMessage.Success("Job removed from history.")
    }
  }

  // --- Learning Memory Actions ---
  fun addLearningMemory(
    projectId: Long?,
    category: String,
    key: String,
    value: String
  ) {
    viewModelScope.launch {
      val memory = LearningMemoryEntity(
        projectId = projectId,
        category = category,
        key = key,
        value = value,
        source = "Explicit Preference"
      )
      repository.insertMemory(memory)
      _uiMessage.value = UiMessage.Success("Saved preference to Continuous Memory.")
    }
  }

  fun deleteLearningMemory(id: Long) {
    viewModelScope.launch {
      repository.deleteMemory(id)
      _uiMessage.value = UiMessage.Success("Memory entry removed.")
    }
  }

  fun resetProjectMemories(projectId: Long) {
    viewModelScope.launch {
      repository.resetProjectMemories(projectId)
      _uiMessage.value = UiMessage.Success("Reset all learning preferences for project.")
    }
  }

  // --- User Settings Actions ---
  fun updateThemeMode(mode: String) {
    viewModelScope.launch {
      val current = userSettings.value ?: UserSettingsEntity()
      repository.updateSettings(current.copy(themeMode = mode))
    }
  }

  fun updateLanguage(lang: String) {
    viewModelScope.launch {
      val current = userSettings.value ?: UserSettingsEntity()
      repository.updateSettings(current.copy(defaultLanguage = lang))
      _uiMessage.value = UiMessage.Success("Language updated to $lang.")
    }
  }

  fun toggleNotifications(enabled: Boolean) {
    viewModelScope.launch {
      val current = userSettings.value ?: UserSettingsEntity()
      repository.updateSettings(current.copy(enableNotifications = enabled))
    }
  }
}
