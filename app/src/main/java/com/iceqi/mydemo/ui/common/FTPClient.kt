package com.iceqi.mydemo.ui.common

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import java.io.IOException
import java.io.InputStream

// Implement a common FTP client
class FTPClient {
    var ip : String? = null
    var port : Int = -1
    var username : String? = null
    var password : String? = null
    var ftp : FTPClient = FTPClient()

    protected var logSucc = false

    /**
     * login FTP server.
     *
     * Must set ip/port/username/password before call this.
     *
     * After connect to server if client not active within specific period of time
     * then server may shutdown the connection. TO avoid this shutdown call keepAlive() periodically.
     *
     * @return 'true' if login succeed or
     * @throws
     */
    fun login(errHandle : (msg : String) -> Unit) : Boolean{
        try {
            ftp.controlEncoding = "UTF-8"
            if(port > -1)
                ftp.connect(ip, port)
            else
                ftp.connect(ip)

            if (!FTPReply.isPositiveCompletion(ftp.replyCode)) {
                ftp.disconnect()
                return false;
            }else {
                if (!ftp.login(username, password)) {
                    ftp.logout()
                    return false
                }
            }
            ftp.setFileType(FTP.BINARY_FILE_TYPE)
            ftp.enterLocalActiveMode()

            logSucc = true
            return true
        }catch (e : Exception){
            if (ftp.isConnected)
                ftp.disconnect()
            errHandle(e.toString())
            return false
        }
    }

    /**
     * To disconnect from FTP server. FTP client object is not reusable after this.
     */
    fun disconnect(){
        logSucc = false
        ip = null
        port = -1
        username = null
        password = null
        try {
            ftp.disconnect()
        }catch (t : Throwable){

        }

    }

    /**
     * Upload stream to a server side file.
     *
     * @param remote Remote path of file storage
     */
    fun upload(remote : String, inStream : InputStream, errHandle : (msg : String) -> Unit){
        if(!ftp.isConnected) {
            errHandle("not connected to server")
            return
        }
        try {
            ftp.storeFile(remote, inStream)
        }catch (t : java.lang.Exception){
            errHandle(t.toString())
        }
    }

    /**
     * Notify server to keep client alive.
     */
    fun keepAlive(errHandle : (msg : String) -> Unit){
        try{
            ftp.noop()
        }catch (t : Throwable){
            errHandle(t.toString())
        }
    }
}