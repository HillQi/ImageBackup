package com.iceqi.mydemo.ui.gallery

import android.content.Context
import android.content.SharedPreferences
import android.text.*
import android.view.*
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import com.iceqi.mydemo.R
import com.iceqi.mydemo.databinding.FtpServerSetupBinding
import com.iceqi.mydemo.ui.common.FTPConfigStore
import com.iceqi.mydemo.ui.common.FTPInfo


class FTPServerSetup{

    lateinit var ctx : Context
    lateinit var inflater : LayoutInflater
    lateinit var parent : View

    private lateinit var popup : PopupWindow
    private lateinit var binding : FtpServerSetupBinding
    private lateinit var dataStore : FTPConfigStore

    fun initView() {
        binding = FtpServerSetupBinding.inflate(inflater)
        popup = PopupWindow(ctx)
        popup.height = WindowManager.LayoutParams.MATCH_PARENT
        popup.width = WindowManager.LayoutParams.MATCH_PARENT
        popup.contentView = binding.root
        popup.isFocusable = true

        val c = OnChange()
        binding.ip.addTextChangedListener(c)
        binding.port.addTextChangedListener(c)
        binding.user.addTextChangedListener(c)
        binding.password.addTextChangedListener(c)


        val o = OnClick()
        binding.cancel.setOnClickListener(o)
        binding.save.setOnClickListener(o)


        dataStore = FTPConfigStore()
        dataStore.ctx = ctx
        var data = dataStore.open()
        if(data != null){
            binding.ip.text = SpannableStringBuilder(data.ip);
            binding.port.text = SpannableStringBuilder(data.port.toString())
            binding.user.text = SpannableStringBuilder(data.user);
            binding.password.text = SpannableStringBuilder(data.password);
        }

        binding.save.isEnabled = false
    }

    fun start(){
        popup.showAtLocation(parent, Gravity.CENTER, 0, 0)
    }



    inner class OnClick : View.OnClickListener{
        override fun onClick(v: View?) {
            when(v){
                binding.cancel -> popup.dismiss()
                binding.save -> {
                    var r = "(\\d{1,2}|(0|1)\\d{2}|2[0-4]\\d|25[0-5])"
                    r = "$r\\.$r\\.$r\\.$r"
                    if(!binding.ip.text.toString().matches(Regex(r))) {
                        Toast.makeText(ctx, R.string.correct_ip_address, Toast.LENGTH_SHORT).show()
                        return
                    }

                    var d = FTPInfo(binding.ip.text.toString(),
                            binding.port.text.toString().toInt(),
                            binding.user.text.toString(),
                            binding.password.text.toString())
                    dataStore.save(d)
                            popup.dismiss()
                }
            }
        }
    }

    inner class OnChange : TextWatcher{
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        }

        override fun afterTextChanged(s: Editable?) {
            val b = binding.ip.text.toString().isNotEmpty()
                    && binding.port.text.toString().isNotEmpty()
                    && binding.user.text.toString().isNotEmpty()
                    && binding.password.text.toString().isNotEmpty()

            binding.save.isEnabled = b
        }
    }

    fun output(o : Any){
        println("mine:: ${o.toString()}")
    }
}