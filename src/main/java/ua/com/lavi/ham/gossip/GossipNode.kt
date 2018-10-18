package ua.com.lavi.ham.gossip

/**
 * Implementation of simple gossip node, configured on start or received from another system
 */
class GossipNode(cluster: String,
                 host: String,
                 port: Int,
                 id: String,
                 heartbeat: Long) : GossipMember(cluster, host, port, id, heartbeat)