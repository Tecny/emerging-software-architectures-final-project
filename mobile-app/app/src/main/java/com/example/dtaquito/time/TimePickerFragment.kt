package com.example.dtaquito.time

import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import com.example.dtaquito.R
import java.util.Calendar
import java.util.Locale

class TimePickerFragment(val listener: (String) -> Unit): DialogFragment(),TimePickerDialog.OnTimeSetListener {
    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val formattedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute)
        listener(formattedTime)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)
        val dialog = TimePickerDialog(activity as Context,
            R.style.datePickerTheme, this, hour, minute, false)

        return dialog
    }
}
