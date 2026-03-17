package ca.uwaterloo.helloasl.data.starRepository

import ca.uwaterloo.helloasl.domain.starModel.StarItem

interface StarRepository {
    fun getStarredItems(): List<StarItem>
    fun removeStar(itemId: String)
}