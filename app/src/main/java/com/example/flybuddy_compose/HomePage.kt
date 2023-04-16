package com.example.flybuddy_compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.*

/**
 * The composable function for displaying flights on the home page.
 * Each flight from the saved flights list is displayed as a card
 * where the user can click to open a menu that displays information
 * about the flight.
 */
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
                Column(modifier = Modifier.padding(bottom = 5.dp)){
                    var flightName = if (selectedFlight.flight.number == null) "N/A" else selectedFlight.flight.number
                    var flightDate = selectedFlight.flight_date
                    Text("Flight $flightName", fontSize = 30.sp, fontFamily = poppinsFamily, fontWeight = FontWeight.Normal)
                    Text("$flightDate", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                }
            },
            text = {
                Column(){
                    var flightAirline = if (selectedFlight.airline.name == null) "N/A" else selectedFlight.airline.name
                    var flightAirlineIata = if (selectedFlight.airline.iata == null) "N/A" else selectedFlight.airline.iata
                    var flightAirlineIcao = if (selectedFlight.airline.icao == null) "N/A" else selectedFlight.airline.icao
                    Text("Airline", fontSize = 20.sp, fontFamily = poppinsFamily, fontWeight = FontWeight.Normal)
                    Text("Name: $flightAirline", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                    Text("IATA Code: $flightAirlineIata", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                    Text("ICAO Code: $flightAirlineIcao", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)

                    Divider(modifier = Modifier.padding(top = 5.dp, bottom = 5.dp))

                    var departureAirport = if (selectedFlight.departure.airport == null) "N/A" else selectedFlight.departure.airport
                    var departureEstimated = if (selectedFlight.departure.estimated == null) "N/A" else selectedFlight.departure.estimated
                    var departureTerminal = if (selectedFlight.departure.terminal == null) "N/A" else selectedFlight.departure.terminal
                    var departureZone = if (selectedFlight.departure.timezone == null) "N/A" else selectedFlight.departure.timezone
                    Text("Departure", fontSize = 20.sp, fontFamily = poppinsFamily, fontWeight = FontWeight.Normal)
                    Text("Airport: $departureAirport", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                    Text("Estimated Departure Time: $departureEstimated", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                    Text("Terminal: $departureTerminal", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                    Text("Time Zone: $departureZone", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)

                    Divider(modifier = Modifier.padding(top = 5.dp, bottom = 5.dp))

                    var arrivalAirport = if (selectedFlight.arrival.airport == null) "N/A" else selectedFlight.arrival.airport
                    var arrivalEstimated = if (selectedFlight.arrival.estimated == null) "N/A" else selectedFlight.arrival.estimated
                    var arrivalTerminal = if (selectedFlight.arrival.terminal == null) "N/A" else selectedFlight.arrival.terminal
                    var arrivalZone = if (selectedFlight.arrival.timezone == null) "N/A" else selectedFlight.arrival.timezone
                    Text("Arrival", fontSize = 20.sp, fontFamily = poppinsFamily, fontWeight = FontWeight.Normal)
                    Text("Airport: $arrivalAirport", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                    Text("Estimated Arrival Time: $arrivalEstimated", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                    Text("Terminal: $arrivalTerminal", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                    Text("Time Zone: $arrivalZone", fontSize = 15.sp, fontFamily = nunitoFamily, fontWeight = FontWeight.Normal)
                }
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