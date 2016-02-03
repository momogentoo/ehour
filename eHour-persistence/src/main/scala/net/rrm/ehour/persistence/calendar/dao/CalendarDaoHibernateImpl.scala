package net.rrm.ehour.persistence.customer.dao

import java.util

import net.rrm.ehour.domain.{CalendarException}
import net.rrm.ehour.persistence.dao.AbstractGenericDaoHibernateImpl
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

/**
 * Calendar DAO
 */
@Repository("calendarDao")
@Transactional(readOnly = true)
class CalendarDaoHibernateImpl extends AbstractGenericDaoHibernateImpl[Integer, CalendarException](classOf[CalendarException]) with CalendarDao {
  private final val CacheRegion = Some("query.Calendar")

  override def findAllByCountry(country_code: String): util.List[CalendarException] = findByNamedQuery("Calendar.findAllByCountry", "country_code", country_code, CacheRegion)

  override def findAll(): util.List[CalendarException] = findByNamedQuery("Calendar.findAll", CacheRegion)
}


