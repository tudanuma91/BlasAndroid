package com.v3.basis.blas.ui.item.item_image_zoom.custom_view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.util.Log
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.ScaleGestureDetector.SimpleOnScaleGestureListener
import android.view.View


class CustomImageView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    //画像表示の回数。1より後は数えなくてよい(処理内容変わらんから)
    private var cnt = 1

    //画像の配列
    lateinit var mBitmap :Bitmap

    //タップしたX座標
    private var touchPointX: Float = 0.0f

    //タップしたY座標
    private var touchPointY: Float = 0.0f

    //表示している画像の縮尺
    private var mLastScaleFactor = 1.0f

    //拡大等の画像を変形させるための変数
    private var bitMatrix = Matrix()

    //Canvasで使うPoint変数
    private var mPaint = Paint()


    // ピンチイン/アウトしたときの動きを定義するクラス
    private val mScaleGestureListener: SimpleOnScaleGestureListener =
        object : SimpleOnScaleGestureListener() {
            override fun onScaleBegin(detector: ScaleGestureDetector): Boolean {
                // ピンチイン/アウト開始
                // タッチの座標を記録
                touchPointX = detector.focusX
                touchPointY = detector.focusY
                return super.onScaleBegin(detector)
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                // ピンチイン/アウト中(毎フレーム呼ばれる)
                // 縮尺を計算
                mLastScaleFactor = detector.scaleFactor
                // マトリックスに加算(縦と横を同じように拡大縮小する)
                bitMatrix.postScale(mLastScaleFactor, mLastScaleFactor, touchPointX, touchPointY)
                // Viewを再読み込み(onDrawの発火)
                invalidate()
                return true
            }
        }


    // スクロールの動きを定義したクラス
    private val mSimpleOnGestureListener: SimpleOnGestureListener =
        object : SimpleOnGestureListener() {
            override fun onScroll(
                e1: MotionEvent?,
                e2: MotionEvent?,
                distanceX: Float,
                distanceY: Float
            ): Boolean {
                //これが移動の処理
                bitMatrix.preTranslate(-distanceX, -distanceY)
                invalidate()
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

        }


    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        //全てのジェスチャーはここを通る。ジェスチャーと処理の内容紐づけ処理
        mGestureDetector.onTouchEvent(event)
        mScaleGestureDetector.onTouchEvent(event)
        return true
    }

    //描画処理
    override fun onDraw(canvas: Canvas?) {
        //最初の1回のみ実行
        if(cnt == 1) {
            // 画面の横幅いっぱいに画像を表示するための計算
            val scaleX =
                width.toFloat() / getImageWidth()
            // 画面の立幅いっぱいに画像を表示するための計算
            val scaleY =
                height.toFloat() / getImageHeight()
            // 小さいほうを適応させる(画像が切れないようにする)
            mLastScaleFactor = Math.min(scaleX, scaleY)
            bitMatrix.postScale(mLastScaleFactor, mLastScaleFactor, 0F, 0F);
            mLastScaleFactor = 1.0f
            cnt++
        }

        //2回目以降はonScaleの変更情報をもとに画像作成する
        canvas!!.save()
        canvas.drawBitmap(mBitmap, bitMatrix, mPaint)
        canvas.restore()
    }

    //ピンチイン処理とスクロール処理を定義
    private val mScaleGestureDetector = ScaleGestureDetector(context,mScaleGestureListener)
    private val mGestureDetector = GestureDetector(context,mSimpleOnGestureListener)

    //画像の横を取得する
    private fun getImageWidth(): Int {
        return mBitmap.width
    }

    //画像の縦を取得する
    private fun getImageHeight(): Int {
        return mBitmap.height
    }

    //mBitMapに画像を定義
    fun setBitMap(bitmap: Bitmap){
        mBitmap = bitmap
    }

}

