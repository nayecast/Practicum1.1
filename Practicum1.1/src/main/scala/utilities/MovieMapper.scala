package utilities

import models.*

object MovieMapper {

  // Convierte Pelicula a Movie
  def toMovie(p: Pelicula): Movie =
    Movie(
      movie_id = CsvClean.i(p.id),
      mov_popularity = CsvClean.bd(p.popularity),
      mov_title = CsvClean.str(p.title),
      mov_release_date = CsvClean.date(p.release_date),
      mov_status = CsvClean.opt(p.status),
      mov_overview = CsvClean.opt(p.overview),
      mov_adult = CsvClean.b(p.adult),
      mov_budget = CsvClean.l(p.budget),
      mov_revenue = CsvClean.l(p.revenue),
      mov_runtime = p.runtime.trim.toIntOption,
      mov_vote_average = CsvClean.bd(p.vote_average),
      mov_vote_count = CsvClean.i(p.vote_count),
      mov_homepage = CsvClean.opt(p.homepage),
      mov_imdb_id = CsvClean.opt(p.imdb_id),
      mov_tagline = CsvClean.opt(p.tagline),
      mov_video = CsvClean.b(p.video),
      mov_original_title = CsvClean.str(p.original_title)
    )

  /* ===================== GENRE ===================== */

  // Extrae géneros de la película
  def toGenres(p: Pelicula): List[Genre] =
    CsvClean
      .extractIdNameList(p.genres)
      .map { case (id, name) =>
        Genre(id, name)
      }

  // Relacion Movie-Genre
  def toMovieGenres(p: Pelicula): List[MovieGenre] =
    CsvClean
      .extractIdNameList(p.genres)
      .map { case (id, _) =>
        MovieGenre(CsvClean.i(p.id), id)
      }

  // Extrae compañías de producción
  def toCompanies(p: Pelicula): List[Company] =
    CsvClean
      .extractIdNameList(p.production_companies)
      .map { case (id, name) =>
        Company(id, name)
      }

  // Relacion Movie-Company
  def toMovieCompanies(p: Pelicula): List[MovieCompany] =
    CsvClean
      .extractIdNameList(p.production_companies)
      .map { case (id, _) =>
        MovieCompany(id, CsvClean.i(p.id))
      }

  // Extrae países
  def toCountries(p: Pelicula): List[Country] =
    CsvClean.extractIsoNameList(p.production_countries)
      .map { case (iso, name) =>
        Country(iso, name)
      }

  // Relacion Movie-Country
  def toMovieCountries(p: Pelicula): List[MovieCountry] =
    CsvClean.extractIsoNameList(p.production_countries)
      .map { case (iso, _) =>
        MovieCountry(CsvClean.i(p.id), iso)
      }

  // Extrae idiomas
  def toLanguages(p: Pelicula): List[Language] =
    CsvClean
      .extractIsoNameListLan(p.spoken_languages)
      .map { case (id, name) =>
        Language(id, name)
      }

  // Relacion Movie-Language
  def toMovieLanguages(p: Pelicula): List[MovieLanguage] =
    CsvClean
      .extractIsoNameListLan(p.spoken_languages)
      .map { case (id, _) =>
        MovieLanguage(id, CsvClean.i(p.id))
      }

  // Extrae keywords
  def toKeywords(p: Pelicula): List[Keyword] =
    CsvClean.extractIdNameList(p.keywords)
      .map { case (id, name) => Keyword(id, name) }

  // Relacion Movie-Keyword
  def toMovieKeywords(p: Pelicula): List[MovieKeyword] =
    CsvClean.extractIdNameList(p.keywords)
      .map { case (id, _) => MovieKeyword(id, CsvClean.i(p.id)) }

  // Extrae cast de la película
  def toCast(p: Pelicula): List[Cast] =
    CsvClean
      .extractCastList(p.cast)
      .map { c =>
        Cast(
          cast_id = c.id,
          cast_character = c.character,
          cast_gender = c.gender,
          cast_order = c.order,
          cast_pers_name = c.name
        )
      }

  // Relacion Movie-Cast
  def toMovieCast(p: Pelicula): List[MovieCast] =
    CsvClean
      .extractCastList(p.cast)
      .map { c =>
        MovieCast(
          cast_id = c.id,
          movie_id = CsvClean.i(p.id)
        )
      }

  // Extrae crew de la película
  def toCrews(p: Pelicula): List[Crew] =
    CsvClean
      .extractCrewList(p.crew)
      .map { case (id, dep, job, name, gender) =>
        Crew(id, dep, job, name, gender)
      }

  // Relacion Movie-Crew
  def toMovieCrews(p: Pelicula): List[MovieCrew] =
    CsvClean
      .extractCrewList(p.crew)
      .map { case (id, _, _, _, _) =>
        MovieCrew(id, CsvClean.i(p.id))
      }

  // Extrae usuarios que calificaron
  def toUsers(p: Pelicula): List[User] =
    CsvClean
      .extractUserRatingList(p.ratings)
      .map { case (userId, _, _) =>
        User(userId)
      }

  // Relacion Movie-UserRating
  def toMovieRatingUsers(p: Pelicula): List[MovieRatingUser] =
    CsvClean
      .extractUserRatingList(p.ratings)
      .map { case (userId, rating, ts) =>
        MovieRatingUser(
          user_id = userId,
          movie_id = CsvClean.i(p.id),
          rating = rating,
          langua_timestamp = ts
        )
      }

  // Extrae colección a la que pertenece la película
  def toCollection(p: Pelicula): Option[Collection] =
    CsvClean.extractCollection(p.belongs_to_collection).map {
      case (id, name, poster, backdrop) =>
        Collection(id, name, poster, backdrop)
    }

  // Relacion Movie-Collection
  def toMovieCollection(p: Pelicula): Option[MovieCollection] =
    CsvClean.extractCollection(p.belongs_to_collection).map {
      case (id, _, _, _) =>
        MovieCollection(id, CsvClean.i(p.id))
    }

}
