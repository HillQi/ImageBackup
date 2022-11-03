package com.iceqi.mydemo.ui.common.tmp

//import java.util.Arrays
data class example(val user: String, val ID: Int, val city: String)
fun main(args: Array<String>) {
    ff({return@ff}, {println("secone")})
    ff1{
        return
    }
    println("here")
}

inline fun ff(/*noinline*/ f : () -> Unit, noinline ff : () -> Unit){
//    ff2{f()
////        if(true)
////            return@ff2
////        return@ff2
//    }
//    if(true)
//        return
    println("before")
    f()
    ff()
    println("ff")
}

inline  fun ff1(f:() -> Unit){

}

//inline
fun ff2(  f:() -> Unit){
    f()
}

class Test (n : String){

    var nn = n
    var ab = 1
    fun ff(bar : (aa : Test) -> Unit){
        var a = Test("2")
        bar(a)
    }

}

inline fun <reified T> List<Any>.filterFruit(): List<T> {
    return this.filter { it is T }.map { it as T }
}

fun fff(){
    val a = A()
}