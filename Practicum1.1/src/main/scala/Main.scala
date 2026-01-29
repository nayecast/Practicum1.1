import cats.effect.{IO, IOApp, Ref}
import dao.MovieDAO
import db.Database
import utilities.{MovieStreams, MovieStats}
import doobie.implicits._

object Main extends IOApp.Simple {

  // Contador de registros insertados por cada tipo de entidad en la base de datos
  case class Contadores(
                         movies: Int = 0,
                         genres: Int = 0,
                         companies: Int = 0,
                         countries: Int = 0,
                         languages: Int = 0,
                         keywords: Int = 0,
                         cast: Int = 0,
                         crew: Int = 0,
                         users: Int = 0,
                         collections: Int = 0,
                         ratings: Int = 0
                       )

  // ===================== CÁLCULO DE ESTADÍSTICAS DEL DATASET =====================
  // Esta función lee los streams de películas y realiza cálculos estadísticos sobre
  // budgets, revenues, popularidad, duración, votos y frecuencia de idiomas.
  private def calcularEstadisticas: IO[Unit] =
    for {
      movies <- MovieStreams.movieStream.compile.toList
      peliculas <- MovieStreams.peliculaStream.compile.toList

      // Filtra películas únicas
      moviesUnique = movies.groupBy(_.movie_id).map(_._2.head).toList

      // Extrae métricas numéricas para análisis estadístico
      budgets = moviesUnique.map(_.mov_budget.toDouble).filter(_ > 0)
      revenues = moviesUnique.map(_.mov_revenue.toDouble).filter(_ > 0)
      popularities = moviesUnique.map(_.mov_popularity.toDouble).filter(_ > 0)
      runtimes = moviesUnique.flatMap(_.mov_runtime).map(_.toDouble).filter(_ > 0)
      voteAverages = moviesUnique.map(_.mov_vote_average.toDouble).filter(_ > 0)
      voteCounts = moviesUnique.map(_.mov_vote_count.toDouble).filter(_ > 0)
      languages = peliculas.map(_.original_language).filter(_.trim.nonEmpty)

      // Calcula estadísticas básicas y frecuencias
      budgetStats = MovieStats.calcularEstadisticas(budgets)
      revenueStats = MovieStats.calcularEstadisticas(revenues)
      popularityStats = MovieStats.calcularEstadisticas(popularities)
      runtimeStats = MovieStats.calcularEstadisticas(runtimes)
      voteAverageStats = MovieStats.calcularEstadisticas(voteAverages)
      voteCountStats = MovieStats.calcularEstadisticas(voteCounts)
      languageFreq = MovieStats.calcularFrecuencias(languages)
      corr = MovieStats.correlacion(budgets, revenues)

      // Imprime resumen estadístico completo
      _ <- IO(MovieStats.imprimirResumenCompleto(
        budgetStats, revenueStats, popularityStats,
        runtimeStats, voteAverageStats, voteCountStats,
        languageFreq, movies.size, moviesUnique.size, corr
      ))
    } yield ()

  // ===================== PROCESO DE CARGA EN BASE DE DATOS =====================
  // Esta función realiza la inserción de todas las entidades en la base de datos
  // utilizando streams y actualiza un contador reactivo para mostrar progreso en vivo
  override def run: IO[Unit] =
    for {
      _ <- IO.println("="*70)
      _ <- IO.println("=== CALCULANDO ESTADÍSTICAS DEL DATASET ===")
      _ <- IO.println("="*70)
      _ <- calcularEstadisticas

      _ <- IO.println("\n" + "="*70)
      _ <- IO.println("=== INICIANDO CARGA EN BASE DE DATOS ===")
      _ <- IO.println("="*70 + "\n")

      // Ref para llevar el conteo reactivo de registros insertados
      contadoresRef <- Ref.of[IO, Contadores](Contadores())

      _ <- Database.mysqlTransactor(runtime).use { xa =>
        for {

          // Inserta películas en lotes de 200 y actualiza contador en tiempo real
          _ <- MovieStreams.movieStream
            .chunkN(200)
            .evalMap { chunk =>
              val uniqueMovies = chunk.toList.groupBy(_.movie_id).map(_._2.head).toList
              MovieDAO.insertMovies(uniqueMovies).transact(xa) >>
                contadoresRef.updateAndGet(c => c.copy(movies = c.movies + uniqueMovies.size))
                  .flatMap(c => IO.print(s"\rMovies insertadas: ${c.movies}"))
            }
            .compile.drain >> IO.println("")

          // Inserta géneros y actualiza contador en tiempo real
          _ <- MovieStreams.genreStream
            .chunkN(200)
            .evalMap { chunk =>
              val uniqueGenres = chunk.toList.groupBy(_.genrer_id).map(_._2.head).toList
              MovieDAO.insertGenres(uniqueGenres).transact(xa) >>
                contadoresRef.updateAndGet(c => c.copy(genres = c.genres + uniqueGenres.size))
                  .flatMap(c => IO.print(s"\rGenres insertadas: ${c.genres}"))
            }
            .compile.drain >> IO.println("")

          // Inserta relaciones Movie-Genres
          _ <- MovieStreams.movieGenreStream
            .chunkN(200)
            .evalMap(chunk => MovieDAO.insertMovieGenres(chunk.toList).transact(xa))
            .compile.drain

          // Inserta compañías y actualiza contador
          _ <- MovieStreams.companyStream
            .chunkN(200)
            .evalMap { chunk =>
              val uniqueCompanies = chunk.toList.groupBy(_.company_id).map(_._2.head).toList
              MovieDAO.insertCompanies(uniqueCompanies).transact(xa) >>
                contadoresRef.updateAndGet(c => c.copy(companies = c.companies + uniqueCompanies.size))
                  .flatMap(c => IO.print(s"\rCompanies insertadas: ${c.companies}"))
            }
            .compile.drain >> IO.println("")

          // Inserta relaciones Movie-Companies
          _ <- MovieStreams.movieCompanyStream
            .chunkN(200)
            .evalMap(chunk => MovieDAO.insertMovieCompanies(chunk.toList).transact(xa))
            .compile.drain

          // Inserta países y actualiza contador
          _ <- MovieStreams.countryStream
            .chunkN(200)
            .evalMap { chunk =>
              val uniqueCountries = chunk.toList.groupBy(_.coun_iso).map(_._2.head).toList
              MovieDAO.insertCountries(uniqueCountries).transact(xa) >>
                contadoresRef.updateAndGet(c => c.copy(countries = c.countries + uniqueCountries.size))
                  .flatMap(c => IO.print(s"\rCountries insertadas: ${c.countries}"))
            }
            .compile.drain >> IO.println("")

          // Inserta relaciones Movie-Countries
          _ <- MovieStreams.movieCountryStream
            .chunkN(200)
            .evalMap(chunk => MovieDAO.insertMovieCountries(chunk.toList).transact(xa))
            .compile.drain

          // Inserta idiomas y actualiza contador
          _ <- MovieStreams.languageStream
            .chunkN(200)
            .evalMap { chunk =>
              val uniqueLanguages = chunk.toList.groupBy(_.language_id).map(_._2.head).toList
              MovieDAO.insertLanguages(uniqueLanguages).transact(xa) >>
                contadoresRef.updateAndGet(c => c.copy(languages = c.languages + uniqueLanguages.size))
                  .flatMap(c => IO.print(s"\rLanguages insertadas: ${c.languages}"))
            }
            .compile.drain >> IO.println("")

          // Inserta relaciones Movie-Languages
          _ <- MovieStreams.movieLanguageStream
            .chunkN(200)
            .evalMap(chunk => MovieDAO.insertMovieLanguages(chunk.toList).transact(xa))
            .compile.drain

          // Inserta keywords y actualiza contador
          _ <- MovieStreams.keywordStream
            .chunkN(200)
            .evalMap { chunk =>
              val uniqueKeywords = chunk.toList.groupBy(_.keyword_id).map(_._2.head).toList
              MovieDAO.insertKeywords(uniqueKeywords).transact(xa) >>
                contadoresRef.updateAndGet(c => c.copy(keywords = c.keywords + uniqueKeywords.size))
                  .flatMap(c => IO.print(s"\rKeywords insertadas: ${c.keywords}"))
            }
            .compile.drain >> IO.println("")

          // Inserta relaciones Movie-Keywords
          _ <- MovieStreams.movieKeywordStream
            .chunkN(200)
            .evalMap(chunk => MovieDAO.insertMovieKeywords(chunk.toList).transact(xa))
            .compile.drain

          // Inserta cast y actualiza contador
          _ <- MovieStreams.castStream
            .chunkN(200)
            .evalMap { chunk =>
              val uniqueCast = chunk.toList.groupBy(_.cast_id).map(_._2.head).toList
              MovieDAO.insertCasts(uniqueCast).transact(xa) >>
                contadoresRef.updateAndGet(c => c.copy(cast = c.cast + uniqueCast.size))
                  .flatMap(c => IO.print(s"\rCast insertadas: ${c.cast}"))
            }
            .compile.drain >> IO.println("")

          // Inserta relaciones Movie-Cast
          _ <- MovieStreams.movieCastStream
            .chunkN(200)
            .evalMap(chunk => MovieDAO.insertMovieCasts(chunk.toList).transact(xa))
            .compile.drain

          // Inserta crew y actualiza contador
          _ <- MovieStreams.crewStream
            .chunkN(200)
            .evalMap { chunk =>
              val uniqueCrew = chunk.toList.groupBy(_.crew_id).map(_._2.head).toList
              MovieDAO.insertCrews(uniqueCrew).transact(xa) >>
                contadoresRef.updateAndGet(c => c.copy(crew = c.crew + uniqueCrew.size))
                  .flatMap(c => IO.print(s"\rCrew insertadas: ${c.crew}"))
            }
            .compile.drain >> IO.println("")

          // Inserta relaciones Movie-Crew
          _ <- MovieStreams.movieCrewStream
            .chunkN(200)
            .evalMap(chunk => MovieDAO.insertMovieCrews(chunk.toList).transact(xa))
            .compile.drain

          // Inserta usuarios y actualiza contador
          _ <- MovieStreams.userStream
            .chunkN(500)
            .evalMap { chunk =>
              val uniqueUsers = chunk.toList.groupBy(_.user_id).map(_._2.head).toList
              MovieDAO.insertUsers(uniqueUsers).transact(xa) >>
                contadoresRef.updateAndGet(c => c.copy(users = c.users + uniqueUsers.size))
                  .flatMap(c => IO.print(s"\rUsers insertadas: ${c.users}"))
            }
            .compile.drain >> IO.println("")

          // Inserta Movie Ratings y actualiza contador
          _ <- MovieStreams.movieRatingUserStream
            .chunkN(500)
            .evalMap { chunk =>
              MovieDAO.insertMovieRatings(chunk.toList).transact(xa) >>
                contadoresRef.updateAndGet(c => c.copy(ratings = c.ratings + chunk.size))
                  .flatMap(c => IO.print(s"\rMovie Ratings insertadas: ${c.ratings}"))
            }
            .compile.drain >> IO.println("")

          // Inserta colecciones y actualiza contador
          _ <- MovieStreams.collectionStream
            .chunkN(200)
            .evalMap { chunk =>
              val uniqueCollections = chunk.toList.groupBy(_.collection_id).map(_._2.head).toList
              MovieDAO.insertCollections(uniqueCollections).transact(xa) >>
                contadoresRef.updateAndGet(c => c.copy(collections = c.collections + uniqueCollections.size))
                  .flatMap(c => IO.print(s"\rCollections insertadas: ${c.collections}"))
            }
            .compile.drain >> IO.println("")

          // Inserta relaciones Movie-Collections
          _ <- MovieStreams.movieCollectionStream
            .chunkN(200)
            .evalMap(chunk => MovieDAO.insertMovieCollections(chunk.toList).transact(xa))
            .compile.drain

          // ================== RESUMEN FINAL ==================
          finalCounts <- contadoresRef.get
          _ <- IO.println("\n" + "="*70)
          _ <- IO.println("=== RESUMEN DE CARGA ===")
          _ <- IO.println("="*70)
          _ <- IO.println(s"Movies:        ${finalCounts.movies}")
          _ <- IO.println(s"Genres:        ${finalCounts.genres}")
          _ <- IO.println(s"Companies:     ${finalCounts.companies}")
          _ <- IO.println(s"Countries:     ${finalCounts.countries}")
          _ <- IO.println(s"Languages:     ${finalCounts.languages}")
          _ <- IO.println(s"Keywords:      ${finalCounts.keywords}")
          _ <- IO.println(s"Cast:          ${finalCounts.cast}")
          _ <- IO.println(s"Crew:          ${finalCounts.crew}")
          _ <- IO.println(s"Users:         ${finalCounts.users}")
          _ <- IO.println(s"Movie Ratings: ${finalCounts.ratings}")
          _ <- IO.println(s"Collections:   ${finalCounts.collections}")
          _ <- IO.println("="*70)
          _ <- IO.println("\n✓ Carga completa exitosamente")

        } yield ()
      }
    } yield ()
}
