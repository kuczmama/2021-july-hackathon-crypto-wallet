package com.example.bitcoin

import android.util.Log
import org.java_websocket.client.WebSocketClient
import org.java_websocket.exceptions.WebsocketNotConnectedException
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI
import javax.net.ssl.SSLSocketFactory

class WebsocketHelper {
    companion object {
        val TAG = WebsocketHelper::class.simpleName

        private var socketStatus:String? = null
        @Volatile private var INSTANCE: WebSocketClient? = null

        fun create(listener: Listener) {
            Log.d(TAG, "create")
            if (INSTANCE == null) {
                INSTANCE = initWebSocket(listener)
            }
        }

        fun destroy() {
            if (socketStatus != null) {
                unsubscribe()
            }
//            INSTANCE?.close()
            INSTANCE = null
        }

        interface Listener {
            fun onMessage(message: String)
        }

        // Initializing WebSocket
        private fun initWebSocket(listener: Listener) : WebSocketClient {
            val coinbaseUri: URI = URI(Constants.WEB_SOCKET_URL)
            Log.d(TAG, "Create Socket")
            val webSocketClient = createWebSocket(coinbaseUri, listener)

            socketStatus = "Connected"

            val socketFactory: SSLSocketFactory = SSLSocketFactory.getDefault() as SSLSocketFactory

            webSocketClient.setSocketFactory(socketFactory)
            webSocketClient.connect()

            return webSocketClient
        }

        //Creating Client WebSocket
        private fun createWebSocket(coinbaseUri: URI?, listener: Listener): WebSocketClient {
            return object : WebSocketClient(coinbaseUri) {

               override fun onOpen(handshakedata: ServerHandshake?) {
                   Log.d(TAG, "onOpen")
                   subscribe()
               }

               override fun onMessage(message: String?) {
                   Log.d(TAG, "onMessage: $message")
                   if (message != null) {
                       listener.onMessage(message)
                   }
               }

               override fun onClose(code: Int, reason: String?, remote: Boolean) {
                   Log.d(TAG, "onClose")
                   unsubscribe()
               }

               override fun onError(ex: Exception?) {
                   Log.e("createWebSocketClient", "onError: ${ex?.message}")
               }
           }
        }

        // Subscribing to coinbase
        private fun subscribe() {
            Log.d(TAG, "subscribe()")
            try {
                INSTANCE?.send(
                    Constants.SUBSCRIBE_COINBASE_SCRIPT
                )
            } catch (e: WebsocketNotConnectedException) {
                Log.e(TAG, "Websocket not connected", e)
            }
        }


        private fun unsubscribe() {
            Log.d(TAG, "unsubscribe()")

            try {
                INSTANCE?.send(
                    Constants.UNSUBSCRIBE_COINBASE_SCRIPT
                )
            } catch (e: WebsocketNotConnectedException) {
                Log.e(TAG, "Websocket not connected")
            } catch (e: Exception) {
                Log.wtf(TAG, "Something horrible happened", e)
            }
        }

    }
}