package it.mindtek.ruah.fragments.understand

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.youtube.player.YouTubeInitializationResult
import com.google.android.youtube.player.YouTubePlayer
import com.google.android.youtube.player.YouTubePlayerSupportFragment
import it.mindtek.ruah.R
import it.mindtek.ruah.activities.ActivityUnderstand
import it.mindtek.ruah.activities.ActivityUnderstandQuestion
import it.mindtek.ruah.activities.ActivityUnit
import it.mindtek.ruah.fragments.write.FragmentWrite
import it.mindtek.ruah.kotlin.extensions.canAccessActivity
import it.mindtek.ruah.kotlin.extensions.db
import it.mindtek.ruah.kotlin.extensions.fileFolder
import it.mindtek.ruah.pojos.PojoUnderstand
import kotlinx.android.synthetic.main.fragment_understand_video.*
import org.jetbrains.anko.backgroundColor
import java.io.File


class FragmentUnderstandVideo : Fragment() {
    private var unitId: Int = -1
    private var stepIndex: Int = -1
    private var audioPlayer: MediaPlayer? = null
    private var videoPlayer: YouTubePlayer? = null
    private var understand: MutableList<PojoUnderstand> = mutableListOf()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_understand_video, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            if (it.containsKey(ActivityUnit.EXTRA_UNIT_ID)) {
                unitId = it.getInt(ActivityUnit.EXTRA_UNIT_ID)
            }
            if (it.containsKey(FragmentWrite.EXTRA_STEP)) {
                stepIndex = it.getInt(EXTRA_STEP)
            }
        }
        setup()
    }

    override fun onResume() {
        super.onResume()
        setupVideoAndAudio(understand[stepIndex])
    }

    private fun setup() {
        if (unitId == -1 || stepIndex == -1) {
            requireActivity().finish()
        }
        understand = db.understandDao().getUnderstandByUnitId(unitId)
        if (understand.size == 0 || understand.size <= stepIndex) {
            requireActivity().finish()
        }
        setupNext()
        setupVideoAndAudio(understand[stepIndex])
        setupSteps()
        val unit = db.unitDao().getUnitById(unitId)
        unit?.let {
            val color = ContextCompat.getColor(requireActivity(), it.color)
            stepLayout.backgroundColor = color
        }
    }

    private fun setupVideoAndAudio(understand: PojoUnderstand) {
        understand.understand?.let {
            showVideo(it.video_url.value)
            setupListen(it.audio.value)
        }
    }

    // CAST_NEVER_SUCCEEDS can be ignored - happens because Youtube SDK's fragment is not androidx.Fragment, but Jetifier will take care of that and cast will succeed
    @Suppress("CAST_NEVER_SUCCEEDS")
    private fun showVideo(videoUrl: String) {
        val playerFragment = childFragmentManager.findFragmentById(R.id.videoPlayer) as YouTubePlayerSupportFragment
        playerFragment.initialize(getString(R.string.youtube_api_key), object : YouTubePlayer.OnInitializedListener {
            override fun onInitializationSuccess(p0: YouTubePlayer.Provider?, p1: YouTubePlayer, p2: Boolean) {
                videoPlayer = p1
                p1.setPlayerStateChangeListener(object : YouTubePlayer.PlayerStateChangeListener {
                    override fun onAdStarted() {}
                    override fun onLoading() {}
                    override fun onVideoStarted() {}
                    override fun onLoaded(p0: String?) {}
                    override fun onVideoEnded() {
                        if (canAccessActivity) {
                            next.isEnabled = true
                        }
                    }

                    override fun onError(p0: YouTubePlayer.ErrorReason?) {}
                })
                p1.setPlaybackEventListener(object : YouTubePlayer.PlaybackEventListener {
                    override fun onSeekTo(p0: Int) {}
                    override fun onBuffering(p0: Boolean) {}
                    override fun onPlaying() {
                        audioPlayer?.pause()
                    }

                    override fun onStopped() {}
                    override fun onPaused() {}
                })
                p1.fullscreenControlFlags = YouTubePlayer.FULLSCREEN_FLAG_CONTROL_SYSTEM_UI
                p1.loadVideo(videoUrl)
            }

            override fun onInitializationFailure(p0: YouTubePlayer.Provider?, p1: YouTubeInitializationResult?) {
                println("YOUTUBE ERROR")
            }
        })
    }

    private fun setupListen(audio: String?) {
        listen.setOnClickListener {
            audio?.let {
                playAudio(it)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupSteps() {
        step.text = "${stepIndex + 1}/${understand.size}"
    }

    private fun setupNext() {
        next.setOnClickListener {
            val intent = Intent(requireActivity(), ActivityUnderstandQuestion::class.java)
            intent.putExtra(ActivityUnit.EXTRA_UNIT_ID, unitId)
            intent.putExtra(ActivityUnderstand.STEP_INDEX, stepIndex)
            startActivity(intent)
        }
        next.isEnabled = false
    }

    private fun playAudio(audio: String) {
        videoPlayer?.pause()
        when {
            audioPlayer == null -> {
                val audioFile = File(fileFolder.absolutePath, audio)
                audioPlayer = MediaPlayer.create(requireActivity(), Uri.fromFile(audioFile))
                audioPlayer!!.setOnCompletionListener {
                    if (canAccessActivity) {
                        audioPlayer!!.pause()
                    }
                }
                audioPlayer!!.start()
            }
            audioPlayer!!.isPlaying -> audioPlayer!!.pause()
            else -> audioPlayer!!.start()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayer?.release()
        videoPlayer?.release()
    }

    companion object {
        const val EXTRA_STEP = "extra step int position"

        fun newInstance(unit_id: Int, stepIndex: Int): FragmentUnderstandVideo {
            val fragment = FragmentUnderstandVideo()
            val bundle = Bundle()
            bundle.putInt(ActivityUnit.EXTRA_UNIT_ID, unit_id)
            bundle.putInt(EXTRA_STEP, stepIndex)
            fragment.arguments = bundle
            return fragment
        }
    }
}