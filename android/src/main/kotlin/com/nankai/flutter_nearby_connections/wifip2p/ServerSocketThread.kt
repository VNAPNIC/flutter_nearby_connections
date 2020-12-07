package com.nankai.flutter_nearby_connections.wifip2p

import android.os.AsyncTask
import android.util.Log
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.ServerSocket

class ServerSocketThread : AsyncTask<Any?, Any?, Any?>() {
    var serverSocket: ServerSocket? = null
    var receivedData = "null"
    private val port = 8888
    var isInterrupted = false
    var listener: OnUpdateListener? = null

    interface OnUpdateListener {
        fun onUpdate(data: String?)
    }

    fun setUpdateListener(listener: OnUpdateListener?) {
        this.listener = listener
    }

    override fun doInBackground(objects: Array<Any?>): Void? {
        try {
            Log.d(TAG, " started doInBackground")
            serverSocket = ServerSocket(8888)
            while (!isInterrupted) {
                val client = serverSocket!!.accept()
                Log.d(TAG, "Accepted Connection")
                val inputstream = client.getInputStream()
                val bufferedReader = BufferedReader(InputStreamReader(inputstream))
                val sb = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    sb.append(line)
                }
                bufferedReader.close()
                Log.d(TAG, "Completed ReceiveDataTask")
                receivedData = sb.toString()
                if (listener != null) {
                    listener!!.onUpdate(receivedData)
                }
                Log.d(TAG, "received data: $receivedData")
            }
            serverSocket!!.close()
            return null
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "IOException occurred")
        }
        return null
    }

    companion object {
        private const val TAG = "ServerSocketThread"
    }
}