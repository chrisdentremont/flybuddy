package com.example.flybuddy_compose

import android.content.Context
import android.widget.Toast

/**
 * Helper function to display an alert dialog
 * with the given text.
 */
fun showDialog(context: Context, message: String){
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}