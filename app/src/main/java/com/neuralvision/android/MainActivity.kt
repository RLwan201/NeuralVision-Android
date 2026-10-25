package com.neuralvision.android

import android.app.Activity
import android.opengl.GLES30
import android.opengl.GLSurfaceView
import android.os.Bundle
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MainActivity : Activity() {

    private lateinit var glView: GLSurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        glView = GLSurfaceView(this).apply {
            setEGLContextClientVersion(3)
            setRenderer(NeuralRenderer())
            renderMode = GLSurfaceView.RENDERMODE_CONTINUOUSLY
        }

        val layout = FrameLayout(this)
        layout.addView(glView)

        val label = TextView(this).apply {
            text = "NEURALVISION v0.2\nGPU SHADER DEMO"
            textSize = 16f
            setTextColor(android.graphics.Color.WHITE)
            gravity = Gravity.CENTER
            setPadding(12, 20, 12, 20)
            setBackgroundColor(0x66000000)
        }

        layout.addView(
            label,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP
            )
        )

        setContentView(layout)
    }

    override fun onPause() {
        super.onPause()
        glView.onPause()
    }

    override fun onResume() {
        super.onResume()
        glView.onResume()
    }
}

class NeuralRenderer : GLSurfaceView.Renderer {

    private var program = 0
    private var timeLocation = 0
    private var resolutionLocation = 0
    private var startTime = System.nanoTime()
    private var width = 1
    private var height = 1

    private val vertexShader = """
        #version 300 es
        void main() {
            vec2 p = vec2(
                float((gl_VertexID << 1) & 2),
                float(gl_VertexID & 2)
            );
            gl_Position = vec4(p * 2.0 - 1.0, 0.0, 1.0);
        }
    """.trimIndent()

    private val fragmentShader = """
        #version 300 es
        precision highp float;

        uniform float uTime;
        uniform vec2 uResolution;
        out vec4 fragColor;

        void main() {
            vec2 uv = gl_FragCoord.xy / uResolution;
            vec2 p = uv - vec2(0.5);
            p.x *= uResolution.x / uResolution.y;

            float pulse = 0.04 * sin(uTime * 1.5);
            float d = length(p) - (0.22 + pulse);

            float glow = exp(-max(d, 0.0) * 20.0);
            float edge = 1.0 -
                smoothstep(0.0, 0.012, abs(d));

            vec3 bg = mix(
                vec3(0.015, 0.025, 0.07),
                vec3(0.02, 0.12, 0.16),
                uv.y
            );

            vec3 orb = vec3(0.0, 0.95, 0.72);
            vec3 color = bg + orb * glow * 0.35;
            color = mix(color, orb, edge);

            float ring = abs(length(p) - 0.30);
            color += vec3(0.05, 0.35, 1.0)
                * exp(-ring * 90.0);

            fragColor = vec4(color, 1.0);
        }
    """.trimIndent()

    override fun onSurfaceCreated(
        gl: GL10?,
        config: EGLConfig?
    ) {
        GLES30.glClearColor(0f, 0f, 0f, 1f)

        val vs = compileShader(
            GLES30.GL_VERTEX_SHADER,
            vertexShader
        )

        val fs = compileShader(
            GLES30.GL_FRAGMENT_SHADER,
            fragmentShader
        )

        program = GLES30.glCreateProgram()
        GLES30.glAttachShader(program, vs)
        GLES30.glAttachShader(program, fs)
        GLES30.glLinkProgram(program)

        val result = IntArray(1)
        GLES30.glGetProgramiv(
            program,
            GLES30.GL_LINK_STATUS,
            result,
            0
        )

        if (result[0] == 0) {
            android.util.Log.e(
                "NeuralVision",
                GLES30.glGetProgramInfoLog(program)
            )
        }

        GLES30.glDeleteShader(vs)
        GLES30.glDeleteShader(fs)

        timeLocation = GLES30.glGetUniformLocation(
            program,
            "uTime"
        )

        resolutionLocation = GLES30.glGetUniformLocation(
            program,
            "uResolution"
        )

        startTime = System.nanoTime()
    }

    override fun onSurfaceChanged(
        gl: GL10?,
        w: Int,
        h: Int
    ) {
        width = w.coerceAtLeast(1)
        height = h.coerceAtLeast(1)
        GLES30.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES30.glClear(GLES30.GL_COLOR_BUFFER_BIT)
        GLES30.glUseProgram(program)

        val time = (
            System.nanoTime() - startTime
        ) / 1_000_000_000f

        GLES30.glUniform1f(timeLocation, time)

        GLES30.glUniform2f(
            resolutionLocation,
            width.toFloat(),
            height.toFloat()
        )

        GLES30.glDrawArrays(
            GLES30.GL_TRIANGLES,
            0,
            3
        )
    }

    private fun compileShader(
        type: Int,
        source: String
    ): Int {
        val shader = GLES30.glCreateShader(type)
        GLES30.glShaderSource(shader, source)
        GLES30.glCompileShader(shader)

        val result = IntArray(1)
        GLES30.glGetShaderiv(
            shader,
            GLES30.GL_COMPILE_STATUS,
            result,
            0
        )

        if (result[0] == 0) {
            android.util.Log.e(
                "NeuralVision",
                GLES30.glGetShaderInfoLog(shader)
            )
        }

        return shader
    }
}
