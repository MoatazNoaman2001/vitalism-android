package com.example.livenativerppg.component.natives

import org.opencv.core.Point

interface RPPGListener {
    fun onRPPGResult(result: RPPGResult?)
    fun onNewPointGenerated(signalPoint: SignalPoint)
}