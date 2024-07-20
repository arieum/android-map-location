package campus.tech.kakao.map.data.db.entity

import androidx.annotation.DrawableRes

data class Place (
    val id: Int? = null,
    @DrawableRes val img: Int,
    val name: String,
    val location: String,
    val category: String,
    val x: String,
    val y: String
)