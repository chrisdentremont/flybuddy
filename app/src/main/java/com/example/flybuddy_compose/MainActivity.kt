package com.example.flybuddy_compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

@Composable
fun FlightSearch(){
    Text(text = "This is the flights page")
}

@Composable
fun Settings(){
    Text(text = "This is the settings page")
}