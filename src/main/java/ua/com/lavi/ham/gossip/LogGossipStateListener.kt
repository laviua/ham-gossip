package ua.com.lavi.ham.gossip

import org.slf4j.LoggerFactory

/**
 * Simple member state listener. Log all events
 */
class LogGossipStateListener : GossipStateListener {

    private val log = LoggerFactory.getLogger(LogGossipStateListener::class.java)

    override fun onChangeState(member: GossipMember, nodeState: GossipNodeState) {
        log.info(member.toString() + " " + nodeState)
    }

}
