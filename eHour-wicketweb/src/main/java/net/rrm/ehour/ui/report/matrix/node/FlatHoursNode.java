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

package net.rrm.ehour.ui.report.matrix.node;

import net.rrm.ehour.report.reports.element.ReportElement;
import net.rrm.ehour.ui.report.matrix.DetailedMatrixReportElement;
import net.rrm.ehour.ui.report.model.ReportNode;
import net.rrm.ehour.ui.report.matrix.DailyUnit;
import net.rrm.ehour.util.DateUtil;
import net.rrm.ehour.util.JodaDateUtil;
import org.apache.log4j.Logger;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Entry end node
 */

public class FlatHoursNode extends ReportNode {
    private static final long serialVersionUID = 7854152602780377915L;
    private static final Integer displayOrder = Integer.valueOf(3);
    protected static final Logger logger = Logger.getLogger(FlatHoursNode.class);
    private Number hours;

    /**
     * Calculate days between two given dates
     *
     * Will make copies of passed-in date and nullify time to 00:00:00
     *
     * @param start
     * @param end
     * @return
     */
    public static int daysBetweenDates(Date start, Date end) {
        Date alignedStart = new Date(start.getTime());
        Date alignedEnd = new Date(end.getTime());

        DateUtil.nullifyTime(alignedStart);
        DateUtil.nullifyTime(alignedEnd);

        return (int) ((alignedEnd.getTime() - alignedStart.getTime()) / (1000 * 60 * 60 * 24)) + 1;
    }

    public FlatHoursNode(DetailedMatrixReportElement element) {
        super(displayOrder, element.isEmptyEntry());
        hours = element.getTotalHours();
        Date startDate = new Date(element.getDateRange().getDateStart().getTime());
        Date endDate = new Date(element.getDateRange().getDateEnd().getTime());
        int totalDays = daysBetweenDates(startDate, endDate);
        logger.debug("Start Date: " + startDate
                + " End Date: " + endDate
                + " Total Days: " + totalDays
                + " Project Code: " + element.getProjectCode()
                + " UserName: " + element.getUserName()
                + " Total Hours: " + element.getTotalHours()
        );

        // Allocate, daily + total
        this.columnValues = new Serializable[totalDays + 1];
        List<DailyUnit> dailyUnits = element.getDailyProjectElements();
        boolean emptyDailyUnits = (dailyUnits == null);

        if (!emptyDailyUnits) {
            for (DailyUnit du : dailyUnits) {
                logger.debug(du.toString());
            }
        }

        int i, j = 0;
        Date currentDate = new Date(startDate.getTime());
        for (i = 0; i < totalDays; ++i) {
            if (!emptyDailyUnits) {
                DailyUnit currentDailyUnit = dailyUnits.get(j);
                logger.debug("Current Date: " + currentDate
                    + " Daily Unit Date: " + currentDailyUnit.getDate()
                    + "Hours: " + currentDailyUnit.getHours());

                if (currentDate.before(currentDailyUnit.getDate()) || currentDate.after(currentDailyUnit.getDate())) {
                    this.columnValues[i] = element.isShowZeroHours() ? 0.0 : "";
                } else {
                    this.columnValues[i] = currentDailyUnit.getHours();
                    // Prevent array out of bound - no more actual records
                    if (j < (dailyUnits.size() - 1)) {
                        j++;
                    }
                }
            }
            else {
                this.columnValues[i] = "";
            }
            // Current date slot + 1
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(currentDate);
            calendar.add(Calendar.DAY_OF_YEAR, 1);
            currentDate = calendar.getTime();
        }

        // Append sub-total of this user in this project
        this.columnValues[i] = element.getTotalHours();
    }

    @Override
    protected Serializable getElementId(ReportElement element) {
        return displayOrder;
    }

    @Override
    public Number getHours() {
        return hours;
    }

    @Override
    protected boolean isLeaf() {
        return true;
    }
}

