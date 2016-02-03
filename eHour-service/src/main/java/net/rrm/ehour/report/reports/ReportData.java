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

package net.rrm.ehour.report.reports;

import com.google.common.collect.Lists;
import net.rrm.ehour.data.DateRange;
import net.rrm.ehour.report.criteria.ReportCriteria;
import net.rrm.ehour.report.criteria.UserSelectedCriteria;
import net.rrm.ehour.report.reports.element.ReportElement;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Data holder for reports
 */

public class ReportData implements Serializable {
    private static final long serialVersionUID = -6344570520998830487L;

    private List<Date> lockedDays;
    private List<? extends ReportElement> reportElements;
    private DateRange reportRange;
    private int totalReportColumns;
    private final UserSelectedCriteria criteria;
    private final ReportCriteria reportCriteria;

    public ReportData(List<? extends ReportElement> reportElements, DateRange reportRange, UserSelectedCriteria criteria) {
        this(Lists.<Date>newArrayList(), reportElements, reportRange, criteria);
    }

    public ReportData(List<Date> lockedDays, List<? extends ReportElement> reportElements, DateRange reportRange, UserSelectedCriteria criteria) {
        this.lockedDays = lockedDays;
        this.reportElements = reportElements;
        this.reportRange = reportRange;
        this.criteria = criteria;
        this.reportCriteria = new ReportCriteria(criteria);
        this.totalReportColumns = 0;
    }

    public boolean isEmpty() {
        return getReportElements().isEmpty();
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<Date> getLockedDays() {
        return lockedDays;
    }

    public DateRange getReportRange() {
        return reportRange;
    }

    public List<? extends ReportElement> getReportElements() {
        return reportElements;
    }

    public UserSelectedCriteria getCriteria() {
        return criteria;
    }

    public ReportCriteria getReportCriteria() {
        return reportCriteria;
    }

    public int getTotalReportColumns() {
        return totalReportColumns;
    }

    public void setTotalReportColumns(int totalReportColumns) {
        this.totalReportColumns = totalReportColumns;
    }
}
