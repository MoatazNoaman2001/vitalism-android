package com.example.livenativerppg.component.natives


class RPPG {
    enum class RPPGAlgorithm {
        g, pca, xminay
    }


    /**
     * Listener must implement this interface.
     */


    fun load(
        listener: RPPGListener,
        algorithm: RPPGAlgorithm,
        width: Int, height: Int, timeBase: Double, downsample: Int,
        samplingFrequency: Double, rescanFrequency: Double,
        minSignalSize: Int, maxSignalSize: Int,
        logPath: String, classifierPath: String,
        log: Boolean, gui: Boolean, isRB:Boolean,
        Wei:Double,
        Hei:Double,
        Agg:Float,
        Q:Float,
    ) {
        _load(self,
            listener,
            algorithm.ordinal,
            width,
            height,
            timeBase,
            downsample,
            samplingFrequency,
            rescanFrequency,
            minSignalSize,
            maxSignalSize,
            logPath,
            classifierPath,
            log,
            gui,
            isRB,Wei, Hei, Agg, Q)
    }

    fun exit() {
        _exit(self)
    }

    fun processFrame(frameRGB: Long, frameGray: Long, now: Double) {
        _processFrame(self, frameRGB, frameGray, now)
    }
    fun processFrameWithNoFD(frameRGB: Long, frameGray: Long, Mask:Long, now: Double) {
        _processFrameWithNoFD(self, frameRGB, frameGray,Mask, now)
    }

    private var self: Long = 0
    private external fun _initialise(): Long
    private external fun _load(
        self: Long,
        listener: RPPGListener,
        algorithm: Int,
        width: Int,
        height: Int,
        timeBase: Double,
        downsample: Int,
        samplingFrequency: Double,
        rescanFrequency: Double,
        minSignalSize: Int,
        maxSignalSize: Int,
        logPath: String,
        classifierPath: String,
        log: Boolean,
        gui: Boolean,
        isRB: Boolean,
        Wei:Double,
        Hei:Double,
        Agg:Float,
        Q:Float,
    )

    private external fun _processFrame(self: Long, frameRGB: Long, frameGray: Long, time: Double)
    private external fun _processFrameWithNoFD(self: Long, frameRGB: Long, frameGray: Long,Mask:Long, time: Double)
    private external fun _exit(self: Long)

    init {
        self = _initialise()
    }
}