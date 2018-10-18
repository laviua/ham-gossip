package ua.com.lavi.ham.gossip

/**
 * Handler invokes when time is out since last checkpoint
 */
interface TimerHandler {

    fun onTimeout(gossipMember: LocalGossipMember)
}