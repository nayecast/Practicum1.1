package dao

import doobie.*
import doobie.implicits.*
import cats.implicits.*
import models.*

object MovieDAO {

  /* ===================== MOVIE ===================== */

  // Inserta una película
  def insertMovie(movie: Movie): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO movie (
        movie_id, mov_popularity, mov_title,
        mov_release_date, mov_status, mov_overview, mov_adult,
        mov_budget, mov_revenue, mov_runtime, mov_vote_average,
        mov_vote_count, mov_homepage, mov_imdb_id, mov_tagline,
        mov_video, mov_original_title
      ) VALUES (
        ${movie.movie_id}, ${movie.mov_popularity}, ${movie.mov_title},
        ${movie.mov_release_date}, ${movie.mov_status}, ${movie.mov_overview},
        ${movie.mov_adult}, ${movie.mov_budget}, ${movie.mov_revenue},
        ${movie.mov_runtime}, ${movie.mov_vote_average}, ${movie.mov_vote_count},
        ${movie.mov_homepage}, ${movie.mov_imdb_id}, ${movie.mov_tagline},
        ${movie.mov_video}, ${movie.mov_original_title}
      )
    """.update.run

  // Inserta varias películas en batch
  def insertMovies(movies: List[Movie]): ConnectionIO[List[Int]] =
    movies.traverse(insertMovie)

  /* ===================== COLLECTION ===================== */

  // Inserta colección
  def insertCollection(c: Collection): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO collections (
        collection_id, collec_name, collec_poster_path, collec_backdrop_path
      ) VALUES (
        ${c.collection_id}, ${c.collec_name},
        ${c.collec_poster_path}, ${c.collec_backdrop_path}
      )
    """.update.run

  // Inserta múltiples colecciones
  def insertCollections(cs: List[Collection]): ConnectionIO[List[Int]] =
    cs.traverse(insertCollection)

  /* ===================== GENRE ===================== */

  // Inserta un género
  def insertGenre(g: Genre): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO genre (genrer_id, gen_name)
      VALUES (${g.genrer_id}, ${g.gen_name})
    """.update.run

  // Inserta múltiples géneros
  def insertGenres(gs: List[Genre]): ConnectionIO[List[Int]] =
    gs.traverse(insertGenre)

  /* ===================== COMPANY ===================== */

  // Inserta compañía
  def insertCompany(c: Company): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO company (company_id, compa_name)
      VALUES (${c.company_id}, ${c.compa_name})
    """.update.run

  // Inserta múltiples compañías
  def insertCompanies(cs: List[Company]): ConnectionIO[List[Int]] =
    cs.traverse(insertCompany)

  /* ===================== COUNTRY ===================== */

  // Inserta país
  def insertCountry(c: Country): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO country (coun_iso, coun_name)
      VALUES (${c.coun_iso}, ${c.coun_name})
    """.update.run

  // Inserta múltiples países
  def insertCountries(cs: List[Country]): ConnectionIO[List[Int]] =
    cs.traverse(insertCountry)

  /* ===================== LANGUAGE ===================== */

  // Inserta idioma
  def insertLanguage(l: Language): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO language (language_id, langu_name)
      VALUES (${l.language_id}, ${l.langu_name})
    """.update.run

  // Inserta múltiples idiomas
  def insertLanguages(ls: List[Language]): ConnectionIO[List[Int]] =
    ls.traverse(insertLanguage)

  /* ===================== KEYWORD ===================== */

  // Inserta keyword
  def insertKeyword(k: Keyword): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO keywords (keyword_id, key_name)
      VALUES (${k.keyword_id}, ${k.key_name})
    """.update.run

  // Inserta múltiples keywords
  def insertKeywords(ks: List[Keyword]): ConnectionIO[List[Int]] =
    ks.traverse(insertKeyword)

  /* ===================== CAST ===================== */

  // Inserta cast
  def insertCast(c: Cast): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO cast (
        cast_id, cast_character, cast_gender, cast_order, cast_pers_name
      ) VALUES (
        ${c.cast_id}, ${c.cast_character},
        ${c.cast_gender}, ${c.cast_order}, ${c.cast_pers_name}
      )
    """.update.run

  // Inserta múltiples cast
  def insertCasts(cs: List[Cast]): ConnectionIO[List[Int]] =
    cs.traverse(insertCast)

  /* ===================== CREW ===================== */

  // Inserta crew
  def insertCrew(c: Crew): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO crew (
        crew_id, crew_department, crew_job, crew_pers_name, crew_gender
      ) VALUES (
        ${c.crew_id}, ${c.crew_department},
        ${c.crew_job}, ${c.crew_pers_name}, ${c.crew_gender}
      )
    """.update.run

  // Inserta múltiples crew
  def insertCrews(cs: List[Crew]): ConnectionIO[List[Int]] =
    cs.traverse(insertCrew)

  /* ===================== USER ===================== */

  // Inserta usuario
  def insertUser(u: User): ConnectionIO[Int] =
    sql""" INSERT IGNORE INTO user (user_id) VALUES (${u.user_id}) """.update.run

  // Inserta múltiples usuarios
  def insertUsers(us: List[User]): ConnectionIO[List[Int]] =
    us.traverse(insertUser)

  /* ===================== MOVIE RATING USER ===================== */

  // Inserta calificación de usuario
  def insertMovieRating(m: MovieRatingUser): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO movie_rating_user (
        movie_id, user_id, rating, timestamp
      ) VALUES (
        ${m.movie_id}, ${m.user_id},
        ${m.rating}, ${m.langua_timestamp}
      )
    """.update.run

  // Inserta múltiples calificaciones
  def insertMovieRatings(ms: List[MovieRatingUser]): ConnectionIO[List[Int]] =
    ms.traverse(insertMovieRating)

  /* ===================== MOVIE_GENRE ===================== */

  // Inserta relación movie-genre
  def insertMovieGenre(mg: MovieGenre): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO movie_genre (movie_id, genrer_id)
      VALUES (${mg.movie_id}, ${mg.genrer_id})
    """.update.run

  // Inserta múltiples relaciones movie-genre
  def insertMovieGenres(mgs: List[MovieGenre]): ConnectionIO[List[Int]] =
    mgs.traverse(insertMovieGenre)

  /* ===================== MOVIE_COMPANY ===================== */

  // Inserta relación movie-company
  def insertMovieCompany(mc: MovieCompany): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO movie_company (movie_id, company_id)
      VALUES (${mc.movie_id}, ${mc.company_id})
    """.update.run

  // Inserta múltiples relaciones movie-company
  def insertMovieCompanies(mcs: List[MovieCompany]): ConnectionIO[List[Int]] =
    mcs.traverse(insertMovieCompany)

  /* ===================== MOVIE_COUNTRY ===================== */

  // Inserta relación movie-country
  def insertMovieCountry(mc: MovieCountry): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO movie_country (movie_id, coun_iso)
      VALUES (${mc.movie_id}, ${mc.coun_iso})
    """.update.run

  // Inserta múltiples relaciones movie-country
  def insertMovieCountries(mcs: List[MovieCountry]): ConnectionIO[List[Int]] =
    mcs.traverse(insertMovieCountry)

  /* ===================== MOVIE_LANGUAGE ===================== */

  // Inserta relación movie-language
  def insertMovieLanguage(ml: MovieLanguage): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO movie_language (movie_id, language_id)
      VALUES (${ml.movie_id}, ${ml.language_id})
    """.update.run

  // Inserta múltiples relaciones movie-language
  def insertMovieLanguages(mls: List[MovieLanguage]): ConnectionIO[List[Int]] =
    mls.traverse(insertMovieLanguage)

  /* ===================== MOVIE_COLLECTION ===================== */

  // Inserta relación movie-collection
  def insertMovieCollection(mc: MovieCollection): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO movie_collection (movie_id, collection_id)
      VALUES (${mc.movie_id}, ${mc.collection_id})
    """.update.run

  // Inserta múltiples relaciones movie-collection
  def insertMovieCollections(mcs: List[MovieCollection]): ConnectionIO[List[Int]] =
    mcs.traverse(insertMovieCollection)

  /* ===================== MOVIE_KEYWORD ===================== */

  // Inserta relación movie-keyword
  def insertMovieKeyword(mk: MovieKeyword): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO movie_keyword (movie_id, keyword_id)
      VALUES (${mk.movie_id}, ${mk.keyword_id})
    """.update.run

  // Inserta múltiples relaciones movie-keyword
  def insertMovieKeywords(mks: List[MovieKeyword]): ConnectionIO[List[Int]] =
    mks.traverse(insertMovieKeyword)

  /* ===================== MOVIE_CAST ===================== */

  // Inserta relación movie-cast
  def insertMovieCast(mc: MovieCast): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO movie_cast (movie_id, cast_id)
      VALUES (${mc.movie_id}, ${mc.cast_id})
    """.update.run

  // Inserta múltiples relaciones movie-cast
  def insertMovieCasts(mcs: List[MovieCast]): ConnectionIO[List[Int]] =
    mcs.traverse(insertMovieCast)

  /* ===================== MOVIE_CREW ===================== */

  // Inserta relación movie-crew
  def insertMovieCrew(mc: MovieCrew): ConnectionIO[Int] =
    sql"""
      INSERT IGNORE INTO movie_crew (movie_id, crew_id)
      VALUES (${mc.movie_id}, ${mc.crew_id})
    """.update.run

  // Inserta múltiples relaciones movie-crew
  def insertMovieCrews(mcs: List[MovieCrew]): ConnectionIO[List[Int]] =
    mcs.traverse(insertMovieCrew)

}
