package com.example.frontzephiro.models

data class Content(
    val id: Long,
    val name: String,
    val description: String,
    val author: String,
    val source: String,
    val language: String,
    val url: String,
    val imagePath: String,
    val tags: List<Tag>
)
