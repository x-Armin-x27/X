package com.example.oneui8launcher

import android.app.Activity
import android.app.WallpaperManager
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.content.Context
import android.widget.*
import android.text.Editable
import android.text.TextWatcher
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {
    private var dark = false
    private var drawerOpen = false
    private lateinit var root: LinearLayout
    private lateinit var content: LinearLayout
    private lateinit var search: EditText
    private lateinit var appGrid: GridLayout
    private lateinit var apps: List<android.content.pm.ResolveInfo>
    private val accent = Color.rgb(83, 103, 190)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = Color.rgb(245, 246, 250)
        window.navigationBarColor = Color.rgb(245, 246, 250)
        loadApps()
        showHome()
    }

    private fun loadApps() {
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        apps = packageManager.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != packageName }
            .sortedBy { it.loadLabel(packageManager).toString().lowercase(Locale.getDefault()) }
    }

    private fun bg(color: Int, radius: Float): GradientDrawable =
        GradientDrawable().apply { setColor(color); cornerRadius = radius }

    private fun pageColor() = if (dark) Color.rgb(20, 22, 29) else Color.rgb(245, 246, 250)
    private fun cardColor() = if (dark) Color.rgb(37, 40, 50) else Color.WHITE
    private fun textColor() = if (dark) Color.WHITE else Color.rgb(28, 30, 38)
    private fun mutedColor() = if (dark) Color.LTGRAY else Color.rgb(105, 110, 125)

    private fun base(title: String) {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(24), dp(20), dp(18))
            setBackgroundColor(pageColor())
        }
        val header = TextView(this).apply {
            text = title
            textSize = 17f
            setTextColor(mutedColor())
            gravity = Gravity.CENTER_VERTICAL
        }
        root.addView(header, LinearLayout.LayoutParams(-1, dp(36)))
        content = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        root.addView(content, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(root)
    }

    private fun showHome() {
        drawerOpen = false
        base("One UI 8  •  Home")
        val time = TextView(this).apply {
            text = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
            textSize = 52f
            typeface = Typeface.create("sans-serif-light", Typeface.NORMAL)
            setTextColor(textColor())
            setPadding(0, dp(34), 0, 0)
        }
        content.addView(time)
        val date = TextView(this).apply {
            text = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Date())
            textSize = 16f
            setTextColor(mutedColor())
        }
        content.addView(date)
        val weather = TextView(this).apply {
            text = "Weather card  •  Add a weather provider in a future version"
            textSize = 13f
            setTextColor(mutedColor())
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(16), 0, dp(16), 0)
            background = bg(cardColor(), dp(22).toFloat())
        }
        val weatherLp = LinearLayout.LayoutParams(-1, dp(68)).apply { topMargin = dp(28) }
        content.addView(weather, weatherLp)

        val spacer = Space(this)
        content.addView(spacer, LinearLayout.LayoutParams(1, 0, 1f))

        val shortcuts = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
        }
        addShortcut(shortcuts, "Apps", "▦") { showDrawer() }
        addShortcut(shortcuts, "Wallpaper", "◐") { chooseWallpaper() }
        addShortcut(shortcuts, "Theme", "◑") { dark = !dark; showHome() }
        addShortcut(shortcuts, "Settings", "⚙") {
            startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
        }
        content.addView(shortcuts, LinearLayout.LayoutParams(-1, dp(90)))
        val hint = TextView(this).apply {
            text = "Swipe-style app list • Tap Apps to open"
            textSize = 12f
            gravity = Gravity.CENTER
            setTextColor(mutedColor())
        }
        content.addView(hint, LinearLayout.LayoutParams(-1, dp(30)))
    }

    private fun addShortcut(parent: LinearLayout, label: String, symbol: String, action: () -> Unit) {
        val item = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            isClickable = true
            isFocusable = true
            setOnClickListener { action() }
        }
        val bubble = TextView(this).apply {
            text = symbol
            textSize = 25f
            gravity = Gravity.CENTER
            setTextColor(if (dark) Color.WHITE else accent)
            background = bg(cardColor(), dp(22).toFloat())
        }
        item.addView(bubble, LinearLayout.LayoutParams(dp(54), dp(54)))
        val name = TextView(this).apply {
            text = label
            textSize = 11f
            setTextColor(textColor())
            gravity = Gravity.CENTER
        }
        item.addView(name)
        parent.addView(item, LinearLayout.LayoutParams(0, -1, 1f))
    }

    private fun showDrawer() {
        drawerOpen = true
        base("Apps")
        search = EditText(this).apply {
            hint = "Search apps"
            textSize = 16f
            singleLine = true
            setTextColor(textColor())
            setHintTextColor(mutedColor())
            setPadding(dp(16), 0, dp(16), 0)
            background = bg(cardColor(), dp(20).toFloat())
        }
        content.addView(search, LinearLayout.LayoutParams(-1, dp(54)))
        val scroll = ScrollView(this)
        appGrid = GridLayout(this).apply { columnCount = 4; useDefaultMargins = true }
        scroll.addView(appGrid)
        val lp = LinearLayout.LayoutParams(-1, 0, 1f).apply { topMargin = dp(18) }
        content.addView(scroll, lp)
        renderApps("")
        search.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) { renderApps(s?.toString() ?: "") }
            override fun afterTextChanged(s: Editable?) {}
        })
        search.setOnEditorActionListener { _, _, _ ->
            (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager)
                .hideSoftInputFromWindow(search.windowToken, 0)
            true
        }
    }

    private fun renderApps(query: String) {
        appGrid.removeAllViews()
        val filtered = apps.filter {
            it.loadLabel(packageManager).toString().contains(query, ignoreCase = true)
        }
        filtered.forEach { info ->
            val cell = LinearLayout(this).apply {
                orientation = LinearLayout.VERTICAL
                gravity = Gravity.CENTER
                setPadding(dp(3), dp(10), dp(3), dp(10))
                isClickable = true
                setOnClickListener {
                    val launch = packageManager.getLaunchIntentForPackage(info.activityInfo.packageName)
                    if (launch != null) startActivity(launch)
                }
            }
            val icon = ImageView(this).apply {
                setImageDrawable(info.loadIcon(packageManager))
                scaleType = ImageView.ScaleType.FIT_CENTER
            }
            cell.addView(icon, LinearLayout.LayoutParams(dp(48), dp(48)))
            val label = TextView(this).apply {
                text = info.loadLabel(packageManager)
                textSize = 11f
                gravity = Gravity.CENTER
                maxLines = 2
                setTextColor(textColor())
            }
            cell.addView(label, LinearLayout.LayoutParams(-1, dp(38)))
            val params = GridLayout.LayoutParams().apply {
                width = 0
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, 1f)
                setMargins(dp(2), dp(2), dp(2), dp(2))
            }
            appGrid.addView(cell, params)
        }
    }

    private fun chooseWallpaper() {
        try {
            startActivity(Intent(Intent.ACTION_SET_WALLPAPER))
        } catch (_: Exception) {
            Toast.makeText(this, "Open Wallpaper settings from your phone", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onBackPressed() {
        if (drawerOpen) showHome() else super.onBackPressed()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
