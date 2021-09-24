package com.helixangler.clockfloaty

import android.content.Context
import android.os.Bundle
import android.view.Gravity
import androidx.fragment.app.Fragment
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import com.google.android.material.slider.Slider

class AppearanceUI : Fragment(R.layout.fragment_appearance) {


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initialize()
    }

    fun initialize() {
        val sectionTitlePart = requireView().findViewById(R.id.appearanceMenuTitle) as View
        val sectionTitle = sectionTitlePart.findViewById(R.id.SectionTitle) as TextView
        sectionTitle.text = getString(R.string.app_appearance_title)
        val settingsOptionsContainer = requireView().findViewById(R.id.appearanceOptionsContainer) as ViewGroup
        for (child in settingsOptionsContainer.children){
            var theChild:LinearLayout = child as LinearLayout
            var childCount:Int = theChild.childCount
            if(childCount >= 1){
                theChild.layoutParams.height = (childCount*48*requireContext().resources.displayMetrics.density).toInt()
                theChild.gravity = Gravity.CENTER_VERTICAL
            }
            else child.layoutParams.height = (requireContext().resources.displayMetrics.density).toInt()
        }


        initializeConfig()
    }

    fun initializeConfig(){
        var dataPreference = this.requireActivity().applicationContext.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        var pickBackgroundColorButton = requireView().findViewById(R.id.optionChangeBackgroundColor) as View
        pickBackgroundColorButton.setOnClickListener {
            callBackgroundColorPick()
        }

        var pickTextColorButton = requireView().findViewById(R.id.optionChangeTextColor) as View
        pickTextColorButton.setOnClickListener {
            callTextColorPick()
        }

        var pickTextFont = requireView().findViewById(R.id.optionChangeTextFont) as View
        pickTextFont.setOnClickListener {
            callTextFontPick()
        }


        var sliderTimeTextSize = requireView().findViewById(R.id.sliderWidgetTimeTextSize) as Slider
        sliderTimeTextSize.value = dataPreference.getInt("widgetTimeTextSize",resources.getInteger(R.integer.defaultWidgetTimeTextSize)).toFloat()
        sliderTimeTextSize.addOnChangeListener { slider, value, fromUser ->
            dataPreference.edit().putInt("widgetTimeTextSize",value.toInt()).apply()
        }
        var sliderDateTextSize = requireView().findViewById(R.id.sliderWidgetDateTextSize) as Slider
        sliderDateTextSize.value = dataPreference.getInt("widgetDateTextSize",resources.getInteger(R.integer.defaultWidgetDateTextSize)).toFloat()
        sliderDateTextSize.addOnChangeListener { slider, value, fromUser ->
            dataPreference.edit().putInt("widgetDateTextSize",value.toInt()).apply()
        }
        var sliderWidth = requireView().findViewById(R.id.sliderWidgetWidth) as Slider
        sliderWidth.value = dataPreference.getInt("widgetWidth",resources.getInteger(R.integer.defaultWidgetWidth)).toFloat()
        sliderWidth.addOnChangeListener { slider, value, fromUser ->
            dataPreference.edit().putInt("widgetWidth",value.toInt()).apply()
        }
        var sliderHeight = requireView().findViewById(R.id.sliderWidgetHeight) as Slider
        sliderHeight.value = dataPreference.getInt("widgetHeight",resources.getInteger(R.integer.defaultWidgetHeight)).toFloat()
        sliderHeight.addOnChangeListener { slider, value, fromUser ->
            dataPreference.edit().putInt("widgetHeight",value.toInt()).apply()
        }
        var sliderXCorner = requireView().findViewById(R.id.sliderWidgetXCornerRadius) as Slider
        sliderXCorner.value = dataPreference.getFloat("widgetRoundX",resources.getInteger(R.integer.defaultWidgetXCornerRadius).toFloat())
        sliderXCorner.addOnChangeListener { slider, value, fromUser ->
            dataPreference.edit().putFloat("widgetRoundX",value).apply()
        }
        var sliderYCorner = requireView().findViewById(R.id.sliderWidgetYCornerRadius) as Slider
        sliderYCorner.value = dataPreference.getFloat("widgetRoundY",resources.getInteger(R.integer.defaultWidgetYCornerRadius).toFloat())
        sliderYCorner.addOnChangeListener { slider, value, fromUser ->
            dataPreference.edit().putFloat("widgetRoundY",value).apply()
        }

    }

    fun callBackgroundColorPick(){
        var colorPickerDialogue = ColorPickerFragment()
        var args:Bundle = Bundle().apply {
            putString("targetColorPickKey","widgetBGColor")
        }
        colorPickerDialogue.arguments = args
        colorPickerDialogue.show(requireActivity().supportFragmentManager, "colorPicker")
    }

    fun callTextColorPick(){
        var colorPickerDialogue = ColorPickerFragment()
        var args:Bundle = Bundle().apply {
            putString("targetColorPickKey","widgetTextColor")
        }
        colorPickerDialogue.arguments = args
        colorPickerDialogue.show(requireActivity().supportFragmentManager, "colorPicker")
    }

    fun callTextFontPick(){
        var fontPickerDialogue = FontPickerFragment()
        fontPickerDialogue.show(requireActivity().supportFragmentManager, "fontPicker")
    }


}