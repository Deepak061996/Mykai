package com.mykaimeal.planner.fragment.mainfragment.commonscreen.instacart

data class GeminiResponse(
    val candidates: List<Candidate>
)

data class Candidate(
    val content: Content
)
