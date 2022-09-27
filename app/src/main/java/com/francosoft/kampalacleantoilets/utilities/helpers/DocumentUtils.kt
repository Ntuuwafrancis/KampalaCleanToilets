package com.francosoft.kampalacleantoilets.utilities.helpers

import android.content.Context
import android.os.Build
import android.os.Environment
import android.widget.TextView
import com.francosoft.kampalacleantoilets.data.models.Toilet
import org.apache.poi.wp.usermodel.HeaderFooterType
import org.apache.poi.xwpf.extractor.XWPFWordExtractor
import org.apache.poi.xwpf.usermodel.ParagraphAlignment
import org.apache.poi.xwpf.usermodel.XWPFDocument
import java.io.*
import java.util.*


object DocumentUtils {

    //initializing an empty word document
    fun createWordDoc(): XWPFDocument {
        return XWPFDocument()
    }

    fun addParagraph(targetDoc:XWPFDocument, report : String){
        //creating a paragraph in our document and setting its alignment
        val paragraph1 = targetDoc.createParagraph()
        paragraph1.alignment = ParagraphAlignment.LEFT

        //creating a run for adding text
        val sentenceRun1 = paragraph1.createRun()

        val heading = "$report Report"
        //format the text
        sentenceRun1.isBold = true
        sentenceRun1.fontSize = 15
        sentenceRun1.fontFamily = "Calibri"
        sentenceRun1.color = "FF01579B"
        sentenceRun1.setText(heading)
        //add a sentence break
        sentenceRun1.addBreak()

//        //add another run
//        val sentenceRun2 = paragraph1.createRun()
//        sentenceRun2.fontSize = 12
//        sentenceRun2.fontFamily = "Comic Sans MS"
//        sentenceRun2.setText("Second sentence run starts here. We love Apache POI. Lorem ipsum dolor sit amet, consectetur adipiscing elit. Sed lacinia dui consectetur euismod ultrices. Aenean et enim pulvinar purus scelerisque dapibus. Duis euismod lorem nec justo viverra ornare. Aliquam est erat, mollis at iaculis eu, ultricies aliquet risus. Proin lacinia ligula sed quam elementum, congue tincidunt lorem iaculis. Nulla facilisi. Praesent faucibus metus eu nisi tincidunt rhoncus vitae et ligula. Pellentesque quam dui, pellentesque vitae placerat eu, tempor ut lectus.")
//        sentenceRun2.addBreak()

    }

    fun addTable(targetDoc: XWPFDocument, toilets: MutableList<Toilet>){
        val ourTable = targetDoc.createTable()

        var num = 0

        //Creating the first row and adding cell values
        val row1 = ourTable.getRow(0)
        row1.getCell(0).let {
            it.text = "No."
            it.color = "FF01579B"
        }

        row1.addNewTableCell().let {
            it.text = "Toilet"
            it.color = "FF01579B"
        }
        row1.addNewTableCell().let {
            it.text = "Type"
            it.color = "FF01579B"
        }
        row1.addNewTableCell().let {
            it.text = "Status"
            it.color = "FF01579B"
        }
        row1.addNewTableCell().let {
            it.text = "Division"
            it.color = "FF01579B"
        }

        for (toilet in toilets) {
            //Creating the next row
            num++
            ourTable.createRow().let {
                it.getCell(0).text = num.toString()
                it.getCell(1).text = toilet.stitle
                it.getCell(2).text = toilet.type
                it.getCell(3).text = toilet.status
                it.getCell(4).text = toilet.division
            }
        }

        val totalTable = targetDoc.createTable()
        val rowt1 = totalTable.getRow(0)
        rowt1.getCell(0).text = "Total Toilets"
        rowt1.addNewTableCell().let {
            it.text = toilets.size.toString()
            it.color = "FF01579B"
        }
    }


    fun addHeaderAndFooter(targetDoc:XWPFDocument){
        //initializing the header
        val docHeader = targetDoc.createHeader(HeaderFooterType.DEFAULT)

        //creating a run for the header. This is for setting the header text and stylings
        val headerRun = docHeader.createParagraph().createRun()
        headerRun.setText("Clean Toilet Locator (CTL)")
        headerRun.fontFamily = "Calibri"
        headerRun.isBold = true
        headerRun.color = "FF000000"

        //initializing the footer
        val docFooter = targetDoc.createFooter(HeaderFooterType.DEFAULT);

        //creating a run for the footer. This sets the footer text and stylings

        val year = Calendar.getInstance().get(Calendar.YEAR)
        val copyright = "Copyright \u00a9 $year, Clean Toilet Locator(CTL)"
        val footerRun = docFooter.createParagraph().createRun()
        footerRun.fontFamily = "Calibri"
        footerRun.isBold = true
        footerRun.color = "FF000000"
        footerRun.setText(copyright)
    }

    fun saveOurDoc(targetDoc:XWPFDocument, filesDir : File?, docName: String, context: Context){
        //Check whether it exists or not, and create one if it does not exist.
        var filesDir2 = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            filesDir2 = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        }
        if (filesDir2 != null && !filesDir2.exists()) {
            filesDir2.mkdirs()
        }

        //Create a word file called test.docx and save it to the file system
        var wordFile = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), docName)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            wordFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), docName)
        }

        try {
            AppExecutors.instance?.diskIO()?.execute{
                val fileOut = FileOutputStream(wordFile)
                targetDoc.write(fileOut)
                fileOut.close()
            }

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun loadDoc(docName: String, context: Context): File? {
        var filesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            filesDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)
        }
        filesDir.let {

            //Check if file exists or not
            if (it.exists()) {
                //check the file in the directory called "myDoc.docx"
                //return the file
                return File(filesDir, docName)
            }
        }
        return null
    }

    fun readDoc(textView: TextView, context: Context, docName: String){
        loadDoc(docName, context)?.let {
            try {
                //Reading it as stream
                val docStream = FileInputStream(it)
                val targetDoc = XWPFDocument(docStream)

                //creating a constructor object for extracting text from the word document
                val wordExtractor = XWPFWordExtractor(targetDoc)
                val docText = wordExtractor.text
                //displaying the text read from the document
                textView.text = docText
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

}