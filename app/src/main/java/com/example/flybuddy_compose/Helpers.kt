package com.example.flybuddy_compose

import android.content.Context
import android.widget.Toast

fun showDialog(context: Context, message: String){
    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
}