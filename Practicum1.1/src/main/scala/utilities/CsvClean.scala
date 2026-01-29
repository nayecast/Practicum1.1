package utilities

import scala.util.Try
import java.time.LocalDate
import models.RawCast

object CsvClean {

  /* ===================== UTILIDADES BÁSICAS DE LIMPIEZA ===================== */

  /** Normaliza un string: quita espacios y maneja null */
  private def norm(s: String): String =
    Option(s).map(_.trim).getOrElse("")

  /** Devuelve el string limpio */
  def str(s: String): String = norm(s)

  /** Devuelve Some(string) si no está vacío, None si está vacío o null */
  def opt(s: String): Option[String] = Option(norm(s)).filter(_.nonEmpty)

  /** Convierte a Int, devuelve 0 si falla */
  def i(s: String): Int = Try(norm(s).toInt).getOrElse(0)

  /** Convierte a Long, devuelve 0L si falla */
  def l(s: String): Long = Try(norm(s).toLong).getOrElse(0L)

  /** Convierte a BigDecimal, devuelve 0 si falla */
  def bd(s: String): BigDecimal = Try(BigDecimal(norm(s))).getOrElse(BigDecimal(0))

  /** Convierte a Boolean ("true"/"1" -> true, cualquier otra cosa -> false) */
  def b(s: String): Boolean =
    norm(s).equalsIgnoreCase("true") || norm(s) == "1"

  /** Convierte a fecha en formato yyyy-MM-dd, devuelve "1900-01-01" si falla */
  def date(s: String): String =
    Try(LocalDate.parse(norm(s))).map(_.toString).getOrElse("1900-01-01")

  /* ===================== EXTRACCIÓN DE LISTAS JSON ===================== */

  /** Extrae lista de (id, name) desde un JSON en string */
  def extractIdNameList(raw: String): List[(Int, String)] =
    if raw == null || raw.trim.isEmpty then Nil
    else
      val fixed = raw.replace("'", "\"").trim
      io.circe.parser.parse(fixed).toOption
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)
        .flatMap { json =>
          for
            id <- json.hcursor.get[Int]("id").toOption
            name <- json.hcursor.get[String]("name").toOption
          yield (id, name)
        }
        .toList

  /** Extrae lista de (iso_3166_1, name) desde un JSON en string (para países) */
  def extractIsoNameList(raw: String): List[(String, String)] =
    if raw == null || raw.trim.isEmpty then Nil
    else
      val fixed = raw.replace("'", "\"").trim
      io.circe.parser.parse(fixed).toOption
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)
        .flatMap { json =>
          for
            iso <- json.hcursor.get[String]("iso_3166_1").toOption
            name <- json.hcursor.get[String]("name").toOption
          yield (iso, name)
        }
        .toList

  /** Extrae lista de (iso_639_1, name) desde un JSON en string (para idiomas) */
  def extractIsoNameListLan(raw: String): List[(String, String)] =
    if raw == null || raw.trim.isEmpty then Nil
    else
      val fixed = raw.replace("'", "\"").trim
      io.circe.parser.parse(fixed).toOption
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)
        .flatMap { json =>
          for
            iso <- json.hcursor.get[String]("iso_639_1").toOption
            name <- json.hcursor.get[String]("name").toOption
          yield (iso, name)
        }
        .toList

  /** Extrae lista de objetos RawCast desde un JSON en string */
  def extractCastList(raw: String): List[RawCast] =
    if raw == null || raw.trim.isEmpty then Nil
    else
      val fixed = raw.replace("'", "\"").trim
      io.circe.parser.parse(fixed).toOption
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)
        .flatMap { json =>
          for
            id <- json.hcursor.get[Int]("id").toOption
            name <- json.hcursor.get[String]("name").toOption
            character <- json.hcursor.get[String]("character").toOption
            gender <- json.hcursor.get[Int]("gender").toOption
            order <- json.hcursor.get[Int]("order").toOption
          yield RawCast(id, name, character, gender, order)
        }
        .toList

  /** Extrae lista de crew desde JSON en string: (id, department, job, name, gender) */
  def extractCrewList(raw: String): List[(Int, String, String, String, Int)] =
    if raw == null || raw.trim.isEmpty then Nil
    else
      val fixed = raw.replace("'", "\"").trim
      io.circe.parser.parse(fixed).toOption
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)
        .flatMap { json =>
          for
            id <- json.hcursor.get[Int]("id").toOption
            department <- json.hcursor.get[String]("department").toOption
            job <- json.hcursor.get[String]("job").toOption
            name <- json.hcursor.get[String]("name").toOption
            gender <- json.hcursor.get[Int]("gender").toOption
          yield (id, department, job, name, gender)
        }
        .toList

  /** Extrae lista de ratings de usuarios: (userId, rating, timestamp) */
  def extractUserRatingList(raw: String): List[(Int, BigDecimal, Long)] =
    if raw == null || raw.trim.isEmpty then Nil
    else
      val fixed = raw.replace("'", "\"").trim
      io.circe.parser.parse(fixed).toOption
        .flatMap(_.asArray)
        .getOrElse(Vector.empty)
        .flatMap { json =>
          for
            userId <- json.hcursor.get[Int]("userId").toOption
            rating <- json.hcursor.get[BigDecimal]("rating").toOption
            ts <- json.hcursor.get[Long]("timestamp").toOption
          yield (userId, rating, ts)
        }
        .toList

  /** Extrae colección de película desde JSON, con id, nombre, poster y backdrop */
  def extractCollection(raw: String): Option[(Int, String, Option[String], Option[String])] =
    if raw == null || raw.trim.isEmpty then None
    else
      val fixed = raw.replace("'", "\"").trim
      io.circe.parser.parse(fixed).toOption.flatMap { json =>
        for
          id <- json.hcursor.get[Int]("id").toOption
          name <- json.hcursor.get[String]("name").toOption
        yield (
          id,
          name,
          json.hcursor.get[String]("poster_path").toOption,
          json.hcursor.get[String]("backdrop_path").toOption
        )
      }

}
