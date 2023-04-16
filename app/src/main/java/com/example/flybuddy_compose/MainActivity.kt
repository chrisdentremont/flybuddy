package com.example.flybuddy_compose
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.*
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.example.flybuddy_compose.ui.theme.FlyBuddy_ComposeTheme
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

/**
 * Used to store the name of every major airline
 * so that the user can filter from the list.
 */
var airlineList: List<String> = listOf("")

/**
 * Used to store the currently selected flight
 * on the home page so that the correct
 * information can be displayed.
 */
var flightData: List<Result>? = null

/**
 * An object to store flights that are currently saved by the
 * user. Persists across different tabs so that the saved flights
 * can be displayed on the home tab.
 */
object SelectedFlights{
    val list: SnapshotStateList<Result> = mutableStateListOf()
}

/**
 * A class to store pagination information from the API request.
 * Holds context info about retrieved flights such as flights
 * returned and the requested limit.
 */
data class Pagination(
    val limit: Int,
    val offset: Int,
    val count: Int,
    val total: Int
)

/**
 * A class to store all information held in a returned flight
 * object. Has sub-classes that store the included fields.
 */
data class Result(
    val flight_date: String,
    val flight_status: String,
    val airline: Airline,
    val flight: Flight,
    val live: Live,
    val departure: DepArr,
    val arrival: DepArr,
)

/**
 * A sub-class that stores flight information regarding arrivals
 * and departures such as timezone and airport information.
 */
data class DepArr(
    val airport: String,
    val timezone: String,
    val terminal: String,
    val estimated: String,
)

/**
 * A sub-class that stores information regarding a flight's
 * current position that includes latitude and longitude
 * and more.
 */
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

/**
 * A sub-class that contains information about the airline
 * linked to a specific flight.
 */
data class Airline(
    val name: String,
    val iata: String,
    val icao: String
)

/**
 * A sub-class that contains information about a flight's
 * identification.
 */
data class Flight(
    val number: String,
    val iata: String,
    val icao: String
)

/**
 * The main class that is returned from the flight API request.
 * Contains the pagination class that contains context information,
 * and the data class that contains the actual flight information.
 */
data class FlightList(
    val pagination: Pagination,
    val data: List<Result>
)

var numberToDisplay = 10
var flightStatus: String? = null
var airlineName: String? = null
var flightNumber: String? = null

/**
 * The interface used to make requests to the aviationstack API.
 * Accepts optional queries such as an airline name or flight number
 * that can be used to filter requested flights.
 */
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

/**
 * A Retrofit helper object that is used to make an API request with
 * a given request link and other information.
 */
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

/**
 * The method that is called directly from the application. This method
 * collects any user-entered query and interacts with the Retrofit
 * helper object to make a request to the aviationstack API.
 */
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

/**
 * The initialization class of the application. The tabs are created,
 * and the list of airlines is read from an existing file.
 */
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

/**
 * The class that contains information about every existing page
 * within the application.
 */
sealed class Screen(val route: String, @StringRes val resourceId: Int) {
    object Home : Screen("home", R.string.home_name)
    object Search : Screen("flightSearch", R.string.search_name)
    object Friends : Screen("friends", R.string.friends_name)
    object Settings : Screen("settings", R.string.settings_name)
}


/**
 * The composable function that represents the tabs at the bottom
 * of the screen and provides functionality to them.
 */
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

/**
 * Font families that are used within the application.
 */
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

/**
 * The composable function that represents the home page. Handles displaying
 * the selected flight list.
 */
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

/**
 * The composable function for displaying the friends page. Currently a
 * mockup of what the friends page would actually look like if
 * properly implemented.
 */
@Composable
fun Friends(){
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .fillMaxWidth()
            .padding(all = 20.dp)
            .verticalScroll(rememberScrollState())
    ){
        Text(
            text = "Friends",
            fontSize = 30.sp,
            fontFamily = poppinsFamily,
            fontWeight = FontWeight.Normal,
            modifier = Modifier.padding(bottom = 20.dp),
        )

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ){
            Column(){
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    elevation = 10.dp,
                ){
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter("https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/f19a8dbd-b9e4-4ce8-9726-5a1412453ce7/dabkw0g-5004586b-8f46-40df-ae0e-9baef2aa6929.png?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7InBhdGgiOiJcL2ZcL2YxOWE4ZGJkLWI5ZTQtNGNlOC05NzI2LTVhMTQxMjQ1M2NlN1wvZGFia3cwZy01MDA0NTg2Yi04ZjQ2LTQwZGYtYWUwZS05YmFlZjJhYTY5MjkucG5nIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmZpbGUuZG93bmxvYWQiXX0.22JifnHYgz5tBl9jIweAjmPxLjujBdDl-rVYBjR1p14"),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(75.dp)
                                    .clip(CircleShape)
                                    .border(5.dp, Color.White, CircleShape)
                            )
                            Text(
                                "Chris D'Entremont",
                                fontSize = 20.sp,
                                fontFamily = poppinsFamily,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                            Button(
                                onClick = { /*TODO*/ },
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(width = 50.dp, height = 50.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete")
                            }
                        }
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ){
            Column(){
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    elevation = 10.dp,
                ){
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter("https://pm1.narvii.com/6345/ed3b88240fbbf8b1b89a2d29980a19d6f8834b1a_hq.jpg"),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(75.dp)
                                    .clip(CircleShape)
                                    .border(5.dp, Color.White, CircleShape)
                            )
                            Text(
                                "Gabriel Madeira",
                                fontSize = 20.sp,
                                fontFamily = poppinsFamily,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                            Button(
                                onClick = { /*TODO*/ },
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(width = 50.dp, height = 50.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete")
                            }
                        }
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ){
            Column(){
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    elevation = 10.dp,
                ){
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter("https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/f19a8dbd-b9e4-4ce8-9726-5a1412453ce7/dabldy4-9446bc17-de1f-4429-bb33-8f2762c8e7ac.png?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7InBhdGgiOiJcL2ZcL2YxOWE4ZGJkLWI5ZTQtNGNlOC05NzI2LTVhMTQxMjQ1M2NlN1wvZGFibGR5NC05NDQ2YmMxNy1kZTFmLTQ0MjktYmIzMy04ZjI3NjJjOGU3YWMucG5nIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmZpbGUuZG93bmxvYWQiXX0.wbNGcuCGuxmYCndImvx3gyEb4FZ9dE0Mv8IlBhzEy-4"),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(75.dp)
                                    .clip(CircleShape)
                                    .border(5.dp, Color.White, CircleShape)
                            )
                            Text(
                                "Mo Merchant",
                                fontSize = 20.sp,
                                fontFamily = poppinsFamily,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                            Button(
                                onClick = { /*TODO*/ },
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(width = 50.dp, height = 50.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete")
                            }
                        }
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)
        ){
            Column(){
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    elevation = 10.dp,
                ){
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Start,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ) {
                            Image(
                                painter = rememberAsyncImagePainter("https://images-wixmp-ed30a86b8c4ca887773594c2.wixmp.com/f/f19a8dbd-b9e4-4ce8-9726-5a1412453ce7/dabheg6-f7233d4d-8ef7-4611-bed8-b65ad9b93060.png?token=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1cm46YXBwOjdlMGQxODg5ODIyNjQzNzNhNWYwZDQxNWVhMGQyNmUwIiwiaXNzIjoidXJuOmFwcDo3ZTBkMTg4OTgyMjY0MzczYTVmMGQ0MTVlYTBkMjZlMCIsIm9iaiI6W1t7InBhdGgiOiJcL2ZcL2YxOWE4ZGJkLWI5ZTQtNGNlOC05NzI2LTVhMTQxMjQ1M2NlN1wvZGFiaGVnNi1mNzIzM2Q0ZC04ZWY3LTQ2MTEtYmVkOC1iNjVhZDliOTMwNjAucG5nIn1dXSwiYXVkIjpbInVybjpzZXJ2aWNlOmZpbGUuZG93bmxvYWQiXX0.D_drECvO2Lur5t0MrGn5HpHHWSwaTmr5Ik_kbu9pmqY"),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(75.dp)
                                    .clip(CircleShape)
                                    .border(5.dp, Color.White, CircleShape)
                            )
                            Text(
                                "Ayla Hebert",
                                fontSize = 20.sp,
                                fontFamily = poppinsFamily,
                                fontWeight = FontWeight.Normal,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                            Button(
                                onClick = { /*TODO*/ },
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .size(width = 50.dp, height = 50.dp)
                            ) {
                                Icon(imageVector = Icons.Filled.Delete, contentDescription = "delete")
                            }
                        }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 10.dp)){
            Column() {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(15.dp),
                    elevation = 10.dp,
                ){
                    Column(
                        modifier = Modifier.padding(5.dp)
                    ){
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(5.dp)
                        ) {
                            Button(
                                onClick = { /*TODO*/ },
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    text = "Add a new friend", fontSize = 20.sp,
                                    fontFamily = poppinsFamily,
                                    fontWeight = FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(start = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * The composable function for displaying the flight search page.
 * Handles input from the user regarding search filtering, and
 * makes the API request when the user submits their information.
 */
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

/**
 * The composable function for displaying the settings page. Currently
 * a mockup of what the settings page would actually look like if
 * properly implemented.
 */
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