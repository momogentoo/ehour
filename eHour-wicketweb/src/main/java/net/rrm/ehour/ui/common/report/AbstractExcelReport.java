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

package net.rrm.ehour.ui.common.report;

import net.rrm.ehour.report.criteria.ReportCriteria;
import net.rrm.ehour.ui.common.report.excel.CellFactory;
import net.rrm.ehour.ui.common.report.excel.ExcelStyle;
import net.rrm.ehour.ui.common.report.excel.ExcelWorkbook;
import net.rrm.ehour.ui.report.matrix.DetailedMatrixReportModel;
import net.rrm.ehour.ui.report.model.TreeReportElement;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.WorkbookUtil;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;

import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Abstract aggregate excel report
 */
public abstract class AbstractExcelReport implements ExcelReport {
    private static final long serialVersionUID = 1L;
    protected static final Logger logger = Logger.getLogger(AbstractExcelReport.class);

    private ReportConfig reportConfig;
    private IModel<ReportCriteria> reportCriteriaModel;

    /**
     * Store current Report Data Model
     */
    protected Report report;

    public AbstractExcelReport(ReportConfig reportConfig, IModel<ReportCriteria> reportCriteriaModel) {
        this.reportConfig = reportConfig;
        this.reportCriteriaModel = reportCriteriaModel;
        this.report = null;
    }

    @Override
    public void write(OutputStream stream) throws IOException {
        report = createReport(reportCriteriaModel.getObject());
        ExcelWorkbook workbook = createWorkbook(report);
        workbook.write(stream);
   }

    protected abstract Report createReport(ReportCriteria reportCriteria);

    /**
     * Create the workbook
     */
    protected ExcelWorkbook createWorkbook(Report treeReport) {
        ReportColumn []effectiveColumns = treeReport.getReportColumns();
        ExcelWorkbook wb = new ExcelWorkbook();

        Sheet sheet = wb.createSheet(WorkbookUtil.createSafeSheetName(getExcelReportName().getObject()));
        int rowNumber = 0;

        // Adjust column width of worksheet
        adjustColumnWidth(sheet, effectiveColumns);

        // Report does not provide columns, use static config
        if (effectiveColumns == null) {
            effectiveColumns = reportConfig.getReportColumns();
            logger.info("Using configured static report columns");
        }

        rowNumber = createHeaders(rowNumber, sheet, treeReport, wb);

        rowNumber = addColumnHeaders(rowNumber, sheet, wb, effectiveColumns);

        rowNumber = fillReportSheet(treeReport, sheet, rowNumber, wb, effectiveColumns);

        appendFootage(sheet, rowNumber, wb);

        /**
         * Check and Add additional sheets for specific report templates
         */
        createAdditionalSheets(wb);

        return wb;
    }

    protected abstract IModel<String> getExcelReportName();

    protected abstract IModel<String> getHeaderReportName();

    private int addColumnHeaders(int rowNumber, Sheet sheet, ExcelWorkbook workbook, ReportColumn[] columnHeaders) {
        int cellNumber = 0;
        IModel<String> headerModel;

        Row row = sheet.createRow(rowNumber++);

        for (ReportColumn reportColumn : columnHeaders) {
            if (reportColumn.isVisible()) {
                logger.debug("Adding column header: " + reportColumn.getDefaultColumnName());
                headerModel = new ResourceModel(reportColumn.getColumnHeaderResourceKey(), reportColumn.getDefaultColumnName());

                CellFactory.createCell(row, cellNumber++, headerModel, workbook,
                        (reportColumn.getOverrideExcelStyle() == null) ? ExcelStyle.HEADER : reportColumn.getOverrideExcelStyle());
            }
        }

        return rowNumber;
    }

    protected int appendFootage(Sheet sheet, int rowNumber, ExcelWorkbook workbook) {
        Row row;
        Date now = Calendar.getInstance().getTime();

        // Add a blank line
        row = sheet.createRow(rowNumber++);

        // Add generated time
        row = sheet.createRow(rowNumber++);
        String reportGenerateTime = "Report Generation Time: " + now;

        CellFactory.createCell(row, 0, reportGenerateTime, workbook, ExcelStyle.NORMAL_FONT);

        sheet.addMergedRegion(new CellRangeAddress(rowNumber - 1, rowNumber - 1, 0, 3));

        return rowNumber;
    }

    @SuppressWarnings("unchecked")
    protected int fillReportSheet(Report reportData, Sheet sheet, int rowNumber, ExcelWorkbook workbook, ReportColumn[] columnHeaders) {
        List<TreeReportElement> matrix = (List<TreeReportElement>) reportData.getReportData().getReportElements();
        Row row;
        TreeReportElement lastElement = null;

        for (TreeReportElement element : matrix) {
            row = sheet.createRow(rowNumber++);

            addColumns(workbook, columnHeaders, row, element, lastElement);

            lastElement = element;
        }

        return rowNumber;
    }

    private void addColumns(ExcelWorkbook workbook,
                            ReportColumn[] columnHeaders,
                            Row row,
                            TreeReportElement element,
                            TreeReportElement lastElement) {
        int i = 0;
        int cellNumber = 0;

        // add cells for a row
        for (Serializable cellValue : element.getRow()) {
            if (columnHeaders[i].isVisible()) {
                // Skip duplicate value
                if (cellValue != null) {
                    if (lastElement != null) {
                        Serializable lastElementCellValue = lastElement.getRow()[i];
                        if (!columnHeaders[i].isAllowDuplicates() && lastElementCellValue != null && lastElementCellValue.equals(cellValue)) {
                            cellValue = "";
                        }
                    }

                    switch (columnHeaders[i].getColumnType()) {
                        case HOUR:
                            CellFactory.createCell(row, cellNumber++, cellValue, workbook, ExcelStyle.DIGIT);
                            break;
                        case TURNOVER:
                        case RATE:
                            CellFactory.createCell(row, cellNumber++, cellValue, workbook, ExcelStyle.CURRENCY);
                            break;
                        case DATE:
                            CellFactory.createCell(row, cellNumber++, cellValue, workbook, ExcelStyle.DATE);
                            break;
                        default:
                            CellFactory.createCell(row, cellNumber++, cellValue, workbook, ExcelStyle.NORMAL_FONT);
                            break;
                    }
                } else {
                    cellNumber++;
                }
            }

            i++;
        }
    }

    @Override
    public String getFilenameWihoutSuffix() {
        return getExcelReportName().getObject().toLowerCase().replace(' ', '_');
    }


    /**
     * Adjust Excel Sheet Column Width
     * <br/>
     * May be overridden
     *
     * @param sheet
     * @param effectiveColumns
     *
     * @return
     */
    protected Sheet adjustColumnWidth(Sheet sheet, ReportColumn []effectiveColumns) {
        /**
         * This style applies to most of static-column reports
         */
        short column;
        for (column = 0; column < 4; column++) {
            sheet.setColumnWidth(column, 5000);
        }

        for (; column < 7; column++) {
            sheet.setColumnWidth(column, 3000);
        }

        return sheet;
    }

    /**
     * Create Excel Summary Sheet Headers
     *
     * May be overridden
     *
     * @param rowNumber
     * @param sheet
     * @param report
     * @param workbook
     * @return
     */
    protected int createHeaders(int rowNumber, Sheet sheet, Report report, ExcelWorkbook workbook) {
        Row row = sheet.createRow(rowNumber++);
        CellFactory.createCell(row, 0, getHeaderReportName(), workbook, ExcelStyle.BOLD_FONT);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 1));

        row = sheet.createRow(rowNumber++);
        CellFactory.createCell(row, 0, new ResourceModel("report.dateStart"), workbook, ExcelStyle.BOLD_FONT);

        if (report.getReportRange() == null ||
                report.getReportRange().getDateStart() == null) {
            CellFactory.createCell(row, 1, "--", workbook, ExcelStyle.BOLD_FONT);
        } else {
            CellFactory.createCell(row, 1, report.getReportCriteria().getReportRange().getDateStart(), workbook, ExcelStyle.BOLD_DATE);
        }

        CellFactory.createCell(row, 3, new ResourceModel("report.dateEnd"), workbook, ExcelStyle.BOLD_FONT);

        if (report.getReportRange() == null || report.getReportRange().getDateEnd() == null) {
            CellFactory.createCell(row, 4, "--", workbook, ExcelStyle.BOLD_FONT);
        } else {
            CellFactory.createCell(row, 4, report.getReportCriteria().getReportRange().getDateEnd(), workbook, ExcelStyle.BOLD_DATE);
        }

        rowNumber++;

        return rowNumber;
    }

    /**
     * Create additional sheets in current ExcelWorkbook
     *
     * @param wb
     * @return
     */
    protected ExcelWorkbook createAdditionalSheets(ExcelWorkbook wb) {
        // Do nothing for most of report templates
        return wb;
    }
}
