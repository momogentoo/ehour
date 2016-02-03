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

package net.rrm.ehour.ui.timesheet.panel;

import net.rrm.ehour.config.EhourConfig;
import net.rrm.ehour.domain.*;
import net.rrm.ehour.timesheet.dto.TimesheetOverview;
import net.rrm.ehour.ui.common.border.GreyBlueRoundedBorder;
import net.rrm.ehour.ui.common.border.GreyRoundedBorder;
import net.rrm.ehour.ui.common.component.sort.TimesheetEntryComparator;
import net.rrm.ehour.ui.common.model.DateModel;
import net.rrm.ehour.ui.common.panel.calendar.CalendarPanel;
import net.rrm.ehour.ui.common.session.EhourWebSession;
import net.rrm.ehour.ui.common.util.HtmlUtil;
import net.rrm.ehour.util.DateUtil;
import net.sf.cglib.core.Local;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Fragment;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.RepeatingView;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.ResourceModel;
import org.joda.time.LocalDate;

import java.util.*;

/**
 * Month overview panel for consultants
 */

public class MonthOverviewPanel extends Panel {
    private static final long serialVersionUID = -8977205040520638758L;

    protected static final Logger logger = Logger.getLogger(MonthOverviewPanel.class);

    private final TimesheetOverview timesheetOverview;
    private final Map<LocalDate, Map<String, CalendarException>> processedCalendarException;
    private final User user;
    private final int thisMonth;
    private final int thisYear;
    private final Calendar overviewFor;
    private static final TimesheetEntryComparator comparator = new TimesheetEntryComparator();
    private static final String BRACKET_NON_LOCAL_HOLIDAY_LEFT = "[";
    private static final String BRACKET_NON_LOCAL_HOLIDAY_RIGHT = "]";
    private static final String BRACKET_LOCAL_HOLIDAY_LEFT = "<";
    private static final String BRACKET_LOCAL_HOLIDAY_RIGHT = ">";

    public MonthOverviewPanel(String id, TimesheetOverview timesheetOverview, final Calendar overviewForMonth,
                              final Map<LocalDate, Map<String, CalendarException>> processedCalendarException,
                              final User user) {
        super(id);

        setOutputMarkupId(true);

        EhourConfig config = EhourWebSession.getEhourConfig();

        this.timesheetOverview = timesheetOverview;
        this.processedCalendarException = processedCalendarException;
        this.user = user;
        thisMonth = overviewForMonth.get(Calendar.MONTH);
        thisYear = overviewForMonth.get(Calendar.YEAR);

        this.overviewFor = (Calendar) overviewForMonth.clone();
        DateUtil.dayOfWeekFix(overviewFor);
        overviewFor.set(Calendar.DAY_OF_WEEK, config.getFirstDayOfWeek());

        GreyRoundedBorder greyBorder = new GreyRoundedBorder("greyFrame",
                new ResourceModel("monthoverview.overview"));
        GreyBlueRoundedBorder blueBorder = new GreyBlueRoundedBorder("blueFrame");

        greyBorder.add(blueBorder);
        add(greyBorder);

        addDayLabels(blueBorder, config);

        createMonthCalendar(blueBorder);
    }

    private void createMonthCalendar(WebMarkupContainer parent) {
        RepeatingView calendarView = new RepeatingView("calendarView");

        while ((overviewFor.get(Calendar.YEAR) == thisYear) &&
                (overviewFor.get(Calendar.MONTH) <= thisMonth) || overviewFor.get(Calendar.YEAR) < thisYear) {
            WebMarkupContainer row = new WebMarkupContainer(calendarView.newChildId());
            calendarView.add(row);

            createWeek(row);
        }

        parent.add(calendarView);
    }

    private void createWeek(WebMarkupContainer row) {
        row.add(new Label("weekNumber", Integer.toString(overviewFor.get(Calendar.WEEK_OF_YEAR))));

        addDayNumbersToWeek(row);
        addDayValuesToWeek(row);
    }

    private void addDayValuesToWeek(WebMarkupContainer row) {
        for (int i = 1; i <= 7; i++, overviewFor.add(Calendar.DATE, 1)) {
            String dayId = "day" + i + "Value";

            if (overviewFor.get(Calendar.MONTH) == thisMonth) {
                // Try to get calendar exception info
                LocalDate localDate = new LocalDate(overviewFor.getTime());
                Map<String, CalendarException> calendarExceptions = processedCalendarException.get(localDate);

                row.add(createDay(dayId, calendarExceptions));

            } else {
                row.add(createEmptyDay(dayId));
            }
        }
    }

    /**
     * Create a virtual TimesheetEntry to display calenar exception information
     * @param calendarExceptions
     * @return
     */
    private List<TimesheetEntry> createCalendarExceptionInfo(Map<String, CalendarException> calendarExceptions) {
        String userCountry = user.getCountry();
        List<TimesheetEntry> virtualTimesheetEntries = new ArrayList<>();


        for (String countryCode : calendarExceptions.keySet()) {
            CalendarException calendarException = calendarExceptions.get(countryCode);
            Boolean isLocalHoliday = false;
            int projectId = 0;

            if (userCountry != null && countryCode.equalsIgnoreCase(userCountry)) {
                isLocalHoliday = true;
            }

            /*
                Determine brackets and prefix to use
                Local holiday looks like  <New Year's Day>
                Non-Local holiday looks like [US: MLK Day]
             */
            String effBracketLeft = isLocalHoliday ? BRACKET_LOCAL_HOLIDAY_LEFT : BRACKET_NON_LOCAL_HOLIDAY_LEFT;
            String effBracketRight = isLocalHoliday ? BRACKET_LOCAL_HOLIDAY_RIGHT : BRACKET_NON_LOCAL_HOLIDAY_RIGHT;
            String effCountryPrefix = isLocalHoliday ? "" : (countryCode + ": ");

            String description = calendarException.getDescription();

            // Add default description if not provided in table
            if (description == null || description.length() == 0) {
                switch (CalendarExceptionType.getCalendarExceptionTypeByValue(calendarException.getExceptionType())) {
                    case NON_WORKING_DAY:
                        description = effBracketLeft + effCountryPrefix + "HOLIDAY" + effBracketRight;
                        break;
                    case WORKING_DAY:
                        description = effBracketLeft + effCountryPrefix + "SP. WORKING DAY" + effBracketRight;
                        break;
                    case NORMAL_DAY:
                        description = effBracketLeft + effCountryPrefix + "NORMAL DAY" + effBracketRight;
                        break;
                    default:
                        break;
                }
            } else {
                description = effBracketLeft + effCountryPrefix + description + effBracketRight;
            }

            // Create virtual time sheet entry
            Project virtualProject = new Project(Integer.valueOf(projectId--));
            virtualProject.setProjectCode(description);

            ProjectAssignment virtualAssignment = new ProjectAssignment();
            virtualAssignment.setProject(virtualProject);

            TimesheetEntryId entryId = new TimesheetEntryId(Calendar.getInstance().getTime(), virtualAssignment);
            TimesheetEntry entry = new TimesheetEntry(entryId);

            virtualTimesheetEntries.add(entry);
        }

        return virtualTimesheetEntries;
    }

    private Fragment createDay(String dayId, Map<String, CalendarException> calendarExceptions) {
        Fragment fragment;

        List<TimesheetEntry> timesheetEntries = null;

        if (timesheetOverview.getTimesheetEntries() != null) {
            timesheetEntries = timesheetOverview.getTimesheetEntries().get(overviewFor.get(Calendar.DAY_OF_MONTH));
        }

        if ((calendarExceptions != null) || (timesheetEntries != null && timesheetEntries.size() > 0)) {
            fragment = createDayContents(dayId, timesheetEntries, calendarExceptions);
        } else {
            fragment = new Fragment(dayId, "noProjects", this);
        }

        if (DateUtil.isWeekend(overviewFor)) {
            fragment.add(AttributeModifier.replace("style", "background-color: #eef6fe"));
        }

        return fragment;
    }

    @SuppressWarnings("serial")
    private Fragment createDayContents(String dayId, List<TimesheetEntry> timesheetEntries, Map<String, CalendarException> calendarExceptions) {
        Fragment fragment;
        fragment = new Fragment(dayId, "showProjects", this);

        // timesheetEntries could be null
        if (timesheetEntries == null) {
            timesheetEntries = new ArrayList<>();
        }

        // Insert calendar exception first
        if (calendarExceptions != null) {
            List<TimesheetEntry> virtualCalendarEntries = createCalendarExceptionInfo(calendarExceptions);
            timesheetEntries.addAll(virtualCalendarEntries);
        }


        //sort by Project Code
        if(timesheetEntries != null) {
            Collections.sort(timesheetEntries, comparator);


            ListView projects = new ListView<TimesheetEntry>("projects", timesheetEntries) {
                @Override
                protected void populateItem(ListItem<TimesheetEntry> item) {
                    TimesheetEntry entry = item.getModelObject();

                    Project project = entry.getEntryId().getProjectAssignment().getProject();
                    Label projectCodeLabel = new Label("projectCode", project.getProjectCode());
                    projectCodeLabel.setMarkupId(String.format("prjV%d", project.getProjectId()));
                    projectCodeLabel.setOutputMarkupId(true);

                    logger.debug(project.getProjectCode());

                    item.add(projectCodeLabel);
                    if (entry.getHours() != null) {
                        item.add(new Label("hours", new Model<Float>(entry.getHours())));
                    }
                    else {
                        item.add(new Label("hours", new Model<String>("")));
                    }
                }
            };

            fragment.add(projects);
        }


        return fragment;
    }

    private Label createEmptyDay(String dayId) {
        Label label = HtmlUtil.getNbspLabel(dayId);

        if (monthIsBeforeCurrent(overviewFor, thisMonth, thisYear)) {
            label.add(AttributeModifier.replace("class", "noMonthBefore"));
        } else {
            label.add(AttributeModifier.replace("class", "noMonthAfter"));
        }
        return label;
    }

    private void addDayNumbersToWeek(WebMarkupContainer row) {
        for (int i = 1; i <= 7; i++, overviewFor.add(Calendar.DATE, 1)) {
            Label dayLabel;
            String id = "day" + i;

            //
            if (overviewFor.get(Calendar.MONTH) == thisMonth) {
                dayLabel = new Label(id, Integer.toString(overviewFor.get(Calendar.DAY_OF_MONTH)));
            }
            // print space holders if not current month
            else {
                dayLabel = HtmlUtil.getNbspLabel(id);

                if (!monthIsBeforeCurrent(overviewFor, thisMonth, thisYear)) {
                    dayLabel.add(AttributeModifier.replace("class", "noMonth"));
                }
            }

            row.add(dayLabel);
        }

        // reset the abused calendar
        overviewFor.add(Calendar.DATE, -7);
    }

    private boolean monthIsBeforeCurrent(Calendar calendar, int thisMonth, int thisYear) {
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        return month < thisMonth && year == thisYear ||
                year < thisYear;
    }

    private void addDayLabels(WebMarkupContainer parent, EhourConfig config) {
        Calendar cal = new GregorianCalendar();
        cal.setFirstDayOfWeek(config.getFirstDayOfWeek());
        cal.set(Calendar.DAY_OF_WEEK, config.getFirstDayOfWeek());

        for (int dayNumber = 1; dayNumber <= 7; dayNumber++, cal.add(Calendar.DAY_OF_WEEK, 1)) {
            parent.add(new Label("day" + dayNumber, new DateModel(cal, config, DateModel.DATESTYLE_TIMESHEET_DAYONLY)));
        }
    }
}
