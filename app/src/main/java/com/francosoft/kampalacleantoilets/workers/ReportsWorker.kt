package com.francosoft.kampalacleantoilets.workers

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.All_TOILETS_REPORT
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.DIVISIONS_REPORT
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.STATUS_REPORT
import com.francosoft.kampalacleantoilets.utilities.helpers.Constants.TYPE_REPORT
import com.francosoft.kampalacleantoilets.utilities.helpers.GenerateReportUtils.createDivisionReport
import com.francosoft.kampalacleantoilets.utilities.helpers.GenerateReportUtils.createStatusReport
import com.francosoft.kampalacleantoilets.utilities.helpers.GenerateReportUtils.createToiletsReport
import com.francosoft.kampalacleantoilets.utilities.helpers.GenerateReportUtils.createTypeReport

class ReportsWorker(context: Context, workerParams: WorkerParameters) : Worker(context,
    workerParams
) {
    override fun doWork(): Result {
        val report = inputData.getString("report")
        generateReports(report, applicationContext)
        return Result.success()
    }

    private fun generateReports(report: String?, context: Context) {
       when(report) {
           All_TOILETS_REPORT -> {
               createToiletsReport(context)
//               Toast.makeText(context, "All Toilets report created in Documents", Toast.LENGTH_SHORT).show()
           }
           DIVISIONS_REPORT -> {
               createDivisionReport(context)
//               Toast.makeText(context, "Divisions reports created in Documents", Toast.LENGTH_SHORT).show()
           }
           TYPE_REPORT -> {
               createTypeReport(context)
//               Toast.makeText(context, "Toilet Types reports created in Documents", Toast.LENGTH_SHORT).show()
           }
           STATUS_REPORT -> {
               createStatusReport(context)
//               Toast.makeText(context, "Toilet Status reports created in Documents", Toast.LENGTH_SHORT).show()
           }
       }
    }


}