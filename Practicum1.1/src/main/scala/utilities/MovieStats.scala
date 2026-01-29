package utilities

object MovieStats {

  /* ===================== ESTADÍSTICAS NUMÉRICAS ===================== */

  /**
   * Calcula estadísticas básicas de una columna numérica:
   * count, mean, std, min, percentiles 25/50/75 y max.
   * Devuelve un Map con los nombres de métricas como claves.
   */
  def calcularEstadisticas(datos: Seq[Double]): Map[String, Double] = {
    if (datos.isEmpty) {
      // Valores por defecto si no hay datos
      Map(
        "count" -> 0.0,
        "mean" -> 0.0,
        "std" -> 0.0,
        "min" -> 0.0,
        "25%" -> 0.0,
        "50%" -> 0.0,
        "75%" -> 0.0,
        "max" -> 0.0
      )
    } else {
      val sorted = datos.sorted
      val count = datos.size
      val mean = datos.sum / count
      val variance = datos.map(x => math.pow(x - mean, 2)).sum / count
      val std = math.sqrt(variance)

      // Calcula percentil interpolando entre valores
      def percentile(p: Double): Double = {
        val index = p * (sorted.size - 1)
        val lower = index.toInt
        val upper = math.min(lower + 1, sorted.size - 1)
        val weight = index - lower
        sorted(lower) * (1 - weight) + sorted(upper) * weight
      }

      Map(
        "count" -> count.toDouble,
        "mean" -> mean,
        "std" -> std,
        "min" -> sorted.head,
        "25%" -> percentile(0.25),
        "50%" -> percentile(0.50),
        "75%" -> percentile(0.75),
        "max" -> sorted.last
      )
    }
  }

  /* ===================== ESTADÍSTICAS DE TEXTO ===================== */

  /** Calcula la frecuencia de cada valor no vacío en una columna de texto */
  def calcularFrecuencias(datos: Seq[String]): Map[String, Int] =
    if (datos.isEmpty) Map.empty[String, Int]
    else
      datos
        .filter(_.nonEmpty)
        .groupBy(identity)
        .view
        .mapValues(_.size)
        .toMap

  /** Devuelve los N elementos más frecuentes en una columna de texto */
  def topN(datos: Seq[String], n: Int = 10): List[(String, Int)] =
    calcularFrecuencias(datos)
      .toList
      .sortBy(-_._2)
      .take(n)

  /* ===================== CORRELACIONES ===================== */

  /**
   * Calcula la correlación lineal entre dos columnas numéricas.
   * Devuelve None si no se puede calcular (listas vacías o tamaños distintos).
   */
  def correlacion(col1: Seq[Double], col2: Seq[Double]): Option[Double] = {
    if (col1.isEmpty || col2.isEmpty || col1.size != col2.size) None
    else {
      val n = col1.size
      val mean1 = col1.sum / n
      val mean2 = col2.sum / n

      val cov = col1.zip(col2).map { case (x, y) =>
        (x - mean1) * (y - mean2)
      }.sum / n

      val std1 = math.sqrt(col1.map(x => math.pow(x - mean1, 2)).sum / n)
      val std2 = math.sqrt(col2.map(x => math.pow(x - mean2, 2)).sum / n)

      if (std1 == 0 || std2 == 0) None
      else Some(cov / (std1 * std2))
    }
  }

  /* ===================== IMPRESIÓN FORMATEADA ===================== */

  /** Imprime de forma legible las estadísticas numéricas de una columna */
  def imprimirEstadisticas(nombre: String, stats: Map[String, Double]): Unit = {
    println(s"\n=== Estadísticas de $nombre ===")
    println(f"Count:        ${stats("count")}%.0f")
    println(f"Mean:         ${stats("mean")}%.2f")
    println(f"Std:          ${stats("std")}%.2f")
    println(f"Min:          ${stats("min")}%.2f")
    println(f"25%%:          ${stats("25%")}%.2f")
    println(f"50%% (Median): ${stats("50%")}%.2f")
    println(f"75%%:          ${stats("75%")}%.2f")
    println(f"Max:          ${stats("max")}%.2f")
  }

  /** Imprime las frecuencias de texto ordenadas y con un límite top N */
  def imprimirFrecuencias(nombre: String, freq: Map[String, Int], top: Int = 10): Unit = {
    println(s"\n=== Distribución de $nombre (Top $top) ===")
    freq.toList
      .sortBy(-_._2)
      .take(top)
      .zipWithIndex
      .foreach { case ((valor, count), idx) =>
        println(f"${idx + 1}%2d. $valor%-20s -> $count%5d")
      }
  }

  /**
   * Imprime un resumen completo del dataset de películas:
   * estadísticas numéricas, frecuencias de idiomas, métricas de limpieza
   * y correlación entre presupuesto y revenue si está disponible.
   */
  def imprimirResumenCompleto(
                               budgetStats: Map[String, Double],
                               revenueStats: Map[String, Double],
                               popularityStats: Map[String, Double],
                               runtimeStats: Map[String, Double],
                               voteAverageStats: Map[String, Double],
                               voteCountStats: Map[String, Double],
                               languageFreq: Map[String, Int],
                               totalRows: Int,
                               cleanRows: Int,
                               budgetRevenueCorr: Option[Double]
                             ): Unit = {
    println("\n" + "="*70)
    println("=== RESUMEN ESTADÍSTICO DEL DATASET DE PELÍCULAS ===")
    println("="*70)

    imprimirEstadisticas("Budget", budgetStats)
    imprimirEstadisticas("Revenue", revenueStats)
    imprimirEstadisticas("Popularity", popularityStats)
    imprimirEstadisticas("Runtime", runtimeStats)
    imprimirEstadisticas("Vote Average", voteAverageStats)
    imprimirEstadisticas("Vote Count", voteCountStats)
    imprimirFrecuencias("Idioma Original", languageFreq, 15)

    println(s"\n=== Métricas de Limpieza ===")
    println(s"Filas totales:       $totalRows")
    println(s"Filas únicas:        $cleanRows")
    println(s"Duplicados eliminados: ${totalRows - cleanRows}")

    budgetRevenueCorr.foreach { corr =>
      println(f"\n=== Correlación Budget-Revenue: $corr%.4f ===")
      if (corr > 0.7) println("  → Correlación fuerte positiva")
      else if (corr > 0.3) println("  → Correlación moderada positiva")
      else println("  → Correlación débil")
    }

    println("\n" + "="*70)
  }
}
