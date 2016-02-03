package net.rrm.ehour.persistence.customer.dao

import java.util

import net.rrm.ehour.domain.{CalendarException}
import net.rrm.ehour.persistence.dao.GenericDao

trait CalendarDao extends GenericDao[Integer, CalendarException] {
  /**
   * Find all calendar exceptions
   **/
  def findAll(): util.List[CalendarException]

  /**
   * Find all calendar exceptions by country code
   */
  def findAllByCountry(country_code: String): util.List[CalendarException]
}