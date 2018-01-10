package com.ravendmaster.linearmqttdashboard

class TabData {
    var id: Int
    var name: String

    constructor() {
        id = 0
        name = ""
    }

    constructor(id: Int, name: String) {
        this.id = id
        this.name = name
    }

}