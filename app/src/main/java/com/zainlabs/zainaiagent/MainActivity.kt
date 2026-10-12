package com.zainlabs.zainaiagent

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.view.inputmethod.EditorInfo
import android.content.Context
import android.text.InputType
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.Executors

class MainActivity : Activity() {
    private val purple = Color.rgb(192, 132, 252)
    private val bg = Color.rgb(16, 9, 31)
    private val panel = Color.rgb(35, 23, 57)
    private val white = Color.rgb(246, 241, 255)
    private val worker = Executors.newSingleThreadExecutor()
    private val history = mutableListOf<Pair<String, String>>()

    private lateinit var root: LinearLayout
    private lateinit var transcript: TextView
    private lateinit var prompt: EditText
    private lateinit var apiKey: EditText
    private lateinit var status: TextView
    private var builderMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
    }

    private fun rounded(color: Int, stroke: Int = purple): GradientDrawable {
        return GradientDrawable().apply {
            setColor(color)
            cornerRadius = 28f
            setStroke(1, stroke)
        }
    }

    private fun label(text: String, size: Float = 16f): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(white)
        }
    }

    private fun buildUi() {
        val scroll = ScrollView(this)
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 24, 22, 26)
            background = GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                intArrayOf(bg, Color.rgb(47, 19, 77), bg)
            )
        }
        scroll.addView(root)
        setContentView(scroll)

        root.addView(label("ZAIN AI", 14f).apply {
            setTextColor(purple)
            letterSpacing = 0.18f
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(label("Agent Boy", 32f).apply {
            typeface = Typeface.DEFAULT_BOLD
            setPadding(0, 5, 0, 2)
        })
        root.addView(label("PURPLE GLASS EDITION  •  AI ASSISTANT", 11f).apply {
            setTextColor(purple)
        })

        val avatar = TextView(this).apply {
            text = "Z"
            textSize = 48f
            gravity = Gravity.CENTER
            setTextColor(white)
            typeface = Typeface.DEFAULT_BOLD
            background = rounded(Color.rgb(80, 35, 122))
            layoutParams = LinearLayout.LayoutParams(118, 118).apply {
                gravity = Gravity.CENTER_HORIZONTAL
                topMargin = 22
                bottomMargin = 18
            }
        }
        root.addView(avatar)

        root.addView(label("Your AI companion", 21f).apply {
            gravity = Gravity.CENTER
            typeface = Typeface.DEFAULT_BOLD
        })
        root.addView(label(
            "Ask questions, solve problems, or plan an app project.",
            14f
        ).apply {
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 20)
        })

        apiKey = EditText(this).apply {
            hint = "Paste your Gemini API key"
            setHintTextColor(Color.LTGRAY)
            setTextColor(white)
            textSize = 14f
            inputType = InputType.TYPE_CLASS_TEXT or
                InputType.TYPE_TEXT_VARIATION_PASSWORD
            singleLine = true
            background = rounded(panel)
            setPadding(16, 8, 16, 8)
        }
        root.addView(apiKey, LinearLayout.LayoutParams(-1, 54))
        root.addView(label(
            "Key is kept in this screen only; it is not saved by this app.",
            11f
        ).apply { setPadding(2, 6, 0, 14) })

        val featureRow = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        featureRow.addView(actionButton("✦  AI CHAT") {
            builderMode = false
            status.text = "AI Chat selected"
            prompt.hint = "Ask anything..."
        })
        featureRow.addView(actionButton("⌘  TERMUX APP BUILDER") {
            builderMode = true
            status.text = "Builder mode: describe your app idea"
            prompt.hint = "Describe the app or website you want..."
        })
        featureRow.addView(actionButton("◇  SECURITY CENTER") {
            showMessage(
                "Security Center",
                "This starter version can explain safe security practices. " +
                "It does not scan other apps, take photos secretly, or control " +
                "Android system settings."
            )
        })
        featureRow.addView(actionButton("⚙  SETTINGS") {
            showMessage(
                "Settings",
                "Version 1.0.0\nPurple Glass theme\nGemini API key is not persisted."
            )
        })
        root.addView(featureRow)

        status = label("Ready. Add your Gemini API key to begin.", 12f).apply {
            setTextColor(purple)
            setPadding(0, 14, 0, 10)
        }
        root.addView(status)

        transcript = label(
            "Welcome! I am Zain AI Agent Boy.\n\n" +
            "Choose AI Chat or Termux App Builder to get started.",
            15f
        ).apply {
            background = rounded(panel)
            setPadding(16, 16, 16, 16)
        }
        root.addView(transcript, LinearLayout.LayoutParams(-1, -2).apply {
            bottomMargin = 16
        })

        prompt = EditText(this).apply {
            hint = "Write your message..."
            setHintTextColor(Color.LTGRAY)
            setTextColor(white)
            textSize = 15f
            minLines = 2
            maxLines = 5
            gravity = Gravity.TOP
            imeOptions = EditorInfo.IME_ACTION_SEND
            background = rounded(panel)
            setPadding(16, 14, 16, 14)
        }
        root.addView(prompt, LinearLayout.LayoutParams(-1, -2))

        root.addView(actionButton("SEND MESSAGE  ↑") { sendMessage() })
        root.addView(label(
            "Developer preview: the Builder plans projects with AI. " +
            "Automatic Termux command execution and full phone control " +
            "require a separately secured Android/Termux bridge.",
            11f
        ).apply {
            setTextColor(Color.LTGRAY)
            setPadding(2, 14, 2, 0)
        })
    }

    private fun actionButton(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            textSize = 13f
            setTextColor(white)
            isAllCaps = false
            background = rounded(Color.rgb(63, 35, 91))
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(-1, 54).apply {
                topMargin = 7
                bottomMargin = 7
            }
        }
    }

    private fun showMessage(title: String, message: String) {
        transcript.text = "$title\n\n$message"
    }

    private fun sendMessage() {
        val key = apiKey.text.toString().trim()
        val question = prompt.text.toString().trim()

        if (key.isEmpty()) {
            status.text = "ERROR: Please enter your Gemini API key."
            return
        }
        if (question.isEmpty()) {
            status.text = "ERROR: Please write a message first."
            return
        }

        prompt.text.clear()
        prompt.isEnabled = false
        status.text = "Connecting to Gemini..."
        history.add("user" to question)
        transcript.text = transcript.text.toString() +
            "\n\nYOU:\n$question\n\nZAIN AI:\nThinking..."

        worker.execute {
            try {
                val answer = callGemini(key, question, builderMode)
                history.add("model" to answer)
                runOnUiThread {
                    transcript.text = transcript.text.toString()
                        .substringBeforeLast("Thinking...") + answer
                    status.text = "Response received."
                    prompt.isEnabled = true
                }
            } catch (e: Exception) {
                history.removeLastOrNull()
                runOnUiThread {
                    transcript.text = transcript.text.toString()
                        .substringBeforeLast("Thinking...") +
                        "Request failed: ${e.message ?: "Unknown error"}"
                    status.text = "ERROR: Check internet, API key, quota, and model access."
                    prompt.isEnabled = true
                }
            }
        }
    }

    private fun callGemini(key: String, question: String, builder: Boolean): String {
        val url = URL(
            "https://generativelanguage.googleapis.com/v1beta/" +
            "models/gemini-2.5-flash:generateContent"
        )
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            connectTimeout = 20000
            readTimeout = 60000
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("x-goog-api-key", key)
        }

        val contents = JSONArray()
        val recent = history.takeLast(12)
        for ((role, text) in recent) {
            contents.put(JSONObject()
                .put("role", role)
                .put("parts", JSONArray().put(JSONObject().put("text", text))))
        }

        val systemPrompt = if (builder) {
            "You are Zain AI Agent Boy, a careful Android and web development " +
            "assistant. Convert the user's idea into a practical project plan. " +
            "List files, dependencies, build commands and tests. Never claim you " +
            "executed commands or built an APK. Do not request passwords or secrets. " +
            "Commands that modify systems must require user review."
        } else {
            "You are Zain AI Agent Boy. Be helpful, accurate, and clear. " +
            "Use Urdu or the user's language when appropriate. Explain uncertainty."
        }

        val body = JSONObject()
            .put("systemInstruction", JSONObject().put(
                "parts", JSONArray().put(JSONObject().put("text", systemPrompt))
            ))
            .put("contents", contents)
            .put("generationConfig", JSONObject().put("temperature", 0.7))

        try {
            connection.outputStream.use {
                it.write(body.toString().toByteArray(Charsets.UTF_8))
            }
            val code = connection.responseCode
            val stream = if (code in 200..299) connection.inputStream
                         else connection.errorStream
            val response = BufferedReader(InputStreamReader(stream)).use {
                it.readText()
            }

            if (code !in 200..299) {
                val details = try {
                    JSONObject(response).optJSONObject("error")
                        ?.optString("message") ?: response
                } catch (_: Exception) { response }
                throw Exception("HTTP $code: ${details.take(500)}")
            }

            val json = JSONObject(response)
            return json.getJSONArray("candidates")
                .getJSONObject(0)
                .getJSONObject("content")
                .getJSONArray("parts")
                .getJSONObject(0)
                .getString("text")
        } finally {
            connection.disconnect()
        }
    }

    override fun onDestroy() {
        worker.shutdownNow()
        super.onDestroy()
    }
}
