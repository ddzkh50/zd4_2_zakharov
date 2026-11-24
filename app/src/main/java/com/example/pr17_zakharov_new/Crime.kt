package com.example.pr17_zakharov_new

import java.util.Date
import java.util.UUID

class Crime(
    val id: UUID = UUID.randomUUID(),
    var title: String = "",
    var date: Date = Date(),
    var isSolved: Boolean = false,
    var suspect: String = "",
    var suspectPhone: String = ""
)
