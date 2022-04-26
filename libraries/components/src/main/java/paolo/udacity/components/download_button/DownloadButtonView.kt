package paolo.udacity.components.download_button

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.annotation.StringRes
import androidx.core.content.withStyledAttributes
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.findViewTreeLifecycleOwner
import paolo.udacity.components.R
import paolo.udacity.core.extensions.darken
import paolo.udacity.core.extensions.dp


class DownloadButtonView @JvmOverloads constructor(context: Context,
                         attrs: AttributeSet? = null,
                         defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {
    // Drawing variables
    private val buttonPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 56.0f
        typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }
    private lateinit var loadingCircle: RectF

    // Animation variables
    var liveProgress: MutableLiveData<Double> = MutableLiveData()
    private var progress: Double = 0.0
    var liveResult: MutableLiveData<Boolean> = MutableLiveData(false)
    var isReady: Boolean = false

    // View State
    private lateinit var downloadState: DownloadButtonState

    init {
        isClickable = true
        minimumHeight = 48.dp

        context.withStyledAttributes(attrs, R.styleable.DownloadButtonView) {
            // Idle variables
            DownloadButtonState.IDLE.textColor = getColor(R.styleable.DownloadButtonView_textColorIdle,
                DownloadButtonState.IDLE.textColor)
            DownloadButtonState.IDLE.buttonColor = getColor(R.styleable.DownloadButtonView_buttonColorIdle,
                DownloadButtonState.IDLE.buttonColor)

            // Downloading variables
            DownloadButtonState.DOWNLOADING.textColor = getColor(R.styleable.DownloadButtonView_textColorDownloading,
                DownloadButtonState.DOWNLOADING.textColor)
            DownloadButtonState.DOWNLOADING.buttonColor = getColor(R.styleable.DownloadButtonView_buttonColorDownloading,
                DownloadButtonState.DOWNLOADING.buttonColor)

            downloadState = DownloadButtonState.IDLE
        }
    }

    override fun performClick(): Boolean {
        when(downloadState) {
            DownloadButtonState.SUCCESSFUL, DownloadButtonState.ERROR -> {
                downloadState = downloadState.next()
            }
            DownloadButtonState.DOWNLOADING -> {
                return true
            }
            DownloadButtonState.IDLE -> {
                if(isReady)
                    downloadState = downloadState.next()
            }
        }
        invalidate()
        super.performClick()
        return true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        // Set the base color of the button (not animated)
        // and characteristic of the view
        buttonPaint.color = downloadState.buttonColor
        drawBaseRectangle(canvas)

        when(downloadState) {
            DownloadButtonState.DOWNLOADING -> {
                drawLoadingProgression(canvas)
            }
            DownloadButtonState.SUCCESSFUL, DownloadButtonState.ERROR,
            DownloadButtonState.IDLE -> {
                observeLoadingProgress()
            }
        }
        // Set text
        textPaint.color = downloadState.textColor
        drawText(downloadState.buttonText, canvas)
    }

    private fun drawBaseRectangle(canvas: Canvas) {
        val innerPadding = 2f
        val x1 = innerPadding * 1f
        val x2 = (width - innerPadding * 2f) + innerPadding
        val y1 = height * 1f
        val y2 = 0f
        canvas.clipRect(x1, y1, x2, y2)
        canvas.drawColor(DownloadButtonState.IDLE.buttonColor)
    }

    private fun drawText(@StringRes text: Int, canvas: Canvas) {
        val x = width / 2f // Text padding
        val y = height / 2f - (textPaint.descent() + textPaint.ascent())/ 2
        canvas.drawText(context.getString(text), x, y, textPaint)
    }

    private fun observeLoadingProgress() {
        findViewTreeLifecycleOwner()?.let { lifecycleOwner ->
            liveProgress.removeObservers(lifecycleOwner)
            liveProgress.observe(lifecycleOwner) {
                progress = it
                invalidate()
            }

            liveResult.removeObservers(lifecycleOwner)
            liveResult.observe(lifecycleOwner) {
                if(downloadState == DownloadButtonState.DOWNLOADING) {
                    downloadState = downloadState.next(it)
                    invalidate()
                }
            }
        }
    }

    private fun drawLoadingProgression(canvas: Canvas) {
        // Loading progress bar given by the progress variable
        buttonPaint.color = downloadState.buttonColor.darken
        canvas.drawRect(0f, 0f, (width * (progress / 100)).toFloat(), height.toFloat(), buttonPaint)

        // Loading progress arc given the progress variable
        // We initialize the loading circle later as the measurements of the canvas only exist
        // when drawing it.
        if(!::loadingCircle.isInitialized) {
            loadingCircle = RectF(width - (height-height/6f), height/6f,
                width - height/6f,height - height/6f)
        }
        circlePaint.color = Color.parseColor(CIRCLE_COLOR)
        canvas.drawArc(loadingCircle, 0f, (360 * (progress / 100)).toFloat(), true, circlePaint)
    }

    companion object {
        // circle related constants
        private const val CIRCLE_COLOR = "#FFFFEB3B"
    }

}