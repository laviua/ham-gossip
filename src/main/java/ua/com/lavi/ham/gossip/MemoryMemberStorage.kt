package ua.com.lavi.ham.gossip

import java.util.*
import java.util.concurrent.ConcurrentSkipListMap

/**
 * Memory storage for members.
 */
class MemoryMemberStorage : MemberStorage {

    private val members = ConcurrentSkipListMap<LocalGossipMember, GossipNodeState>()

    override fun up(member: LocalGossipMember) {
        members[member] = GossipNodeState.UP
    }

    override fun remove(member: LocalGossipMember) {
        members.remove(member)
    }

    override fun down(member: LocalGossipMember) {
        members[member] = GossipNodeState.DOWN
    }

    override fun findAliveMembers(): List<LocalGossipMember> {
        return members.entries
                .asSequence()
                .filter { it -> GossipNodeState.UP == it.value }
                .map { it.key }
                .toList()
    }

    override fun findDeadMembers(): List<LocalGossipMember> {
        return members.entries
                .asSequence()
                .filter { it -> GossipNodeState.DOWN == it.value }
                .map { it.key }
                .toList()
    }

    override fun isDead(remoteMember: GossipMember): Boolean {
        return findDead(remoteMember).isPresent
    }

    override fun isAlive(remoteMember: GossipMember): Boolean {
        return findAlive(remoteMember).isPresent
    }

    override fun findAlive(remoteMember: GossipMember): Optional<LocalGossipMember> {
        return findAliveMembers()
                .stream()
                .filter { x -> x.clusteredAddress() == remoteMember.clusteredAddress() }
                .findFirst()
    }

    override fun findDead(remoteMember: GossipMember): Optional<LocalGossipMember> {
        return findDeadMembers()
                .stream()
                .filter { x -> x.clusteredAddress() == remoteMember.clusteredAddress() }
                .findFirst()
    }

    override fun findAll(): Map<LocalGossipMember, GossipNodeState> {
        return members
    }
}