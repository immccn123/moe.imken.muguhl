package moe.imken.muguhl

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.util.fastMap
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import moe.imken.muguhl.databinding.FragmentMainBinding
import moe.imken.muguhl.services.FloatWindowService
import moe.imken.muguhl.presets.PresetManager


class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null

    private val binding get() = _binding!!
    private lateinit var presetManager: PresetManager

    private val requestOverlayPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}

    private var floatWindowService: FloatWindowService? = null

    private fun requestPermission(ctx: Context) {
        // Min SDK = 23
        if (!Settings.canDrawOverlays(ctx)) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_OVERLAY_PERMISSION
            intent.data = Uri.parse("package:${ctx.packageName}")
            requestOverlayPermissionLauncher.launch(intent)
        }
    }

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            val binder = service as FloatWindowService.LocalBinder
            floatWindowService = binder.getService()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            floatWindowService = null
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        presetManager = PresetManager(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val autoCompleteTextView = binding.presetSelector.editText as? AutoCompleteTextView

        autoCompleteTextView?.apply {
            setOnClickListener {
                PresetSwitcherFragment {
                    setText(presetManager.getCurrentPreset().name)
                }.show(parentFragmentManager, null)
            }
            setText(presetManager.getCurrentPreset().name)
        }

        binding.buttonIoCover.setOnClickListener { v ->
            val ctx = v.context
            if (!Settings.canDrawOverlays(ctx)) {
                MaterialAlertDialogBuilder(ctx).setTitle(R.string.permission_required)
                    .setMessage(R.string.permission_required_desc)
                    .setPositiveButton(R.string.go_to_settings) { _, _ -> requestPermission(ctx) }
                    .setNegativeButton(R.string.exit) { _, _ -> activity?.finishAndRemoveTask() }
                    .show()
            } else {
                val serviceClass = FloatWindowService::class.java
                if (floatWindowService === null) {
                    val intent = Intent(ctx, serviceClass)
                    ctx.bindService(intent, connection, Context.BIND_AUTO_CREATE)
                } else {
                    ctx.unbindService(connection)
                    floatWindowService?.stopSelf()
                    floatWindowService = null
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}