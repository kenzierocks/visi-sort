package me.kenzierocks.visisort.math

import com.flowpowered.math.TrigMath
import com.flowpowered.math.vector.Vector2f

fun Vector2f.angle() = TrigMath.atan2(y.toDouble(), x.toDouble())