package com.helixangler.clockfloaty

import android.content.Context
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.DialogFragment
import java.io.File
import kotlin.collections.ArrayList


class FontPickerFragment : DialogFragment() {
    private var pickedFont:String? = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_font_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generateFontConfig(savedInstanceState)
        generateButtonsListeners()
    }

    fun generateFontConfig(savedInstanceState: Bundle?){
        var dataPreference = this.requireActivity().applicationContext.getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )

        if(savedInstanceState != null) pickedFont = savedInstanceState!!.getString(
            "pickedFont",
            dataPreference.getString(
                "widgetTextFont",
                getString(R.string.defaultWidgetFont)
            )
        )
        else pickedFont = dataPreference.getString(
            "widgetTextFont",
            getString(R.string.defaultWidgetFont)
        )

        showPreviewFont()

        var fontListing = requireView().findViewById(R.id.fontOptionsContainer) as ListView
        val path = "/system/fonts"
        val file = File(path)
        val fileList: ArrayList<String> = ArrayList<String>()

        for(item in file.listFiles().asList()){
            fileList.add(item.name)
        }

        var adapter:ArrayAdapter<String> = ArrayAdapter(
            requireContext(),
            R.layout.support_simple_spinner_dropdown_item,
            fileList
        )

        fontListing.adapter = adapter
        fontListing.setOnItemClickListener { parent, view, position, id ->
            pickedFont = fontListing.getItemAtPosition(position) as String
            showPreviewFont()
        }

    }

    fun generateButtonsListeners(){
        var confirmButton: Button = requireView().findViewById(R.id.confirmColorPick) as Button
        var cancelButton: Button = requireView().findViewById(R.id.cancelColorPick) as Button

        confirmButton.setOnClickListener {
            confirmFontPick()
        }
        cancelButton.setOnClickListener {
            cancelFontPick()
        }
    }

    fun showPreviewFont(){
        requireView().findViewById<TextView>(R.id.previewFont).typeface = Typeface.createFromFile(
            "/system/fonts/${pickedFont}"
        )
        requireView().findViewById<TextView>(R.id.previewFontNameDisplay).text = pickedFont
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("pickedFont",pickedFont)
    }

    fun confirmFontPick(){
        var dataPreference = this.requireActivity().applicationContext.getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )

        dataPreference.edit().putString("widgetTextFont",pickedFont).apply()
        dismiss()
    }

    fun cancelFontPick(){
        dismiss()
    }

}