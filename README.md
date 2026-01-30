# Practicum1.1
# ProyectoIntegrador_PFR
Repositorio del proyecto integrador - Programación funcional y reactiva

# Análisis de Datos de Películas con Programación Funcional Reactiva (FRP)

## 1. Descripción del Dataset

El dataset utilizado es **`pi_movies_complete.csv`**, ubicado en la ruta `src/main/resources/`.  
El archivo está separado por punto y coma (`;`) y contiene información financiera, técnica y de popularidad de películas.

El dataset incluye columnas numéricas, columnas de texto y columnas en formato JSON.  
Para este análisis no se consideran columnas en formato JSON, ya que requieren un tratamiento distinto.

---

## 2. Tabla de Datos

A continuación, se detallan las columnas utilizadas en el análisis:

| Nombre de la columna | Tipo de dato | Propósito | Observaciones |
|---------------------|-------------|-----------|---------------|
| budget | Numérico (Double) | Representa el presupuesto de la película | Se eliminan valores nulos y ceros |
| revenue | Numérico (Double) | Representa los ingresos generados | Se eliminan valores nulos y ceros |
| vote_average | Numérico (Double) | Calificación promedio de usuarios | Valores entre 0 y 10 |
| original_language | Texto (String) | Idioma original de la película | Usada para análisis de frecuencia |

Columnas como `genres`, `cast`, `crew` y otras en formato JSON no son consideradas en este trabajo.

---

## 3. Lectura de Columnas Numéricas

- El archivo CSV se lee desde la carpeta de recursos del proyecto utilizando `scala.io.Source`.  
- Las columnas numéricas se extraen dinámicamente a partir del nombre de la columna y se convierten a tipo `Double`, descartando valores no numéricos o inválidos.
- La lectura de datos se maneja de forma reactiva mediante **REScala**, permitiendo que los datos se propaguen automáticamente a los análisis posteriores.
En el siguiente fragmento del codigo es donde se evidencia este punto:
```scala
val rawLines = Var(List[String]())

rawLines.set(Source.fromFile(filePath).getLines().toList)

val headers = Signal {
  rawLines.value.headOption.map(_.split(";").toList).getOrElse(Nil)
}

val data = Signal {
  rawLines.value.drop(1).map(_.split(";", -1).toList)
}
```
En el codigo se lee el CSV desde resources, se separa por ; y se almacenan las filas de forma reactiva.

---

## 4. Análisis de Columnas Numéricas

Para las columnas `budget`, `revenue` y `vote_average` se calculan las siguientes estadísticas básicas:

- Media
- Valor mínimo
- Valor máximo

Estas métricas se recalculan automáticamente si los datos cambian, gracias al uso de señales reactivas.

Esto se cumple en el siguiente fragmento de codigo:

```scala
def numericColumn(name: String): Signal[List[Double]] = Signal {
  val i = colIndex(name).value
  if (i < 0) Nil
  else data.value.flatMap(r =>
    Try(r(i).toDouble).toOption
  ).filter(_ > 0)
}

def stats(col: Signal[List[Double]]): Signal[(Double, Double, Double)] = Signal {
  val xs = col.value
  if (xs.isEmpty) (0.0, 0.0, 0.0)
  else (xs.sum / xs.size, xs.min, xs.max)
}

val budgetStats = stats(numericColumn("budget"))
val revenueStats = stats(numericColumn("revenue"))
val ratingStats = stats(numericColumn("vote_average"))

```

---

## 5. Análisis de Columnas Tipo Texto

Se realiza un análisis de frecuencia sobre la columna `original_language`, con el objetivo de identificar la distribución de idiomas presentes en el dataset.

Las columnas en formato JSON no se incluyen en este análisis.

En este fragmento de codigo se cumple:

```scala
def textColumn(name: String): Signal[List[String]] = Signal {
  val i = colIndex(name).value
  if (i < 0) Nil
  else data.value.map(_(i)).filter(_.nonEmpty)
}

val languageFreq = Signal {
  textColumn("original_language").value
    .groupBy(identity)
    .view.mapValues(_.size).toMap
}

```

---

## 6. Limpieza de Datos

Durante el proceso de limpieza se aplican las siguientes reglas:

- Eliminación de filas con valores nulos.
- Eliminación de filas con valores cero en `budget` y `revenue`.
- Eliminación de filas inconsistentes o con errores de conversión numérica.

El resultado es un conjunto de datos depurado, apto para análisis estadístico.

En este fragmento de codigo se cumple:

```scala
val cleanedData = Signal {
  val iBudget = colIndex("budget").value
  val iRevenue = colIndex("revenue").value

  if (iBudget < 0 || iRevenue < 0) Nil
  else data.value.filter { r =>
    Try(r(iBudget).toDouble).getOrElse(0.0) > 0 &&
    Try(r(iRevenue).toDouble).getOrElse(0.0) > 0
  }
}
```

---

## 7. Programación Funcional Reactiva

- El proyecto utiliza **REScala** para implementar Programación Funcional Reactiva (FRP).  
- Las estadísticas y análisis se definen como `Signal`, lo que permite que cualquier cambio en los datos de entrada actualice automáticamente los resultados.

---

## 8. Codigo Avance y Resultados
Se aclara que cada variable y funcion del codigo fueron hechos en ingles para respetar el idioma del dataset para asi evitar futuras confusiones.

```scala
import rescala.default.*
import scala.io.Source
import scala.util.Try

object MovieAnalysisFRP extends App {

  // Ruta del archivo CSV
  val filePath = "src/main/resources/pi_movies_small.csv"

  // Variable reactiva que contendrá todas las líneas del archivo
  val rawLines = Var(List[String]())

  // Obtiene los encabezados del CSV de forma reactiva
  val headers = Signal {
    rawLines.value.headOption.map(_.split(";").toList).getOrElse(Nil)
  }

  // Datos sin la cabecera, cada fila como lista de columnas
  val data = Signal {
    rawLines.value.drop(1).map(_.split(";", -1).toList)
  }

  // Obtiene el índice de una columna por nombre
  def colIndex(name: String): Signal[Int] =
    Signal(headers.value.indexOf(name))

  // Acceso seguro a una columna evitando IndexOutOfBounds
  def safeAt(row: List[String], i: Int): Option[String] =
    if (i >= 0 && i < row.length) Some(row(i)) else None

  // Lectura reactiva de columnas numéricas válidas (> 0)
  def numericColumn(name: String): Signal[List[Double]] = Signal {
    val i = colIndex(name).value
    if (i < 0) Nil
    else data.value
      .flatMap(r => safeAt(r, i).flatMap(v => Try(v.toDouble).toOption))
      .filter(_ > 0)
  }

  // Lectura reactiva de columnas de texto no vacías
  def textColumn(name: String): Signal[List[String]] = Signal {
    val i = colIndex(name).value
    if (i < 0) Nil
    else data.value.flatMap(r => safeAt(r, i)).filter(_.nonEmpty)
  }

  // Estadísticas básicas: media, mínimo y máximo
  def stats(col: Signal[List[Double]]): Signal[(Double, Double, Double)] = Signal {
    val xs = col.value
    if (xs.isEmpty) (0.0, 0.0, 0.0)
    else (xs.sum / xs.size, xs.min, xs.max)
  }

  // Análisis numérico
  val budgetStats  = stats(numericColumn("budget"))
  val revenueStats = stats(numericColumn("revenue"))
  val ratingStats  = stats(numericColumn("vote_average"))

  // Análisis de frecuencia para columnas de texto
  val languageFreq = Signal {
    textColumn("original_language")
      .value
      .groupBy(identity)
      .view.mapValues(_.size)
      .toMap
  }

  // Limpieza de datos: filas con budget y revenue válidos
  val cleanedData = Signal {
    val iBudget  = colIndex("budget").value
    val iRevenue = colIndex("revenue").value

    if (iBudget < 0 || iRevenue < 0) Nil
    else data.value.filter { r =>
      safeAt(r, iBudget).flatMap(v => Try(v.toDouble).toOption).exists(_ > 0) &&
        safeAt(r, iRevenue).flatMap(v => Try(v.toDouble).toOption).exists(_ > 0)
    }
  }

  // Métricas de limpieza
  val totalRows = Signal(data.value.size)
  val removedRows = Signal(totalRows.value - cleanedData.value.size)

  // Carga inicial del archivo CSV
  rawLines.set(Source.fromFile(filePath).getLines().toList)

  // Salida de resultados
  println("Presupuesto (media, min, max): " + budgetStats.now)
  println("Ingresos (media, min, max): " + revenueStats.now)
  println("Rating (media, min, max): " + ratingStats.now)
  println("Distribución idiomas: " + languageFreq.now)
  println("Filas totales: " + totalRows.now)
  println("Filas limpias: " + cleanedData.now.size)
  println("Filas eliminadas en limpieza: " + removedRows.now)
  println("Columnas limpiadas: budget, revenue")
}

```
### Resultados
- Presupuesto (media, min, max): (2.5374285714285713E7,90000.0,1.3E8)
- Ingresos (media, min, max): (1.0972644486666666E8,1081.0,8.47423452E8)
- Rating (media, min, max): (6.056976744186046,1.0,9.5)
- Distribución idiomas: HashMap(fr -> 7, it -> 2, de -> 1, ru -> 1, da -> 3, wo -> 1, en -> 75, ja -> 1, zh -> 1, pl -> 1, pt -> 1, cs -> 1, es -> 2, hi -> 2)
- Filas totales: 99
- Filas limpias: 9
- Filas eliminadas en limpieza: 90
- Columnas limpiadas: budget, revenue

## Libreria Circe

- Circe es una librería de Scala utilizada para trabajar con datos en formato JSON. Permite parsear strings JSON, validar su estructura y convertirlos en objetos Scala de forma segura y tipada.

- La librería se basa en el concepto de decodificación y codificación, donde un JSON puede transformarse en una case class de Scala (decode) o una case class puede transformarse en JSON (encode). Esto facilita el manejo de datos estructurados y reduce errores en tiempo de ejecución.

- Circe utiliza el tipo Either para el manejo de errores, lo que permite identificar si una operación fue exitosa o si ocurrió un fallo al leer algún campo del JSON. Además, ofrece herramientas como el cursor, que permite acceder a campos específicos incluso cuando están anidados.

### Aplicacion de la libreria Circe en un JSON pequeño:

```scala
import io.circe._
import io.circe.parser._
import io.circe.generic.auto._

object Circe extends App {

  val jsonString =
    """
      |{
      |  "id": 1,
      |  "nombre": "Juan",
      |  "edad": 30,
      |  "activo": true,
      |  "direccion": {
      |    "ciudad": "Madrid",
      |    "cp": "28001"
      |  }
      |}
    """.stripMargin

  // Conversión del string JSON a un objeto Json
  val json = parse(jsonString) match {
    case Right(value) => value
    case Left(error) =>
      println(error)
      Json.Null
  }

  // Cursor utilizado para acceder a los campos del JSON
  val cursor = json.hcursor

  // Lectura de campos simples
  val id = cursor.get[Int]("id")
  val nombre = cursor.get[String]("nombre")
  val edad = cursor.get[Int]("edad")
  val activo = cursor.get[Boolean]("activo")

  println(id)
  println(nombre)
  println(edad)
  println(activo)

  // Lectura de campos anidados
  val ciudad = cursor.downField("direccion").get[String]("ciudad")
  val cp = cursor.downField("direccion").get[String]("cp")

  println(ciudad)
  println(cp)

  // Definición de las case classes
  case class Direccion(ciudad: String, cp: String)

  case class Persona(
                      id: Int,
                      nombre: String,
                      edad: Int,
                      activo: Boolean,
                      direccion: Direccion
                    )

  // Decodificación del JSON completo a la case class Persona
  val personaCompleta = decode[Persona](jsonString)
  println(personaCompleta)

  // Decodificación utilizando solo algunos campos del JSON
  case class PersonaBasica(nombre: String, edad: Int)

  val personaBasica = decode[PersonaBasica](jsonString)
  println(personaBasica)

  // Uso de un campo opcional
  case class PersonaConOpcional(
                                 nombre: String,
                                 telefono: Option[String]
                               )

  val personaOpcional = decode[PersonaConOpcional](jsonString)
  println(personaOpcional)

}
```

## Resultados

```scala

Right(1)
Right(Juan)
Right(30)
Right(true)
Right(Madrid)
Right(28001)
Right(Persona(1,Juan,30,true,Direccion(Madrid,28001)))
Right(PersonaBasica(Juan,30))
Right(PersonaConOpcional(Juan,None))

```

### Aplicación de Circe en el dataset de películas

- Una vez comprendido el uso básico de Circe con un JSON pequeño, la librería se aplicó al dataset real de películas, el cual contiene múltiples columnas con información almacenada en formato JSON.
- Estas columnas incluyen datos anidados y estructurados, como listas de objetos y campos opcionales, lo que hace necesario validar su estructura antes de realizar cualquier análisis.

### Tratamiento de la columna crew

La columna crew contiene información del equipo técnico de cada película (director, productor, departamento, etc.) en formato JSON. Debido a su complejidad, esta columna se utilizó como caso inicial de estudio para la validación y limpieza de datos JSON.

El proceso aplicado fue el siguiente:

- Se verificó que el contenido de la columna no estuviera vacío.

- Se intentó parsear el contenido utilizando la librería Circe.

- Solo se consideraron válidas aquellas filas donde el JSON pudiera ser parseado correctamente.

```scala

def normalizeJson(raw: String): Option[String] = {
  val cleaned = cleanJsonString(raw)
  parse(cleaned).toOption.map(_.noSpaces)
}

```

Además, se contabilizó el número de filas con JSON válido en la columna crew, permitiendo evaluar la calidad de esta columna dentro del dataset.

### Generalización a todas las columnas JSON

Una vez validado el correcto funcionamiento del proceso de limpieza sobre la columna crew, el mismo criterio se generalizó para aplicarse a todas las columnas que contienen datos en formato JSON dentro del dataset.

Las columnas consideradas fueron:

- crew

- cast

- genres

- keywords

- production_companies

- production_countries

- spoken_languages

- belongs_to_collection

El proceso de limpieza descarta cualquier fila en la que al menos una de estas columnas no contenga un JSON válido. De esta forma, se garantiza que todas las filas seleccionadas para el análisis final contienen información estructuralmente consistente.

Este enfoque es reutilizable y escalable, y se implementó de forma reactiva utilizando REScala, permitiendo que cualquier cambio en los datos se propague automáticamente al resto del análisis.

```scala

val jsonColumns = List(
  "crew",
  "cast",
  "genres",
  "keywords",
  "production_companies",
  "production_countries",
  "spoken_languages",
  "belongs_to_collection"
)

```

### Normalización y validación de JSON con Circe

```scala

def cleanJsonString(raw: String): String =
  raw.trim
    .replace("'", "\"")
    .replace("None", "null")
    .replace("True", "true")
    .replace("False", "false")

def normalizeJson(raw: String): Option[String] = {
  val cleaned = cleanJsonString(raw)
  parse(cleaned).toOption.map(_.noSpaces)
}

```

Este fragmento se encarga de preparar y validar el contenido JSON presente en el dataset.
Primero, se realiza una limpieza básica del string para corregir inconsistencias comunes (comillas simples, valores None y booleanos no estándar).
Posteriormente, se intenta parsear el contenido utilizando Circe. Si el parseo es exitoso, se devuelve el JSON normalizado; en caso contrario, se descarta devolviendo None.
De esta forma, solo se consideran válidos los registros que contienen un JSON correctamente estructurado.

### Limpieza conjunta de columnas JSON

```scala
val cleanedJsonData = Signal {
  val indexes = jsonIndexes.value
  if (indexes.exists(_ < 0)) Nil
  else {
    data.value.flatMap { row =>
      val newRow = row.toArray
      val valid = indexes.forall { i =>
        safeAt(row, i).flatMap(normalizeJson) match {
          case Some(jsonClean) =>
            newRow(i) = jsonClean
            true
          case None => false
        }
      }
      if (valid) Some(newRow.toList) else None
    }
  }
}

```

En esta etapa se realiza la limpieza efectiva del dataset en relación con las columnas JSON.
Para cada fila, se verifica que todas las columnas JSON puedan ser normalizadas correctamente.
Si alguna de ellas contiene un JSON inválido, la fila completa es descartada.
Cuando el JSON es válido, su versión normalizada reemplaza el valor original, garantizando consistencia estructural en los datos finales.
Este proceso se implementa de forma reactiva mediante REScala, por lo que cualquier cambio en los datos se propaga automáticamente.

### Conteo de filas con JSON válido por columna

```scala

def validJsonRows(columnName: String): Signal[Int] = Signal {
  val i = colIndex(columnName).value
  if (i < 0) 0
  else data.value.count(r => safeAt(r, i).flatMap(normalizeJson).isDefined)
}

```

Este método permite cuantificar cuántas filas contienen un JSON válido en una columna específica.
Para cada fila, se intenta normalizar el contenido utilizando el mismo criterio aplicado en la limpieza.
El resultado se utiliza únicamente con fines de diagnóstico, permitiendo evaluar la calidad de los datos JSON en cada columna antes y después del proceso de limpieza.

### Validación del formato de fechas

Adicionalmente, se realizó una validación sobre la columna release_date, verificando que las fechas cumplan con el formato estándar yyyy-MM-dd.
Este proceso no modifica los datos, únicamente clasifica las filas como válidas o inválidas según el formato esperado.

### Función de validación del formato yyyy-MM-dd

```scala
def isValidDate(s: String): Boolean =
  Try(LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE)).isSuccess
```
### Conteo de filas con fecha válida

```scala

val validDateRows = Signal {
  val iDate = headers.value.indexOf("release_date")
  if (iDate < 0) 0
  else data.value.count(r => safeAt(r, iDate).exists(isValidDate))
}

```

### Conteo de filas con fecha inválida

```scala

val invalidDateRows = Signal {
  totalRows.value - validDateRows.value
}

```
### Resultados obtenidos

```scala
Filas totales: 3499

Filas limpias (numéricas): 613
Filas limpias (JSON): 142
Filas limpias (completa): 36
```

### Validacion de fechas

```scala

Filas con fecha válida (yyyy-MM-dd): 3337
Filas con fecha inválida: 162

```

### Validación de columnas JSON

```scala

crew: 3249 filas válidas
cast: 2382 filas válidas
genres: 3467 filas válidas
keywords: 3358 filas válidas
production_companies: 3325 filas válidas
production_countries: 3427 filas válidas
spoken_languages: 3439 filas válidas
belongs_to_collection: 334 filas válidas

```

#  Poblamiento de Base de Datos – Proyecto Movies

## Estrategia de Poblamiento

1. Primero se pobló la tabla **movie**, ya que es la tabla principal del sistema.
2. Posteriormente, el proceso se extenderá a las demás tablas relacionadas:
   - collections
   - genres
   - companies
   - countries
   - languages
   - keywords
   - cast
   - crew
   - users
   - movie_rating_user

Este enfoque evita problemas de integridad referencial y facilita la validación por etapas.

---

## Lectura del Archivo CSV

El archivo `pi_movies_complete.csv` se procesa como un stream:

```scala
Files[IO]
  .readAll(Path(path2DataFile))
  .through(fs2.text.utf8.decode)
  .through(
    decodeUsingHeaders[Pelicula](
      ';',
      QuoteHandling.RFCCompliant
    )
  )
```

---

## Modelos de Datos (`models`)

### Modelo Pelicula

Este modelo representa la estructura original del CSV y se utiliza únicamente para la lectura de datos.

```scala
case class Pelicula(
  id: Int,
  title: String,
  original_title: String,
  popularity: Double,
  release_date: String,
  status: String,
  overview: String,
  adult: Boolean,
  budget: Int,
  revenue: Long,
  runtime: Int,
  vote_average: Double,
  vote_count: Int,
  homepage: String,
  imdb_id: String,
  tagline: String,
  video: Boolean
)
```

### Modelo Movie

Este modelo corresponde directamente a la tabla `movie` en la base de datos.

```scala
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
```

<img width="207" height="590" alt="image" src="https://github.com/user-attachments/assets/f5ed066d-4491-4613-8eba-16f6b66d45f0" />


---

## Limpieza y Normalización de Datos

Se implementaron funciones auxiliares para limpiar y convertir los valores del CSV:

```scala
def i(v: String): Int = v.trim.toIntOption.getOrElse(0)
def l(v: String): Long = v.trim.toLongOption.getOrElse(0L)
def bd(v: String): BigDecimal =
  v.trim.replace(",", ".").toDoubleOption.map(BigDecimal(_)).getOrElse(BigDecimal(0))
```

---

## Transformación de Datos

Cada registro leído se transforma de `Pelicula` a `Movie` antes de insertarse:

```scala
def toMovie(p: Pelicula): Movie = ...
```

---

## Inserción en Base de Datos

Se utilizan sentencias `INSERT INTO` mediante Doobie:

```scala
def insertMovie(movie: Movie): ConnectionIO[Int] = ...
```

<img width="1919" height="1079" alt="image" src="https://github.com/user-attachments/assets/80ae267b-a049-4eab-a944-c521139a47b5" />


---

## Manejo de Errores

El uso de `attempt` permite descartar filas inválidas sin detener el proceso completo:

```scala
.attempt
.collect { case Right(p) => toMovie(p) }
```

---



