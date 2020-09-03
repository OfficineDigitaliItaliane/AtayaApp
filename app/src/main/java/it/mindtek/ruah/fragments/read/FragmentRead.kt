package it.mindtek.ruah.fragments.read

import android.annotation.SuppressLint
import android.graphics.*
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.TextPaint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import it.mindtek.ruah.R
import it.mindtek.ruah.activities.ActivityUnit
import it.mindtek.ruah.adapters.AnswersAdapter
import it.mindtek.ruah.config.ImageWithMarkersGenerator
import it.mindtek.ruah.interfaces.ReadActivityInterface
import it.mindtek.ruah.kotlin.extensions.canAccessActivity
import it.mindtek.ruah.kotlin.extensions.db
import it.mindtek.ruah.kotlin.extensions.fileFolder
import it.mindtek.ruah.kotlin.extensions.setVisible
import it.mindtek.ruah.pojos.PojoRead
import kotlinx.android.synthetic.main.fragment_read.*
import org.jetbrains.anko.backgroundColor
import org.jetbrains.anko.dip
import java.io.File


class FragmentRead : Fragment() {
    private var unitId: Int = -1
    private var stepIndex: Int = -1
    private var adapter: AnswersAdapter? = null
    private var optionsPlayers: MutableList<MediaPlayer> = mutableListOf()
    private var communicator: ReadActivityInterface? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_read, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.let {
            if (it.containsKey(ActivityUnit.EXTRA_UNIT_ID)) {
                unitId = it.getInt(ActivityUnit.EXTRA_UNIT_ID)
            }
            if (it.containsKey(EXTRA_STEP)) {
                stepIndex = it.getInt(EXTRA_STEP)
            }
        }
        setup()
    }

    private fun setup() {
        if (unitId == -1 || stepIndex == -1) {
            requireActivity().finish()
        }
        if (requireActivity() is ReadActivityInterface) {
            communicator = requireActivity() as ReadActivityInterface
        }
        val read = db.readDao().getReadByUnitId(unitId)
        if (read.size == 0 || read.size <= stepIndex) {
            requireActivity().finish()
        }
        val unit = db.unitDao().getUnitById(unitId)
        unit?.let {
            val color = ContextCompat.getColor(requireActivity(), it.color)
            stepBackground.backgroundColor = color
        }
        next.isEnabled = false
        setupSteps(read)
        setupNext(read)
        setupPicture(read[stepIndex])
        setupAnswers(read[stepIndex])
    }

    @SuppressLint("SetTextI18n")
    private fun setupSteps(read: MutableList<PojoRead>) {
        step.text = "${stepIndex + 1}/${read.size}"
    }

    private fun setupNext(read: MutableList<PojoRead>) {
        next.setOnClickListener {
            if (stepIndex + 1 < read.size) {
                destroyPlayers()
                communicator?.goToNext(stepIndex + 1)
            } else {
                communicator?.goToFinish()
            }
        }
    }

    private fun setupPicture(read: PojoRead) {
        read.read?.let {
            val pictureFile = File(fileFolder.absolutePath, it.picture.value)
            val bitmap = ImageWithMarkersGenerator.createImageWithMarkers(read.markers, pictureFile)
            stepImage.setImageBitmap(bitmap)
            if (it.picture.credits.isNotBlank()) {
                stepImageCredits.setVisible()
                stepImageCredits.text = it.picture.credits
            }
        }
    }

    private fun setupAnswers(read: PojoRead) {
        options.layoutManager = LinearLayoutManager(requireActivity())
        val optionsList = read.options

        options.adapter = adapter
    }

    private fun playOptionAudio(index: Int, audio: String) {
        pausePlayers(index)
        var player = optionsPlayers.getOrNull(index)
        when {
            player == null -> {
                val audioFile = File(fileFolder.absolutePath, audio)
                player = MediaPlayer.create(requireActivity(), Uri.fromFile(audioFile))
                player!!.setOnCompletionListener {
                    if (canAccessActivity) {
                        player.pause()
                    }
                }
                player.start()
                optionsPlayers.add(index, player)
            }
            player.isPlaying -> player.pause()
            else -> player.start()
        }
    }

    private fun pausePlayers(index: Int? = null) {
        val players = if (index != null) {
            optionsPlayers.filter {
                it != optionsPlayers[index]
            }
        } else {
            optionsPlayers
        }
        players.map {
            it.pause()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyPlayers()
    }

    private fun destroyPlayers() {
        optionsPlayers.map {
            it.release()
        }
    }

    companion object {
        const val EXTRA_STEP = "extra_current_step_number"

        fun newInstance(unitId: Int, stepIndex: Int): FragmentRead {
            val frag = FragmentRead()
            val bundle = Bundle()
            bundle.putInt(ActivityUnit.EXTRA_UNIT_ID, unitId)
            bundle.putInt(EXTRA_STEP, stepIndex)
            frag.arguments = bundle
            return frag
        }
    }
}