package com.francosoft.kampalacleantoilets.data.models

data class Review(
    val id: Int,
    val toiletId: Int,
    val username: String,
    val rating: Int,
    val review: String,
    val date: String
)
