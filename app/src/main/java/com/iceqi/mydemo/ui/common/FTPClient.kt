package com.iceqi.mydemo.ui.common

import org.apache.commons.net.ftp.FTP
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.apache.tools.ant.taskdefs.Java
import java.io.InputStream
import java.nio.charset.StandardCharsets

// Implement a common FTP client
class FTPClient {
    var ip : String? = null
    var port : Int = -1
    var username : String? = null
    var password : String? = null
    var ftp : FTPClient = FTPClient()

    protected var login = false

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
            ftp.bufferSize = 1024000
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
            ftp.enterLocalPassiveMode()
            ftp.doCommand("opts", "utf8 on")
            login = true
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
        login = false
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
     * Change work folder. If folder does not exist then try to create
     * sequence folder.
     *
     * @return 'true' If change folder succeed or 'false' if not.
     */
    fun makeDirectories(path: String) : Boolean{
        val folders = path.split("/")
        if(folders != null && folders.isNotEmpty()){
            for(s in folders){
                if(s.isNotEmpty()){
                    var succ = ftp.changeWorkingDirectory(s)
                    if(!succ){
                        succ = ftp.makeDirectory(s)
                        if(succ)
                            ftp.changeWorkingDirectory(s)
                        else
                            return false
                    }
                }
            }
        }

        return true
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