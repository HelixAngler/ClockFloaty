package com.helixangler.clockfloaty

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.*
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.DisplayMetrics
import android.view.*
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.*
import android.graphics.RectF
import android.graphics.drawable.BitmapDrawable
import android.graphics.Typeface
import android.util.TypedValue
import androidx.core.app.NotificationCompat


class ClockService : Service() {

    private var FLAG_LY: Int= 0
    private val COMMAND="com.helixangler.clockyfloaty.CLOCKYFLOATYFLOATCOMMAND"
    private val EXIT="CLOCKYFLOATYONEXIT"
    private val NOTIFICATION_CHANNEL_GENERAL = "clockyfloaty_general"
    private val CODE_FOREGROUND_SERVICE = 1
    private val CODE_EXIT = 2

    private lateinit var floatingWidget:View
    private lateinit var windowMan:WindowManager
    private lateinit var dataPreference:SharedPreferences
    private lateinit var dateAndTimeHandler:Handler
    private lateinit var timeRunnable:Runnable
    private lateinit var changesListener:SharedPreferences.OnSharedPreferenceChangeListener
    private lateinit var windowManagerLayoutParams:WindowManager.LayoutParams

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {

        val command = intent?.getStringExtra(COMMAND) ?: ""

        if(command == EXIT){
            dataPreference.edit().putBoolean("activateWidget",false).apply()
            return START_NOT_STICKY
        }

        notificate()

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            FLAG_LY = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            FLAG_LY = WindowManager.LayoutParams.TYPE_PHONE
        }

        windowMan = getSystemService(WINDOW_SERVICE) as WindowManager
        dataPreference = this.applicationContext.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)
        floatingWidget = LayoutInflater.from(this).inflate(R.layout.clock_floating_widget,null)
        floatingWidget.visibility = View.VISIBLE

        var theWidget:View = floatingWidget.findViewById( R.id.float_widget) as View
        configureAppearance(theWidget,dataPreference)

        var lyParams:WindowManager.LayoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            FLAG_LY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
            PixelFormat.TRANSLUCENT)

        lyParams.gravity = Gravity.TOP or Gravity.LEFT

        val size:Array<Int> = getSize()
        val screenWidth:Float = size[0].toFloat()
        val screenHeight:Float = size[1].toFloat()
        lyParams.x = ((screenWidth - theWidget.layoutParams.width)/2.0F).toInt()
        lyParams.y = ((screenHeight - theWidget.layoutParams.height)/2.0F).toInt()

        windowMan.addView(floatingWidget,lyParams)
        windowManagerLayoutParams = lyParams

        getTimeRunnable(floatingWidget,dataPreference)
        widgetMovement(theWidget, windowManagerLayoutParams)

        changesListener = SharedPreferences.OnSharedPreferenceChangeListener{sharedPreferences:SharedPreferences, key:String ->
            onPreferencesChanged(floatingWidget,lyParams,sharedPreferences,key)
        }

        dataPreference.registerOnSharedPreferenceChangeListener(changesListener)

        return START_STICKY

    }

    private fun widgetMovement(theWidget:View, lyParams:WindowManager.LayoutParams){

        theWidget.setOnTouchListener (object: View.OnTouchListener{
            var onMove:Boolean = false
            var offsetX:Float = 0.0F
            var offsetY:Float = 0.0F
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                when(event?.action){
                    MotionEvent.ACTION_UP -> {
                        if(onMove){
                            offsetX = 0.0F
                            offsetY = 0.0F
                            onMove = false
                        }
                    }
                    MotionEvent.ACTION_DOWN -> {
                        if(!onMove){
                            offsetX = event.rawX - lyParams.x
                            offsetY = event.rawY - lyParams.y
                            onMove = true
                        }
                        lyParams.x =(event.rawX - offsetX).toInt()
                        lyParams.y = (event.rawY  - offsetY).toInt()
                        return true

                    }

                    MotionEvent.ACTION_MOVE -> {

                        lyParams.x = (event.rawX - offsetX).toInt()
                        lyParams.y = (event.rawY - offsetY).toInt()
                        windowMan.updateViewLayout(floatingWidget,lyParams)
                        return true

                    }

                }

                return false
            }
        })
    }

    private fun getSize():Array<Int>{

        val winManager = getSystemService(WINDOW_SERVICE) as WindowManager
        var width:Int = 0
        var height:Int = 0

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){

            val winMetrics = winManager.currentWindowMetrics
            val winInsets = winMetrics.windowInsets.getInsetsIgnoringVisibility(WindowInsets.Type.systemBars())
            width = winMetrics.bounds.width() - (winInsets.left + winInsets.right)
            height = winMetrics.bounds.height() - (winInsets.top + winInsets.bottom)

        } else {

            var dpMetrics = DisplayMetrics()
            winManager.defaultDisplay.getMetrics(dpMetrics)
            width = dpMetrics.widthPixels
            height = dpMetrics.heightPixels

        }

        return arrayOf(width,height)

    }

    private fun onPreferencesChanged(

        baseWidget:View,layoutParameters: WindowManager.LayoutParams,
        sharedPreferences:SharedPreferences,
        key:String
    ){
        var isTimeFormatChanged:Boolean = false
        var isAppearanceChanged:Boolean = false
        var isActivateWidgetChanged:Boolean = false

        var theWidget = baseWidget.findViewById(R.id.float_widget) as View

        when(key){

            "show24Hour","showSecond","showMilliSecond","showUSFormat" -> {
                isTimeFormatChanged = true
            }
            "activateWidget"->{
                isActivateWidgetChanged = true
            }
            "widgetWidth","widgetHeight","widgetRoundX","widgetRoundY","widgetBGColor","widgetTextColor","widgetTextFont","widgetTimeTextSize","widgetDateTextSize" -> {
                isAppearanceChanged = true
            }

        }

        if(isTimeFormatChanged){


            println("executed")
            getTimeRunnable(floatingWidget,sharedPreferences)

        } else if(isAppearanceChanged){
            configureAppearance(theWidget, sharedPreferences)
        } else if(isActivateWidgetChanged){
            if(!sharedPreferences.getBoolean("activateWidget",false)){

                stopService()

            }
        }
    }

    private fun getTimeRunnable(
        baseWidget:View,
        sharedPreferences: SharedPreferences
    ):Handler{
        var show24Hour:Boolean = sharedPreferences.getBoolean("show24Hour",false)
        var showSecond:Boolean = sharedPreferences.getBoolean("showSecond",false)
        var showMilliSecond:Boolean = sharedPreferences.getBoolean("showMilliSecond",false)
        var showUSFormat:Boolean = sharedPreferences.getBoolean("showUSFormat",false)

        var theWidget: View = baseWidget.findViewById(R.id.float_widget) as View

        if(!this::dateAndTimeHandler.isInitialized) {
            dateAndTimeHandler = Handler(Looper.getMainLooper())
        }

        var milliSecondTolerance:Long = 1000L
        var timePattern:String = ""
        var datePattern:String = "dd MMM yyyy"

        if(show24Hour) timePattern += "HH"
        else timePattern += "hh"
        timePattern += ":mm"
        if(showSecond) timePattern += ":ss"

        if(showSecond && showMilliSecond) {

            timePattern += ".SS"
            milliSecondTolerance = 10L

        }

        if(!show24Hour) timePattern += " aa"

        var timeFunction:(Date) -> String = {date:Date ->
            SimpleDateFormat(timePattern).format(date)
        }

        if(showUSFormat){
            datePattern = "MMM dd, yyyy"
        }

        var dateFunction:(Date) -> String = {date:Date ->
            SimpleDateFormat(datePattern).format(date)
        }

        if(this::timeRunnable.isInitialized){
            dateAndTimeHandler.removeCallbacks(timeRunnable)
        }

        timeRunnable = object:Runnable{

            var timeFunc = timeFunction
            var dateFunc = dateFunction
            var timeTolerance = milliSecondTolerance

            override fun run(){

                var currentTime:Date = Date()
                theWidget.findViewById<TextView>(R.id.clock)?.text = timeFunc(currentTime)
                theWidget.findViewById<TextView>(R.id.date_time)?.text = dateFunc(currentTime)
                dateAndTimeHandler.postDelayed(this,timeTolerance)

            }

        }

        dateAndTimeHandler.postDelayed(timeRunnable,0)

        return dateAndTimeHandler

    }

    override fun onDestroy() {
        super.onDestroy()
        dataPreference.unregisterOnSharedPreferenceChangeListener(changesListener)
        dateAndTimeHandler.removeCallbacks(timeRunnable)
        windowMan.removeView(floatingWidget)
        ServiceRunningSingleton.isRunning = false
    }


    private fun configureAppearance(theWidget:View, sharedPreferences: SharedPreferences){

        theWidget.layoutParams.width = (sharedPreferences.getInt("widgetWidth",resources.getInteger(R.integer.defaultWidgetWidth))*resources.displayMetrics.density).toInt()
        theWidget.layoutParams.height = (sharedPreferences.getInt("widgetHeight",resources.getInteger(R.integer.defaultWidgetHeight))*resources.displayMetrics.density).toInt()
        var widgetBackground:Bitmap = Bitmap.createBitmap(theWidget.layoutParams.width,theWidget.layoutParams.height, Bitmap.Config.ARGB_8888)
        var canvas:Canvas = Canvas(widgetBackground)
        val paint = Paint()
        paint.isAntiAlias = true
        paint.color = Color.parseColor(
            sharedPreferences.getString("widgetBGColor",getString(R.string.defaultWidgetBackgroundColor))
        )
        val rect = Rect(0, 0, theWidget.layoutParams.width, theWidget.layoutParams.height)
        val rectF = RectF(rect)
        canvas.drawRoundRect(
            rectF,
            sharedPreferences.getFloat("widgetRoundX",resources.getInteger(R.integer.defaultWidgetXCornerRadius).toFloat()),
            sharedPreferences.getFloat("widgetRoundY",resources.getInteger(R.integer.defaultWidgetYCornerRadius).toFloat()),
            paint
        )



        theWidget.background = BitmapDrawable(resources,widgetBackground)
        var floatingWidgetTimeDisplay:TextView = theWidget.findViewById(R.id.clock) as TextView
        var floatingWidgetDateDisplay:TextView = theWidget.findViewById(R.id.date_time) as TextView
        floatingWidgetTimeDisplay.setTextColor(Color.parseColor(
            sharedPreferences.getString("widgetTextColor",getString(R.string.defaultWidgetTextColor))
        ))
        floatingWidgetDateDisplay.findViewById<TextView>(R.id.date_time)?.setTextColor(Color.parseColor(
            sharedPreferences.getString("widgetTextColor",getString(R.string.defaultWidgetTextColor))
        ))



        val face = Typeface.createFromFile(
            "/system/fonts/" + sharedPreferences.getString("widgetTextFont",getString(R.string.defaultWidgetFont))
        )

        floatingWidgetTimeDisplay.typeface = face
        floatingWidgetDateDisplay.typeface = face
        floatingWidgetTimeDisplay.setTextSize(TypedValue.COMPLEX_UNIT_SP,sharedPreferences.getInt("widgetTimeTextSize",resources.getInteger(R.integer.defaultWidgetTimeTextSize)).toFloat())
        floatingWidgetDateDisplay.setTextSize(TypedValue.COMPLEX_UNIT_SP,sharedPreferences.getInt("widgetDateTextSize",resources.getInteger(R.integer.defaultWidgetDateTextSize)).toFloat())
    }

    private fun stopService() {
        stopForeground(true)
        stopSelf()
    }

    private fun notificate(){
        val man = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val intentExit = Intent(this,ClockService::class.java).apply{
            putExtra(COMMAND,EXIT)
        }
        val pendingExit = PendingIntent.getService(
            this,CODE_EXIT, intentExit, 0
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            try {
                with(
                    NotificationChannel(
                        NOTIFICATION_CHANNEL_GENERAL,
                        getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT
                    )
                ) {
                    enableLights(false)
                    setShowBadge(false)
                    enableVibration(false)
                    setSound(null, null)
                    lockscreenVisibility = Notification.VISIBILITY_PUBLIC
                    man.createNotificationChannel(this)
                }
            } catch (ignored: Exception) {
                // Ignore exception.
            }
        }

        with(
            NotificationCompat.Builder(
                this,
                NOTIFICATION_CHANNEL_GENERAL
            )
        ) {
            setTicker(null)
            setContentTitle(getString(R.string.app_name))
            setContentText(getString(R.string.clock_floaty_running_message))
            setAutoCancel(false)
            setOngoing(true)
            setWhen(System.currentTimeMillis())
            setSmallIcon(R.drawable.ic_launcher_foreground)
            priority = Notification.DEFAULT_ALL
            addAction(
                NotificationCompat.Action(
                    0,
                    "Close App",
                    pendingExit
                )
            )
            startForeground(CODE_FOREGROUND_SERVICE, build())
        }

    }

}