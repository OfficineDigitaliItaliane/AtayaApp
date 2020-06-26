package it.mindtek.ruah.fragments.read

import android.annotation.SuppressLint
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import it.mindtek.ruah.R
import it.mindtek.ruah.activities.ActivityUnit
import it.mindtek.ruah.adapters.AnswersAdapter
import it.mindtek.ruah.config.GlideApp
import it.mindtek.ruah.interfaces.ReadActivityInterface
import it.mindtek.ruah.kotlin.extensions.db
import it.mindtek.ruah.kotlin.extensions.fileFolder
import it.mindtek.ruah.pojos.PojoRead
import kotlinx.android.synthetic.main.fragment_read.*
import org.jetbrains.anko.backgroundColor
import java.io.File

class FragmentRead : Fragment() {
    private var unitId: Int = -1
    private var stepIndex: Int = -1
    private var adapter: AnswersAdapter? = null
    private var correctCount = 0
    private var player: MediaPlayer? = null
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
        next.isEnabled = false
        setupSteps(read)
        val unit = db.unitDao().getUnitById(unitId)
        unit?.let {
            val color = ContextCompat.getColor(requireActivity(), it.color)
            stepBackground.backgroundColor = color
        }
        next.setOnClickListener {
            if (stepIndex + 1 < read.size) {
                if (player != null)
                    destroyPlayer()
                communicator?.goToNext(stepIndex + 1)
            } else {
                communicator?.goToFinish()
            }
        }
        val currentRead = read[stepIndex]
        currentRead.read?.let {
            val pictureFile = File(fileFolder.absolutePath, it.picture.value)
            GlideApp.with(this).load(pictureFile).placeholder(R.color.grey).into(picture)
        }
        setupAnswers(currentRead)
    }

    @SuppressLint("SetTextI18n")
    private fun setupSteps(read: MutableList<PojoRead>) {
        step.text = "${stepIndex + 1}/${read.size}"
    }

    private fun setupAnswers(read: PojoRead) {
        answers.layoutManager = LinearLayoutManager(requireActivity())
        adapter = AnswersAdapter(read.answersConverted, { answer ->
            val correctNum = read.answersConverted.map { it.correct }.count { it }
            if (answer.correct) {
                correctCount += 1
            }
            if (correctCount >= correctNum) {
                next.isEnabled = true
            }
        }, {
            playAudio(it.audio.value)
        })
        answers.adapter = adapter
    }

    private fun playAudio(audio: String) {
        if (player != null) {
            destroyPlayer()
        }
        val audioFile = File(fileFolder.absolutePath, audio)
        player = MediaPlayer.create(requireActivity(), Uri.fromFile(audioFile))
        player?.setOnCompletionListener {
            destroyPlayer()
        }
        player?.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        destroyPlayer()
    }

    private fun destroyPlayer() {
        player?.release()
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