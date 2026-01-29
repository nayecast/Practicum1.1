package models

case class Collection(
                       collection_id: Int,
                       collec_name: String,
                       collec_poster_path: Option[String],
                       collec_backdrop_path: Option[String]
                     )
