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

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import org.teslasoft.assistant.lite.R
import org.teslasoft.assistant.preferences.dto.ApiEndpointObject
import org.teslasoft.assistant.util.Hash

class EditApiEndpointDialogFragment : DialogFragment() {
    companion object {
        fun newInstance(label: String, host: String, apiKey: String, position: Int) : EditApiEndpointDialogFragment {
            val editApiEndpointDialogFragment = EditApiEndpointDialogFragment()

            val args = Bundle()
            args.putString("label", label)
            args.putString("host", host)
            args.putString("apiKey", apiKey)
            args.putInt("position", position)

            editApiEndpointDialogFragment.arguments = args

            return editApiEndpointDialogFragment
        }
    }

    private var textDialogTitle: TextView? = null
    private var fieldLabel: TextInputEditText? = null
    private var fieldHost: TextInputEditText? = null
    private var fieldApiKey: TextInputEditText? = null
    private var apiNote: TextView? = null

    private var builder: AlertDialog.Builder? = null

    private var listener: StateChangesListener? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_edit_api_endpoint, container, false)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = MaterialAlertDialogBuilder(this.requireContext(), R.style.App_MaterialAlertDialog)

        val view: View = this.layoutInflater.inflate(R.layout.fragment_edit_api_endpoint, null)

        textDialogTitle = view.findViewById(R.id.text_dialog_title)
        fieldLabel = view.findViewById(R.id.field_label)
        fieldHost = view.findViewById(R.id.field_host)
        fieldApiKey = view.findViewById(R.id.field_api_key)
        apiNote = view.findViewById(R.id.api_note)

        fieldLabel?.setText(requireArguments().getString("label"))
        fieldHost?.setText(requireArguments().getString("host"))

        if (requireArguments().getInt("position") == -1) {
            textDialogTitle?.text = getString(R.string.label_add_api_endpoint)
        }

        builder!!.setView(view)
            .setCancelable(false)
            .setPositiveButton(R.string.btn_save) { _, _ -> validateForm() }
            .setNegativeButton(R.string.btn_cancel) { _, _ ->  }

        if (requireArguments().getInt("position") != -1) {
            builder!!.setNeutralButton(R.string.btn_delete) { _, _ -> run {
                MaterialAlertDialogBuilder(this.requireContext(), R.style.App_MaterialAlertDialog)
                    .setTitle(R.string.label_delete_api_endpoint)
                    .setMessage(R.string.message_delete_api_endpoint)
                    .setPositiveButton(R.string.yes) { _, _ -> listener!!.onDelete(requireArguments().getInt("position"), Hash.hash(requireArguments().getString("label")!!)) }
                    .setNegativeButton(R.string.no) { _, _ ->  listener!!.onCancel(requireArguments().getInt("position"))}
                    .show()
            }}
        }

        return builder!!.create()
    }

    private fun validateForm() {
        if (fieldLabel?.text.toString().isEmpty()) {
            listener!!.onError(getString(R.string.label_error_api_endpoint_empty), requireArguments().getInt("position"))
            return
        }

        if (fieldHost?.text.toString().isEmpty()) {
            listener!!.onError(getString(R.string.label_error_api_endpoint_empty), requireArguments().getInt("position"))
            return
        }

        if (fieldApiKey?.text.toString().isEmpty()) {
            fieldApiKey?.setText(requireArguments().getString("apiKey"))
        }

        if (requireArguments().getInt("position") == -1) {
            listener!!.onAdd(
                ApiEndpointObject(
                    fieldLabel?.text.toString(),
                    fieldHost?.text.toString(),
                    fieldApiKey?.text.toString()
                )
            )
        } else {
            listener!!.onEdit(
                requireArguments().getString("label")!!,
                ApiEndpointObject(
                    fieldLabel?.text.toString(),
                    fieldHost?.text.toString(),
                    fieldApiKey?.text.toString()
                ),
                requireArguments().getInt("position")
            )
        }
    }

    fun setListener(listener: StateChangesListener) {
        this.listener = listener
    }

    interface StateChangesListener {
        fun onAdd(apiEndpoint: ApiEndpointObject) { /* default */ }
        fun onEdit(oldLabel: String, apiEndpoint: ApiEndpointObject, position: Int) { /* default */ }
        fun onDelete(position: Int, id: String) { /* default */ }
        fun onError(message: String, position: Int) { /* default */ }
        fun onCancel(position: Int) { /* default */ }
    }
}
