package com.ifpr.androidapptemplatear.ui

import androidx.fragment.app.Fragment
import com.ifpr.androidapptemplatear.R
import android.content.Context
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.ar.core.Config
import com.ifpr.androidapptemplatear.databinding.FragmentCameraBinding
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position
import okio.IOException
import java.io.File

class CameraFragment : Fragment() {

    private lateinit var sceneView: ArSceneView
    private lateinit var modelNode: ArModelNode
    private lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var txtCoins: TextView

    private lateinit var sharedViewModel: SharedModelViewModel
    private lateinit var economyManager: EconomyManager

    private val CAMERA_PERMISSION_CODE = 1001
    private var currentModelFile: String = "neutrofilo.glb"

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreate(savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity())[SharedModelViewModel::class.java]
        sharedViewModel.selectedModel.observe(viewLifecycleOwner) { path ->
            loadModel(path)
        }

        val view = inflater.inflate(R.layout.fragment_camera, container, false)

        sceneView = view.findViewById<ArSceneView?>(R.id.sceneView).apply {
            lightEstimationMode = Config.LightEstimationMode.DISABLED
        }

        placeButton = view.findViewById(R.id.place)
        txtCoins = view.findViewById(R.id.txtCoins)

        placeButton.setOnClickListener {
            placeModel()
        }

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        economyManager = EconomyManager(requireContext())
        updateCoinsUI()
        checkCameraPermission()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopSound()
        _binding = null
    }

    private fun placeModel() {
        modelNode.anchor()
        modelNode.isEditable = true
        modelNode.isScaleEditable = false

        sceneView.planeRenderer.isVisible = false

        // Recompensa de 50 moedas por posicionar o modelo
        val earnedCoins = 50
        val totalCoins = economyManager.addCoins(earnedCoins)
        updateCoinsUI()

        Toast.makeText(
            requireContext(),
            "🏎️ Modelo fixado! Ganhaste +$earnedCoins moedas!",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun updateCoinsUI() {
        txtCoins.text = "🪙 ${economyManager.getCoins()}"
    }

    private fun checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_PERMISSION_CODE
            )
        } else {
            initAR()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == CAMERA_PERMISSION_CODE && grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            initAR()
        } else {
            Toast.makeText(requireContext(), "Permissão de câmera negada", Toast.LENGTH_LONG).show()
        }
    }

    private fun initAR() {
        loadModel(currentModelFile)
    }

    private fun loadModel(fileName: String) {
        currentModelFile = fileName

        try {
            sceneView.removeChild(modelNode)
        } catch (e: Exception) {

        }

        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = getFilePath(requireContext(), fileName),
                centerOrigin = Position(0f, 0f, 1f),
            ) {
                sceneView.planeRenderer.isVisible = true
            }
            isEditable = true
            isScaleEditable = false
        }

        sceneView.addChild(modelNode)
        handleModelSound(fileName)
    }

    private fun handleModelSound(fileName: String) {
        val isCarModel = fileName.contains("audi", ignoreCase = true) ||
                fileName.contains("bmw", ignoreCase = true) ||
                fileName.contains("mclaren", ignoreCase = true) ||
                fileName.contains("carro", ignoreCase = true)

        if (isCarModel) {
            playSound("SOUNDS/som carro.mp3", isLooping = true)
        } else {
            stopSound()
        }
    }

    private fun playSound(assetPath: String, isLooping: Boolean = false) {
        try {
            stopSound()
            val descriptor = requireContext().assets.openFd(assetPath)
            mediaPlayer = MediaPlayer().apply {
                setDataSource(descriptor.fileDescriptor, descriptor.startOffset, descriptor.length)
                descriptor.close()
                this.isLooping = isLooping
                prepare()
                start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopSound() {
        mediaPlayer?.let {
            if (it.isPlaying) {
                it.stop()
            }
            it.release()
        }
        mediaPlayer = null
    }

    private fun getFilePath(context: Context?, fileName: String): String {
        try {
            context?.assets?.open(fileName)?.close()
            return fileName
        } catch (e: IOException) {
            val internalFile = File(context?.filesDir, fileName)
            if (internalFile.exists()) {
                return Uri.fromFile(internalFile).toString()
            } else {
                return "Arquivo não encontrado em assets nem em filesDir: $fileName"
            }
        }
    }
}