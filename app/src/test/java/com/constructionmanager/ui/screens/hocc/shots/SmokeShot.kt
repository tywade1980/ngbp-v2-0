package com.constructionmanager.ui.screens.hocc.shots

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import androidx.compose.material3.MaterialTheme
import com.constructionmanager.ui.screens.hocc.Board
import com.constructionmanager.ui.screens.hocc.Tile
import org.junit.Rule
import org.junit.Test

class SmokeShot {
    @get:Rule val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

    @Test fun tile() = paparazzi.snapshot {
        MaterialTheme(colorScheme = Board.colors, typography = Board.typography) { Tile("Contract", "$15,995.00") }
    }
}
