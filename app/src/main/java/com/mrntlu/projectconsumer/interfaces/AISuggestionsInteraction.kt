package com.mrntlu.projectconsumer.interfaces

import com.mrntlu.projectconsumer.models.common.AISuggestion

interface AISuggestionsInteraction: Interaction<AISuggestion> {
    fun onAddToListPressed(item: AISuggestion, position: Int)
}