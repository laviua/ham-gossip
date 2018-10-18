package ua.com.lavi.ham.gossip

abstract class GossipMember(

        val cluster: String,
        val host: String,
        val port: Int,
        var id: String,
        var heartbeat: Long) : Comparable<GossipMember> {

    fun address(): String {
        return "$host:$port"
    }

    fun clusteredAddress(): String {
        return "$cluster:${address()}"
    }

    override fun toString(): String {
        return "Member [address=${address()}(), id=$id, heartbeat=$heartbeat]"
    }

    override fun compareTo(other: GossipMember): Int {
        return this.address().compareTo(other.address())
    }
}
