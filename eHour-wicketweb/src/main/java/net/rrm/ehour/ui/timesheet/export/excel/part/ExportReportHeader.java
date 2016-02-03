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

package net.rrm.ehour.ui.timesheet.export.excel.part;

import net.rrm.ehour.config.EhourConfig;
import net.rrm.ehour.config.service.ConfigurationService;
import net.rrm.ehour.data.DateRange;
import net.rrm.ehour.persistence.value.ImageLogo;
import net.rrm.ehour.ui.common.model.DateModel;
import net.rrm.ehour.ui.common.report.PoiUtil;
import net.rrm.ehour.ui.common.report.Report;
import net.rrm.ehour.ui.common.report.excel.CellFactory;
import net.rrm.ehour.ui.common.report.excel.ExcelWorkbook;
import net.rrm.ehour.ui.common.session.EhourWebSession;
import net.rrm.ehour.ui.common.util.WebUtils;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFClientAnchor;
import org.apache.poi.xssf.usermodel.XSSFShape;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.model.StringResourceModel;
import org.apache.wicket.spring.injection.annot.SpringBean;


/**
 * Created on Mar 25, 2009, 6:37:20 AM
 *
 * @author Thies Edeling (thies@te-con.nl)
 */
public class ExportReportHeader extends AbstractExportReportPart
{
//    private static final Logger logger = Logger.getLogger(ExportReportHeader.class);
    private static final int BLANK_ROWS_AFTER_LOGO = 2;

    @SpringBean(name = "configurationService")
    private ConfigurationService configurationService;

    public ExportReportHeader(int cellMargin, Sheet sheet, Report report, ExcelWorkbook workbook)
    {
        super(cellMargin, sheet, report, workbook);
    }

    @Override
    public int createPart(int rowNumber)
    {
        rowNumber = addLogo(rowNumber);
        rowNumber = addTitleRow(rowNumber);
        rowNumber = addTitleDateRow(rowNumber);
        rowNumber++;

        return rowNumber;
    }


    private int addLogo(int rowNumber)
    {
        ImageLogo excelLogo = getConfigurationService().getExcelLogo();

        if (excelLogo == null) {
            return rowNumber;
        }

        byte[] image = excelLogo.getImageData();

        int index = getWorkbook().addPicture(image, PoiUtil.getImageType(excelLogo.getImageType()));
        CreationHelper helper = getWorkbook().getCreationHelper();
//        ClientAnchor anchor = new XSSFClientAnchor(0, 0, 0, 0, (short) 1, 0, (short) 8, 7);
        ClientAnchor anchor = helper.createClientAnchor();

        anchor.setCol1(1);
        anchor.setRow1(1);

        Drawing patriarch = getSheet().createDrawingPatriarch();
        Picture picture = patriarch.createPicture(anchor, index);
        picture.resize();
//        anchor.setAnchorType(ClientAnchor.MOVE_DONT_RESIZE); // 0 = Move and size with Cells, 2 = Move but don't size with cells, 3 = Don't move or size with cells.

        // Return dynamic row number according to best logo size
        return rowNumber + anchor.getRow2() + BLANK_ROWS_AFTER_LOGO;
    }


    private int addTitleRow(int rowNumber)
    {
        Row row = getSheet().createRow(rowNumber++);

        CellFactory.createCell(row, getCellMargin(), getExcelReportName(getReport().getReportRange()), getWorkbook());
        return rowNumber;
    }

    private int addTitleDateRow(int rowNumber)
    {
        Row row = getSheet().createRow(rowNumber++);

        CellFactory.createCell(row, getCellMargin(), new ResourceModel("excelMonth.date"), getWorkbook());
        CellFactory.createCell(row, getCellMargin() + 2, WebUtils.formatDate("MMMM yyyy", getReport().getReportRange().getDateStart()), getWorkbook());

        return rowNumber;
    }

    private IModel<String> getExcelReportName(DateRange dateRange)
    {
        EhourConfig config = EhourWebSession.getEhourConfig();

        return new StringResourceModel("excelMonth.reportName",
                null,
                new Object[]{EhourWebSession.getUser().getFullName(),
                             new DateModel(dateRange.getDateStart(), config, DateModel.DATESTYLE_MONTHONLY)});
    }

    private ConfigurationService getConfigurationService()
    {
        if (configurationService == null)
        {
            WebUtils.springInjection(this);
        }

        return configurationService;
    }
}
