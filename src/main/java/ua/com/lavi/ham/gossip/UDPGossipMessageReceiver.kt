package ua.com.lavi.ham.gossip

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import java.io.IOException
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetSocketAddress
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * UDP Receiver of information from remote gossip members
 */

class UDPGossipMessageReceiver(private val memberStorage: MemberStorage,
                               private val me: LocalGossipMember,
                               private val cluster: String,
                               private val timerHandler: TimerHandler,
                               private val cleanupInterval: Long,
                               private val gossipStateListener: GossipStateListener) {

    private val log = LoggerFactory.getLogger(UDPGossipMessageReceiver::class.java)
    private val executorService: ExecutorService = Executors.newSingleThreadExecutor(NamedThreadFactory("UDPGossipMessageReceiver"))

    private val running: AtomicBoolean = AtomicBoolean(true)
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())

    fun start() {
        log.info("Starting of receiving udp gossip messages")
        executorService.submit {
            val socket = DatagramSocket(InetSocketAddress(me.host, me.port))
            log.info("Gossip socket successfully initialized on port " + me.port)
            while (running.get()) {
                try {
                    val buf = ByteArray(socket.receiveBufferSize)
                    val datagramPacket = DatagramPacket(buf, buf.size)
                    socket.receive(datagramPacket)
                    val gossipMembersMessage = objectMapper.readValue(buf, GossipMembersMessage::class.java)

                    val remoteMembers = gossipMembersMessage.members
                            .asSequence()
                            .filter { it -> it.id != me.id }
                            .filter { cluster == it.cluster }
                            .map { GossipNode(it.cluster, it.host, it.port, it.id, it.heartbeat) }
                            .toList()

                    for (remoteMember in remoteMembers) {
                        if (memberStorage.isAlive(remoteMember)) {
                            updateExistedLiveNode(remoteMember)
                        } else if (!memberStorage.isAlive(remoteMember) && !memberStorage.isDead(remoteMember)) {
                            registerNewNode(remoteMember)
                        } else {
                            if (memberStorage.isDead(remoteMember)) {
                                resurrectDeadNode(remoteMember)
                            } else {
                                log.warn("Something goes wrong.. Node is in the superstate")
                            }
                        }
                    }

                } catch (e: IOException) {
                    log.warn("Error: ", e)
                    running.set(false)
                    socket.close()
                }
            }
            socket.close()
        }
    }

    fun shutdown() {
        running.set(false)
        executorService.shutdown()
        log.info("Shutdown receiver is done...")
    }


    private fun resurrectDeadNode(remoteMember: GossipMember) {
        log.debug("Resurrecting findDeadMembers node: {}", remoteMember)
        val localDeadMember = memberStorage.findDead(remoteMember).get()
        if (remoteMember.heartbeat > localDeadMember.heartbeat) {
            val newLocalMember = LocalGossipMember(remoteMember.cluster, remoteMember.host, remoteMember.port, remoteMember.id, remoteMember.heartbeat, timerHandler, cleanupInterval)
            memberStorage.remove(newLocalMember)
            memberStorage.up(newLocalMember)
            gossipStateListener.onChangeState(newLocalMember, GossipNodeState.UP)
            newLocalMember.startTimeoutTimer()
        }
    }

    private fun updateExistedLiveNode(remoteMember: GossipMember) {
        val localMember = memberStorage.findAlive(remoteMember).get()
        if (remoteMember.heartbeat > localMember.heartbeat) {
            localMember.heartbeat = remoteMember.heartbeat
            localMember.resetTimeoutTimer()
        }
    }

    private fun registerNewNode(remoteMember: GossipMember) {
        val newLocalMember = LocalGossipMember(remoteMember.cluster,
                remoteMember.host, remoteMember.port, remoteMember.id,
                remoteMember.heartbeat, timerHandler, cleanupInterval)

        memberStorage.up(newLocalMember)
        gossipStateListener.onChangeState(newLocalMember, GossipNodeState.UP)
        newLocalMember.startTimeoutTimer()
    }
}