package utilities

import cats.effect.IO
import fs2.Stream
import fs2.io.file.{Files, Path}
import fs2.text
import fs2.data.csv.*
import models.*

object MovieStreams {

  // Ruta del archivo CSV con los datos completos de películas
  private val path2DataFile = "src/main/resources/data/pi_movies_complete.csv"

  // Número esperado de columnas en el CSV para validar cada fila
  private val EXPECTED_COLS = 28

  /* ===================== STREAM DE PELÍCULAS ===================== */
  /**
   * Stream que lee el CSV completo y lo transforma en objetos Pelicula.
   * Se filtran filas que no tengan el número esperado de columnas.
   */
  def peliculaStream: Stream[IO, Pelicula] =
    Files[IO]
      .readAll(Path(path2DataFile))
      .through(text.utf8.decode)
      .through(decodeWithoutHeaders[List[String]](';'))
      .collect { case row if row.size == EXPECTED_COLS =>
        Pelicula(
          adult = row(0),
          belongs_to_collection = row(1),
          budget = row(2),
          genres = row(3),
          homepage = row(4),
          id = row(5),
          imdb_id = row(6),
          original_language = row(7),
          original_title = row(8),
          overview = row(9),
          popularity = row(10),
          poster_path = row(11),
          production_companies = row(12),
          production_countries = row(13),
          release_date = row(14),
          revenue = row(15),
          runtime = row(16),
          spoken_languages = row(17),
          status = row(18),
          tagline = row(19),
          title = row(20),
          video = row(21),
          vote_average = row(22),
          vote_count = row(23),
          keywords = row(24),
          cast = row(25),
          crew = row(26),
          ratings = row(27)
        )
      }

  /* ===================== STREAMS TRANSFORMADOS ===================== */
  // Stream de películas convertido a Movie (modelo para base de datos)
  val movieStream: Stream[IO, Movie] =
    peliculaStream.map(MovieMapper.toMovie)

  // Stream de géneros únicos extraídos de todas las películas
  val genreStream: Stream[IO, Genre] =
    peliculaStream
      .flatMap(p => Stream.emits(MovieMapper.toGenres(p)))
      .evalScan(Set.empty[Int] -> Option.empty[Genre]) {
        case ((seen, _), g) =>
          if seen.contains(g.genrer_id) then IO.pure(seen -> None)
          else IO.pure((seen + g.genrer_id) -> Some(g))
      }
      .collect { case (_, Some(g)) => g }

  // Relación Movie-Genre
  val movieGenreStream: Stream[IO, MovieGenre] =
    peliculaStream.flatMap(p => Stream.emits(MovieMapper.toMovieGenres(p)))

  // Stream de compañías únicas
  val companyStream: Stream[IO, Company] =
    peliculaStream
      .flatMap(p => Stream.emits(MovieMapper.toCompanies(p)))
      .evalScan(Set.empty[Int] -> Option.empty[Company]) {
        case ((seen, _), c) =>
          if seen.contains(c.company_id) then IO.pure(seen -> None)
          else IO.pure((seen + c.company_id) -> Some(c))
      }
      .collect { case (_, Some(c)) => c }

  // Relación Movie-Company
  val movieCompanyStream: Stream[IO, MovieCompany] =
    peliculaStream.flatMap(p => Stream.emits(MovieMapper.toMovieCompanies(p)))

  // Stream de países únicos
  val countryStream: Stream[IO, Country] =
    peliculaStream
      .flatMap(p => Stream.emits(MovieMapper.toCountries(p)))
      .evalScan(Set.empty[String] -> Option.empty[Country]) {
        case ((seen, _), c) =>
          if seen.contains(c.coun_iso) then IO.pure(seen -> None)
          else IO.pure((seen + c.coun_iso) -> Some(c))
      }
      .collect { case (_, Some(c)) => c }

  // Relación Movie-Country
  val movieCountryStream: Stream[IO, MovieCountry] =
    peliculaStream.flatMap(p => Stream.emits(MovieMapper.toMovieCountries(p)))

  // Stream de idiomas únicos
  val languageStream: Stream[IO, Language] =
    peliculaStream
      .flatMap(p => Stream.emits(MovieMapper.toLanguages(p)))
      .evalScan(Set.empty[String] -> Option.empty[Language]) {
        case ((seen, _), l) =>
          if seen.contains(l.language_id) then IO.pure(seen -> None)
          else IO.pure((seen + l.language_id) -> Some(l))
      }
      .collect { case (_, Some(l)) => l }

  // Relación Movie-Language
  val movieLanguageStream: Stream[IO, MovieLanguage] =
    peliculaStream.flatMap(p => Stream.emits(MovieMapper.toMovieLanguages(p)))

  // Stream de keywords únicas
  val keywordStream: Stream[IO, Keyword] =
    peliculaStream
      .flatMap(p => Stream.emits(MovieMapper.toKeywords(p)))
      .evalScan(Set.empty[Int] -> Option.empty[Keyword]) {
        case ((seen, _), k) =>
          if seen.contains(k.keyword_id) then IO.pure(seen -> None)
          else IO.pure((seen + k.keyword_id) -> Some(k))
      }
      .collect { case (_, Some(k)) => k }

  // Relación Movie-Keyword
  val movieKeywordStream: Stream[IO, MovieKeyword] =
    peliculaStream.flatMap(p => Stream.emits(MovieMapper.toMovieKeywords(p)))

  // Stream de cast únicos
  val castStream: Stream[IO, Cast] =
    peliculaStream
      .flatMap(p => Stream.emits(MovieMapper.toCast(p)))
      .evalScan(Set.empty[Int] -> Option.empty[Cast]) {
        case ((seen, _), c) =>
          if seen.contains(c.cast_id) then IO.pure(seen -> None)
          else IO.pure((seen + c.cast_id) -> Some(c))
      }
      .collect { case (_, Some(c)) => c }

  // Relación Movie-Cast
  val movieCastStream: Stream[IO, MovieCast] =
    peliculaStream.flatMap(p => Stream.emits(MovieMapper.toMovieCast(p)))

  // Stream de crew únicos
  val crewStream: Stream[IO, Crew] =
    peliculaStream
      .flatMap(p => Stream.emits(MovieMapper.toCrews(p)))
      .evalScan(Set.empty[Int] -> Option.empty[Crew]) {
        case ((seen, _), c) =>
          if seen.contains(c.crew_id) then IO.pure(seen -> None)
          else IO.pure((seen + c.crew_id) -> Some(c))
      }
      .collect { case (_, Some(c)) => c }

  // Relación Movie-Crew
  val movieCrewStream: Stream[IO, MovieCrew] =
    peliculaStream.flatMap(p => Stream.emits(MovieMapper.toMovieCrews(p)))

  // Stream de usuarios únicos
  val userStream: Stream[IO, User] =
    peliculaStream
      .flatMap(p => Stream.emits(MovieMapper.toUsers(p)))
      .evalScan(Set.empty[Int] -> Option.empty[User]) {
        case ((seen, _), u) =>
          if seen.contains(u.user_id) then IO.pure(seen -> None)
          else IO.pure((seen + u.user_id) -> Some(u))
      }
      .collect { case (_, Some(u)) => u }

  // Relación Movie-Ratings 
  val movieRatingUserStream: Stream[IO, MovieRatingUser] =
    peliculaStream.flatMap(p => Stream.emits(MovieMapper.toMovieRatingUsers(p)))

  // Stream de colecciones únicas
  val collectionStream: Stream[IO, Collection] =
    peliculaStream
      .flatMap(p => Stream.emits(MovieMapper.toCollection(p).toList))
      .evalScan(Set.empty[Int] -> Option.empty[Collection]) {
        case ((seen, _), c) =>
          if seen.contains(c.collection_id) then IO.pure(seen -> None)
          else IO.pure((seen + c.collection_id) -> Some(c))
      }
      .collect { case (_, Some(c)) => c }

  // Relación Movie-Collection
  val movieCollectionStream: Stream[IO, MovieCollection] =
    peliculaStream.flatMap(p => Stream.emits(MovieMapper.toMovieCollection(p).toList))
}
