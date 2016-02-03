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

package net.rrm.ehour.ui.report.matrix;

import com.google.common.collect.Maps;
import net.rrm.ehour.report.criteria.ReportCriteria;
import net.rrm.ehour.report.reports.element.FlatReportElement;
import net.rrm.ehour.ui.common.report.*;
import net.rrm.ehour.ui.common.report.excel.CellFactory;
import net.rrm.ehour.ui.common.report.excel.ExcelStyle;
import net.rrm.ehour.ui.common.report.excel.ExcelWorkbook;
import net.rrm.ehour.ui.report.matrix.node.FlatHoursNode;
import net.rrm.ehour.ui.report.model.TreeReportElement;
import net.rrm.ehour.util.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.collections.ArrayListStack;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static net.rrm.ehour.ui.common.report.ReportColumn.DisplayType.ALLOW_DUPLICATES;
import static net.rrm.ehour.ui.common.report.ReportColumn.DisplayType.VISIBLE;

/**
 * Control specific layouts for Matrix Report
 */
public class DetailedMatrixReportExcel extends AbstractExcelReport {
    private static final long serialVersionUID = 7211392864328367507L;

    public DetailedMatrixReportExcel(IModel<ReportCriteria> reportCriteriaModel) {
        super(DetailedMatrixReportConfig.DETAILED_REPORT_BY_DAY, reportCriteriaModel);
    }

    @Override
    protected Report createReport(ReportCriteria reportCriteria) {
        return new DetailedMatrixReportModel(reportCriteria);
    }

    @Override
    protected IModel<String> getExcelReportName() {
        return new ResourceModel("report.title.matrix");
    }

    @Override
    protected IModel<String> getHeaderReportName() {
        return new ResourceModel("report.title.matrix");
    }

    /**
     * Override for Matrix-Style Report Headers Creation
     *
     * @param rowNumber
     * @param sheet
     * @param report
     * @param workbook
     * @return
     */
    @Override
    protected int createHeaders(int rowNumber, Sheet sheet, Report report, ExcelWorkbook workbook) {

        Row row = sheet.createRow(rowNumber++);
        CellFactory.createCell(row, 0, "Project Timesheet Report", workbook, ExcelStyle.NORMAL_FONT);
        sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));

        row = sheet.createRow(rowNumber++);

        row = sheet.createRow(rowNumber++);
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
        String reportDateRange = "Report Period: " +
                ((report.getReportRange() == null) ? "-- to --" :
                        ((report.getReportRange().getDateStart() == null ? "--" : formatter.format(report.getReportRange().getDateStart()))
                        + " to "
                        + (report.getReportRange().getDateEnd() == null ? "--" : formatter.format(report.getReportRange().getDateEnd()))));


        CellFactory.createCell(row, 0, reportDateRange, workbook, ExcelStyle.BOLD_FONT);

        sheet.addMergedRegion(new CellRangeAddress(2, 2, 0, 3));

        rowNumber++;

        return rowNumber;
    }

    @Override
    protected Sheet adjustColumnWidth(Sheet sheet, ReportColumn[]effectiveColumns) {
        int column;

        if (effectiveColumns == null) {
            throw new IllegalStateException("effectiveColumns is null from DetailedMatrixReportModel?");
        }

        // Project Name
        sheet.setColumnWidth(0, 8000);

        // Resource Name
        sheet.setColumnWidth(1, 4500);

        // Daily Hours
        for (column = 2; column < effectiveColumns.length; column++) {
            sheet.setColumnWidth(column, 1500);
        }

        // Enlarge width of "Total" column
        sheet.setColumnWidth(column - 1, 2000);

        return sheet;
    }

    /**
     * Sort a list of FlatReportElement into Collection, sorted by User as key, a list of items for value,
     * sorted by Date and Project Code internally
     * @param flatReportElements
     * @return
     */
    private Map<String, List<FlatReportElement>> sortFlatReportElementsForPersonalDetails(List<FlatReportElement> flatReportElements) {
        Map<String, List<FlatReportElement>> results = Maps.newHashMap();

        // Stage 1 - Sort by full user name
        for (FlatReportElement flatReportElement : flatReportElements) {
            // full user name as key
            String fullUserName = DetailedMatrixReportModel.getFullName(flatReportElement.getUserLastName(),
                    flatReportElement.getUserFirstName());

            List<FlatReportElement> perUserList = results.get(fullUserName);
            if (perUserList == null) {
                perUserList = new ArrayList<>();
                results.put(fullUserName, perUserList);
            }

            perUserList.add(flatReportElement);
        }

        // Stage 2 - Sort by Date and Project Code for each per-user list
        for (List<FlatReportElement> perUserList : results.values()) {
            Collections.sort(perUserList, new Comparator<FlatReportElement>() {
                @Override
                public int compare(FlatReportElement o1, FlatReportElement o2) {
                    if (o1.getDayDate() == null) {
                        return -1;
                    } else if (o2.getDayDate() == null) {
                        return 1;
                    } else {
                        int dateComparison = o1.getDayDate().compareTo(o2.getDayDate());
                        // Same day? compare by project code
                        if (dateComparison == 0) {
                            return o1.getProjectCode().compareTo(o2.getProjectCode());
                        }
                        else {
                            return dateComparison;
                        }
                    }
                }
            });
        }

        return results;
    }

    @Override
    protected ExcelWorkbook createAdditionalSheets(ExcelWorkbook wb) {
        /**
         * Need to create separate sheet for each person
         */
        if ((report == null) || !(this.report instanceof DetailedMatrixReportModel)) {
            throw new IllegalStateException("DetailedMatrixReportExcel lacks DetailedMatrixReportModel for report data");
        }

        DetailedMatrixReportModel model = (DetailedMatrixReportModel)this.report;

        // Check if data has been retrieved from DetailedReportService or not
        List<FlatReportElement> flatReportElements = model.getDetailedFlatReportElements(true);

        // Sort items by user, and then date & project code
        Map<String, List<FlatReportElement>> userDetailLists = sortFlatReportElementsForPersonalDetails(flatReportElements);

        // Get a sorted list of user names
        List<String> usernames = new ArrayList<>();
        usernames.addAll(userDetailLists.keySet());
        Collections.sort(usernames);

        ReportColumn []reportColumns = buildPerUserReportColumns(report.getReportCriteria());

        // Create per-user sheet
        for (String user : usernames) {
            int rowNumber = 0;

            Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(user));

            adjustPerUserColumnWidth(sheet);

            rowNumber = createPerUserHeaders(rowNumber, sheet, wb, reportColumns);

            Float totalHoursOfUser = Float.valueOf(0);

            for (FlatReportElement flatReportElement : userDetailLists.get(user)) {
                Row row = sheet.createRow(rowNumber++);

                addPerUserDataColumns(wb, reportColumns, row, user, flatReportElement);

                totalHoursOfUser += flatReportElement.getTotalHours().floatValue();
            }

            // Total Hours
            Row row = sheet.createRow(rowNumber++);
            CellFactory.createCell(row, 0, "Total", wb, ExcelStyle.BOLD_FONT);
            CellFactory.createCell(row, 4, totalHoursOfUser, wb, ExcelStyle.DIGIT);
        }

        return wb;
    }


    /**
     * Adjust column width for per-user sheet
     * @param sheet
     */
    private void adjustPerUserColumnWidth(Sheet sheet) {
        short column;
        for (column = 0; column < 4; column++) {
            sheet.setColumnWidth(column, 5000);
        }

        for (; column < 5; column++) {
            sheet.setColumnWidth(column, 3000);
        }
    }

    /**
     * Build Per-User Sheet Column Configuration
     * @param criteria
     * @return
     */
    private ReportColumn[] buildPerUserReportColumns(ReportCriteria criteria) {
        int totalColumns = 5;
        ReportColumn [] reportColumns = new ReportColumn[totalColumns];

        for (int i = 0; i < totalColumns; ++i) {
            switch (i) {
                case 0:
                    reportColumns[i] = new ReportColumn("Date", ColumnType.DATE, VISIBLE);
                    reportColumns[i].setDefaultColumnName("Date");
                    break;
                case 1:
                    reportColumns[i] = new ReportColumn("Client", ColumnType.STRING, VISIBLE, ALLOW_DUPLICATES);
                    reportColumns[i].setDefaultColumnName("Client");
                    break;
                case 2:
                    reportColumns[i] = new ReportColumn("Project", ColumnType.STRING, VISIBLE, ALLOW_DUPLICATES);
                    reportColumns[i].setDefaultColumnName("Project");
                    break;
                case 3:
                    reportColumns[i] = new ReportColumn("Name", ColumnType.STRING, VISIBLE, ALLOW_DUPLICATES);
                    reportColumns[i].setDefaultColumnName("Name");
                    break;
                case 4:
                    reportColumns[i] = new ReportColumn("Hours", ColumnType.HOUR, VISIBLE, ALLOW_DUPLICATES);
                    reportColumns[i].setDefaultColumnName("Hours");
                    break;
            }
        }

        return reportColumns;
    }

    /**
     * Create per-User sheet header columns
     * @param rowNumber
     * @param sheet
     * @param workbook
     * @param columnHeaders
     * @return
     */
    private int createPerUserHeaders(int rowNumber, Sheet sheet, ExcelWorkbook workbook, ReportColumn []columnHeaders) {
        int cellNumber = 0;
        IModel<String> headerModel;

        Row row = sheet.createRow(rowNumber++);

        for (ReportColumn reportColumn : columnHeaders) {
            if (reportColumn.isVisible()) {
                headerModel = new ResourceModel(reportColumn.getColumnHeaderResourceKey(), reportColumn.getDefaultColumnName());

                CellFactory.createCell(row, cellNumber++, headerModel, workbook,
                        (reportColumn.getOverrideExcelStyle() == null) ? ExcelStyle.HEADER : reportColumn.getOverrideExcelStyle());
            }
        }


        return rowNumber;
    }

    /**
     * Populate data entries into per-user sheet
     * @param workbook
     * @param columnHeaders
     * @param row
     * @param name
     * @param flatReportElement
     */
    private void addPerUserDataColumns(ExcelWorkbook workbook,
                            ReportColumn[] columnHeaders,
                            Row row, String name,
                            FlatReportElement flatReportElement) {
        int cellNumber = 0;

        // add cells for a row
        for (int i = 0; i < 5; ++i) {
            switch (i) {
                case 0: // Date
                    CellFactory.createCell(row, cellNumber++, flatReportElement.getDayDate(), workbook, ExcelStyle.DATE);
                    break;
                case 1: // Client
                    CellFactory.createCell(row, cellNumber++, flatReportElement.getCustomerName(), workbook, ExcelStyle.NORMAL_FONT);
                    break;
                case 2: // Project
                    CellFactory.createCell(row, cellNumber++, flatReportElement.getProjectName(), workbook, ExcelStyle.NORMAL_FONT);
                    break;
                case 3: // Name
                    CellFactory.createCell(row, cellNumber++, name, workbook, ExcelStyle.NORMAL_FONT);
                    break;
                case 4: // Hours
                    CellFactory.createCell(row, cellNumber++, flatReportElement.getTotalHours(), workbook, ExcelStyle.DIGIT);
                    break;
                default:
                    break;
            }
        }
    }
}
