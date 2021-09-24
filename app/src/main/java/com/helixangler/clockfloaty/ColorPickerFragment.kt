package com.helixangler.clockfloaty

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.graphics.createBitmap
import androidx.fragment.app.DialogFragment
import com.skydoves.colorpickerview.ColorEnvelope
import com.skydoves.colorpickerview.ColorPickerView
import com.skydoves.colorpickerview.listeners.ColorEnvelopeListener
import com.skydoves.colorpickerview.sliders.AlphaSlideBar
import com.skydoves.colorpickerview.sliders.BrightnessSlideBar


class ColorPickerFragment : DialogFragment() {

    private var pickedColor:String? = "#FFFFFFFF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        var infl = inflater.inflate(R.layout.fragment_color_picker, container, false)



        return infl
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        generateBackgroundColorDisplayContainer()
        generateColorPickerConfig(savedInstanceState)
        generateButtonsListeners()
    }

    fun generateColorPickerConfig(savedInstanceState: Bundle?){
        var dataPreference = this.requireActivity().applicationContext.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        //Toast.makeText(requireContext(),arguments?.getString("targetColorPickKey",""),Toast.LENGTH_SHORT).show()
        if(savedInstanceState != null) pickedColor = savedInstanceState!!.getString("pickedColor",dataPreference.getString(requireArguments().getString("targetColorPickKey","default"),"#FFFFFFFF"))
        else pickedColor = dataPreference.getString(requireArguments().getString("targetColorPickKey","default"),"#FFFFFFFF")
        var colorWheel = requireView().findViewById(R.id.colorWheel) as ColorPickerView
        var alpBar = requireView().findViewById(R.id.alphaBar) as AlphaSlideBar
        var briBar = requireView().findViewById(R.id.brightBar) as BrightnessSlideBar
        colorWheel.attachAlphaSlider(alpBar)
        colorWheel.attachBrightnessSlider(briBar)
        colorWheel.setInitialColor(Color.parseColor(pickedColor))
        colorWheel.setColorListener(ColorEnvelopeListener{envelope: ColorEnvelope?, fromUser: Boolean ->
            var colorDisplayContainer = requireView().findViewById(R.id.colorDisplayContainer) as View
            var colorDisplay = colorDisplayContainer.findViewById(R.id.colorDisplay) as View
            var colorDisplayName = requireView().findViewById(R.id.colorDisplayName) as TextView
            colorDisplay.setBackgroundColor(envelope!!.color)
            pickedColor = "#${envelope.hexCode}"
            colorDisplayName.text = pickedColor

        })
    }

    fun generateBackgroundColorDisplayContainer(){
        var densityPixelSize:Int = resources.displayMetrics.density.toInt()
        var chessboardSquareSize:Int = 5
        var checkerboardPaint: Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = BitmapShader(
                Bitmap.createBitmap(
                    densityPixelSize*chessboardSquareSize*2,
                    densityPixelSize*chessboardSquareSize*2,
                    Bitmap.Config.ARGB_8888).apply {
                    Canvas(this).apply {
                        var fill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                            style = Paint.Style.FILL
                            color = 0x22000000
                        }
                        drawRect(0f, 0f, (densityPixelSize*chessboardSquareSize).toFloat(),(densityPixelSize*chessboardSquareSize).toFloat(), fill)
                        drawRect((densityPixelSize*chessboardSquareSize).toFloat(),(densityPixelSize*chessboardSquareSize).toFloat(), (densityPixelSize*chessboardSquareSize*2).toFloat(),(densityPixelSize*chessboardSquareSize*2).toFloat(),fill)
                    }
                }
                ,Shader.TileMode.REPEAT,Shader.TileMode.REPEAT)
        }

        var colorDisplayContainer = requireView().findViewById(R.id.colorDisplayContainer) as View
        var checkerboardBackground:Bitmap = createBitmap(colorDisplayContainer.layoutParams.width, colorDisplayContainer.layoutParams.height, Bitmap.Config.ARGB_8888)
        var canvas: Canvas = Canvas(checkerboardBackground)
        canvas.drawRect(
            0.0F, 0.0F, colorDisplayContainer.layoutParams.width.toFloat(), colorDisplayContainer.layoutParams.height.toFloat(), checkerboardPaint
        )
        colorDisplayContainer.background = BitmapDrawable(resources,checkerboardBackground)
    }

    fun generateButtonsListeners(){
        var confirmButton:Button = requireView().findViewById(R.id.confirmColorPick) as Button
        var cancelButton:Button = requireView().findViewById(R.id.cancelColorPick) as Button

        confirmButton.setOnClickListener {
            confirmColorPick()
        }
        cancelButton.setOnClickListener {
            cancelColorPick()
        }
    }

    fun confirmColorPick(){
        var dataPreference = this.requireActivity().applicationContext.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)

        dataPreference.edit().putString(arguments?.getString("targetColorPickKey","default"),pickedColor).apply()
        dismiss()
    }

    fun cancelColorPick(){
        dismiss()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("pickedColor",pickedColor)
    }

}