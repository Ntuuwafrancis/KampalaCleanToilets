package com.francosoft.kampalacleantoilets.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters

class GeofenceWorker(context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {
    override fun doWork(): Result {
        return Result.success()
    }
}