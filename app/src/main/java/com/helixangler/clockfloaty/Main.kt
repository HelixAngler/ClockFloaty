package com.helixangler.clockfloaty

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.bottomnavigation.BottomNavigationView

class Main : AppCompatActivity() {
    private var menuId:Int = R.id.btn_main_menu
    lateinit var changesListener: SharedPreferences.OnSharedPreferenceChangeListener
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initialize(savedInstanceState)

    }
    fun initialize(savedInstanceState: Bundle?){

        permissionCheck()

        val MenuFragment = HomeUI()
        val SettingsFragment = SettingsUI()
        val AppearanceFragment = AppearanceUI()
        val mainUIContainer = findViewById(R.id.mainUIContainer) as View
        val NavigationUI = findViewById(R.id.bottom_bar) as BottomNavigationView

        if(savedInstanceState != null) {
            menuId = savedInstanceState.getInt("menuId")
        }
        NavigationUI.selectedItemId = menuId
        switchMenu(menuId)


        NavigationUI.setOnItemSelectedListener { it ->
            switchMenu(it.itemId)
            false
        }


        var dataPreference = this.applicationContext.getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )

        initAppearanceParams(dataPreference)

        if(dataPreference.getBoolean("activateWidget",false)){
            initClockWidget()
        }


        changesListener = SharedPreferences.OnSharedPreferenceChangeListener{
                sharedPreferences:SharedPreferences,
                key:String -> if(key == "activateWidget" ){
                    if(sharedPreferences.getBoolean("activateWidget",false)){
                        initClockWidget()
                    }
                }
        }
        dataPreference.registerOnSharedPreferenceChangeListener(changesListener)


    }

    fun switchMenu(itemId: Int){
        var UITransitioning = supportFragmentManager.beginTransaction()
        val MenuFragment = HomeUI()
        val SettingsFragment = SettingsUI()
        val AppearanceFragment = AppearanceUI()
        var isValid:Boolean = false
        when(itemId){
            R.id.btn_main_menu -> {
                UITransitioning.replace(R.id.fragment_container, MenuFragment)
                isValid = true
            }
            R.id.btn_settings -> {
                UITransitioning.replace(R.id.fragment_container, SettingsFragment)
                isValid = true
            }
            R.id.btn_appearance -> {
                UITransitioning.replace(R.id.fragment_container, AppearanceFragment)
                isValid = true
            }
        }
        if (isValid){
            menuId = itemId
            UITransitioning.commit()
        }
    }

    fun initAppearanceParams(sharedPreferences: SharedPreferences){
        var editor:SharedPreferences.Editor = sharedPreferences.edit()
        editor.putInt(
            "widgetWidth",
            sharedPreferences.getInt(
                "widgetWidth",
                resources.getInteger(R.integer.defaultWidgetWidth)
            )
        )
        editor.putInt(
            "widgetHeight",
            sharedPreferences.getInt(
                "widgetHeight",
                resources.getInteger(R.integer.defaultWidgetHeight)
            )
        )
        editor.putInt(
            "widgetTimeTextSize",
            sharedPreferences.getInt(
                "widgetTimeTextSize",
                resources.getInteger(R.integer.defaultWidgetTimeTextSize)
            )
        )
        editor.putInt(
            "widgetDateTextSize",
            sharedPreferences.getInt(
                "widgetDateTextSize",
                resources.getInteger(R.integer.defaultWidgetDateTextSize)
            )
        )
        editor.putFloat(
            "widgetRoundX",
            sharedPreferences.getFloat(
                "widgetRoundX",
                resources
                    .getInteger(R.integer.defaultWidgetXCornerRadius)
                    .toFloat()
            )
        )
        editor.putFloat(
            "widgetRoundY",
            sharedPreferences.getFloat(
                "widgetRoundY",
                resources
                    .getInteger(R.integer.defaultWidgetYCornerRadius)
                    .toFloat()
            )
        )
        editor.putString(
            "widgetBGColor",
            sharedPreferences.getString(
                "widgetBGColor",
                getString(R.string.defaultWidgetBackgroundColor)
            )
        )
        editor.putString(
            "widgetTextColor",
            sharedPreferences.getString(
                "widgetTextColor",
                getString(R.string.defaultWidgetTextColor)
            )
        )
        editor.putString(
            "widgetTextFont",
            sharedPreferences.getString(
                "widgetTextFont",
                getString(R.string.defaultWidgetFont)
            )
        )
        editor.apply()
    }

    fun initClockWidget(){
        if(!ServiceRunningSingleton.isRunning) {
            val clockIntent: Intent = Intent(this, ClockService::class.java)
            startService(clockIntent)
            ServiceRunningSingleton.isRunning = true
        }
    }

    fun permissionCheck(){
        if(!Settings.canDrawOverlays(this)){
            val resultProcessing = { result: ActivityResult ->
                if (!Settings.canDrawOverlays(this)) {
                    Toast.makeText(
                        this,
                        "Floating Window Should be allowed for this App",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }

            }

            val launcher: ActivityResultLauncher<Intent> = registerForActivityResult(
                ActivityResultContracts.StartActivityForResult(),resultProcessing)
            val checkIntent: Intent = Intent(
                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                Uri.parse("package:${this.packageName}")
            )
            launcher.launch(checkIntent)

        }

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("menuId",menuId)
        super.onSaveInstanceState(outState)
    }

}