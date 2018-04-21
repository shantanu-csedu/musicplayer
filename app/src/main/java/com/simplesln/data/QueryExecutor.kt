package com.simplesln.data

import android.arch.lifecycle.LiveData
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService

class QueryExecutor<T>(executorService: ExecutorService,callable : Callable<T>) : LiveData<T>() {
    init {
        executorService.submit({
            var result = callable.call()
            postValue(result)
        })
    }
}