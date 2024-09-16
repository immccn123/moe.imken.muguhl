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
import androidx.activity.result.contract.ActivityResultContracts
import moe.imken.muguhl.databinding.FragmentFirstBinding


/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private val binding get() = _binding!!

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

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonIoCover.setOnClickListener { v ->
            val ctx = v.context
            if (!Settings.canDrawOverlays(ctx)) {
                requestPermission(ctx)
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