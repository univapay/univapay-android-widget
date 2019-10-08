package com.univapay.models

class AppCredentials{

    var appId: String
     private set

    constructor(id: String){
        if(id.isEmpty()){
            throw Exception("Set the app credentials properly")
        } else {
            this.appId = id
        }
    }
}
