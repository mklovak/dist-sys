package com.sadliak.dtos

import com.sadliak.models.NodeInfo

data class HealthResponseDto(override val status: Int = 0,
                             val data: List<NodeInfo>) : ResponseDto
