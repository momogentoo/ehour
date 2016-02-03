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
import net.rrm.ehour.report.reports.ReportData;
import net.rrm.ehour.report.reports.element.FlatReportElement;
import net.rrm.ehour.report.service.DetailedReportService;
import net.rrm.ehour.ui.common.report.ColumnType;
import net.rrm.ehour.ui.common.report.DetailedMatrixReportConfig;
import net.rrm.ehour.ui.common.report.ReportColumn;
import net.rrm.ehour.ui.common.util.WebUtils;
import net.rrm.ehour.ui.report.model.ReportNode;
import net.rrm.ehour.ui.report.model.ReportNodeFactory;
import net.rrm.ehour.ui.report.model.TreeReportModel;
import net.rrm.ehour.ui.report.matrix.node.FlatHoursNode;
import net.rrm.ehour.ui.report.matrix.node.FlatProjectNode;
import net.rrm.ehour.ui.report.matrix.node.FlatUserNode;
import net.rrm.ehour.util.DateUtil;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.apache.wicket.spring.injection.annot.SpringBean;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;

import static net.rrm.ehour.ui.common.report.ReportColumn.DisplayType.ALLOW_DUPLICATES;
import static net.rrm.ehour.ui.common.report.ReportColumn.DisplayType.VISIBLE;

/**
 * Detailed Matrix Style Detailed report
 */

public class DetailedMatrixReportModel extends TreeReportModel {

    private static final long serialVersionUID = -21703829501229502L;

    /**
     * Total number of static columns for matrix report - Project, Resource Name and Total.
     * <br/>
     * Remaining columns are dynamic by number of days
     */
    public static final int FIXED_COLUMN_NUMBERS = 3;
    private static final String TIMESHEET_TOTAL = "TOTAL";
    protected static final Logger logger = Logger.getLogger(DetailedMatrixReportModel.class);

    @SpringBean(name = "detailedReportService")
    private DetailedReportService detailedReportService;

    /**
     * Keep a reference to last queried detailed FlatReportElements;
     */
    private List<FlatReportElement> lastQueriedDetailedFlatReportElements;

    public DetailedMatrixReportModel(ReportCriteria reportCriteria) {
        super(reportCriteria, DetailedMatrixReportConfig.DETAILED_REPORT_BY_DAY);

        // Matrix style provides grand total data
        setGrandTotalProvided(true);

        this.lastQueriedDetailedFlatReportElements = null;
    }

    @Override
    protected ReportData fetchReportData(ReportCriteria reportCriteria) {
        return getDetailedReportService().getDetailedReportData(reportCriteria);
    }

    /**
     * Build full user name by last name and first name
     * @param lastName
     * @param firstName
     * @return
     */
    public static String getFullName(String lastName, String firstName) {
        StringBuilder fullName = new StringBuilder();

        if (!StringUtils.isBlank(lastName)) {
            fullName.append(lastName);

            if (!StringUtils.isBlank(firstName)) {
                fullName.append(", ");
            }
        }

        if (!StringUtils.isBlank(firstName)) {
            fullName.append(firstName);
        }

        return fullName.toString();
    }

    /**
     * Re-consolidate per-user per-project data
     *
     * @param originalElements
     * @param reportCriteria
     * @return
     */
    private List<DetailedMatrixReportElement> sortFlatReportElements(List<FlatReportElement> originalElements, ReportCriteria reportCriteria) {
        List<DetailedMatrixReportElement> maxtrixReportElements = new ArrayList<>();
        Map<String, Map<String, List<FlatReportElement>>> tempPool = Maps.newHashMap();
        Map<String, Integer> projectNameIdMapping = Maps.newHashMap();

        logger.debug("originalElements contains " + originalElements.size() + " records");

        // Stage 1 sorting by project and user
        for (FlatReportElement flatElement : originalElements) {
            String projectName = flatElement.getProjectName();
            String fullUserName = getFullName(flatElement.getUserLastName(), flatElement.getUserFirstName());

            projectNameIdMapping.put(projectName, flatElement.getProjectId());

            // Get per-Project Pool
            if (!tempPool.containsKey(projectName)) {
                tempPool.put(projectName, new HashMap<String, List<FlatReportElement>>());
            }

            Map<String, List<FlatReportElement>> perProjectPool = tempPool.get(projectName);

            // Get per-User Pool
            if (!perProjectPool.containsKey(fullUserName)) {
                perProjectPool.put(fullUserName, new ArrayList<FlatReportElement>());
            }

            List<FlatReportElement> perProjectperUserList = perProjectPool.get(fullUserName);
            perProjectperUserList.add(flatElement);
        }

        // Stage 2 consolidation

        // get a quick sum of all users for each single day
        Map<Date, DailyUnit> perProjectDailySum = Maps.newTreeMap();

        // Create an element for a subtotal of the project
        DetailedMatrixReportElement timesheetTotalElement = new DetailedMatrixReportElement();

        // Initialization
        // FIXME: use resource name
        timesheetTotalElement.setProjectName(TIMESHEET_TOTAL);
        timesheetTotalElement.setProjectId(Integer.MAX_VALUE);
        timesheetTotalElement.setDateRange(reportCriteria.getReportRange());
        timesheetTotalElement.setUserId(Integer.MAX_VALUE);
        timesheetTotalElement.setUserName("");
        timesheetTotalElement.setShowProjectName(true);

        for (String projectName : tempPool.keySet()) {
            // Set to true so first DetailedMatrixElement of the project should set appropriate flags
            Integer order = Integer.valueOf(0);

            // Get project id
            Integer projectId = projectNameIdMapping.get(projectName);

            // No projectId? Something is wrong
            if (projectId == null) {
                throw new IllegalStateException(projectName + " has no ProjectId");
            }

            // In a single project, user to daily hours mapping
            Map<String, List<FlatReportElement>> perProjectPool = tempPool.get(projectName);

            if (perProjectPool == null) {
                throw new IllegalStateException(projectName + " has no per-user map");
            }

            // Create an element for a subtotal of the project
            DetailedMatrixReportElement perProjectTotalElement = new DetailedMatrixReportElement();

//            // Initialization
            perProjectTotalElement.setProjectName(projectName);
            perProjectTotalElement.setProjectId(projectId);
            perProjectTotalElement.setDateRange(reportCriteria.getReportRange());
            perProjectTotalElement.setUserId(Integer.MAX_VALUE - 1);
            perProjectTotalElement.setUserName("Total");
            perProjectTotalElement.setShowProjectName(false);

            // Get consolidated result per user per project
            for (String userName : perProjectPool.keySet()) {
                // Create a DetailedMatrixReportElement for each user
                DetailedMatrixReportElement perUserDetailedMatrixElement = new DetailedMatrixReportElement();

                // Initialization
                perUserDetailedMatrixElement.setProjectName(projectName);
                perUserDetailedMatrixElement.setProjectId(projectId);
                perUserDetailedMatrixElement.setDateRange(reportCriteria.getReportRange());
                perUserDetailedMatrixElement.setOrder(order++);

                // Get user's flat elements for this project
                List<FlatReportElement> perProjectperUserList = perProjectPool.get(userName);
                if (perProjectperUserList == null || perProjectperUserList.size() == 0) {
                    throw new IllegalStateException(userName + " has no or empty per-user per-project FlatReportElement list");
                }

                FlatReportElement firstFlatReportElement = perProjectperUserList.get(0);

                List<DailyUnit> dailyUnits = new ArrayList<>();
                for (FlatReportElement flatElement : perProjectperUserList) {
                    DailyUnit du = new DailyUnit();
                    du.setHours(flatElement.getTotalHours());
                    du.setDate(flatElement.getDayDate());

                    // Align to start of day 00:00:00 AM
                    DateUtil.nullifyTime(du.getDate());

                    // Get a total of hours of this user in this project
                    // FIXME: using floatValue of TotalHours
                    perUserDetailedMatrixElement.setTotalHours(perUserDetailedMatrixElement.getTotalHours().floatValue() + du.getHours().floatValue());

                    // Add to list
                    dailyUnits.add(du);

                    // Update daily sum of hours in this project
                    if (!perProjectDailySum.containsKey(du.getDate())) {
                        DailyUnit projectDU = new DailyUnit();
                        projectDU.setHours(du.getHours());
                        projectDU.setDate(du.getDate());
                        perProjectDailySum.put(du.getDate(), projectDU);
                    }
                    else {
                        DailyUnit projectDU = perProjectDailySum.get(du.getDate());
                        projectDU.setHours(projectDU.getHours().floatValue() + du.getHours().floatValue());
                    }
                }
                perUserDetailedMatrixElement.setDailyProjectElements(dailyUnits);

                perUserDetailedMatrixElement.setUserName(userName);

                // Leverage first user flat element to fill out a few values
                perUserDetailedMatrixElement.setFirstName(firstFlatReportElement.getUserFirstName());
                perUserDetailedMatrixElement.setLastName(firstFlatReportElement.getUserLastName());
                perUserDetailedMatrixElement.setProjectCode(firstFlatReportElement.getProjectCode());
                perUserDetailedMatrixElement.setCustomerId(firstFlatReportElement.getCustomerId());
                perUserDetailedMatrixElement.setUserId(firstFlatReportElement.getUserId());
                perUserDetailedMatrixElement.setProjectId(projectId);

                perProjectTotalElement.setCustomerId(firstFlatReportElement.getCustomerId());

                maxtrixReportElements.add(perUserDetailedMatrixElement);

                // Update perProjectTotalElement
                perProjectTotalElement.setTotalHours(perProjectTotalElement.getTotalHours().floatValue() + perUserDetailedMatrixElement.getTotalHours().floatValue());
            }

            // Update per-project's daily hours total
            List<DailyUnit> perTimesheetDailyUnits = new ArrayList<>();
            for (Date hoursDate : perProjectDailySum.keySet()) {
                perTimesheetDailyUnits.add(perProjectDailySum.get(hoursDate));
            }

            // According to report users, get a total of daily hours overall, not by each single project
            timesheetTotalElement.setDailyProjectElements(perTimesheetDailyUnits);

//            // For per-project, will show 0.0 hours of a day
//            perProjectTotalElement.setShowZeroHours(true);
//            perProjectTotalElement.setOrder(order++);

            // Append per-project's summary element
//            maxtrixReportElements.add(perProjectTotalElement);

            // Update timesheetTotalElement
            timesheetTotalElement.setTotalHours(perProjectTotalElement.getTotalHours().floatValue() + timesheetTotalElement.getTotalHours().floatValue());
        }

        // Append per-project's summary element
        timesheetTotalElement.setShowZeroHours(true);
        maxtrixReportElements.add(timesheetTotalElement);


        logger.debug("maxtrixReportElements contains " + maxtrixReportElements.size() + " records");
        for (DetailedMatrixReportElement ele : maxtrixReportElements) {
            logger.debug(ele.toString());
        }

        return maxtrixReportElements;
    }

    /**
     * Get detailed FlatReportElements, either from last query or from a new query
     * @param reload - true to reload from database, false to get last query if not null
     * @return
     */
    public List<FlatReportElement> getDetailedFlatReportElements(boolean reload) {
        if (lastQueriedDetailedFlatReportElements != null && !reload) {
            return lastQueriedDetailedFlatReportElements;
        }
        else {
            return (List<FlatReportElement>)fetchReportData(getReportCriteria()).getReportElements();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public  ReportData preprocess(ReportData reportData, ReportCriteria reportCriteria) {

        List<FlatReportElement> originalElements = (List<FlatReportElement>) reportData.getReportElements();

        // Update reference
        this.lastQueriedDetailedFlatReportElements = originalElements;

        sortOnProjectAndUser(originalElements);

        List<DetailedMatrixReportElement> maxtrixReportElements = sortFlatReportElements(originalElements, reportCriteria);

//        sortOnProjectAndUser(maxtrixReportElements);

        ReportData preprocessedData = new ReportData(reportData.getLockedDays(), maxtrixReportElements, reportData.getReportRange(), reportCriteria.getUserSelectedCriteria());

        preprocessedData.setTotalReportColumns(FlatHoursNode.daysBetweenDates(reportCriteria.getReportRange().getDateStart(),
                reportCriteria.getReportRange().getDateEnd()) + FIXED_COLUMN_NUMBERS);

        return preprocessedData;
    }

    private void sortOnProjectAndUser(List<FlatReportElement> reportElements) {
        Collections.sort(reportElements, new Comparator<FlatReportElement>() {
            @Override
            public int compare(FlatReportElement o1, FlatReportElement o2) {
                String comparableValue1 = o1.getProjectCode() + "_" + getFullName(o1.getUserLastName(), o1.getUserFirstName());
                String comparableValue2 = o2.getProjectCode() + "_" + getFullName(o2.getUserLastName(), o2.getUserFirstName());

                return comparableValue1.compareTo(comparableValue2);
            }
        });
    }

//    private void sortOnProjectAndUser(List<DetailedMatrixReportElement> reportElements) {
//        Collections.sort(reportElements, new Comparator<DetailedMatrixReportElement>() {
//            @Override
//            public int compare(DetailedMatrixReportElement o1, DetailedMatrixReportElement o2) {
//                String comparableValue1 = o1.getProjectCode() + "_" + getFullName(o1.getLastName(), o1.getFirstName());
//                String comparableValue2 = o2.getProjectCode() + "_" + getFullName(o2.getLastName(), o2.getFirstName());
//
//                return comparableValue1.compareTo(comparableValue2);
//            }
//        });
//    }

    private DetailedReportService getDetailedReportService() {
        if (detailedReportService == null) {
            WebUtils.springInjection(this);
        }

        return detailedReportService;
    }

    /**
     * Build dynamic report columns
     *
     * @param criteria
     * @return
     */
    private ReportColumn[] buildReportColumns(ReportCriteria criteria) {
        int totalDays = FlatHoursNode.daysBetweenDates(criteria.getReportRange().getDateStart(), criteria.getReportRange().getDateEnd());
        int totalColumns = totalDays + DetailedMatrixReportModel.FIXED_COLUMN_NUMBERS;
        ReportColumn [] reportColumns = new ReportColumn[totalColumns];
        SimpleDateFormat formatter = new SimpleDateFormat("MM/dd");
        Date startDate = new Date();
        startDate.setTime(criteria.getReportRange().getDateStart().getTime());
        DateUtil.nullifyTime(startDate);

        for (int i = 0; i < (totalColumns - 1); ++i) {
            switch (i) {
                case 0:
                    reportColumns[i] = new ReportColumn("Project", ColumnType.STRING, VISIBLE);
                    reportColumns[i].setDefaultColumnName("Project");
                    break;
                case 1:
                    reportColumns[i] = new ReportColumn("Resource Name", ColumnType.STRING, VISIBLE, ALLOW_DUPLICATES);
                    reportColumns[i].setDefaultColumnName("Resource Name");
                    break;
                default:
                    String dateString = formatter.format(startDate);
                    Calendar calDate = Calendar.getInstance();
                    calDate.setTime(startDate);
                    reportColumns[i] = new ReportColumn(dateString, ColumnType.HOUR, VISIBLE, ALLOW_DUPLICATES);
                    reportColumns[i].setDefaultColumnName(dateString);
                    // Weekend? mark date in RED
//                    if (DateUtil.isWeekend(calDate)) {
//                        reportColumns[i].setOverrideExcelStyle(ExcelStyle.WEEKEND_FONT);
//                    }
                    startDate.setTime(startDate.getTime() + 24 * 3600000);
                    break;
            }
        }
        reportColumns[totalColumns - 1] = new ReportColumn("Total", ColumnType.HOUR, VISIBLE, ALLOW_DUPLICATES);
        reportColumns[totalColumns - 1].setDefaultColumnName("Total");

        return reportColumns;
    }

    @Override
    public ReportNodeFactory<DetailedMatrixReportElement> getReportNodeFactory() {
        return new ReportNodeFactory<DetailedMatrixReportElement>() {
            @Override
            public ReportNode createReportNode(DetailedMatrixReportElement flatElement, int hierarchyLevel) {
//                // Header Columns?
//                if (flatElement.isHeader()) {
//                    return new FlatHeaderNode(flatElement);
//                }

                switch (hierarchyLevel) {
                    case 0:
                        return new FlatProjectNode(flatElement);
                    case 1:
                        return new FlatUserNode(flatElement);
                    case 2:
                        return new FlatHoursNode(flatElement);
                }

                throw new RuntimeException("Hierarchy level too deep");
            }

            /**
             * Only needed for the root node, customer
             */
            public Serializable getElementId(DetailedMatrixReportElement flatElement) {
                return flatElement.getCustomerId();
            }
        };
    }

    @Override
    public ReportColumn[] getReportColumns() {
        // DetailedMatrix report will provide dynamic columns
        return buildReportColumns(getReportCriteria());
    }
}
