package com.sksamuel.cohort.db

import com.sksamuel.cohort.WarmupHealthCheck
import javax.sql.DataSource
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

/**
 * A Cohort [WarmupHealthCheck] that warms a [DataSource].
 *
 * Uses the JDBC4 method isValid(timeout) with the given [timeout] to check that the connection
 * returned is open and usable.
 */
class DataSourceWarmup(
   private val ds: DataSource,
   private val timeout: Duration = 1.seconds,
   override val iterations: Int = 100,
) : WarmupHealthCheck() {

   override val name: String = "datasource_warmup"

   override suspend fun warm(iteration: Int) {
      ds.connection.use { it.isValid(timeout.inWholeSeconds.toInt()) }
   }
}
