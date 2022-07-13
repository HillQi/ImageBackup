package com.iceqi.mydemo.ui.gallery

import android.widget.CheckBox
import android.widget.ImageView
import kotlin.reflect.KProperty


class TestA {

      class Dele {
         var form = ""
         operator fun getValue(ref: Any, prop: KProperty<*>)
                 : String {
             return form + "=" + form.length
         }

         operator fun setValue(thisRef: Any, prop: KProperty<*>, v: String) {
             form = v
         }
     }

     class P {
        var n: String by Dele()
    }

    init{
        val v = P()
        v.n = "sss"
        val vv = v.n
    }

}

class ImageViewTag(val  path : String){

}

class FF{
    init{
        val v = TestA.P()
        v.n = "sss"
        val vv = v.n
    }
}


// variable that we shall initialize at a later point in code
lateinit var person1:Person

fun main(args: Array<String>) {
    var vv = ImageViewTag( "fdasdfdsafsdafsda")
    println(vv.path)
    // initializing variable lately
    person1 = Person("Ted",28)
    print(person1.name + " is " + person1.age.toString())
}

data class Person(var name:String, var age:Int)