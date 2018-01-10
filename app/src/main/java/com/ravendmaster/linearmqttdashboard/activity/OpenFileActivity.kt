package com.ravendmaster.linearmqttdashboard.activity

import java.io.File
import java.util.ArrayList
import java.util.Collections
import java.util.Comparator

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.View.OnClickListener
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.AdapterView.OnItemLongClickListener
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.Toast

import com.ravendmaster.linearmqttdashboard.R

class OpenFileActivity : Activity(), OnClickListener, OnItemClickListener {

    internal lateinit var LvList: ListView

    internal var listItems = ArrayList<String>()

    internal lateinit var adapter: ArrayAdapter<String>

    internal lateinit var BtnOK: Button
    internal lateinit var BtnCancel: Button

    internal var currentPath2: String? = null

    internal var selectedFilePath: String? = null /* Full path, i.e. /mnt/sdcard/folder/file.txt */
    internal var selectedFileName: String? = null /* File Name Only, i.e file.txt */

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_open_file)

        try {
            /* Initializing Widgets */
            LvList = findViewById<View>(R.id.LvList) as ListView
            BtnOK = findViewById<View>(R.id.BtnOK) as Button
            BtnCancel = findViewById<View>(R.id.BtnCancel) as Button

            /* Initializing Event Handlers */

            LvList.onItemClickListener = this

            BtnOK.setOnClickListener(this)
            BtnCancel.setOnClickListener(this)

            //

            setCurrentPath(Environment.getExternalStorageDirectory().absolutePath + "/")
        } catch (ex: Exception) {
            Toast.makeText(this,
                    "Error in OpenFileActivity.onCreate: " + ex.message,
                    Toast.LENGTH_SHORT).show()
        }

    }

    internal fun setCurrentPath(path: String) {
        val folders = ArrayList<String>()

        val files = ArrayList<String>()

        currentPath2 = path

        val allEntries = File(path).listFiles()

        for (i in allEntries.indices) {
            if (allEntries[i].isDirectory) {
                folders.add(allEntries[i].name)
            } else if (allEntries[i].isFile) {
                files.add(allEntries[i].name)
            }
        }

        Collections.sort(folders) { s1, s2 -> s1.compareTo(s2, ignoreCase = true) }

        Collections.sort(files) { s1, s2 -> s1.compareTo(s2, ignoreCase = true) }

        listItems.clear()

        for (i in folders.indices) {
            listItems.add(folders[i] + "/")
        }

        for (i in files.indices) {
            listItems.add(files[i])
        }

        adapter = ArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                listItems)
        adapter.notifyDataSetChanged()

        LvList.adapter = adapter
    }

    override fun onBackPressed() {
        if (currentPath2 != Environment.getExternalStorageDirectory().absolutePath + "/") {
            setCurrentPath(File(currentPath2!!).parent + "/")
        } else {
            super.onBackPressed()
        }
    }

    override fun onClick(v: View) {
        val intent: Intent

        when (v.id) {
            R.id.BtnOK -> {

                intent = Intent()
                intent.putExtra("fileName", selectedFilePath)
                intent.putExtra("shortFileName", selectedFileName)
                setResult(Activity.RESULT_OK, intent)

                this.finish()
            }
            R.id.BtnCancel -> {

                intent = Intent()
                intent.putExtra("fileName", "")
                intent.putExtra("shortFileName", "")
                setResult(Activity.RESULT_CANCELED, intent)

                this.finish()
            }
        }
    }

    override fun onItemClick(parent: AdapterView<*>, view: View, position: Int,
                             id: Long) {
        val entryName = parent.getItemAtPosition(position) as String
        if (entryName.endsWith("/")) {
            setCurrentPath(currentPath2!! + entryName)
        } else {
            selectedFilePath = currentPath2!! + entryName

            selectedFileName = entryName

            this.title = ("Select file"
                    + "[" + entryName + "]")
        }
    }
}