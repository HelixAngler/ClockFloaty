package com.helixangler.clockfloaty

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.View
import android.widget.CompoundButton
import android.widget.Switch
import android.widget.TextView

class HomeUI : Fragment(R.layout.fragment_home) {

    lateinit var changesListener: SharedPreferences.OnSharedPreferenceChangeListener
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    fun initialize() {
        val sectionTitlePart = requireView().findViewById(R.id.homeMenuTitle) as View
        val sectionTitle = sectionTitlePart.findViewById(R.id.SectionTitle) as TextView
        sectionTitle.text = getString(R.string.app_home_title)

        var dataPreference = this.requireActivity().applicationContext.getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )
        var togglerActivateWidget: Switch = requireView()
            .findViewById(R.id.switchActivation) as Switch
        togglerActivateWidget.isChecked = dataPreference.getBoolean(
            "activateWidget",
            false
        )

        togglerActivateWidget
            .setOnCheckedChangeListener { button: CompoundButton, isChecked: Boolean ->
                dataPreference
                    .edit()
                    .putBoolean(
                        "activateWidget",
                        isChecked
                    )
                    .apply()
            }
        changesListener = SharedPreferences
            .OnSharedPreferenceChangeListener { sharedPreferences:SharedPreferences, key:String ->
                if(key == "activateWidget" ){
                    togglerActivateWidget.isChecked = sharedPreferences.getBoolean(
                        "activateWidget",
                        false
                    )
                }
            }

        dataPreference.registerOnSharedPreferenceChangeListener(changesListener)


    }


}