package com.ramstudio.kaskita.presentation.detailCommunity

import androidx.lifecycle.ViewModel
import com.ramstudio.kaskita.domain.repository.ICommunityRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DetailCommunityViewModel @Inject constructor(
    private val repository: ICommunityRepository
) : ViewModel() {


}