package com.github.leonhandreke.musikhochschulecalendar

import java.time.{LocalDate, LocalDateTime, ZoneId}

import org.jsoup.Jsoup
import org.scalatest.Matchers

import scala.collection.JavaConversions
import scala.io.Source

class MusikhochschuleCalendarTest extends org.scalatest.FunSuite with Matchers {

  test("parse example file") {
    val f = Source.fromURL(getClass.getResource("/musikhochschule-veranstaltungen-2016-08-07.html"), "ISO-8859-1")
    val document = try Jsoup.parse(f.mkString) finally f.close()

    val calendar = MusikhochschuleCalendar.getCalendarFromDocument(document)

    calendar.getNames.get(0).getValue shouldEqual ("Hochschule für Musik und Theater München")

    val events = JavaConversions.asScalaBuffer(calendar.getEvents)

    (events.head.getSummary.getValue) should equal ("Mittagsmusik der Klavierklasse Prof. Wolfram Schmitt-Leonardy")

    val startInstant = events.head.getDateStart.getValue.toInstant
    val expectedStartDate = LocalDateTime.of(2016, 1, 8, 13, 15)
    val expectedStartInstant =
      expectedStartDate.toInstant(ZoneId.of("Europe/Berlin").getRules.getOffset(expectedStartDate))
    startInstant should equal (expectedStartInstant)

    events.head.getLocation.getValue should equal ("Gasteig: Kleiner Konzertsaal")
  }

}
