package com.example.flybuddy_compose
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
import androidx.lifecycle.viewmodel.ViewModelFactoryDsl
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
var flightData: List<Result>? = null

object SelectedFlights{
    val list: SnapshotStateList<Result> = mutableStateListOf()
}

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
    val live: Live,
    val departure: DepArr,
    val arrival: DepArr,
)

data class DepArr(
    val airport: String,
    val timezone: String,
    val terminal: String,
    val estimated: String,
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
            "65dac06ed42005ac71d1adb5f0c04e48",
            numberToDisplay,
            flightStatus,
            airlineName,
            flightNumber
        ).body()
    }

    flightCall.await()
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
    Column(
        modifier = Modifier.verticalScroll(rememberScrollState())
    ){
        Text(
            modifier = Modifier.padding(24.dp),
            fontSize = 30.sp,
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Normal,
            text = "Your Flights")

        HomeCards()

        if(SelectedFlights.list.isEmpty()){
            Text(
                modifier = Modifier.padding(24.dp),
                fontSize = 20.sp,
                fontFamily = nunitoFamily,
                fontWeight = FontWeight.Normal,
                text = "You don't have any flights added - search for some!")
        }
    }
}

@Composable
fun Friends(){
    Text(text = "This is the friends page")
}


@Composable
fun FlightSearch(){
    Column(
        modifier = Modifier
            .fillMaxHeight()
    ){

        SearchHeader()

        AirlineSearch()

        StatusSearch()

        NumberSearch()

        FlightResults()
    }
}

@Composable
fun Settings(){
    var requestsFromAnyone by remember { mutableStateOf(false) }
    var visibleFlights by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(all = 20.dp)
    ){
        Text(
            text = "Settings",
            fontSize = 30.sp,
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 20.dp)
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ){
            Column(){
                Text(
                    text = "Allow requests from anyone",
                    fontSize = 18.sp,
                    fontFamily = poppinsFamily,
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    text = "Anyone will be able to send you a friend request.",
                    fontSize = 12.sp,
                    fontFamily = nunitoFamily,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.widthIn(0.dp, 300.dp)
                )
            }
            Switch(checked = requestsFromAnyone, onCheckedChange = {requestsFromAnyone = it})
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 30.dp)
        ){
            Column(){
                Text(
                    text = "Flight visibility",
                    fontSize = 18.sp,
                    fontFamily = poppinsFamily,
                    fontWeight = FontWeight.Normal,
                )
                Text(
                    text = "Anyone can see your flights if checked, otherwise only friends can see them.",
                    fontSize = 12.sp,
                    fontFamily = nunitoFamily,
                    fontWeight = FontWeight.Normal,
                    modifier = Modifier.widthIn(0.dp, 300.dp)
                )
            }
            Switch(checked = visibleFlights, onCheckedChange = {visibleFlights = it})
        }

        Divider(modifier = Modifier.padding(bottom = 30.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ){
            Button(
                onClick = {},
                modifier = Modifier.padding(end = 10.dp)
            ){
                Text(text = "Change Email")
            }
            Button(
                onClick = {}
            ){
                Text(text = "Change Password")
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ){
            Button(
                colors = ButtonDefaults.buttonColors(backgroundColor = Color.Red),
                onClick = {}
            ){
                Text(text = "Delete Account", color = Color.White)
            }
        }
    }
}