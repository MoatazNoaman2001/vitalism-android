package com.example.livenativerppg.component.natives

class RPPGListenerList {
    private var _results: ArrayList<RPPGResult> = ArrayList()

    public fun addResult(result: RPPGResult) {
        _results.add(result)
    }

    public fun removeResult(result: RPPGResult) {
        _results.remove(result)
    }

    public fun removeResult(index: Int) {
        _results.removeAt(index)
    }

    fun getSize():Int{
        return _results.size
    }

    var results = _results
        get() {
            return field
        }

}