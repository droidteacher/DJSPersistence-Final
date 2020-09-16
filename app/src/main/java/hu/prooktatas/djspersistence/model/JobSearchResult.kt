package hu.prooktatas.djspersistence.model

import java.io.Serializable

data class JobSearchResult(
    val id: String,
    val type: String,
    val url: String,
    val created_at: String,
    val company: String,
    val company_url: String,
    val location: String,
    val title: String
): Serializable {
    var favorite = false
}