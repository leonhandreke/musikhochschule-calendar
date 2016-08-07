package com.github.leonhandreke.musikhochschulecalendar

import java.time.LocalDateTime

class Event(val title: String, val location: String, val startDate: LocalDateTime) {

  override def toString = s"Event(title=$title, location=$location, startDate=$startDate)"
}
