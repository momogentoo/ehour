/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package net.rrm.ehour.domain;


import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Calendar;
import java.util.Date;


@Entity
@Table(name = "CALENDAR_EXCEPTION")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CalendarException extends DomainObject<Integer, CalendarException>{
    private static final long serialVersionUID = 154653675325412269L;
    private static final String DEFAULT_USER_COUNTRY = "CN";

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID")
    private Integer id;

    @NotNull
    @Column(name = "CALENDAR_DATE")
    private Date calendarDate;


    @NotNull
    @Column(name = "COUNTRY_CODE")
    private String country = DEFAULT_USER_COUNTRY;

    @NotNull
    @Column(name = "EXCEPTION_TYPE")
    private Integer exceptionType;

    @Column(name = "DESCRIPTION")
    private String description;



    public CalendarException() {
    }

    public CalendarException(Integer id) {
        this.id = id;
    }

    /**
     * full constructor
     */
    public CalendarException(CalendarException calendarException) {
        this.id = calendarException.id;
        this.calendarDate = calendarException.calendarDate;
        this.country = calendarException.country;
        this.exceptionType = calendarException.exceptionType;
        this.description = calendarException.description;
    }

    public CalendarException(Integer id, Date calendarDate, String country, Integer exceptionType, String description) {
        this.id = id;
        this.calendarDate = calendarDate;
        this.country = country;
        this.exceptionType = exceptionType;
        this.description = description;
    }

    public Date getCalendarDate() {
        return calendarDate;
    }

    public void setCalendarDate(Date calendarDate) {
        this.calendarDate = calendarDate;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Integer getExceptionType() {
        return exceptionType;
    }

    public void setExceptionType(Integer exceptionType) {
        this.exceptionType = exceptionType;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).append("date", getCalendarDate())
                .append("country", getCountry())
                .append("exceptionType", getExceptionType())
                .append("description", getDescription())
                .toString();
    }

    /**
     * @see Comparable#compareTo(Object)
     */
    public int compareTo(CalendarException object) {
        return new CompareToBuilder()
                .append(this.getCalendarDate(), object.getCalendarDate())
                .append(this.getCountry(), object.getCountry())
                .append(this.getExceptionType(), object.getExceptionType())
                .append(this.getDescription(), object.getDescription())
                .toComparison();
    }

    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof CalendarException)) {
            return false;
        }

        CalendarException castOther = (CalendarException) other;
        return new EqualsBuilder()
                .append(calendarDate, castOther.calendarDate)
                .append(country, castOther.country)
                .append(exceptionType, castOther.exceptionType)
                .append(description, castOther.description)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(calendarDate).append(country).append(exceptionType).append(description).toHashCode();
    }

    @Override
    public Integer getPK() {
        return id;
    }
}
