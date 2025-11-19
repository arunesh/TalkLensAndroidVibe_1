package com.talkbox.docs.talklens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.talkbox.docs.talklens.core.common.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the main app
 */
@HiltViewModel
class TalkLensAppViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val isSetupCompleted: StateFlow<Boolean> = preferencesManager.isSetupCompleted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    suspend fun markSetupCompleted() {
        preferencesManager.setSetupCompleted(true)
    }
}
