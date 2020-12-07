package com.nankai.flutter_nearby_connections.wifip2p

import android.os.AsyncTask
import android.util.Log
import java.io.ByteArrayInputStream
import java.io.FileNotFoundException
import java.io.IOException
import java.io.InputStream
import java.net.InetSocketAddress
import java.net.Socket

class ClientSocket(ip: String?, message: String?) : AsyncTask<Any?, Any?, Any?>() {

    private var socket: Socket? = null
    private var host: String? = null
    private var data: String? = null

    companion object {
        private const val TAG = "ClientSocket"
    }

    init {
        if (message != null) {
            data = message
        } else data = "null data"
        host = ip
    }

    override fun doInBackground(objects: Array<Any?>): Any? {
        sendData()
        return null
    }

    override fun onPostExecute(o: Any?) {
        super.onPostExecute(o)
        Log.d(TAG, "SendDataTask Completed")
    }

    private fun sendData() {
        val port = 8888
        var len: Int
        socket = Socket()
        val buf = ByteArray(1024)
        try {
            /**
             * Create a client socket with the host,
             * port, and timeout information.
             */
            socket?.bind(null)
            Log.d(TAG, "Trying to connect...")
            socket?.connect(InetSocketAddress(host, port), 500)
            Log.d(TAG, "Connected...")
            /**
             * Create a byte stream from a JPEG file and pipe it to the output stream
             * of the socket. This data will be retrieved by the server device.
             */
            val outputStream = socket!!.getOutputStream()
            //ContentResolver cr = context.getContentResolver();
            var inputStream: InputStream? = null
            inputStream = ByteArrayInputStream(data?.toByteArray())
            while (inputStream.read(buf).also { len = it } != -1) {
                outputStream.write(buf, 0, len)
            }
            outputStream.close()
            inputStream.close()
        } catch (e: FileNotFoundException) {
            //catch logic
            Log.d(TAG, e.toString())
        } catch (e: IOException) {
            //catch logic
            //activity.makeToast(ClientSocket.TAG + " " +e.toString());
            Log.d(TAG, e.toString())
        }
        /**
         * Clean up any open sockets when done
         * transferring or if an exception occurred.
         */
        finally {
            if (socket?.isConnected == true) {
                try {
                    socket?.close()
                } catch (e: IOException) {
                    //catch logic
                }
            }
        }
    }

    inner class SendDataTask(private val toSend: String) : AsyncTask<Any?, Any?, Any?>() {
        override fun doInBackground(objects: Array<Any?>): Any? {
            sendString()
            return null
        }

        private fun sendString() {}
    }
}