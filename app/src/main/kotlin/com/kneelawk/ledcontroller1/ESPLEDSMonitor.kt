package com.kneelawk.ledcontroller1

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicBoolean

class ESPLEDSMonitor(scope: CoroutineScope) {
    companion object {
        private const val TAG = "ESPLEDSMonitor"
        private const val UDP_PREFIX = "ESPLEDS"

        private val MAX_ESP_AGE = Duration.ofSeconds(30)
        private val UDP_PREFIX_BYTES = UDP_PREFIX.toByteArray()
    }

    private val listening = AtomicBoolean(true)
    private var esps = hashMapOf<String, ESPLEDS>()
    private val espsLock = Mutex()

    init {
        scope.launch(Dispatchers.IO) {
            val socket = DatagramSocket(12888, InetAddress.getByName("0.0.0.0"))
            val data = ByteArray(512)
            while (listening.get()) {
                val packet = DatagramPacket(data, data.size)
                socket.receive(packet)

                if (ByteUtils.startsWith(data, UDP_PREFIX_BYTES)) {
                    val len = data[UDP_PREFIX_BYTES.size]
                    val message = String(packet.data, UDP_PREFIX_BYTES.size + 1, len.toInt())

                    val components = message.split('|')

                    if (components.size == 2) {
                        val ip = components[0]
                        val name = components[1]
                        val now = Instant.now()

                        espsLock.withLock {
                            esps.put(ip, ESPLEDS(ip, name, now))
                        }
                    } else {
                        Log.w(TAG, "Unknown message received: '$message'")
                    }
                } else {
                    Log.w(TAG, "Unknown message received: '${String(data)}'")
                }

                yield()
            }
            socket.disconnect()
        }
        scope.launch {
            while (listening.get()) {
                val now = Instant.now()
                espsLock.withLock {
                    esps.values.removeIf { Duration.between(it.lastUpdate, now) > MAX_ESP_AGE }
                }

                delay(2000)
            }
        }
    }

    suspend fun collectESPs(list: MutableList<ESPLEDS>) {
        list.clear()
        espsLock.withLock {
            list.addAll(esps.values)
        }
    }

    fun destroy() {
        listening.set(false)
    }
}