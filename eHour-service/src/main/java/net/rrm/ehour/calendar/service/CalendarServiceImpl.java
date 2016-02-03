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

import com.google.common.collect.Maps;
import net.rrm.ehour.domain.CalendarException;
import net.rrm.ehour.persistence.customer.dao.CalendarDao;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * Calendar service implementation
 */
@Service("calendarService")
public class CalendarServiceImpl implements CalendarService {
    @Autowired
    private CalendarDao calendarDAO;

    public CalendarServiceImpl() {
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarException> getCalendarExceptions()
    {
        return calendarDAO.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CalendarException> getCalendarExceptions(String countryCode)
    {
        return calendarDAO.findAllByCountry(countryCode);
    }

    @Override
    public Map<LocalDate, CalendarException> prepCalendarExceptions(List<CalendarException> exceptions) {
        Map<LocalDate, CalendarException> result = Maps.newHashMap();

        for (CalendarException expDay : exceptions) {
            LocalDate localDate = new LocalDate(expDay.getCalendarDate());
            result.put(localDate, expDay);
        }

        return result;
    }

    @Override
    public Map<LocalDate, Map<String, CalendarException>> prepMultiCountryCalendarExceptions(List<CalendarException> exceptions) {
        Map<LocalDate, Map<String, CalendarException>> result = Maps.newHashMap();

        for (CalendarException expDay : exceptions) {
            LocalDate localDate = new LocalDate(expDay.getCalendarDate());
            Map<String, CalendarException> multiCountryMap = result.get(localDate);

            // New Date
            if (multiCountryMap == null) {
                multiCountryMap = Maps.newHashMap();
                result.put(localDate, multiCountryMap);
            }

            multiCountryMap.put(expDay.getCountry(), expDay);
        }

        return  result;
    }

}
