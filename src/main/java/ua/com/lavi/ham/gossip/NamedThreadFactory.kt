package ua.com.lavi.ham.gossip

import java.util.concurrent.ThreadFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Custom thread factory allows to use named prefixes for threads
 */
class NamedThreadFactory(private val prefix: String) : ThreadFactory {
    private var counter = AtomicInteger()

    override fun newThread(runnable: Runnable): Thread {
        return Thread(runnable, prefix + "-" + counter.incrementAndGet())
    }
}
