
import io.gatling.core.Predef._
import io.gatling.http.Predef._
import java.util.UUID.randomUUID
import scala.io.BufferedSource
import concurrent.duration.DurationInt


/**
 * Load test for the rest service.
 */
class PublicationCreationSimulation extends io.gatling.core.scenario.Simulation
  with PublicationSimulationConfig {

  /**
   * http configuration.
   */
  val httpProtocol = http.baseUrl(baseURL)

  val pubId: String = ""
  val feeder = Iterator.continually(Map("id" -> (randomUUID().toString)))

  /**
   * Scenario for simulation.
   */
  val scn = scenario("Simulation for publication creation").forever(
      feed(feeder)
      .exec(
        http("create offer")
          .put(offersEndpoint)
          .header("Content-Type", "application/json")
          .body(ElFileBody(templateFile))
          .check(headerRegex("Location","""offeredResource/(\\*)""").saveAs(pubId))
      )
      .exec (
        http("getoffer")
          .get(offersEndpoint + "/" + pubId)
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

