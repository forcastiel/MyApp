package com.shrek.klib.colligate.span

import android.graphics.Canvas
import android.graphics.MaskFilter
import android.graphics.Paint
import android.graphics.drawable.Drawable
import android.text.Spannable
import android.text.SpannableString
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.*
import android.view.MotionEvent
import android.view.View
import android.widget.TextView


/**
 * @author Shrek
 * @date:  2016-08-15
 */
class TextSpan(val content: String) {

    //所有样式
    val styles = mutableListOf(mutableListOf<CharacterStyle>())

    val textConstructor = mutableListOf<String>(content)

//    var startIndex = 0 
//        private set(value) {
//            field = value
//            endIndex = value + content.length
//        }

//    var endIndex = 0
//        private set

    fun backgroundColor(colorVal: Int) {
        styles[0].add(BackgroundColorSpan(colorVal))
    }

    fun foregroundColor(colorVal: Int) {
        styles[0].add(ForegroundColorSpan(colorVal))
    }

    /**
     * 模糊(BlurMaskFilter)、浮雕(EmbossMaskFilter) BlurMaskFilter
     */
    fun maskFilter(maskFilter: MaskFilter) {
        styles[0].add(MaskFilterSpan(maskFilter))
    }

    /**
     * 光栅效果 StrikethroughSpan()
     */
//    fun rasterizer(rasterizer: Rasterizer) {
//        styles[0].add(RasterizerSpan(rasterizer))
//    }

    /**
     * 删除线（中划线）
     */
    fun strikethrough() {
        styles[0].add(StrikethroughSpan())
    }

    /**
     * 下划线
     */
    fun underline() {
        styles[0].add(UnderlineSpan())
    }

    /**
     * 设置图片 （DynamicDrawableSpan.ALIGN_BASELINE  or DynamicDrawableSpan.ALIGN_BOTTOM）
     */
    fun dynamicDrawable(drawable: Drawable, isAlignBaseLine: Boolean) {
        styles[0].add(object : DynamicDrawableSpan(if (isAlignBaseLine) DynamicDrawableSpan.ALIGN_BASELINE else DynamicDrawableSpan.ALIGN_BOTTOM) {
            override fun getDrawable(): Drawable {
                drawable.setBounds(0, 0, drawable.minimumWidth, drawable.minimumHeight)
                return drawable
            }
        })
    }

    /**
     * 字体大小(像素)
     */
    fun absoluteSize(textSize: Int) {
        styles[0].add(AbsoluteSizeSpan(textSize, false))
    }

    /**
     * 图片
     */
    fun image(drawable: Drawable, width: Int = drawable.minimumWidth, height: Int = drawable.minimumHeight, isAlignBaseLine: Boolean = true) {
        drawable.setBounds(0, 0, width, height)
        styles[0].add(ImageSpan(drawable, if (isAlignBaseLine) DynamicDrawableSpan.ALIGN_BASELINE else DynamicDrawableSpan.ALIGN_BOTTOM))
    }

    /**
     * 居中图片
     */
    fun centerImage(drawable: Drawable, width: Int = drawable.minimumWidth, height: Int = drawable.minimumHeight){
        drawable.setBounds(0, 0, width, height)
        styles[0].add(CenterImageSpan(drawable))
    }

    /**
     * ScaleXSpan 基于x轴缩放
     */
    fun scaleX(scaleRate: Float) {
        styles[0].add(ScaleXSpan(scaleRate))
    }

    /**
     *  相对大小（文本字体）
     */
    fun relativeSize(scanRate: Float) {
        styles[0].add(RelativeSizeSpan(scanRate))
    }

    /**
     *  字体样式：粗体、斜体等 Typeface
     */
    fun style(typeface: Int) {
        styles[0].add(StyleSpan(typeface))
    }

    /**
     * 下标（数学公式会用到）
     */
    fun subscript() {
        styles[0].add(SubscriptSpan())
    }

    /**
     * 上标（数学公式会用到）
     */
    fun superscript() {
        styles[0].add(SuperscriptSpan())
    }

    /**
     * 垂直居中文字
     */
    fun textVerticalCenter(px:Float, txtColor: Int, pressColor: Int, strikeFlag: Boolean = false){
        styles[0].add(VerticalCenterSpan(px, txtColor, pressColor, strikeFlag))
    }

    /**
     *  文本字体
     */
    fun typeface(typeface: String) {
        styles[0].add(TypefaceSpan(typeface))
    }

    /**
     * 点击事件
     */
    fun click( paintDoing:TextPaint.()->Unit = {},onClickListener: (View?) -> Unit) {
        styles[0].add(object : ClickableSpan() {
            
            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                paintDoing.invoke(ds)
            }

            override fun onClick(p0: View?) {
                onClickListener(p0)
            }
            
        })
    }
    /**
     * 文本超链接
     */
    fun url(linkAddress: String) {
        styles[0].add(URLSpan(linkAddress))
    }

    operator fun plus(nextVal: TextSpan): TextSpan {
//        var index = 0
//        textConstructor.forEach {
//            index += it.length
//        }
//        startIndex = index - content.length

        styles.addAll(nextVal.styles)
        textConstructor.addAll(nextVal.textConstructor)
        return this
    }

    operator fun plus(strVal: String): TextSpan {
        val nextVal = TextSpan(strVal)
        styles.addAll(nextVal.styles)
        textConstructor.addAll(nextVal.textConstructor)
        return this
    }
    
}

/**
 * 扩展
 */
fun String.style(spanDoing: TextSpan.() -> Unit): TextSpan {
    val span = TextSpan(this)
    spanDoing.invoke(span)
    return span
}

operator fun String.plus(span:TextSpan):TextSpan{
    val nextSpan = TextSpan(this)
    return nextSpan + span
}

/**
 * 扩展字符串
 */
fun TextView.setText(textSpan: TextSpan) {
    val builder = StringBuilder()
    textSpan.textConstructor.forEach{
        builder.append(it)
    }

    val spanStr = SpannableString(builder.toString())
    var index = 0
    textSpan.textConstructor.forEachIndexed { position, str ->
        val end = index + str.length
        textSpan.styles[position].forEach {
            spanStr.setSpan(it, index, end,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
            if(it is ClickableSpan ) this.movementMethod = LinkMovementMethod()
            if(it is VerticalCenterSpan) this.movementMethod = VerticalCenterTextMethod()
        }
        index+= str.length
    }
    
    text = spanStr
}

fun TextSpan.toSpannableString():SpannableString{
    val builder = StringBuilder()
    textConstructor.forEach{
        builder.append(it)
    }

    val spanStr = SpannableString(builder.toString())
    var index = 0
    textConstructor.forEachIndexed { position, str ->
        val end = index + str.length
        styles[position].forEach {
            spanStr.setSpan(it, index, end,
                    Spannable.SPAN_INCLUSIVE_EXCLUSIVE)
        }
        index+= str.length
    }
    return spanStr
}

class CenterImageSpan(drawable: Drawable): ImageSpan(drawable) {
    override fun draw(canvas: Canvas, text: CharSequence?, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val drawable = drawable
        val fontMetricsInt = paint.fontMetricsInt
        val transY = (y + fontMetricsInt.descent + y + fontMetricsInt.ascent) / 2 - drawable.bounds.bottom / 2
        canvas.save()
        canvas.translate(x, transY.toFloat())
        drawable.draw(canvas)
        canvas.restore()
    }
}

class VerticalCenterSpan(private val fontSizePx: Float, private val txtColor: Int, private val pressColor: Int,
                         private val strikeFlag: Boolean = false) : ReplacementSpan() {
    private var isPressed: Boolean = false

    override fun getSize(paint: Paint, text: CharSequence, start: Int, end: Int, fm: Paint.FontMetricsInt?): Int {
        val spanText = text.subSequence(start, end)
        val txtPaint = getCustomTextPaint(paint)
        return txtPaint.measureText(spanText.toString()).toInt()
    }

    override fun draw(canvas: Canvas, text: CharSequence, start: Int, end: Int, x: Float, top: Int, y: Int, bottom: Int, paint: Paint) {
        val spanText = text.subSequence(start, end)
        val txtPaint = getCustomTextPaint(paint)
        val txtFm = txtPaint.fontMetricsInt
        canvas.drawText(spanText.toString(), x, (y - ((y + txtFm.descent + y + txtFm.ascent) / 2 - (bottom + top) / 2)).toFloat(), txtPaint)
    }

    private fun getCustomTextPaint(srcPaint: Paint): TextPaint {
        val paint = TextPaint(srcPaint)
        paint.color = if (isPressed) pressColor else txtColor
        paint.textSize = fontSizePx
        paint.isStrikeThruText = strikeFlag
        return paint
    }

    fun pressStateChange(isPressed: Boolean) {
        this.isPressed = isPressed
    }
}

class VerticalCenterTextMethod: LinkMovementMethod(){
    private fun verticalCenterSpanPress(buffer: Spannable?, widget: TextView?, isPressed: Boolean){
        buffer?.getSpans(0, buffer.length, VerticalCenterSpan::class.java)?.also {
            if (it.isNotEmpty()){
                it.forEach { span ->
                    span.pressStateChange(isPressed)
                }
                widget?.invalidate()
            }
        }
    }

    override fun onTouchEvent(widget: TextView?, buffer: Spannable?, event: MotionEvent?): Boolean {
        when (event?.action) {
            MotionEvent.ACTION_DOWN -> { verticalCenterSpanPress(buffer, widget, true) }
            MotionEvent.ACTION_UP -> { verticalCenterSpanPress(buffer, widget, false) }
            MotionEvent.ACTION_CANCEL -> { verticalCenterSpanPress(buffer, widget, false) }
        }

        return super.onTouchEvent(widget, buffer, event)
    }
}