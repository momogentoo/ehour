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
import net.rrm.ehour.ui.report.matrix.DetailedMatrixReportModel;
import net.rrm.ehour.ui.report.model.ReportNode;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Flat headers node
 */

public class FlatHeaderNode extends ReportNode {
    private static final long serialVersionUID = -9117536405550375613L;

    public FlatHeaderNode(DetailedMatrixReportElement element) {

        super(0, element.isEmptyEntry());
        int totalDays = FlatHoursNode.daysBetweenDates(element.getDateRange().getDateStart(), element.getDateRange().getDateEnd());
        int totalColumns = totalDays + DetailedMatrixReportModel.FIXED_COLUMN_NUMBERS;
        this.columnValues = new Serializable[totalColumns];
        SimpleDateFormat formatter = new SimpleDateFormat("mm/dd");
        Date startDate = element.getDateRange().getDateStart();
        for (int i = 0; i < totalColumns; ++i) {
            switch (i) {
                case 0:
                    this.columnValues[i] = "Project";
                    break;
                case 1:
                    this.columnValues[i] = "Resource Name";
                    break;
                default:
                    this.columnValues[i] = formatter.format(startDate);
                    startDate.setTime(startDate.getTime() + 24 * 3600000);
                    break;
            }
        }
        this.columnValues[totalColumns - 1] = "Total";
    }

    @Override
    protected Serializable getElementId(ReportElement element) {
        return 0;
    }

    @Override
    protected boolean isLeaf() {
        return true;
    }
}
