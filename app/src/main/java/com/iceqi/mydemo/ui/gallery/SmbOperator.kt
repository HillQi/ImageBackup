package com.iceqi.mydemo.ui.gallery

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.dbrg.smb2.Smb2Helper
import com.iceqi.mydemo.utils.MyLog
import java.io.File
import java.io.FileInputStream
import java.io.IOException

/**
 * Operate Smb interface
 */
@RequiresApi(Build.VERSION_CODES.O)
class SmbOperator {
    @RequiresApi(Build.VERSION_CODES.O)
    val smb2Helper = Smb2Helper()
    val log = MyLog()


    public fun init(){
        smb2Helper.init()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    public fun copyFile(localFile : File, overWriteIfExist : Boolean) : Boolean{
            return smb2Helper.writeSmbFile2(
                localFile,
                localFile.name,
                overWriteIfExist,
            )
    }
}

