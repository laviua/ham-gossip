package ua.com.lavi.ham.gossip

import java.util.*

interface MemberStorage {
    fun up(member: LocalGossipMember)
    fun remove(member: LocalGossipMember)
    fun down(member: LocalGossipMember)
    fun findAliveMembers(): List<LocalGossipMember>
    fun findDeadMembers(): List<LocalGossipMember>
    fun isDead(remoteMember: GossipMember): Boolean
    fun isAlive(remoteMember: GossipMember): Boolean
    fun findAlive(remoteMember: GossipMember): Optional<LocalGossipMember>
    fun findDead(remoteMember: GossipMember): Optional<LocalGossipMember>
    fun findAll(): Map<LocalGossipMember, GossipNodeState>
}