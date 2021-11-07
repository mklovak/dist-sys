package com.sadliak.models

data class WriteConcern(val value: Int) {
    init {
        require(value > 0) { "Write concern parameter value \"$value\" must be greater than 0" }
    }
}
