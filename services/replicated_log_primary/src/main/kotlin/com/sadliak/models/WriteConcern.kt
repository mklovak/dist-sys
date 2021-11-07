package com.sadliak.models

data class WriteConcern(val w: Int) {
    init {
        require(w > 0) { "Write concern parameter value \"$w\" must be greater than 0" }
    }
}
