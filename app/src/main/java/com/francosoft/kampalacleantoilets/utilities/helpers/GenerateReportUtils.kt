package com.francosoft.kampalacleantoilets.utilities.helpers

import android.content.Context
import com.francosoft.kampalacleantoilets.data.models.Toilet
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

object GenerateReportUtils {
    private var toilets = mutableListOf<Toilet>()

    init {
        val database = FirebaseDatabase.getInstance()
        val dbref = database.getReference("toilet")
        val valueEventListener: ValueEventListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                toilets.clear()
                for (ds in dataSnapshot.children) {
                    val toilet = ds.getValue(Toilet::class.java) as Toilet

                    if(toilet.approved.equals("delete") || toilet.approved.equals("approved")){
                        toilets.add(toilet)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
            }
        }
        dbref.addListenerForSingleValueEvent(valueEventListener)
    }


    fun createToiletsReport(context: Context) {
        val toiletsReport = DocumentUtils.createWordDoc()
        val docName = "All Toilets CTL-Report.docx"
        DocumentUtils.addParagraph(toiletsReport, "All Toilets")
        DocumentUtils.addTable(toiletsReport, toilets)
        DocumentUtils.addHeaderAndFooter(toiletsReport)
        DocumentUtils.saveOurDoc(
            toiletsReport,
            context.applicationContext.filesDir,
            docName,
           context
        )

//        readDoc(view.findViewById(R.id.tvReportView), view.context, "All Toilets CTL-Report.docx")

    }

    fun createStatusReport(context: Context) {
        var docName = ""
        val statusList = mutableListOf<String>()
        val operatingList = mutableListOf<Toilet>()
        val closedList = mutableListOf<Toilet>()
        val repairList = mutableListOf<Toilet>()
        statusList.add("under repair")
        statusList.add("operating")
        statusList.add("closed")
        for (toilet in toilets) {
            when(toilet.status) {
                "operating" -> {operatingList.add(toilet)}
                "under repair" -> {repairList.add(toilet)}
                "closed" -> {closedList.add(toilet)}
            }
        }

        for (status in statusList) {
            when(status){
                "operating" -> {
                    docName = "Operating Toilets CTL-Report.docx"
                    val operatingReport = DocumentUtils.createWordDoc()
                    DocumentUtils.addHeaderAndFooter(operatingReport)
                    DocumentUtils.addParagraph(operatingReport, "Operating Toilets")
                    DocumentUtils.addTable(operatingReport, operatingList)
                    DocumentUtils.saveOurDoc(
                        operatingReport,
                        context.applicationContext.filesDir,
                        docName,
                        context
                    )
//                    Toast.makeText(view.context, "$docName created in Documents", Toast.LENGTH_SHORT).show()
//                    DocumentUtils.readDoc(tv, view.context.applicationContext.filesDir, docName )
                }
                "under repair" -> {
                    docName = "Under-Repair CTL-Report.docx"
                    val repairReport = DocumentUtils.createWordDoc()
                    DocumentUtils.addHeaderAndFooter(repairReport)
                    DocumentUtils.addParagraph(repairReport, "Under Repair Toilets")
                    DocumentUtils.addTable(repairReport, repairList)
                    DocumentUtils.saveOurDoc(
                        repairReport,
                        context.applicationContext.filesDir,
                        docName,
                        context
                    )
//                    DocumentUtils.readDoc(tv, view.context.applicationContext.filesDir, "Under-Repair CTL-Report.docx" )
                }
                "closed" -> {
                    docName = "Closed Toilets CTL-Report.docx"
                    val closedReport = DocumentUtils.createWordDoc()
                    DocumentUtils.addHeaderAndFooter(closedReport)
                    DocumentUtils.addParagraph(closedReport, "Closed Toilets")
                    DocumentUtils.addTable(closedReport, closedList)
                    DocumentUtils.saveOurDoc(
                        closedReport,
                        context.applicationContext.filesDir,
                        docName,
                        context
                    )
//                    Toast.makeText(view.context, "$docName created in Documents", Toast.LENGTH_SHORT).show()
//                    readDoc(tv , view.context.applicationContext.filesDir, "Closed Toilets CTL-Report.docx")
                }
            }
        }

    }

    fun createTypeReport(context: Context) {
        var docName = ""
        val typeList = mutableListOf<String>()
        val publicList = mutableListOf<Toilet>()
        val privateList = mutableListOf<Toilet>()

        typeList.add("public")
        typeList.add("private")
        for (toilet in toilets) {
            when(toilet.type) {
                "public" -> {publicList.add(toilet)}
                "private" -> {privateList.add(toilet)}
            }
        }

        for (type in typeList) {
            when(type){
                "public" -> {
                    docName = "Public Toilets CTL-Report.docx"
                    val publicReport = DocumentUtils.createWordDoc()
                    DocumentUtils.addHeaderAndFooter(publicReport)
                    DocumentUtils.addParagraph(publicReport, "Public Toilets")
                    DocumentUtils.addTable(publicReport, publicList)
                    DocumentUtils.saveOurDoc(
                        publicReport,
                        context.applicationContext.filesDir,
                        docName,
                        context
                    )
//                    Toast.makeText(view.context, "$docName created in Documents", Toast.LENGTH_SHORT).show()
//                    DocumentUtils.readDoc(tv, view.context.applicationContext.filesDir, "Public Toilets CTL-Report.docx" )
                }
                "private" -> {
                    docName = "Private CTL-Report.docx"
                    val privateReport = DocumentUtils.createWordDoc()
                    DocumentUtils.addHeaderAndFooter(privateReport)
                    DocumentUtils.addParagraph(privateReport, "Private Toilets")
                    DocumentUtils.addTable(privateReport, privateList)
                    DocumentUtils.saveOurDoc(
                        privateReport,
                        context.applicationContext.filesDir,
                        docName,
                        context
                    )
//                    Toast.makeText(view.context, "$docName created in Documents", Toast.LENGTH_SHORT).show()
//                    DocumentUtils.readDoc(tv, view.context.applicationContext.filesDir, "Private CTL-Report.docx" )
                }
            }
        }
    }

    fun createDivisionReport(context: Context) {
        var docName = ""
        val divisionList = mutableListOf<String>()
        val centralList = mutableListOf<Toilet>()
        val rubagaList = mutableListOf<Toilet>()
        val nakawaList = mutableListOf<Toilet>()
        val makindyeList = mutableListOf<Toilet>()
        val kawempeList = mutableListOf<Toilet>()
        divisionList.add("central")
        divisionList.add("rubaga")
        divisionList.add("nakawa")
        divisionList.add("makindye")
        divisionList.add("kawempe")

        for (toilet in toilets) {
            when(toilet.division) {
                "central" -> {centralList.add(toilet)}
                "rubaga" -> {rubagaList.add(toilet)}
                "nakawa" -> {nakawaList.add(toilet)}
                "makindye" -> {makindyeList.add(toilet)}
                "kawempe" -> {kawempeList.add(toilet)}
            }
        }

        for (division in divisionList) {
            when(division){
                "central" -> {
                    docName = "Central Division Toilets CTL-Report.docx"
                    val centralReport = DocumentUtils.createWordDoc()
                    DocumentUtils.addHeaderAndFooter(centralReport)
                    DocumentUtils.addParagraph(centralReport, "Central Division Toilets")
                    DocumentUtils.addTable(centralReport, centralList)
                    DocumentUtils.saveOurDoc(
                        centralReport,
                        context.applicationContext.filesDir,
                        docName,
                        context
                    )
//                    Toast.makeText(view.context, "$docName created in Documents", Toast.LENGTH_SHORT).show()
//                    DocumentUtils.readDoc(tv, view.context.applicationContext.filesDir, "Central Division Toilets CTL-Report.docx" )
                }
                "rubaga" -> {
                    docName = "Rubaga Division CTL-Report.docx"
                    val rubagaReport = DocumentUtils.createWordDoc()
                    DocumentUtils.addHeaderAndFooter(rubagaReport)
                    DocumentUtils.addParagraph(rubagaReport, "Rubaga Division Toilets")
                    DocumentUtils.addTable(rubagaReport, rubagaList)
                    DocumentUtils.saveOurDoc(
                        rubagaReport,
                        context.applicationContext.filesDir,
                        docName,
                        context
                    )
//                    Toast.makeText(view.context, "$docName created in Documents", Toast.LENGTH_SHORT).show()
//                    DocumentUtils.readDoc(tv, view.context.applicationContext.filesDir, "Rubaga Division CTL-Report.docx" )
                }
                "nakawa" -> {
                    docName = "Nakawa Division Toilets CTL-Report.docx"
                    val nakawaReport = DocumentUtils.createWordDoc()
                    DocumentUtils.addHeaderAndFooter(nakawaReport)
                    DocumentUtils.addParagraph(nakawaReport, "Nakawa Division Toilets")
                    DocumentUtils.addTable(nakawaReport, nakawaList)
                    DocumentUtils.saveOurDoc(
                        nakawaReport,
                        context.applicationContext.filesDir,
                        docName,
                        context
                    )
//                    Toast.makeText(view.context, "$docName created in Documents", Toast.LENGTH_SHORT).show()
//                    readDoc(tv , view.context.applicationContext.filesDir, "Nakawa Division Toilets CTL-Report.docx")
                }
                "makindye" -> {
                    docName = "Makindye Division Toilets CTL-Report.docx"
                    val makindyeReport = DocumentUtils.createWordDoc()
                    DocumentUtils.addHeaderAndFooter(makindyeReport)
                    DocumentUtils.addParagraph(makindyeReport, "Makindye Division Toilets")
                    DocumentUtils.addTable(makindyeReport, makindyeList)
                    DocumentUtils.saveOurDoc(
                        makindyeReport,
                        context.applicationContext.filesDir,
                        docName,
                        context
                    )
//                    Toast.makeText(view.context, "$docName created in Documents", Toast.LENGTH_SHORT).show()
//                    readDoc(tv , view.context.applicationContext.filesDir, "Makindye Division Toilets CTL-Report.docx")
                }
                "kawempe" -> {
                    docName = "Kawempe Division Toilets CTL-Report.docx"
                    val kawempeReport = DocumentUtils.createWordDoc()
                    DocumentUtils.addHeaderAndFooter(kawempeReport)
                    DocumentUtils.addParagraph(kawempeReport, "Kawempe Division Toilets")
                    DocumentUtils.addTable(kawempeReport, kawempeList)
                    DocumentUtils.saveOurDoc(
                        kawempeReport,
                        context.applicationContext.filesDir,
                        docName,
                        context
                    )
//                    Toast.makeText(view.context, "$docName created in Documents", Toast.LENGTH_SHORT).show()
//                    readDoc(tv , view.context.applicationContext.filesDir, "Kawempe Division Toilets CTL-Report.docx")
                }
            }
        }
    }
}