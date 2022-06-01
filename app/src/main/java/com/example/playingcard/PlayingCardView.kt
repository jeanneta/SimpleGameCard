package com.example.playingcard

import android.app.admin.FactoryResetProtectionPolicy
import android.content.Context
import android.graphics.*
import android.graphics.Color.WHITE
import android.graphics.drawable.Drawable
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.util.AttributeSet
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import androidx.annotation.ColorLong


/**
 * TODO: document your custom view class.
 */
class PlayingCardView : View {
    companion object ValidCards {
        val suits = listOf("♥", "♦", "♠", "♣")
        val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        val CARD_STANDAR_HEIGHT = 240.0f
        val CORNER_RADIUS = 12.0f
        val FACE_CARD_SCALE_FACTOR = 0.85f
        val TEXT_STANDARD_SIZE = 14.0f
    }

    private val faceImageMap = mapOf(
        "J♥" to R.drawable.j_heart,
        "J♦" to R.drawable.j_diamond,
        "J♠" to R.drawable.j_spade,
        "J♣" to R.drawable.j_club,
        "Q♥" to R.drawable.q_heart,
        "Q♦" to R.drawable.q_diamond,
        "Q♠" to R.drawable.q_spade,
        "Q♣" to R.drawable.q_club,
        "K♥" to R.drawable.k_heart,
        "K♦" to R.drawable.k_diamond,
        "K♠" to R.drawable.k_spade,
        "K♣" to R.drawable.k_club
    )


    private val cornerScaleFactor: Float
        get() {
            return height / CARD_STANDAR_HEIGHT
        }

    private val cornerRadius: Float
        get() {
            return CORNER_RADIUS * cornerScaleFactor
        }


    var suit: String? = null
        set(value) {
            if (value in suits) {
                field = value
                invalidate()
                //Don't call onDraw()
            }
        }

    var rank: String? = null
        set(value) {
            if (value in ranks) {
                field = value
                invalidate()
            }
        }

    var faceUp = true
        set(value) {
            field = value
            invalidate()
        }

    constructor(context: Context) : super(context) {
        init(null)
    }

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        init(attrs)
    }

    private val mPaint = Paint() //for drawing border and face images
    private val mTextPaint = TextPaint()

    private fun init(attrs: AttributeSet?) {
        // Load attributes
        val a = context.obtainStyledAttributes(attrs, R.styleable.PlayingCardView)
        mPaint.isAntiAlias = true
        mTextPaint.isAntiAlias = true

        rank = a.getString(R.styleable.PlayingCardView_rank)
        suit = a.getString(R.styleable.PlayingCardView_suit)
        faceUp = a.getBoolean(R.styleable.PlayingCardView_faceUp, true)

        a.recycle()
    }

    private val gestureDetector by lazy {
        GestureDetector(context, object : GestureDetector.SimpleOnGestureListener(){
            override fun onDown(e: MotionEvent?): Boolean {
                return true

            }

            override fun onFling(
                e1: MotionEvent?,
                e2: MotionEvent?,
                velocityX: Float,
                velocityY: Float
            ): Boolean {
                faceUp = !faceUp
                return true
            }
        })
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (gestureDetector.onTouchEvent(event)){
            return true
        }
        return super.onTouchEvent(event)
    }

    private val cornerOffset: Float
        get() {
            return cornerRadius * 0.5f
        }

    private fun drawCorners(canvas: Canvas) {
        val textToDraw = rank + "\n" + suit

        mTextPaint.textSize = TEXT_STANDARD_SIZE * cornerScaleFactor

        //"♥", "♦", "♠", "♣"
        if (suit == "♥" || suit == "♦") {
            mTextPaint.color = Color.RED
        } else {
            mTextPaint.color = Color.BLACK
        }

        val textWidth = mTextPaint.measureText(textToDraw).toInt()
        val textLayout = StaticLayout.Builder.obtain(
            textToDraw, 0, textToDraw.length, mTextPaint, textWidth
        )
            .setAlignment(Layout.Alignment.ALIGN_CENTER)
            .setLineSpacing(0.0f, 0.9f)
            .setIncludePad(false).build()

        canvas.save()
        canvas.translate(cornerOffset, cornerOffset)
        textLayout.draw(canvas)
        canvas.translate(width - 2 * cornerOffset, height - 2 * cornerOffset)
        canvas.rotate(180f)
        textLayout.draw(canvas)
        canvas.restore()

    }

    private val PIP_HOFFSET_PERCENTAGE = 0.165f
    private val PIP_VOFFSET1_PERCENTAGE = 0.100f
    private val PIP_VOFFSET2_PERCENTAGE = 0.175f
    private val PIP_VOFFSET3_PERCENTAGE = 0.300f
    private val PIP_FONT_SCALE_FACTOR = 0.01f

    private fun drawPipsWithOffsetUpsideDown(
        canvas: Canvas,
        hoffset: Float, voffset: Float, upsideDown: Boolean
    ) {
        if (upsideDown) {
            canvas.save()
            canvas.translate(width - 1.0f, height - 1.0f)
            canvas.rotate(180f)
        }
        val middle = PointF(width / 2.0f, height / 2.0f)
        mTextPaint.textSize = TEXT_STANDARD_SIZE * width * PIP_FONT_SCALE_FACTOR

        if (suit == "♥" || suit == "♦") {
            mTextPaint.color = Color.RED
        } else {
            mTextPaint.color = Color.BLACK
        }
        val bounds = Rect()
        mTextPaint.getTextBounds(suit, 0, suit!!.length, bounds)

        val pipOrigin = PointF(
            middle.x - bounds.width() / 2.0f - hoffset * width,
            middle.y + bounds.height() / 2.0f - voffset * height
        )

        canvas.drawText(suit!!, pipOrigin.x, pipOrigin.y, mTextPaint)

        if (hoffset > 0.0f) {
            pipOrigin.x += 2 * hoffset * width
            canvas.drawText(suit!!, pipOrigin.x, pipOrigin.y, mTextPaint)
        }
        if (upsideDown) {
            canvas.restore()
        }
    }

    private fun drawPipsWithOffset(
        canvas: Canvas,
        hoffset: Float,
        voffset: Float,
        mirror: Boolean
    ) {
        drawPipsWithOffsetUpsideDown(canvas, hoffset, voffset, false)
        if (mirror) {
            drawPipsWithOffsetUpsideDown(canvas, hoffset, voffset, true)
        }
    }

    private fun drawPips(canvas: Canvas) {
        if (rank == "A" || rank == "3" || rank == "5" || rank == "9") {
            drawPipsWithOffset(canvas, 0f, 0f, false)
        }
        if (rank == "6" || rank == "7" || rank == "8") {
            drawPipsWithOffset(canvas, PIP_HOFFSET_PERCENTAGE, 0f, false)
        }
        if (rank == "2" || rank == "3" || rank == "7" || rank == "8" || rank == "10") {
            drawPipsWithOffset(canvas, 0f, PIP_VOFFSET2_PERCENTAGE, rank != "7")
        }
        if (rank == "4" || rank == "5" || rank == "6" || rank == "7" || rank == "8"
            || rank == "9" || rank == "10"
        ) {
            drawPipsWithOffset(canvas, PIP_HOFFSET_PERCENTAGE, PIP_VOFFSET3_PERCENTAGE, true)
        }
        if (rank == "9" || rank == "10") {
            drawPipsWithOffset(canvas, PIP_HOFFSET_PERCENTAGE, PIP_VOFFSET1_PERCENTAGE, true)
        }
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val path = Path()
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        path.addRoundRect(rect, cornerRadius, cornerRadius, Path.Direction.CW)
        canvas.clipPath(path)

        //Draw card boundary
        mPaint.style = Paint.Style.FILL
        mPaint.color = Color.WHITE
        canvas.drawPath(path, mPaint)

        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = 3.0f
        mPaint.color = Color.BLACK
        canvas.drawPath(path, mPaint)

        rect.inset(
            rect.width() * (1 - FACE_CARD_SCALE_FACTOR),
            rect.height() * (1 - FACE_CARD_SCALE_FACTOR)
        )
        if (faceUp) {
            val imageID = faceImageMap.get(rank + suit)
            if (imageID != null) {
                val faceImage = BitmapFactory.decodeResource(resources, imageID)
                canvas.drawBitmap(faceImage, null, rect, mPaint)
            } else {
                drawPips(canvas)
            }
            drawCorners(canvas)
        }
        else{
            val backImage = BitmapFactory.decodeResource(resources, R.drawable.cardback)
            canvas.drawBitmap(backImage, null, rect, mPaint)
        }
    }
}