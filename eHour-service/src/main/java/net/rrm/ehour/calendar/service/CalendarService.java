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

package net.rrm.ehour.calendar.service;

import net.rrm.ehour.domain.CalendarException;
import org.joda.time.LocalDate;

import java.util.List;
import java.util.Map;

/**
 * Calendar Service
 */

public interface CalendarService {

    /**
     * Get all calendar exceptions
     *
     * @return
     */
    public List<CalendarException> getCalendarExceptions();

    /**
     * Get a list of calendar exceptions by country code
     * @param countryCode
     * @return
     */
    public List<CalendarException> getCalendarExceptions(String countryCode);

    /**
     * Pre-process list of calendar exceptions for a single country so a single day could have one exception only
     * @return
     */
    public Map<LocalDate, CalendarException> prepCalendarExceptions(List<CalendarException> exceptions);

    /**
     * Pre-process list of multi-country calendar exceptions
     *
     * LocalDate -> { Country Code -> CalendarException }
     *
     * @param exceptions
     * @return
     */
    public Map<LocalDate, Map<String, CalendarException>> prepMultiCountryCalendarExceptions(List<CalendarException> exceptions);
}
