package ua.com.lavi.ham.gossip

import org.slf4j.LoggerFactory

/**
 * Implementation of TimerHandler.
 * Mark member as a down and invoke defined listener
 */
class TimerHandlerImpl(private val memberStorage: MemberStorage,
                       private val gossipStateListener: GossipStateListener) : TimerHandler {

    private val log = LoggerFactory.getLogger(TimerHandlerImpl::class.java)

    override fun onTimeout(gossipMember: LocalGossipMember) {
        log.debug("Dead member detected: $gossipMember")
        memberStorage.down(gossipMember)
        gossipStateListener.onChangeState(gossipMember, GossipNodeState.DOWN)
    }
}