package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ViolationReport
import com.example.data.repository.ViolationRepository
import com.example.util.GpsCoordinate
import com.example.util.LocationHelper
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ReportViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ViolationRepository

    // GPS & Current User Location - initialized with default coordinates to prevent frozen state
    private val _currentGps = MutableStateFlow<GpsCoordinate?>(
        GpsCoordinate(
            latitude = -0.3785,
            longitude = 102.2982,
            accuracyMeters = 5.0f,
            address = "Jl. Sultan Ibrahim, Rengat, Indragiri Hulu"
        )
    )
    val currentGps: StateFlow<GpsCoordinate?> = _currentGps.asStateFlow()

    private val _isFetchingGps = MutableStateFlow(false)
    val isFetchingGps: StateFlow<Boolean> = _isFetchingGps.asStateFlow()

    // Form State
    val formTitle = MutableStateFlow("")
    val formDescription = MutableStateFlow("")
    val formCategory = MutableStateFlow(ViolationReport.CAT_PKL)
    val formUrgency = MutableStateFlow(ViolationReport.URGENCY_MEDIUM)
    val formReporterName = MutableStateFlow("")
    val formReporterPhone = MutableStateFlow("")
    val formIsAnonymous = MutableStateFlow(false)
    val formLandmark = MutableStateFlow("")
    val formPhotoUri = MutableStateFlow<String?>(null)
    val formManualAddress = MutableStateFlow("Jl. Sultan Ibrahim, Rengat, Indragiri Hulu")

    private val _submissionSuccess = MutableStateFlow<String?>(null)
    val submissionSuccess: StateFlow<String?> = _submissionSuccess.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    // Selected Report for Detail View
    private val _selectedReportId = MutableStateFlow<Long?>(null)
    val selectedReportId: StateFlow<Long?> = _selectedReportId.asStateFlow()

    init {
        val db = AppDatabase.getInstance(application)
        repository = ViolationRepository(db.violationDao())

        // Ensure database has initial sample reports so the app always displays live data
        viewModelScope.launch {
            try {
                repository.seedSampleReportsIfEmpty()
            } catch (_: Exception) {}
        }
    }

    val allReports: StateFlow<List<ViolationReport>> = repository.allReports
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow<String?>(null)
    val statusFilter: StateFlow<String?> = _statusFilter.asStateFlow()

    private val _categoryFilter = MutableStateFlow<String?>(null)
    val categoryFilter: StateFlow<String?> = _categoryFilter.asStateFlow()

    val filteredReports: StateFlow<List<ViolationReport>> = combine(
        allReports,
        _searchQuery,
        _statusFilter,
        _categoryFilter
    ) { reports, query, status, category ->
        reports.filter { report ->
            val matchesQuery = query.isBlank() ||
                    report.title.contains(query, ignoreCase = true) ||
                    report.description.contains(query, ignoreCase = true) ||
                    report.address.contains(query, ignoreCase = true) ||
                    report.ticketNumber.contains(query, ignoreCase = true) ||
                    report.category.contains(query, ignoreCase = true)

            val matchesStatus = status == null || report.status == status
            val matchesCategory = category == null || report.category == category

            matchesQuery && matchesStatus && matchesCategory
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = emptyList()
    )

    fun selectReport(id: Long?) {
        _selectedReportId.value = id
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setStatusFilter(status: String?) {
        _statusFilter.value = if (_statusFilter.value == status) null else status
    }

    fun setCategoryFilter(category: String?) {
        _categoryFilter.value = if (_categoryFilter.value == category) null else category
    }

    fun refreshLocation(context: Context) {
        viewModelScope.launch {
            _isFetchingGps.value = true
            try {
                val loc = LocationHelper.getCurrentLocation(context)
                _currentGps.value = loc
                if (formManualAddress.value.isBlank()) {
                    formManualAddress.value = loc.address
                }
            } catch (_: Exception) {
                // Keep default location
            } finally {
                _isFetchingGps.value = false
            }
        }
    }

    fun setPhotoUri(uriString: String?) {
        formPhotoUri.value = uriString
    }

    fun submitReport(context: Context, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            _isSubmitting.value = true
            try {
                val gps = _currentGps.value ?: LocationHelper.getCurrentLocation(context)
                val ticketNum = repository.generateTicketNumber()

                val newReport = ViolationReport(
                    ticketNumber = ticketNum,
                    category = formCategory.value,
                    title = formTitle.value.trim(),
                    description = formDescription.value.trim(),
                    urgency = formUrgency.value,
                    reporterName = if (formIsAnonymous.value) "Anonim" else formReporterName.value.trim().ifBlank { "Masyarakat" },
                    reporterPhone = if (formIsAnonymous.value) "" else formReporterPhone.value.trim(),
                    isAnonymous = formIsAnonymous.value,
                    latitude = gps.latitude,
                    longitude = gps.longitude,
                    accuracyMeters = gps.accuracyMeters,
                    address = formManualAddress.value.trim().ifBlank { gps.address },
                    landmark = formLandmark.value.trim(),
                    photoUri = formPhotoUri.value,
                    status = ViolationReport.STATUS_PENDING,
                    officerNotes = null,
                    timestamp = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )

                repository.submitReport(newReport)
                _submissionSuccess.value = ticketNum

                // Reset form
                resetForm()
                onSuccess(ticketNum)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    fun updateOfficerAction(reportId: Long, newStatus: String, notes: String?) {
        viewModelScope.launch {
            repository.updateStatus(reportId, newStatus, notes)
        }
    }

    fun acceptDispatch(report: ViolationReport) {
        viewModelScope.launch {
            repository.acceptDispatch(report)
        }
    }

    fun advanceDispatch(report: ViolationReport) {
        viewModelScope.launch {
            repository.advanceDispatch(report)
        }
    }

    fun deleteReport(reportId: Long) {
        viewModelScope.launch {
            repository.deleteReport(reportId)
        }
    }

    fun resetForm() {
        formTitle.value = ""
        formDescription.value = ""
        formCategory.value = ViolationReport.CAT_PKL
        formUrgency.value = ViolationReport.URGENCY_MEDIUM
        formReporterName.value = ""
        formReporterPhone.value = ""
        formIsAnonymous.value = false
        formLandmark.value = ""
        formPhotoUri.value = null
        formManualAddress.value = _currentGps.value?.address ?: "Jl. Sultan Ibrahim, Rengat, Indragiri Hulu"
        _submissionSuccess.value = null
    }
}
