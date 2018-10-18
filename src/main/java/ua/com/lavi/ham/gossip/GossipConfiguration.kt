package ua.com.lavi.ham.gossip

/**
 * Configuration of Gossip service.
 */
data class GossipConfiguration(val me: GossipNode,
                               val members: List<GossipNode> = arrayListOf(),
                               var gossipInterval: Int = 1000,
                               var cleanupInterval: Long = 10000
)