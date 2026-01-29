package models

case class MovieRatingUser(
                            user_id: Int,
                            movie_id: Int,
                            rating: BigDecimal,
                            langua_timestamp: Long
                          )
