package com.example.flybuddy_compose

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.flybuddy_compose.ui.theme.LightBlue
import com.kanyidev.searchable_dropdown.SearchableExpandedDropDownMenu
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import java.util.*

@Composable
fun SearchHeader(){
    Row(
        modifier = Modifier
            .padding(
                top = 50.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 20.dp
            )
            .width(400.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ){
        Text("Search for a flight", fontSize = 30.sp, fontFamily = poppinsFamily, fontWeight = FontWeight.Normal)
    }
}

@Composable
fun AirlineSearch(){
    Row(
        modifier = Modifier
            .padding(
                top = 0.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 20.dp
            )
            .width(400.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ){
        SearchableExpandedDropDownMenu(
            listOfItems = airlineList,
            onDropDownItemSelected = { item ->
                airlineName = item
            },
            placeholder = "Airline",
            openedIcon = Icons.Default.ArrowForward,
            closedIcon = Icons.Default.ArrowDropDown,
            parentTextFieldCornerRadius = 0.dp
        )
    }
}

@Composable
fun StatusSearch(){
    val statusList: List<String> = listOf("Scheduled", "Active", "Landed", "Cancelled", "Incident", "Diverted")

    Row(
        modifier = Modifier
            .padding(
                top = 0.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 10.dp
            )
            .width(400.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ){
        SearchableExpandedDropDownMenu(
            listOfItems = statusList,
            onDropDownItemSelected = { item ->
                flightStatus = item.lowercase()
            },
            placeholder = "Status",
            openedIcon = Icons.Default.ArrowForward,
            closedIcon = Icons.Default.ArrowDropDown,
            parentTextFieldCornerRadius = 0.dp
        )
    }
}

@Composable
fun NumberSearch(){
    var text by remember { mutableStateOf("") }
    Row(
        modifier = Modifier
            .padding(
                top = 0.dp,
                start = 20.dp,
                end = 20.dp,
                bottom = 20.dp
            )
            .width(400.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = text,
            onValueChange = {
                text = it
                flightNumber = it
            },
            textStyle = TextStyle(
                fontFamily = poppinsFamily,
                fontWeight = FontWeight.Normal
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            placeholder = { Text(text = "Enter a flight number...", style = TextStyle(color = Color.Gray)) },
            label = { Text(text = "Flight #") },
        )


    }
}

@Composable
fun FlightResults(){
    val context = LocalContext.current
    var flightsExist by remember { mutableStateOf(false) }
    var showLoading by remember { mutableStateOf(false) }

    Column(modifier = Modifier.verticalScroll(rememberScrollState())){
        Row(
            modifier = Modifier
                .padding(
                    top = 0.dp,
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            Button(
                onClick = {
                    val result = GlobalScope.async {
                        flightsExist = false
                        showLoading = true
                        val flightList = callFlightApi()
                        if (flightList != null) {
                            flightData = flightList.data
                            flightsExist = true
                        }
                        showLoading = false
                    }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue)
            ){
                Icon(imageVector = Icons.Default.Search, contentDescription = "searchIcon", modifier = Modifier.padding(end=5.dp))
                Text(text = "Search")
            }
        }

        Row(
            modifier = Modifier
                .padding(
                    top = 0.dp,
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){
            if(showLoading){
                CircularProgressIndicator()
            }
        }

        Row(
            modifier = Modifier
                .padding(
                    top = 10.dp,
                    start = 20.dp,
                    end = 20.dp,
                    bottom = 20.dp
                )
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Column(

            ){
                if(flightsExist){
                    flightData?.forEach { flight ->
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
                                .padding(15.dp),
                            elevation = 10.dp
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
                                            if(SelectedFlights.list.contains(flight)){
                                                showDialog(context, "You are already tracking this flight!")
                                            }else{
                                                SelectedFlights.list.add(flight)
                                                showDialog(context, "Flight $flightNumber has been added to your list.")
                                            }
                                        },
                                    ){
                                        Icon(imageVector = Icons.Default.Add, contentDescription = "hdrPlus", modifier = Modifier.padding(end=5.dp))
                                        Text(text = "Add Flight")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }


}
