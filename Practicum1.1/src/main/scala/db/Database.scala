package db

import cats.effect._
import doobie.hikari.HikariTransactor
import doobie.util.ExecutionContexts
import scala.concurrent.ExecutionContext

object Database {

  // Definimos un pool de hilos para las operaciones bloqueantes de JDBC
  private val connectionContext: Resource[IO, ExecutionContext] =
    ExecutionContexts.fixedThreadPool[IO](64) // Aumentado para alto tráfico
  
  /**
   * Genera un Transactor para MySQL
   */
  def mysqlTransactor(implicit runtime: unsafe.IORuntime): Resource[IO, HikariTransactor[IO]] = for {
    ce <- connectionContext
    xa <- HikariTransactor.newHikariTransactor[IO](
      "com.mysql.cj.jdbc.Driver",
      "jdbc:mysql://localhost:3306/movies",
      "PRACTICUM",
      "companypracticum1",
      ce
    )
    _ <- Resource.eval(xa.configure { ds =>
      IO.delay {
        ds.setMaximumPoolSize(50)
        ds.setConnectionTimeout(30000)
      }
    })
  } yield xa
}