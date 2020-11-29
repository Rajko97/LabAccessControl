package com.vtsappsteam.labaccesscontrol.http

import com.vtsappsteam.labaccesscontrol.utils.Constants
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

object SocketIO {
    private val opts : IO.Options = IO.Options()
    private lateinit var socket : Socket

    init {
        try {
            socket = IO.socket(Constants.API_ADDRESS, opts.apply {
                reconnection = true
                reconnectionDelay = 1000
                timeout = -1
            })
            socket.connect()
        } catch (e : URISyntaxException) {
            e.printStackTrace()
        }
    }

    fun unlock(token : String) {
        socket.emit("unlock-req", token)
    }

    fun checkIn(token : String) {
        socket.emit("checkIn", token)
    }

    fun checkOut(token: String) {
        socket.emit("checkOut", token)
    }

    fun getSocket() : Socket {
        return socket
    }
}