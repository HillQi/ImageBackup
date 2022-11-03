package com.iceqi.mydemo.ui.common.tmp

import java.io.*
import java.text.SimpleDateFormat
import java.util.*

class MyTest {
}

fun t1(f: (x: Int, y: Int) -> Unit){
    f(1, 3)

    println("======")
}

fun t2(){
//    t1 { x, y ->
//        println(x*y)
//    }
    t1 out@{x, y ->
        if(x < y)
            return@out
        println(x*y)
    }
}

interface I{

}
open class F(x: Int){

}

class FF(val x: Int, val y: Int): F(x), I{

}

fun typeCheck(){
//    val a: Any = ""
//
//    when(a){
//        is String -> return
//    }

    println("=======")
    output(F(1) is FF)
}

fun output(o: Any){
    println(o.toString())
}

fun main(str: Array<String>){
//    println(".....")
//    t2()
//
////    listOf(1, 2, 3, 4, 5).forEach lit@{
////        if (it == 3) return // 局部返回到该 lambda 表达式的调用者——forEach 循环
////        print(it)
////    }
////    print(" done with explicit label")
//    typeCheck()


//    Calendar calendar = Calendar.getInstance();
//
//    calendar.setTimeInMillis(milliSeconds)

//    var s = "2022-09-10 00:00:00"

//    var l = 1000*60*60*24*7
//    val c = Calendar.getInstance()
//
//    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
//    output(sdf.format(c.time))
//    val now = c.timeInMillis
//    c.timeInMillis = now - l
//    output(sdf.format(c.time))
//    val date = SimpleDateFormat("yyyy-MM-dd").format(c.time)
//    output(sdf.format(sdf.parse("$date 00:00:00")))


//    val s = arrayOf("1", "2", "3")
//    for( i in 1 until s.size)
//        output(s[i])


}

open class A{
    protected open fun a(){

    }

    fun bb(){
        val a = A()
        a.a()
    }
}

open class B : A(){
//    override fun a(){
//
//    }

    fun b(){
        val a = A()
        a()
    }
}