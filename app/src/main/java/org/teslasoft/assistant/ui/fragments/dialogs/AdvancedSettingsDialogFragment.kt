/**************************************************************************
 * Copyright (c) 2023-2025 Dmytro Ostapenko. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **************************************************************************/

package org.teslasoft.assistant.ui.fragments.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputLayout
import org.teslasoft.assistant.lite.R
import org.teslasoft.assistant.preferences.Preferences

class AdvancedSettingsDialogFragment : BottomSheetDialogFragment() {
    companion object {
        fun newInstance(name: String, chatId: String) : AdvancedSettingsDialogFragment {
            val advancedSettingsDialogFragment = AdvancedSettingsDialogFragment()

            val args = Bundle()
            args.putString("name", name)
            args.putString("chatId", chatId)

            advancedSettingsDialogFragment.arguments = args

            return advancedSettingsDialogFragment
        }
    }

    private var gpt_35_turbo: RadioButton? = null
    private var gpt_4: RadioButton? = null
    private var gpt_4_turbo: RadioButton? = null
    private var gpt_4_o: RadioButton? = null
    private var o3_mini: RadioButton? = null
    private var o3: RadioButton? = null
    private var o1_mini: RadioButton? = null
    private var o1: RadioButton? = null
    private var see_all_models: RadioButton? = null
    private var see_favorite_models: RadioButton? = null
    private var ft: RadioButton? = null
    private var ftInput: EditText? = null
    private var maxTokens: EditText? = null
    private var endSeparator: EditText? = null
    private var prefix: EditText? = null
    private var ftFrame: TextInputLayout? = null
    private var temperatureSeekbar: com.google.android.material.slider.Slider? = null
    private var topPSeekbar: com.google.android.material.slider.Slider? = null
    private var frequencyPenaltySeekbar: com.google.android.material.slider.Slider? = null
    private var presencePenaltySeekbar: com.google.android.material.slider.Slider? = null
    private var btnSave: MaterialButton? = null
    private var btnCancel: MaterialButton? = null

    private var listener: StateChangesListener? = null

    private var model = "gpt-3.5-turbo"

    private var context: Context? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context = this.activity
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_advanced_settings, container, false)
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        gpt_35_turbo = view.findViewById(R.id.gpt_35_turbo)
        gpt_4 = view.findViewById(R.id.gpt_4)
        gpt_4_turbo = view.findViewById(R.id.gpt_4_turbo)
        gpt_4_o = view.findViewById(R.id.gpt_4_o)
        o3_mini = view.findViewById(R.id.gpt_o3_mini)
        o3 = view.findViewById(R.id.gpt_o3)
        o1_mini = view.findViewById(R.id.gpt_o1_mini)
        o1 = view.findViewById(R.id.gpt_o1)
        see_all_models = view.findViewById(R.id.see_all_models)
        see_favorite_models = view.findViewById(R.id.see_favorite_models)
        ft = view.findViewById(R.id.ft)
        ftInput = view.findViewById(R.id.ft_input)
        maxTokens = view.findViewById(R.id.max_tokens)
        endSeparator = view.findViewById(R.id.end_separator)
        prefix = view.findViewById(R.id.prefix)
        ftFrame = view.findViewById(R.id.ft_frame)
        temperatureSeekbar = view.findViewById(R.id.temperature_slider)
        frequencyPenaltySeekbar = view.findViewById(R.id.frequency_penalty_slider)
        presencePenaltySeekbar = view.findViewById(R.id.presence_penalty_slider)
        topPSeekbar = view.findViewById(R.id.top_p_slider)
        btnSave = view.findViewById(R.id.btn_post)
        btnCancel = view.findViewById(R.id.btn_discard)

        val preferences: Preferences = Preferences.getPreferences(requireActivity(), arguments?.getString("chatId")!!)

        temperatureSeekbar?.value = preferences.getTemperature() * 10
        topPSeekbar?.value = preferences.getTopP() * 10
        frequencyPenaltySeekbar?.value = preferences.getFrequencyPenalty() * 10
        presencePenaltySeekbar?.value = preferences.getPresencePenalty() * 10

        temperatureSeekbar?.addOnChangeListener { _, value, _ ->
            preferences.setTemperature(value / 10.0f)
        }

        temperatureSeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }

        topPSeekbar?.addOnChangeListener { _, value, _ ->
            preferences.setTopP(value / 10.0f)
        }

        topPSeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }

        frequencyPenaltySeekbar?.addOnChangeListener { _, value, _ ->
            preferences.setFrequencyPenalty(value / 10.0f)
        }

        frequencyPenaltySeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }

        presencePenaltySeekbar?.addOnChangeListener { _, value, _ ->
            preferences.setPresencePenalty(value / 10.0f)
        }

        presencePenaltySeekbar?.setLabelFormatter {
            return@setLabelFormatter "${it/10.0}"
        }

        maxTokens?.setText(preferences.getMaxTokens().toString())
        endSeparator?.setText(preferences.getEndSeparator())
        prefix?.setText(preferences.getPrefix())

        gpt_35_turbo?.setOnClickListener {
            model = "gpt-3.5-turbo"
            clearSelection()
            gpt_35_turbo?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_35_turbo?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftFrame?.visibility = View.GONE
            validateForm()
        }

        o3_mini?.setOnClickListener {
            model = "o3-mini"
            clearSelection()
            o3_mini?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            o3_mini?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftFrame?.visibility = View.GONE
            validateForm()
        }

        o3?.setOnClickListener {
            model = "o3"
            clearSelection()
            o3?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            o3?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftFrame?.visibility = View.GONE
            validateForm()
        }

        o1_mini?.setOnClickListener {
            model = "o1-mini"
            clearSelection()
            o1_mini?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            o1_mini?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftFrame?.visibility = View.GONE
            validateForm()
        }

        o1?.setOnClickListener {
            model = "o1"
            clearSelection()
            o1?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            o1?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftFrame?.visibility = View.GONE
            validateForm()
        }

        gpt_4?.setOnClickListener {
            model = "gpt-4"
            clearSelection()
            gpt_4?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_4?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftFrame?.visibility = View.GONE
            validateForm()
        }

        gpt_4_turbo?.setOnClickListener {
            model = "gpt-4-turbo-preview"
            clearSelection()
            gpt_4_turbo?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_4_turbo?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftFrame?.visibility = View.GONE
            validateForm()
        }

        gpt_4_o?.setOnClickListener {
            model = "gpt-4o"
            clearSelection()
            gpt_4_o?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            gpt_4_o?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftFrame?.visibility = View.GONE
            validateForm()
        }

        ft?.setOnClickListener {
            model = ftInput?.text.toString()
            clearSelection()
            ft?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
            ft?.background = getDarkAccentDrawableV2(
                ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
            ftFrame?.visibility = View.VISIBLE
        }

        see_all_models?.setOnClickListener {
            val advancedModelSelectorDialogFragment = AdvancedModelSelectorDialogFragment.newInstance(model, requireArguments().getString("chatId").toString())
            advancedModelSelectorDialogFragment.setModelSelectedListener { model ->
                this@AdvancedSettingsDialogFragment.model = model

                reloadModelList(model)
                validateForm()
            }
            advancedModelSelectorDialogFragment.show(requireActivity().supportFragmentManager, "advancedModelSelectorDialogFragment")
        }

        see_favorite_models?.setOnClickListener {
            val advancedModelSelectorDialogFragment = AdvancedFavoriteModelSelectorDialogFragment.newInstance(model, requireArguments().getString("chatId").toString())
            advancedModelSelectorDialogFragment.setModelSelectedListener { model ->
                this@AdvancedSettingsDialogFragment.model = model

                reloadModelList(model)
                validateForm()
            }
            advancedModelSelectorDialogFragment.show(requireActivity().supportFragmentManager, "advancedFavoriteModelSelectorDialogFragment")
        }

        ftInput?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                /* unused */
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                model = s.toString()
            }

            override fun afterTextChanged(s: Editable?) {
                /* unused */
            }
        })

        btnSave?.setOnClickListener {
            validateForm()
            Toast.makeText(requireActivity(), "Settings saved", Toast.LENGTH_SHORT).show()
        }

        btnCancel?.setOnClickListener {
            dismiss()
        }

        model = requireArguments().getString("name").toString()
        reloadModelList(model)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.ThemeOverlay_App_BottomSheetDialog)
    }

    private fun reloadModelList(model: String) {
        when (model) { // load default model if settings not found
            "gpt-3.5-turbo" -> {
                gpt_35_turbo?.isChecked = true
                clearSelection()
                gpt_35_turbo?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_35_turbo?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftFrame?.visibility = View.GONE
            }
            "o3-mini" -> {
                o3_mini?.isChecked = true
                clearSelection()
                o3_mini?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                o3_mini?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftFrame?.visibility = View.GONE
            }
            "o3" -> {
                o3?.isChecked = true
                clearSelection()
                o3?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                o3?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftFrame?.visibility = View.GONE
            }
            "o1-mini" -> {
                o1_mini?.isChecked = true
                clearSelection()
                o1_mini?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                o1_mini?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftFrame?.visibility = View.GONE
            }
            "o1" -> {
                o1?.isChecked = true
                clearSelection()
                o1?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                o1?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftFrame?.visibility = View.GONE
            }
            "gpt-4" -> {
                gpt_4?.isChecked = true
                clearSelection()
                gpt_4?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_4?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftFrame?.visibility = View.GONE
            }
            "gpt-4-turbo-preview" -> {
                gpt_4_turbo?.isChecked = true
                clearSelection()
                gpt_4_turbo?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_4_turbo?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftFrame?.visibility = View.GONE
            }
            "gpt-4o" -> {
                gpt_4_o?.isChecked = true
                clearSelection()
                gpt_4_o?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                gpt_4_o?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftFrame?.visibility = View.GONE
            }
            else -> {
                ft?.isChecked = true
                ftInput?.setText(model)
                clearSelection()
                ft?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.window_background))
                ft?.background = getDarkAccentDrawableV2(
                    ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v4)!!)
                ftFrame?.visibility = View.VISIBLE
            }
        }
    }

    private fun clearSelection() {
        see_all_models?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        see_all_models?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        see_favorite_models?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        see_favorite_models?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        gpt_35_turbo?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_35_turbo?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        o3_mini?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        o3_mini?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        o3?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        o3?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        o1_mini?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        o1_mini?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        o1?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        o1?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        gpt_4?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_4?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        gpt_4_turbo?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_4_turbo?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        gpt_4_o?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        gpt_4_o?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))

        ft?.background = getDarkAccentDrawable(
            ContextCompat.getDrawable(requireActivity(), R.drawable.btn_accent_tonal_selector_v3)!!, requireActivity())
        ft?.setTextColor(ContextCompat.getColor(requireActivity(), R.color.neutral_200))
    }

    private fun getDarkAccentDrawable(drawable: Drawable, context: Context) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColor(context))
        return drawable
    }

    private fun getDarkAccentDrawableV2(drawable: Drawable) : Drawable {
        DrawableCompat.setTint(DrawableCompat.wrap(drawable), getSurfaceColorV2())
        return drawable
    }

    private fun getSurfaceColor(context: Context) : Int {
        return context.getColor(android.R.color.transparent)
    }

    private fun getSurfaceColorV2() : Int {
        return requireActivity().getColor(R.color.accent_900)
    }

    private fun validateForm() {
        if (ftInput?.text.toString() == "" && ft?.isChecked == true) {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
            return
        }

        if (maxTokens?.text.toString() == "") {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
            return
        }

        if (maxTokens?.text.toString().toInt() > 8192 && model.contains("gpt-4")) {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
            return
        }

        if (maxTokens?.text.toString().toInt() > 2048 && !model.contains("gpt-4")) {
            listener!!.onFormError(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
            return
        }

        listener!!.onSelected(model, maxTokens?.text.toString(), endSeparator?.text.toString(), prefix?.text.toString())
    }

    fun setStateChangedListener(listener: StateChangesListener) {
        this.listener = listener
    }

    interface StateChangesListener {
        fun onSelected(name: String, maxTokens: String, endSeparator: String, prefix: String)
        fun onFormError(name: String, maxTokens: String, endSeparator: String, prefix: String)
    }
}
