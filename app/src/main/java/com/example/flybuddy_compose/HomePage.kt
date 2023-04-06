package com.example.flybuddy_compose

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.selects.select
import java.util.*

@Composable
fun HomeCards(){
    val context = LocalContext.current
    var flightList: SnapshotStateList<Result> by remember { mutableStateOf(SelectedFlights.list) }
    var selectedFlight: Result by remember { mutableStateOf(Result("", "",
        Airline("", "", ""), Flight("", "", ""),
        Live("", 0f, 0f, 0f, 0f, 0f, 0f, false),
        DepArr("", "", "", ""), DepArr("", "", "", ""))) }
    var showFlightBox by remember { mutableStateOf(false) }

    flightList.forEach { flight ->
        var flightNumber = flight.flight.number
        if(flightNumber == null){
            flightNumber = "-"
        }

        var flightAirline = flight.airline.name
        if(flightAirline == null){
            flightAirline = "N/A"
        }

        var flightDepAirport = flight.departure.airport
        if(flightDepAirport == null){
            flightDepAirport = "N/A"
        }else if(flightDepAirport.contains("/")){
            flightDepAirport = flightDepAirport.substringAfterLast("/")
        }

        var flightArrAirport = flight.arrival.airport
        if(flightArrAirport == null){
            flightArrAirport = "N/A"
        }else if(flightArrAirport.contains("/")){
            flightArrAirport = flightArrAirport.substringAfterLast("/")
        }

        var flightStatus = flight.flight_status
        if(flightStatus == null){
            flightStatus = "N/A"
        }else{
            flightStatus = flightStatus.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(15.dp)
                .clickable {
                    selectedFlight = flight
                    showFlightBox = true
                },
            elevation = 10.dp,
        ) {
            Column(
                modifier = Modifier.padding(15.dp)
            ) {
                Text("Flight $flightNumber", fontSize = 20.sp, fontFamily = poppinsFamily, fontWeight = FontWeight.Normal)
                Text("Airline: $flightAirline", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                Text("Departing: $flightDepAirport", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                Text("Arrving: $flightArrAirport", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                Text("Status: $flightStatus", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ){
                    Button(
                        onClick = {
                            flightList.remove(flight)
                            val flightNumber = flight.flight.number
                            showDialog(context, "Flight $flightNumber has been deleted.")
                        },
                    ){
                        Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete")
                    }
                }
            }
        }
    }

    if(showFlightBox){
        AlertDialog(
            onDismissRequest = {
                showFlightBox = false
            },
            title = {
                var flightName = selectedFlight.flight.number
                Text("Flight $flightName")
            },
            text = {
                var flightAirline = selectedFlight.airline.name
                Text("Airline: $flightAirline")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showFlightBox = false
                    },
                ){
                    Icon(imageVector = Icons.Filled.Close, contentDescription = "close")
                }
            }
        )
    }
}