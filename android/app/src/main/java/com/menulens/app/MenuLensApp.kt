package com.menulens.app

import androidx.compose.runtime.Composable
import com.menulens.app.navigation.AppNavGraph
import com.menulens.app.ui.theme.MenuLensTheme

@Composable
fun MenuLensApp() {
    MenuLensTheme {
        AppNavGraph()
    }
}
