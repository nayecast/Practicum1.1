package models

case class Movie(
                  movie_id: Int,
                  mov_popularity: BigDecimal,
                  mov_title: String,
                  mov_release_date: String,
                  mov_status: Option[String],
                  mov_overview: Option[String],
                  mov_adult: Boolean,
                  mov_budget: Long,
                  mov_revenue: Long,
                  mov_runtime: Option[Int],
                  mov_vote_average: BigDecimal,
                  mov_vote_count: Int,
                  mov_homepage: Option[String],
                  mov_imdb_id: Option[String],
                  mov_tagline: Option[String],
                  mov_video: Boolean,
                  mov_original_title: String
                )
