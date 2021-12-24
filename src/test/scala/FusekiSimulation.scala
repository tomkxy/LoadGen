import io.gatling.core.Predef._
import io.gatling.http.Predef._

import java.util.UUID.randomUUID
import scala.concurrent.duration.DurationInt
import scala.io.BufferedSource


/**
 * Load test for the rest service.
 */
class FusekiSimulation extends io.gatling.core.scenario.Simulation
  with FusekiSimulationConfig {

  /**
   * http configuration.
   */
  val httpProtocol = http.baseUrl(baseURL)

  val pubId: String = ""
  val feeder = Iterator.continually(Map("id" -> (randomUUID().toString)))

  /**
   *   make sure that Fuseki instances are running on consecutive ports starting from 1 until nrOfInstances (max is nine)
   */
  val feedHostId = Iterator.continually(
      Map("port" -> (basePort + scala.util.Random.nextInt( nrOfInstances) ))
  )

  /**
   * Scenario for simulation.
   */
  val scn = scenario("Simulation for Fuseki").forever(
    feed(feeder)
      .feed(feedHostId)
      .exec { session =>
          println(session("id").as[String])
          session
      }
      .exec(
        http(fuseki_host_pattern)
          .post("")
          .header("Content-Type", "text/turtle")
          .body(ElFileBody(templateFile)
          ))
      .exec(
        http("get resource")
          .post("")
          .header("Accept", "application/sparql-results+json")
          .queryParam("query", "SELECT (COUNT(*) as ?Triples)   WHERE { <http://mdp.example-resource/${id}> a <https://w3id.org/idsa/core/DataResource>}")
          .check(status.is(200))
          .check(jsonPath("$..value").is("1"))
      )
      .pause(pause)
  )


  /**
   * Sets the scenario.
   */
  setUp(scn.inject(atOnceUsers(user_count)))
    .maxDuration(duration.minutes)
    .protocols(httpProtocol)
    .assertions(global.successfulRequests.percent.is(percentSuccess)) //Check test result
}

