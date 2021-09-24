package com.helixangler.clockfloaty

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView
import androidx.core.view.children

class SettingsUI : Fragment(R.layout.fragment_settings) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    fun initialize() {
        val sectionTitlePart = requireView().findViewById(R.id.settingsMenuTitle) as View
        val sectionTitle = sectionTitlePart.findViewById(R.id.SectionTitle) as TextView
        sectionTitle.text = getString(R.string.app_settings_title)
        val settingsOptionsContainer = requireView().findViewById(R.id.settingsOptionsContainer) as ViewGroup
        for (child in settingsOptionsContainer.children){
            child.layoutParams.height = (48*requireContext().resources.displayMetrics.density).toInt()

        }


        initializeConfig()
    }

    fun initializeConfig(){
        var dataPreference = this.requireActivity().applicationContext.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        var show24Hour:Boolean = dataPreference.getBoolean("show24Hour",false)
        var showSecond:Boolean = dataPreference.getBoolean("showSecond",false)
        var showMilliSecond:Boolean = dataPreference.getBoolean("showMilliSecond",false)
        var showUSFormat:Boolean = dataPreference.getBoolean("showUSFormat",false)

        var toggler24Hour:Switch = requireView().findViewById(R.id.switch24Hour)
        var togglerSecond:Switch = requireView().findViewById(R.id.switchSecond)
        var togglerMilliSecond:Switch = requireView().findViewById(R.id.switchMilliSecond)
        var togglerUSFormat:Switch = requireView().findViewById(R.id.switchUSFormat)

        toggler24Hour.isChecked = show24Hour
        togglerSecond.isChecked = showSecond
        togglerMilliSecond.isChecked = showMilliSecond
        togglerUSFormat.isChecked = showUSFormat

        if (!showSecond){
            togglerMilliSecond.alpha = 0.5f
        }

        toggler24Hour.setOnCheckedChangeListener{button: CompoundButton, isChecked: Boolean ->
            dataPreference.edit().putBoolean("show24Hour",isChecked).apply()
        }

        togglerSecond.setOnCheckedChangeListener{button: CompoundButton, isChecked: Boolean ->
            if (!isChecked){
                togglerMilliSecond.isChecked = false
                togglerMilliSecond.alpha = 0.5f
            } else {
                togglerMilliSecond.alpha = 1.0f
            }
            togglerMilliSecond.isClickable = isChecked
            dataPreference.edit().putBoolean("showSecond",isChecked).apply()
        }

        togglerMilliSecond.setOnCheckedChangeListener{button: CompoundButton, isChecked: Boolean ->
            if (isChecked) togglerSecond.isChecked = isChecked
            dataPreference.edit().putBoolean("showMilliSecond",isChecked).apply()

        }

        togglerUSFormat.setOnCheckedChangeListener{button: CompoundButton, isChecked: Boolean ->
            dataPreference.edit().putBoolean("showUSFormat",isChecked).apply()
        }

    }


}