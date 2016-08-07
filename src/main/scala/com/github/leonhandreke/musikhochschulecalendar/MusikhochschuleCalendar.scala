package com.github.leonhandreke.musikhochschulecalendar

import java.time.{LocalDateTime, ZoneId, ZoneOffset}
import java.time.format.DateTimeFormatter
import java.util.GregorianCalendar

import biweekly.ICalendar
import biweekly.component.VEvent
import biweekly.util.Duration
import org.jsoup.Jsoup
import org.jsoup.nodes.{Document, Element}

import scala.collection.JavaConversions
import scala.util.matching.Regex

object MusikhochschuleCalendar {

  final val DATE_REGEX = new Regex("""(\d+\.\d+\.\d+)\s+(\d+:\d+)""")
  final val MUSIKHOCHSCHULE_URL = "http://website.musikhochschule-muenchen.de/de/index.php?option=com_content&task=view&id=565&Itemid=602&d1=01&m1=01&y1=2016&d2=31&m2=12&y2=2017"

  def main(args: Array[String]): Unit = {

    val document = Jsoup
      .connect(MUSIKHOCHSCHULE_URL)
      .timeout(0)
      .get();

    val calendar = getCalendarFromDocument(document)

    print(calendar.write)
  }

  def getCalendarFromDocument(document: Document): ICalendar = {
    val eventsTable = document.body()
      .getElementById("VER_2013_DISPLAYSEARCHRESULTS")
      .getElementsByTag("table")
      .get(1)

    val eventRows = JavaConversions.asScalaBuffer(eventsTable.getElementsByTag("tr"))
      .filter((e: Element) => e.children().size() == 4)
    val events = eventRows.map(tableRowToEvent)

    val calendar = new ICalendar()
    events foreach ((event : Event) => {
      val c = new VEvent()
      c.setSummary(event.title)

      c.setDateStart(
        java.util.Date.from(
          event.startDate.toInstant(ZoneId.of("Europe/Berlin").getRules.getOffset(event.startDate))),
        /* hasTime */ true)
      c.setDuration(Duration.builder().hours(2).build())
      c setLocation event.location
      calendar.addEvent(c)
    })

    return calendar
  }

  def tableRowToEvent(e: Element): Event = {
    // Don't use fancy capture groups here because the stupid website doesn't
    // use spaces but ASCII 160 SYN characters
    val dateString = """\d+\.\d+\.\d+""".r.findFirstIn(e.child(0).text).get
    val timeString = """\d+:\d+""".r.findFirstIn(e.child(0).text).getOrElse("00:00")
    val startDate = LocalDateTime.parse(
      dateString + " " + timeString,
      DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))
    val location = e.child(1).children().get(0).text()
    val title = e.child(2).getElementsByTag("strong").text
    new Event(title, location, startDate)
  }
}