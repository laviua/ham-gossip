package ua.com.lavi.ham.gossip

/**
 * User defined gossip member listener
 * onChangeState invokes when member changed state
 */
interface GossipStateListener {
    fun onChangeState(member: GossipMember, nodeState: GossipNodeState)
}
