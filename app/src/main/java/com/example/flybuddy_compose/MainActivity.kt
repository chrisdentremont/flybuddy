package com.example.flybuddy_compose
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.flybuddy_compose.ui.theme.FlyBuddy_ComposeTheme
import com.example.flybuddy_compose.ui.theme.LightBlue
import com.kanyidev.searchable_dropdown.SearchableExpandedDropDownMenu
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.*

var airlineList: List<String> = listOf("")

data class Pagination(
    val limit: Int,
    val offset: Int,
    val count: Int,
    val total: Int
)

data class Result(
    val flight_date: String,
    val flight_status: String,
    val airline: Airline,
    val flight: Flight,
    val live: Live
)

data class Live(
    val updated: String,
    val latitude: Float,
    val longitude: Float,
    val altitude: Float,
    val direction: Float,
    val speed_horizontal: Float,
    val speed_vertical: Float,
    val is_ground: Boolean
)

data class Airline(
    val name: String,
    val iata: String,
    val icao: String
)

data class Flight(
    val number: String,
    val iata: String,
    val icao: String
)

data class FlightList(
    val pagination: Pagination,
    val data: List<Result>
)

var numberToDisplay = 10
var flightStatus: String? = null
var airlineName: String? = null
var flightNumber: String? = null

interface AirlineApi {
    @GET("/v1/flights")
    suspend fun getFlights(
        @Query("access_key") apiKey: String?,
        @Query("limit") limit: Int?,
        @Query("flight_status") status: String?,
        @Query("airline_name") airline: String?,
        @Query("flight_number") flightNumber: String?,
    ) : Response<FlightList>
}

object RetrofitHelper {
    var interceptor = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    var client = OkHttpClient().newBuilder().addInterceptor(interceptor).build()

    fun getInstance(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://api.aviationstack.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()
    }
}

suspend fun callFlightApi(): FlightList? {
    val airlineApi = RetrofitHelper.getInstance().create(AirlineApi::class.java)
    var result: FlightList? = null;

    // launching a new coroutine
    val flightCall = GlobalScope.async {
        result = airlineApi.getFlights(
            "4477ae85a5e57781069e0b9969e5bf9e",
            numberToDisplay,
            flightStatus,
            airlineName,
            flightNumber
        ).body()
    }

    flightCall.await()
    Log.d("flight result", result.toString())
    return result
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        airlineList = assets.open("airlines.txt").bufferedReader().use() {it.readLines()}

        setContent {
            FlyBuddy_ComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    //Actual app content
                    NavBar()
                }
            }
        }
    }
}

sealed class Screen(val route: String, @StringRes val resourceId: Int) {
    object Home : Screen("home", R.string.home_name)
    object Search : Screen("flightSearch", R.string.search_name)
    object Friends : Screen("friends", R.string.friends_name)
    object Settings : Screen("settings", R.string.settings_name)
}


@Preview(showBackground = true)
@Composable
fun NavBar(){
    val routeMap = mapOf(
        Screen.Home to Icons.Filled.Home,
        Screen.Friends to Icons.Filled.Person,
        Screen.Search to Icons.Filled.Search,
        Screen.Settings to Icons.Filled.Settings
    )

    val navController = rememberNavController()

    Scaffold(
        bottomBar = {
            BottomNavigation {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination
                routeMap.forEach { (key, value) ->
                    BottomNavigationItem(
                        icon = {Icon(value, contentDescription = null)},
                        label = { Text(stringResource(key.resourceId)) },
                        selected = currentDestination?.hierarchy?.any { it.route == key.route } == true,
                        onClick = {
                            navController.navigate(key.route) {
                                // Pop up to the start destination of the graph to
                                // avoid building up a large stack of destinations
                                // on the back stack as users select items
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                // Avoid multiple copies of the same destination when
                                // reselecting the same item
                                launchSingleTop = true
                                // Restore state when reselecting a previously selected item
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) {
        innerPadding ->
        NavHost(navController, startDestination = Screen.Home.route, Modifier.padding(innerPadding)) {
            composable(Screen.Home.route) {Home()}
            composable(Screen.Friends.route) {Friends()}
            composable(Screen.Search.route) { FlightSearch() }
            composable(Screen.Settings.route) { Settings() }
        }
    }



}

val poppinsFamily = FontFamily(
    Font(R.font.poppins_regular, FontWeight.Normal),
    Font(R.font.poppins_bold, FontWeight.Bold),
    Font(R.font.poppins_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.poppins_light, FontWeight.Light),
)

val nunitoFamily = FontFamily(
    Font(R.font.nunito_regular, FontWeight.Normal),
    Font(R.font.nunito_bold, FontWeight.Bold),
    Font(R.font.nunito_italic, FontWeight.Normal, FontStyle.Italic),
    Font(R.font.nunito_light, FontWeight.Light)
)

@Composable
fun Home(){
    Text(
        modifier = Modifier.padding(24.dp),
        fontSize = 30.sp,
        fontFamily = poppinsFamily,
        fontWeight = FontWeight.Normal,
        text = "Card Content")
}

@Composable
fun Friends(){
    Text(text = "This is the friends page")
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun FlightSearch(){
    var text by remember {mutableStateOf("")}
    var statusExpanded by remember { mutableStateOf(false) }

    val statusList: List<String> = listOf("Scheduled", "Active", "Landed", "Cancelled", "Incident", "Diverted")

    val context = LocalContext.current
    val year: Int
    val month: Int
    val day: Int

    val calendar = Calendar.getInstance()
    year = calendar.get(Calendar.YEAR)
    month = calendar.get(Calendar.MONTH)
    day = calendar.get(Calendar.DAY_OF_MONTH)
    calendar.time = Date()


    Column{

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
                placeholder = {Text(text = "Enter a flight number...", style = TextStyle(color = Color.Gray))},
                label = {Text(text = "Flight #")},
            )


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
            horizontalArrangement = Arrangement.SpaceEvenly
        ){
            Button(
                onClick = {
                          Log.d("flight search info", listOf(numberToDisplay, flightNumber, flightStatus, airlineName).toString())
                          val result = GlobalScope.async {
                              callFlightApi()
                          }
                },
                colors = ButtonDefaults.buttonColors(backgroundColor = LightBlue)
            ){
                Icon(imageVector = Icons.Default.Search, contentDescription = "searchIcon", modifier = Modifier.padding(end=5.dp))
                Text(text = "Search")
            }
        }
    }
}

@Composable
fun Settings(){
    Text(text = "This is the settings page")
}