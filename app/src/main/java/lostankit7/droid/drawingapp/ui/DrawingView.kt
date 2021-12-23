package lostankit7.droid.drawingapp.ui

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class DrawingView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    private var mDrawPath: CustomPath? = null
    private var mCanvasBitmap: Bitmap? = null

    // The Paint class holds the style and color information about how to draw geometries, text and bitmaps.
    private var mDrawPaint: Paint? = null
    private var mCanvasPaint: Paint? = null

    // A variable for stroke/brush size to draw on the canvas.
    var brushThickness: Float = 5f
        set(value) {
            field = value
            mDrawPaint!!.strokeWidth = brushThickness
        }

    // A variable to hold a color of the stroke.
    var brushColor = Color.parseColor("#000000")

    /**
     * The Canvas class holds the "draw" calls. To draw something, we need 4 basic components:
     * A Bitmap to hold the pixels,
     * a Canvas to host the draw calls (writing into the bitmap),
     * a drawing primitive (e.g. Rect, Path, text, Bitmap),
     * and a paint to describe the colors and styles for the drawing)
     */
    private var mCanvas: Canvas? = null

    /** list of paths drawn in screen */
    private val mPaths = mutableListOf<CustomPath>()

    /** list of undo paths*/
    private val mUndoPaths = mutableListOf<CustomPath>()

    init {
        setUpDrawing()
    }

    /**
     * This method initializes the attributes of the
     * View For Drawing class.
     */
    private fun setUpDrawing() {
        mDrawPaint = Paint()
        mDrawPath = CustomPath(brushColor, brushThickness)

        mDrawPaint!!.apply {
            color = color
            style = Paint.Style.STROKE // This is to draw a STROKE style
            strokeJoin = Paint.Join.ROUND // This is for store join
            strokeCap = Paint.Cap.ROUND // This is for stroke Cap
        }

        mCanvasPaint = Paint(Paint.DITHER_FLAG) // Paint flag that enables dithering when blitting.
        brushThickness = 15.toFloat() //initial brush/ stroke size is defined.
    }

    override fun onSizeChanged(w: Int, h: Int, wprev: Int, hprev: Int) {
        super.onSizeChanged(w, h, wprev, hprev)
        mCanvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        mCanvas = Canvas(mCanvasBitmap!!)
    }

    /**
     * This method is called when a stroke is drawn on the canvas
     * as a part of the painting.
     */
    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        /**
         * Draw the specified bitmap, with its top/left corner at (x,y), using the specified paint,
         * transformed by the current matrix.
         *If the bitmap and canvas have different densities, this function will take care of
         * automatically scaling the bitmap to draw at the same density as the canvas.
         */
        canvas.drawBitmap(mCanvasBitmap!!, 0f, 0f, mCanvasPaint)

        /**
         * as after each drawing, view is cleared and onDraw is called,
         * and so we will draw previous paths again in canvas as we have stored those paths in mPaths
         */
        for (path in mPaths) {
            mDrawPaint!!.apply {
                strokeWidth = path.brushThickness
                color = path.color
                canvas.drawPath(path, this)
            }
        }

        if (!mDrawPath!!.isEmpty) {
            mDrawPaint!!.apply {
                strokeWidth = mDrawPath!!.brushThickness
                color = mDrawPath!!.color
                canvas.drawPath(mDrawPath!!, this)
            }
        }
    }

    /**
     * This method acts as an event listener when a touch
     * event is detected on the device.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val touchX = event.x // Touch event of X coordinate
            val touchY = event.y // touch event of Y coordinate

            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    mDrawPath!!.apply {
                        color = brushColor
                        brushThickness = this@DrawingView.brushThickness
                        reset() // Clear any lines and curves from the path, making it empty.
                        moveTo(
                            touchX,
                            touchY
                        ) // Set the beginning of the next contour to the point (x,y).
                    }
                }

                MotionEvent.ACTION_MOVE -> {
                    mDrawPath!!.lineTo(
                        touchX,
                        touchY
                    ) // Add a line from the last point to the specified point (x,y).
                }

                MotionEvent.ACTION_UP -> {
                    /**
                     * when user has released the screen after drawing
                     * we will add that path to paths list (mPaths), to keep track of paths
                     * and as after action-up on draw will be called ,so we'll draw these paths in screen again
                     */
                    mPaths.add(mDrawPath!!)
                    mDrawPath = CustomPath(brushColor, brushThickness)
                }
                else -> return false
            }
            invalidate()
        }
        return true
    }

    // An inner class for custom path with two params as color and stroke size.
    inner class CustomPath(var color: Int, var brushThickness: Float) : Path()

    fun undoPaint() {
        if (mPaths.size > 0) {
            mUndoPaths.add(mPaths.removeAt(mPaths.size - 1))
            invalidate()
        }
    }

    fun redoPaint() {
        if (mUndoPaths.size > 0) {
            mPaths.add(mUndoPaths.removeAt(mUndoPaths.size - 1))
            invalidate()
        }
    }

}