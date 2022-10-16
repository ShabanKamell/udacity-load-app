package com.example.loadapp

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import androidx.core.content.ContextCompat
import kotlin.properties.Delegates

sealed class LoadingButtonState {
    object Loading : LoadingButtonState()
    object Completed : LoadingButtonState()
    object Idle : LoadingButtonState()
}

class LoadingButton @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    private var widthSize = 0
    private var heightSize = 0
    private var buttonText: String
    private var buttonBackgroundColor = R.attr.buttonBackgroundColor
    private var progress: Float = 0f
    private var valueAnimator = ValueAnimator()
    private val textRect = Rect()

    private var buttonState: LoadingButtonState by Delegates.observable(
        LoadingButtonState.Idle
    ) { _, _, newValue ->
        when (newValue) {
            LoadingButtonState.Loading -> setupLoadingState()
            LoadingButtonState.Completed -> setupCompletedState()
            LoadingButtonState.Idle -> {
                setupIdleState()
            }
        }
        invalidate()
    }

    private fun setupLoadingState() {
        setText(context.getString(R.string.we_are_loading))
        setBgColor(R.color.blue)
        valueAnimator = ValueAnimator.ofFloat(0f, 1f).apply {
            addUpdateListener {
                progress = animatedValue as Float
                invalidate()
            }
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            duration = 3000
            start()
        }
        disableButton()
    }

    private fun setupIdleState() {
        setText(context.getString(R.string.download))
    }

    private fun setupCompletedState() {
        setText(context.getString(R.string.downloaded))
        setBgColor(R.color.colorPrimary)
        valueAnimator.cancel()
        resetProgress()
        enableButton()
    }

    init {
        context.theme.obtainStyledAttributes(
            attrs,
            R.styleable.LoadingButton,
            0, 0
        ).apply {

            try {
                buttonText = getString(R.styleable.LoadingButton_text).toString()
                buttonBackgroundColor = ContextCompat.getColor(context, R.color.colorPrimary)
            } finally {
                recycle()
            }
        }
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        textAlign = Paint.Align.CENTER
        textSize = 50.0f
        color = Color.WHITE
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorPrimary)
    }

    private val inProgressBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = ContextCompat.getColor(context, R.color.colorPrimaryDark)
    }

    private val inProgressArcPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.YELLOW
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val cornerRadius = 10.0f
        val backgroundWidth = measuredWidth.toFloat()
        val backgroundHeight = measuredHeight.toFloat()

        drawBackground(
            canvas,
            backgroundWidth,
            backgroundHeight,
            cornerRadius
        )
        when (buttonState) {
            LoadingButtonState.Loading -> {
                drawLoadingState(
                    canvas,
                    backgroundHeight,
                    cornerRadius
                )
            }
            LoadingButtonState.Completed -> {}
            LoadingButtonState.Idle -> {}
        }

        val centerX = measuredWidth.toFloat() / 2
        val centerY = measuredHeight.toFloat() / 2 - textRect.centerY()

        canvas.drawText(buttonText, centerX, centerY, textPaint)
    }

    private fun drawBackground(
        canvas: Canvas,
        backgroundWidth: Float,
        backgroundHeight: Float,
        cornerRadius: Float
    ) {
        canvas.drawColor(buttonBackgroundColor)
        textPaint.getTextBounds(buttonText, 0, buttonText.length, textRect)
        canvas.drawRoundRect(
            0f,
            0f,
            backgroundWidth,
            backgroundHeight,
            cornerRadius,
            cornerRadius,
            backgroundPaint
        )
    }

    private fun drawLoadingState(
        canvas: Canvas,
        backgroundHeight: Float,
        cornerRadius: Float
    ) {
        var progressVal = progress * measuredWidth.toFloat()
        canvas.drawRoundRect(
            0f,
            0f,
            progressVal,
            backgroundHeight,
            cornerRadius,
            cornerRadius,
            inProgressBackgroundPaint
        )

        val arcDiameter = cornerRadius * 2
        val arcRectSize = measuredHeight.toFloat() - paddingBottom.toFloat() - arcDiameter

        progressVal = progress * 360f
        canvas.drawArc(
            paddingStart + arcDiameter,
            paddingTop.toFloat() + arcDiameter,
            arcRectSize,
            arcRectSize,
            0f,
            progressVal,
            true,
            inProgressArcPaint
        )
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val minWidth: Int = paddingLeft + paddingRight + suggestedMinimumWidth
        val width: Int = resolveSizeAndState(minWidth, widthMeasureSpec, 1)
        val height: Int = resolveSizeAndState(
            MeasureSpec.getSize(width),
            heightMeasureSpec,
            0
        )
        widthSize = width
        heightSize = height
        setMeasuredDimension(width, height)
    }

    private fun disableButton() {
        findViewById<LoadingButton>(R.id.loading_button).isEnabled = false
    }

    private fun enableButton() {
        findViewById<LoadingButton>(R.id.loading_button).isEnabled = true
    }

    fun setLoadingButtonState(state: LoadingButtonState) {
        buttonState = state
    }

    private fun setText(buttonText: String) {
        this.buttonText = buttonText
        invalidate()
        requestLayout()
    }

    private fun setBgColor(backgroundColor: Int) {
        buttonBackgroundColor = backgroundColor
        invalidate()
        requestLayout()
    }

    private fun resetProgress() {
        progress = 0f
    }
}