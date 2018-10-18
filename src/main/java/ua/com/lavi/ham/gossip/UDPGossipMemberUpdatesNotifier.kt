package ua.com.lavi.ham.gossip

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.slf4j.LoggerFactory
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.util.*

/**
 * Notify random neighbor about findAliveMembers members
 */
open class UDPGossipMemberUpdatesNotifier(private val memberStorage: MemberStorage,
                                          private val me: LocalGossipMember,
                                          private val gossipInterval: Int) : Runnable {

    private val log = LoggerFactory.getLogger(UDPGossipMemberUpdatesNotifier::class.java)
    private val objectMapper = ObjectMapper().registerModule(KotlinModule())
    private val random: Random = Random()

    override fun run() {
        try {
            val memberList = memberStorage.findAliveMembers()

            if (memberList.isEmpty()) {
                log.debug("Can't find any neighbor in the cluster: ${me.cluster}")
                return
            }
            val message = buildMessage(memberList)

            val randomNeighbor = memberList[random.nextInt(memberList.size)]
            log.debug("Send UDP updates of members in the cluster to: " + randomNeighbor.address())
            DatagramSocket().use { socket ->
                socket.soTimeout = gossipInterval
                val bytes = objectMapper.writeValueAsBytes(message)
                socket.send(DatagramPacket(bytes, bytes.size, InetAddress.getByName(randomNeighbor.host), randomNeighbor.port))
            }

        } catch (e: Exception) {
            log.warn("Error: ", e)
        }
    }

    private fun buildMessage(memberList: List<LocalGossipMember>): GossipMembersMessage {
        // refresh heartbeat and pack into the message
        me.refreshHeartbeat()

        val members = arrayListOf(convertMemberToNode(me))
        members.addAll(memberList
                .asSequence()
                .map { convertMemberToNode(it) }
                .toList())

        val message = GossipMembersMessage()
        message.members = members
        return message
    }

    private fun convertMemberToNode(member: LocalGossipMember): GossipNode {
        return GossipNode(member.cluster, member.host, member.port, member.id, member.heartbeat)
    }

}
