package com.shadowapp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast

class MainActivity : Activity() {

    private val pink = Color.rgb(222, 112, 160)
    private val darkPink = Color.rgb(133, 61, 94)
    private val palePink = Color.rgb(255, 240, 247)
    private val whitePink = Color.rgb(255, 251, 253)

    private lateinit var campaignName: EditText
    private lateinit var message: EditText
    private lateinit var status: TextView

    private val prefs by lazy {
        getSharedPreferences("shadowapp", MODE_PRIVATE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = palePink
        window.navigationBarColor = palePink
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 20, 22, 24)
            setBackgroundColor(palePink)
        }

        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        content.addView(TextView(this).apply {
            text = "♡ ShadowApp"
            textSize = 30f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(darkPink)
        })

        content.addView(TextView(this).apply {
            text = "Tus campañas, a tu estilo ✨"
            textSize = 16f
            setTextColor(darkPink)
            setPadding(0, 4, 0, 22)
        })

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(18, 18, 18, 18)
            background = rounded(whitePink, 24)
        }

        card.addView(TextView(this).apply {
            text = "🌸  Nueva campaña"
            textSize = 21f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(darkPink)
        })

        campaignName = EditText(this).apply {
            hint = "Nombre de la campaña"
            singleLine = true
            textSize = 16f
            setTextColor(darkPink)
            background = rounded(Color.WHITE, 14)
            setPadding(14, 10, 14, 10)
        }
        card.addView(campaignName, fieldParams())

        message = EditText(this).apply {
            hint = "Escribe tu mensaje aquí…"
            minLines = 4
            gravity = Gravity.TOP
            textSize = 16f
            setTextColor(darkPink)
            background = rounded(Color.WHITE, 14)
            setPadding(14, 12, 14, 12)
        }
        card.addView(message, fieldParams())

        card.addView(makeButton("♡  Guardar campaña", pink) {
            saveCampaign()
        }, fieldParams())

        card.addView(makeButton("✿  Cargar campaña guardada", darkPink) {
            loadCampaign()
        }, fieldParams())

        card.addView(makeButton("＋  Adjuntar archivos", pink) {
            chooseFiles()
        }, fieldParams())

        card.addView(makeButton("♡  Compartir con WhatsApp", darkPink) {
            shareMessage()
        }, fieldParams())

        content.addView(card, fieldParams())

        status = TextView(this).apply {
            text = "Hecho con cariño ♡\nPrepara tu mensaje y compártelo cuando quieras."
            textSize = 14f
            setTextColor(darkPink)
            gravity = Gravity.CENTER
            setPadding(8, 22, 8, 12)
        }
        content.addView(status, fieldParams())

        scroll.addView(content)
        page.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        setContentView(page)

        loadCampaign()
    }

    private fun rounded(color: Int, radius: Int): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = radius.toFloat()
        }
    }

    private fun fieldParams(): LinearLayout.LayoutParams {
        return LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            bottomMargin = 12
        }
    }

    private fun makeButton(
        label: String,
        color: Int,
        action: () -> Unit
    ): Button {
        return Button(this).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(Color.WHITE)
            background = rounded(color, 18)
            setPadding(12, 10, 12, 10)
            setOnClickListener { action() }
        }
    }

    private fun saveCampaign() {
        val name = campaignName.text.toString().trim()

        if (name.isEmpty()) {
            campaignName.error = "Escribe un nombre"
            return
        }

        prefs.edit()
            .putString("campaign_name", name)
            .putString("campaign_message", message.text.toString())
            .apply()

        status.text = "♡ Campaña guardada: $name"
        Toast.makeText(this, "¡Guardado! ♡", Toast.LENGTH_SHORT).show()
    }

    private fun loadCampaign() {
        if (!::campaignName.isInitialized || !::message.isInitialized) return

        campaignName.setText(prefs.getString("campaign_name", ""))
        message.setText(prefs.getString("campaign_message", ""))

        status.text = "Tus ideas están a salvo ♡"
    }

    private fun chooseFiles() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }

        try {
            startActivityForResult(intent, 101)
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "No se pudo abrir el selector de archivos",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    @Deprecated("Compatibilidad con esta versión inicial")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            val count = data.clipData?.itemCount ?: if (data.data != null) 1 else 0
            status.text = "♡ Has seleccionado $count archivo(s)"
        }
    }

    private fun shareMessage() {
        val text = message.text.toString().trim()

        if (text.isEmpty()) {
            message.error = "Escribe primero tu mensaje"
            return
        }

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }

        try {
            startActivity(Intent.createChooser(intent, "Compartir con ♡"))
            status.text = "Elige WhatsApp y confirma allí el envío ♡"
        } catch (_: Exception) {
            Toast.makeText(
                this,
                "No hay una aplicación disponible para compartir",
                Toast.LENGTH_LONG
            ).show()
        }
    }
}
