package com.yizisu.music.and.video.view

import android.animation.*
import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import com.yizisu.basemvvm.mvvm.MvvmActivity
import com.yizisu.basemvvm.mvvm.mvvm_helper.ILifecycleObserver
import com.yizisu.basemvvm.mvvm.mvvm_helper.registerOnSuccessLiveBean
import com.yizisu.basemvvm.utils.safeGet
import com.yizisu.music.and.video.AppData
import com.yizisu.music.and.video.bean.SongModel
import com.yizisu.music.and.video.service.music.MusicEventListener
import com.yizisu.music.and.video.service.music.MusicService
import com.yizisu.music.and.video.utils.updateCover
import com.yizisu.playerlibrary.helper.PlayerModel
import de.hdodenhof.circleimageview.CircleImageView

class AutoRotationImageView : CircleImageView, MusicEventListener, ILifecycleObserver {
    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    init {
        if (AppData.currentPlaySong.data == null) {
            updateCover(null, null)
        }
        context.safeGet<MvvmActivity>()?.apply {
            lifecycle.addObserver(this@AutoRotationImageView)
           registerOnSuccessLiveBean( AppData.currentPlaySong) {
                updateCover(it, null)
            }
        }
    }

    private var animIsRun = false
    private val anim by lazy {
        ObjectAnimator.ofPropertyValuesHolder(
            this, PropertyValuesHolder.ofFloat(View.ROTATION, rotation, rotation+360f)
        ).apply {
            interpolator = LinearInterpolator()
            repeatCount = -1
            duration = 20000
            setAutoCancel(true)
        }
    }
    private val lastPlayerModel: PlayerModel?
        get() = AppData.currentPlaySong.data

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        MusicService.addMusicEventListener(this)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        MusicService.removeMusicEventListener(this)
        startAnim(false)
    }

    override fun onPlay(playStatus: Boolean, playerModel: SongModel?) {
        super.onPlay(playStatus, playerModel)
        if (lastPlayerModel == playerModel) {
            startAnim(playStatus)
        }
    }

    override fun onError(throwable: Throwable, playerModel: SongModel?) {
        super.onError(throwable, playerModel)
        anim.pause()
    }

    override fun onPause(playStatus: Boolean, playerModel: SongModel?) {
        super<MusicEventListener>.onPause(playStatus, playerModel)
        if (lastPlayerModel == playerModel) {
            startAnim(playStatus)
        }
    }

    override fun onStart() {
        super.onStart()
        if (animIsRun) {
            anim.resume()
        }
    }

    override fun onStop() {
        super<ILifecycleObserver>.onStop()
        anim.pause()
    }

    override fun onVisibilityChanged(changedView: View, visibility: Int) {
        super.onVisibilityChanged(changedView, visibility)
        //隐藏不执行动画
        if (animIsRun) {
            if (changedView == this) {
                if (visibility == View.VISIBLE) {
                    anim.resume()
                } else {
                    anim.pause()
                }
            }
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        anim.pause()
        anim.cancel()
    }

    private fun startAnim(isPlay: Boolean) {
        if (isPlay) {
            animIsRun = true
            if (!anim.isStarted) {
                anim.start()
            } else {
                anim.resume()
            }
        } else {
            animIsRun = false
            anim.pause()
        }
    }
}