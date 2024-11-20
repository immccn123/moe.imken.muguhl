package moe.imken.muguhl

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.radiobutton.MaterialRadioButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textview.MaterialTextView
import moe.imken.muguhl.databinding.FragmentPresetSwitcherBinding
import moe.imken.muguhl.presets.Preset
import moe.imken.muguhl.presets.PresetManager

class PresetSwitcherFragment(private val onSelectionChanged: () -> Unit) :
    BottomSheetDialogFragment() {
    private var _binding: FragmentPresetSwitcherBinding? = null
    private val binding get() = _binding!!

    inner class PresetSelectionAdapter(private val items: List<Preset>) :
        RecyclerView.Adapter<PresetSelectionAdapter.SelectionViewHolder>() {
        private var lastSelection = -1

        inner class SelectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val title = view.findViewById<MaterialTextView>(R.id.text_title)!!
            val radioButton = view.findViewById<MaterialRadioButton>(R.id.ratio_button)!!
            val deleteButton = view.findViewById<MaterialButton>(R.id.delete_button)!!
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectionViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.list_item_preset, parent, false)
            return SelectionViewHolder(view)
        }

        override fun onBindViewHolder(holder: SelectionViewHolder, position: Int) {
            val presetManager = PresetManager(requireContext())
            val item = items[position]
            holder.title.text = item.name
            holder.radioButton.isChecked = (presetManager.getCurrentPresetId() == item.id)
            if (holder.radioButton.isChecked) lastSelection = holder.adapterPosition
            holder.itemView.setOnClickListener {
                if (lastSelection != holder.adapterPosition) {
                    presetManager.switchPreset(item.id)
                    onSelectionChanged()
                    holder.radioButton.isChecked = true
                    this@PresetSwitcherFragment.dismiss()
                }
            }
            holder.deleteButton.setOnClickListener {
                this@PresetSwitcherFragment.dialog?.hide()
                MaterialAlertDialogBuilder(requireContext()).setTitle("确定要删除吗？")
                    .setMessage("如果删除了就再也回不来了")
                    .setNegativeButton("否") { _, _ -> this@PresetSwitcherFragment.dialog?.show() }
                    .setPositiveButton("是") { _, _ ->
                        if (presetManager.removePreset(item.id) > 0) {
                            refreshSwitchAdapter()
                            onSelectionChanged()
                            this@PresetSwitcherFragment.dialog?.show()
                        }
                    }.setOnCancelListener {
                        dismiss()
                    }.show()
            }
        }

        override fun getItemCount(): Int = items.size
    }

    inner class CreatePresetDialog : MaterialAlertDialogBuilder(requireContext()) {
        private var onConfirmed: (inputText: String, isChecked: Boolean) -> Unit = { _, _ -> }
        private var onRejected: () -> Unit = {}
        fun setOnConfirmed(listener: (inputText: String, isChecked: Boolean) -> Unit): CreatePresetDialog {
            onConfirmed = listener
            return this
        }

        fun setOnRejected(listener: () -> Unit): CreatePresetDialog {
            onRejected = listener
            return this
        }

        init {
            val dialogView = layoutInflater.inflate(R.layout.dialog_create_preset, null)

            setTitle("新建预设")
            setView(dialogView)

            setPositiveButton("确认") { _, _ ->
                val inputText =
                    dialogView.findViewById<TextInputEditText>(R.id.preset_name_input).text.toString()
                val isChecked =
                    dialogView.findViewById<MaterialCheckBox>(R.id.copy_preset_checkbox).isChecked
                onConfirmed(inputText, isChecked)
            }
            setNegativeButton("取消") { _, _ -> onRejected() }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.let {
            val bottomSheet =
                it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.layoutParams?.height = ViewGroup.LayoutParams.MATCH_PARENT
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPresetSwitcherBinding.inflate(inflater, container, false)
        val recyclerView = binding.recyclerView
        val presetManager = PresetManager(requireContext())

        recyclerView.layoutManager = LinearLayoutManager(context)
        refreshSwitchAdapter()

        binding.createPresetButton.setOnClickListener {
            dialog?.hide()
            CreatePresetDialog().setOnConfirmed { inputText, isChecked ->
                val currentPreset = presetManager.getCurrentPreset()
                if (isChecked) presetManager.createPreset(
                    Preset(
                        -1,
                        inputText,
                        currentPreset.x,
                        currentPreset.y,
                        currentPreset.width,
                        currentPreset.height,
                        currentPreset.color
                    )
                )
                else TODO()
                refreshSwitchAdapter()
                dialog?.show()
            }.setOnRejected { dialog?.show() }.setOnCancelListener { dismiss() }.show()
        }

        return binding.root
    }

    fun refreshSwitchAdapter() {
        val presetManager = PresetManager(requireContext())
        val presets = presetManager.getAllPresets()

        val adapter = PresetSelectionAdapter(presets)
        val recyclerView = binding.recyclerView
        recyclerView.adapter = adapter
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}