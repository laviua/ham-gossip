package ua.com.lavi.ham.gossip

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class GossipServiceTest {

    private val log = LoggerFactory.getLogger(GossipServiceTest::class.java)

    @Test
    fun shouldNotHaveAnyNode() {
        val me = GossipNode("xCluster", "127.0.0.1", 50000, "notebook", 0)
        val members = arrayListOf(GossipNode("xCluster", "127.0.0.1", 50001, "wasp", 0), GossipNode("xCluster", "127.0.0.1", 50002, "raven", 0))
        val gossipConfiguration = GossipConfiguration(me, members)

        val gossipService = GossipService(gossipConfiguration, LogGossipStateListener())
        Assertions.assertTrue(gossipService.getAllMembers().isEmpty())
    }

    @Test
    fun shouldStartGossipOnlyInSingleNodeWithTwoDead() {
        val upCountDownLatch = CountDownLatch(2)
        val downCountDownLatch = CountDownLatch(2)
        val gossipStateListener = object : GossipStateListener {
            override fun onChangeState(member: GossipMember, nodeState: GossipNodeState) {
                if (member.id == "raven" && nodeState == GossipNodeState.UP) {
                    upCountDownLatch.countDown()
                    log.info(member.toString() + " " + nodeState)
                }
                if (member.id == "wasp" && nodeState == GossipNodeState.UP) {
                    upCountDownLatch.countDown()
                    log.info(member.toString() + " " + nodeState)
                }

                if (member.id == "raven" && nodeState == GossipNodeState.DOWN) {
                    downCountDownLatch.countDown()
                    log.info(member.toString() + " " + nodeState)
                }
                if (member.id == "wasp" && nodeState == GossipNodeState.DOWN) {
                    downCountDownLatch.countDown()
                    log.info(member.toString() + " " + nodeState)
                }
            }
        }
        val me = GossipNode("xCluster", "127.0.0.1", 50000, "notebook", 0)
        val members = arrayListOf(GossipNode("xCluster", "127.0.0.1", 50001, "wasp", 0), GossipNode("xCluster", "127.0.0.1", 50002, "raven", 0))
        val gossipConfiguration = GossipConfiguration(me, members)

        gossipConfiguration.cleanupInterval = 3000
        val gossipService = GossipService(gossipConfiguration, gossipStateListener)
        gossipService.start()
        upCountDownLatch.await(10, TimeUnit.SECONDS)
        downCountDownLatch.await(10, TimeUnit.SECONDS)
        gossipService.shutdown()
        Assertions.assertTrue(gossipService.getAllMembers().size == 2)
        Assertions.assertTrue(gossipService.getAllMembers().values.any { it == GossipNodeState.DOWN })
    }

    @Test
    fun shouldUpAndWorkMoreThanCleanupWithTwoNodes() {

        //start first node
        val gossipConfiguration1 = GossipConfiguration(GossipNode("xCluster", "127.0.0.1", 50000, "notebook", 0),  arrayListOf(GossipNode("xCluster", "127.0.0.1", 50001, "wasp", 0)))

        gossipConfiguration1.cleanupInterval = 2000
        val gossipService1 = GossipService(gossipConfiguration1, LogGossipStateListener())
        gossipService1.start()
        //start second node
        val gossipConfiguration2 = GossipConfiguration(GossipNode("xCluster", "127.0.0.1", 50001, "wasp", 0),  arrayListOf(GossipNode("xCluster", "127.0.0.1", 50000, "notebook", 0)))

        gossipConfiguration2.cleanupInterval = 2000
        val gossipService2 = GossipService(gossipConfiguration2, LogGossipStateListener())
        gossipService2.start()

        Thread.sleep(3000)

        Assertions.assertTrue(gossipService1.getAllMembers().size == 1)
        Assertions.assertTrue(gossipService1.getAllMembers().values.any { it == GossipNodeState.UP })

        Assertions.assertTrue(gossipService2.getAllMembers().size == 1)
        Assertions.assertTrue(gossipService2.getAllMembers().values.any { it == GossipNodeState.UP })

        gossipService1.shutdown()
        gossipService2.shutdown()
    }

}