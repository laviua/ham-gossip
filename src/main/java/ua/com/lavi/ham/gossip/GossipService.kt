package ua.com.lavi.ham.gossip

import org.slf4j.LoggerFactory
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * The main service of gossip framework
 * It allows to run and shutdown sending and receiving gossip messages
 */
class GossipService(private val gossipConfiguration: GossipConfiguration,
                    private val gossipStateListener: GossipStateListener) {

    private val log = LoggerFactory.getLogger(GossipService::class.java)

    private val cluster = gossipConfiguration.me.cluster
    private val memberStorage = MemoryMemberStorage()
    private val timerHandler = TimerHandlerImpl(memberStorage, gossipStateListener)
    private val me: LocalGossipMember = buildLocalMember(gossipConfiguration.me.host, gossipConfiguration.me.port, gossipConfiguration.me.id)
    private var udpGossipMessageReceiver: UDPGossipMessageReceiver? = UDPGossipMessageReceiver(memberStorage, me, cluster, timerHandler, gossipConfiguration.cleanupInterval, gossipStateListener)
    private val memberUpdatesNotifier = UDPGossipMemberUpdatesNotifier(memberStorage, me, gossipConfiguration.gossipInterval)
    private val scheduledExecutor = Executors.newScheduledThreadPool(1, NamedThreadFactory("UDPGossipMemberUpdatesNotifier"))

    /**
     * Start gossip service
     */
    fun start() {
        for (seedNode in gossipConfiguration.members) {
            val member = buildLocalMember(seedNode.host, seedNode.port, seedNode.id)
            memberStorage.up(member)
            gossipStateListener.onChangeState(member, GossipNodeState.UP)
            member.startTimeoutTimer()
        }

        scheduledExecutor.scheduleAtFixedRate(memberUpdatesNotifier, 0L, gossipConfiguration.gossipInterval.toLong(), TimeUnit.MILLISECONDS)
        udpGossipMessageReceiver!!.start()

        log.info("The GossipService is initiated.")

    }

    /**
     * Shutdown the gossip service.
     */
    fun shutdown() {
        udpGossipMessageReceiver!!.shutdown()
        scheduledExecutor.shutdown()
    }

    /**
     * Get all nodes in the system
     */
    fun getAllMembers(): Map<LocalGossipMember, GossipNodeState> {
        return memberStorage.findAll()
    }

    private fun buildLocalMember(address: String, port: Int, id: String): LocalGossipMember {
        return LocalGossipMember(cluster, address, port, id, System.currentTimeMillis(), timerHandler, gossipConfiguration.cleanupInterval)
    }

}
