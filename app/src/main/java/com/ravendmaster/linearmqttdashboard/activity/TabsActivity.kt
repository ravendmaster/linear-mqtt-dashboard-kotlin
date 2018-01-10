package com.ravendmaster.linearmqttdashboard.activity

import android.content.DialogInterface
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentTransaction
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.PopupMenu
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.EditText

import com.ravendmaster.linearmqttdashboard.TabData
import com.ravendmaster.linearmqttdashboard.TabListFragment
import com.ravendmaster.linearmqttdashboard.service.AppSettings
import com.ravendmaster.linearmqttdashboard.R

class TabsActivity : AppCompatActivity() {

    internal lateinit var mTabsListFragment: TabListFragment

    internal var currentTabData: TabData? = null


    fun showPopupMenuTabEditButtonOnClick(view: View) {

        val tab = view.tag as TabData

        val popup = PopupMenu(this, view)
        popup.menuInflater.inflate(R.menu.menu_tab, popup.menu)

        currentTabData = tab

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.tab_edit -> {
                    showTabEditDialog()
                    return@OnMenuItemClickListener true
                }
                R.id.tab_remove -> {
                    showTabRemoveDialog()
                    return@OnMenuItemClickListener true
                }
            }
            false
        })
        popup.show()
    }

    private fun showFragment(fragment: Fragment) {
        val transaction = supportFragmentManager.beginTransaction()
        transaction.replace(R.id.tabs_container, fragment, "fragment").commit()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        instance = this

        setContentView(R.layout.activity_tabs)

        mTabsListFragment = TabListFragment.newInstance()
        showFragment(mTabsListFragment)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        //getMenuInflater().inflate(R.menu.options_editor, menu);
        return super.onCreateOptionsMenu(menu)
    }

    internal fun showTabRemoveDialog() {
        val ad = AlertDialog.Builder(this)
        ad.setTitle("Remove tab")  // заголовок
        ad.setMessage("A set of widgets on the panel will be lost. Continue?") // сообщение
        ad.setPositiveButton("Yes") { dialog, arg1 ->
            val appSettings = AppSettings.instance
            appSettings.removeTabByDashboardID(currentTabData!!.id)
            mTabsListFragment.notifyDataSetChanged()
        }
        ad.setNegativeButton("No") { dialog, arg1 -> }
        ad.show()
    }

    internal fun showTabEditDialog() {

        val li = LayoutInflater.from(this)
        val promptsView = li.inflate(R.layout.tab_name_edit, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setView(promptsView)
        val userInput = promptsView.findViewById<View>(R.id.editTextDialogUserInput) as EditText
        if (currentTabData != null) {
            userInput.setText(currentTabData!!.name)
        }
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton("OK"
                ) { dialog, id ->
                    val newName = userInput.text.toString()
                    if (currentTabData == null) {
                        MainActivity.presenter.addNewTab(newName)
                    } else {
                        currentTabData!!.name = newName
                    }
                    mTabsListFragment.notifyDataSetChanged()
                }
                .setNegativeButton("Cancel"
                ) { dialog, id -> dialog.cancel() }
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId) {
            R.id.add_new_tab -> {
                currentTabData = null
                showTabEditDialog()
            }
            R.id.close ->

                finish()
        }
        return true

    }

    override fun onPause() {
        super.onPause()

        MainActivity.presenter.saveTabsList(this)


    }

    companion object {

        var instance: TabsActivity? = null
    }
}
