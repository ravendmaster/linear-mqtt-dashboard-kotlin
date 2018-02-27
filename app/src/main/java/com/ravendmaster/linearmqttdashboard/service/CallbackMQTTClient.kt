package com.ravendmaster.linearmqttdashboard.service

import com.ravendmaster.linearmqttdashboard.Log
import com.ravendmaster.linearmqttdashboard.Utilities
import org.fusesource.hawtbuf.Buffer
import org.fusesource.hawtbuf.UTF8Buffer
import org.fusesource.mqtt.client.Callback
import org.fusesource.mqtt.client.CallbackConnection
import org.fusesource.mqtt.client.Listener
import org.fusesource.mqtt.client.MQTT
import org.fusesource.mqtt.client.QoS
import org.fusesource.mqtt.client.Topic

import java.net.URISyntaxException
import java.util.ArrayList

class CallbackMQTTClient(internal var imqttMessageReceiver: IMQTTMessageReceiver) {

    private var mqtt: MQTT = MQTT()
    private var callbackConnection: CallbackConnection? = null
    var isConnected: Boolean = false
        internal set

/*
    internal val pseudoID: String
        get() = "35" +
                Build.BOARD.length % 10 + Build.BRAND.length % 10 +
                +Build.DEVICE.length % 10 +
                Build.DISPLAY.length % 10 + Build.HOST.length % 10 +
                Build.ID.length % 10 + Build.MANUFACTURER.length % 10 +
                Build.MODEL.length % 10 + Build.PRODUCT.length % 10 +
                Build.TAGS.length % 10 + Build.TYPE.length % 10 +
                Build.USER.length % 10
*/
    init {
        mqtt.keepAlive = 10
        mqtt.reconnectDelay = 1000
        mqtt.reconnectDelayMax = 3000
        mqtt.reconnectBackOffMultiplier = 1.0
        mqtt.reconnectAttemptsMax
    }

    interface IMQTTMessageReceiver {
        fun onReceiveMQTTMessage(topic: String, payload: Buffer)
    }

    fun publish(topic: String, payload: Buffer, retained: Boolean) {

        if (callbackConnection == null ) {
            isConnected = false
            return
        }
        Log.d(javaClass.name, "publish() "+topic)

        callbackConnection!!.publish(topic, payload.toByteArray(), QoS.AT_LEAST_ONCE, retained, object : Callback<Void> {

            override fun onSuccess(p0: Void?) {
                isConnected = true
                //Log.d(javaClass.name, "PUBLISH SUCCESS")
            }

            override fun onFailure(value: Throwable?) {
                isConnected = false
                Log.d(javaClass.name, "PUBLISH FAILED!!! " + value.toString())
            }
        })
    }


    fun subscribe(topic: String) {

        Log.d("test", "subscribe():" + topic)
        if ((callbackConnection == null) || topic.isEmpty()) return
            val aaa=arrayOf(Topic(topic, QoS.AT_LEAST_ONCE));
            try {
                callbackConnection!!.subscribe(aaa, object : Callback<ByteArray> {
                    override fun onSuccess(bytes: ByteArray?) {
                    }

                    override fun onFailure(throwable: Throwable?) {
                        Log.d(javaClass.name, "subscribe failed!!! " + throwable.toString())
                    }
                })
            }
            catch (e:Exception){

            }
    }

    fun subscribeMass(topicsList: ArrayList<String>) {
        if(callbackConnection==null)return
        val topics = arrayOfNulls<Topic>(topicsList.size)//{new Topic(topic, QoS.AT_LEAST_ONCE)};
        var index = 0
        for (topic in topicsList) {
            topics[index++] = Topic(topic, QoS.AT_LEAST_ONCE)
            Log.d(javaClass.name, "subscribeMass():" + topic)
        }

        callbackConnection!!.subscribe(topics, object : Callback<ByteArray> {
            override fun onSuccess(bytes: ByteArray) {
            }
            override fun onFailure(throwable: Throwable) {
                Log.d(javaClass.name, "subscribe failed!!! " + throwable.toString())
            }
        })

    }

    fun unsubscribeMass(topicsList: ArrayList<String>) {
        if(callbackConnection==null)return
        val topics = arrayOfNulls<UTF8Buffer>(topicsList.size)// {new UTF8Buffer(topic)};
        var index = 0
        for (topic in topicsList) {
            topics[index++] = UTF8Buffer(topic)
            Log.d(javaClass.name, "unsubscribeMass():" + topic)
        }

        callbackConnection!!.unsubscribe(topics, object : Callback<Void> {
            override fun onSuccess(aVoid: Void?) {}

            override fun onFailure(throwable: Throwable?) {
                Log.d("test", "unsubscribe failed!!! " + throwable.toString())
            }
        })
    }

    fun unsubscribe(topic: String?) {
        Log.d("test", "unsubscribe():" + topic!!)
        if (callbackConnection == null || topic.isEmpty()) return
        val topics = arrayOf(UTF8Buffer(topic))
        try {
            callbackConnection!!.unsubscribe(topics, object : Callback<Void> {
                override fun onSuccess(aVoid: Void?) {}

                override fun onFailure(throwable: Throwable?) {
                    Log.d("test", "unsubscribe failed!!! " + throwable.toString())
                }
            })
        }catch(e:Exception){

        }
    }


    fun disconnect() {
        if (!isConnected) return
        Log.d("test", "DISCONNECT")
        isConnected = false
        if (callbackConnection != null) {
            //Log.d(javaClass.name, "callbackConnection.disconnect()")

            callbackConnection!!.disconnect(object : Callback<Void> {
                override fun onSuccess(aVoid: Void?) {
                    //wait = false;
                    Log.d("test", "disconnect success")
                }

                override fun onFailure(throwable: Throwable?) {
                    Log.d("test", "disconnect failed!!!" + throwable.toString())
                }
            })

        }
    }

    fun connect(settings: AppSettings) {
        Log.d("test", "CONNECT!!! " + this.toString())

        mqtt.setUserName(settings.username)
        mqtt.setPassword(settings.password)

        try {
            if (settings.server.indexOf("://") == -1) {
                mqtt.setHost(settings.server, Utilities.parseInt(settings.port, 1883))
            } else {
                mqtt.setHost(settings.server + ":" + Utilities.parseInt(settings.port, 1883))
            }

        } catch (e: URISyntaxException) {
            e.printStackTrace()
        }

        Log.d(javaClass.name, "callbackConnection = mqtt.callbackConnection();")

        callbackConnection = mqtt.callbackConnection()

        callbackConnection!!.listener(object : Listener {
            override fun onConnected() {
                isConnected = true
                Log.d(javaClass.name, "callbackConnection.listener onConnected()")
            }

            override fun onDisconnected() {
                isConnected = false
                Log.d(javaClass.name, "callbackConnection.listener onDisconnected()")
            }

            override fun onPublish(topic: UTF8Buffer, payload: Buffer, ack: Runnable) {
                ack.run()

                isConnected = true

                var urbanTopic = topic.toString()
                if (urbanTopic[urbanTopic.length - 1] == '$') {
                    urbanTopic = urbanTopic.substring(0, urbanTopic.length - 1)
                }
                //Log.d(getClass().getName(), "onPublish "+urbanTopic+" payload:"+new String(payload.toByteArray()));
                imqttMessageReceiver.onReceiveMQTTMessage(urbanTopic, payload)
            }

            override fun onFailure(throwable: Throwable) {
                Log.d(javaClass.name, "callbackConnection.listener onFailure() " + throwable.toString())
            }
        })


        callbackConnection!!.connect(object : Callback<Void> {
            override fun onSuccess(Void: Void?) {
                isConnected = true
                Log.d(javaClass.name, "callbackConnection.reConnect onSuccess()")
            }

            override fun onFailure(throwable: Throwable?) {

            }
        })

    }

}
