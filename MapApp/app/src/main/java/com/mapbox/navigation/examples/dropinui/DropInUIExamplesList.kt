package com.mapbox.navigation.examples.dropinui

import android.content.Context
import androidx.core.content.ContextCompat
import com.mapbox.navigation.examples.MapboxExample
import com.mapbox.navigation.examples.R
import com.mapbox.navigation.examples.dropinui.basic.NavigationViewActivity

fun Context.examplesList() = listOf(
    MapboxExample(
        ContextCompat.getDrawable(this, R.drawable.mapbox_screenshot_navigation_view),
        getString(R.string.title_navigation_view),
        getString(R.string.description_navigation_view),
        NavigationViewActivity::class.java
    ),
)
