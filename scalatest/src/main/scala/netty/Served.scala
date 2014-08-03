package unfiltered.scalatest.netty

import io.netty.util.ResourceLeakDetector
import org.scalatest.{ Suite, Outcome }
import unfiltered.netty.Server
import unfiltered.scalatest.Hosted

trait Planned extends Served { self: Hosted =>
  def setup = _.plan(unfiltered.netty.cycle.Planify(intent))
  def intent[A, B]: unfiltered.Cycle.Intent[A, B]
}

trait Served extends Suite with Hosted {
  // Enables paranoid resource leak detection which reports where the leaked object was accessed recently,
  // at the cost of the highest possible overhead (for testing purposes only).
  ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID)

  def setup: Server => Server
  def getServer = setup(Server.http(port))

  override protected def withFixture(test: NoArgTest): Outcome = {
    val server = getServer
    server.start()
    try {
      test() // Invoke the test function
    } finally {
      server.stop()
      server.destroy()
    }
  }
}
