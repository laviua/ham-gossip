package ua.com.lavi.ham.gossip

import java.util.*
import kotlin.concurrent.schedule

/**
 * Implementation of gossip member in the local cluster system with internal timer
 */
class LocalGossipMember(cluster: String,
                        host: String,
                        port: Int,
                        id: String,
                        heartbeat: Long,
                        private val timerHandler: TimerHandler,
                        private val cleanupTimeout: Long) : GossipMember(cluster, host, port, id, heartbeat) {

    private var timer: Timer = Timer("Timer ${clusteredAddress()}")

    private var timerTask: TimerTask? = null

    fun startTimeoutTimer() {
        timerTask = timer.schedule(delay = cleanupTimeout) { timerHandler.onTimeout(this@LocalGossipMember) }
    }

    fun refreshHeartbeat() {
        heartbeat = System.currentTimeMillis()
    }

    fun resetTimeoutTimer() {
        timerTask?.cancel()
        startTimeoutTimer()
    }
}