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

package net.rrm.ehour.ui.report.detailed;

import net.rrm.ehour.report.criteria.ReportCriteria;
import net.rrm.ehour.ui.common.report.AbstractExcelReport;
import net.rrm.ehour.ui.common.report.DetailedReportConfig;
import net.rrm.ehour.ui.common.report.Report;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

public class DetailedReportExcel extends AbstractExcelReport {
    private static final long serialVersionUID = 7211392869328367507L;

    public DetailedReportExcel(IModel<ReportCriteria> reportCriteriaModel) {
        super(DetailedReportConfig.DETAILED_REPORT_BY_DAY, reportCriteriaModel);
    }

    @Override
    protected Report createReport(ReportCriteria reportCriteria) {
        return new DetailedReportModel(reportCriteria);
    }

    @Override
    protected IModel<String> getExcelReportName() {
        return new ResourceModel("report.title.detailed");
    }

    @Override
    protected IModel<String> getHeaderReportName() {
        return new ResourceModel("report.title.detailed");
    }

}
