package com.vvadu.iot_project

public class Model{
    var temp:Float = 0.0f
    var humid:Float = 0.0f
    var gasl:Int = 0
    lateinit var time:String

    constructor(temp: Float, humid:Float, gasl:Int, time:String) {
        this.temp = temp
        this.humid = humid
        this.gasl = gasl
        this.time = time
    }

    constructor()
}