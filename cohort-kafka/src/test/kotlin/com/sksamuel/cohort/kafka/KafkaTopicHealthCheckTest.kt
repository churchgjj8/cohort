package com.sksamuel.cohort.kafka

import com.sksamuel.cohort.HealthStatus
import io.kotest.core.extensions.install
import io.kotest.core.spec.style.FunSpec
import io.kotest.extensions.testcontainers.kafka.KafkaContainerExtension
import io.kotest.extensions.testcontainers.kafka.admin
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import org.apache.kafka.clients.admin.NewTopic
import org.testcontainers.containers.KafkaContainer
import org.testcontainers.utility.DockerImageName
import kotlin.time.Duration.Companion.seconds

class KafkaTopicHealthCheckTest : FunSpec({

   val kafka = install(KafkaContainerExtension(KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka"))))

   test("health check should pass if the topic exists") {

      val healthcheck = KafkaTopicHealthCheck(kafka.admin(), "mytopic")
      healthcheck.check().status shouldBe HealthStatus.Unhealthy
      kafka.admin().use { it.createTopics(listOf(NewTopic("mytopic", 1, 1))).all().get() }
      delay(1.seconds)
      healthcheck.check().status shouldBe HealthStatus.Healthy
   }
})
