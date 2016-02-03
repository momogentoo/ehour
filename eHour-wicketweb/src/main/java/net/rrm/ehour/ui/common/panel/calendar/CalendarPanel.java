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

package net.rrm.ehour.ui.common.panel.calendar;

import net.rrm.ehour.calendar.service.CalendarService;
import net.rrm.ehour.config.EhourConfig;
import net.rrm.ehour.data.DateRange;
import net.rrm.ehour.domain.CalendarException;
import net.rrm.ehour.domain.CalendarExceptionType;
import net.rrm.ehour.domain.User;
import net.rrm.ehour.ui.common.decorator.LoadingSpinnerDecorator;
import net.rrm.ehour.ui.common.event.AjaxEvent;
import net.rrm.ehour.ui.common.event.EventPublisher;
import net.rrm.ehour.ui.common.event.PayloadAjaxEvent;
import net.rrm.ehour.ui.common.formguard.GuardFormAjaxCallListener;
import net.rrm.ehour.ui.common.model.DateModel;
import net.rrm.ehour.ui.common.panel.sidepanel.SidePanel;
import net.rrm.ehour.ui.common.session.EhourWebSession;
import net.rrm.ehour.ui.common.util.HtmlUtil;
import net.rrm.ehour.util.DateUtil;
import org.apache.log4j.Logger;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.ajax.attributes.IAjaxCallListener;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.spring.injection.annot.SpringBean;
import org.joda.time.LocalDate;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import static java.util.Calendar.DAY_OF_MONTH;
import static java.util.Calendar.DAY_OF_WEEK;

/**
 * Navigation Calendar
 */

public class CalendarPanel extends SidePanel {
    private static final long serialVersionUID = -7777893083323915299L;
    protected static final Logger logger = Logger.getLogger(CalendarPanel.class);

    private WebMarkupContainer calendarFrame;
    private User user;
    private boolean fireWeekClicks;
    private DateRange highlightWeekStartingAt;
    private EhourConfig config;

    private CalendarWeekFactory calendarWeekFactory;

    @SpringBean
    private CalendarService calendarService;

    public CalendarPanel(String id, User user) {
        this(id, user, true);
    }

    public CalendarPanel(String id, User user, boolean allowWeekClicks) {
        super(id);

        calendarWeekFactory = new CalendarWeekFactory();

        setOutputMarkupId(true);

        config = EhourWebSession.getEhourConfig();

        this.user = user;
        fireWeekClicks = allowWeekClicks;

        calendarFrame = getFrame();
        add(calendarFrame);

        buildCalendar(calendarFrame);
    }

    private void buildCalendar(WebMarkupContainer parent) {
        // first get the data
        Calendar month = EhourWebSession.getSession().getNavCalendar();

        List<CalendarWeek> weeks = calendarWeekFactory.createWeeks(config.getFirstDayOfWeek(), user.getUserId(), ((GregorianCalendar) month.clone()));

        // set month label
        parent.add(new Label("currentMonth", new DateModel(month, EhourWebSession.getEhourConfig(), DateModel.DATESTYLE_MONTHONLY)));

        // previous & next month links
        parent.add(createPreviousMonthLink("previousMonthLink"));

        parent.add(createNextMonthLink("nextMonthLink"));

        // content
        addCalendarWeeks(parent, weeks);
    }

    public void refreshCalendar(AjaxRequestTarget target) {
        WebMarkupContainer replacementFrame = getFrame();
        buildCalendar(replacementFrame);

        calendarFrame.replaceWith(replacementFrame);
        calendarFrame = replacementFrame;
        target.add(replacementFrame);
    }

    private WebMarkupContainer getFrame() {
        WebMarkupContainer calendarFrame = new WebMarkupContainer("calendarFrame");
        calendarFrame.setOutputMarkupId(true);

        return calendarFrame;
    }

    private AjaxLink<Void> createNextMonthLink(String id) {
        AjaxLink<Void> nextMonthLink = new ChangeMonthLink(id, 1);
        return nextMonthLink;
    }

    private AjaxLink<Void> createPreviousMonthLink(String id) {
        AjaxLink<Void> previousMonthLink = new ChangeMonthLink(id, -1);
        return previousMonthLink;
    }

    @SuppressWarnings("serial")
    private void addCalendarWeeks(WebMarkupContainer container, final List<CalendarWeek> weeks) {

        // Load calendar exceptions according to the country code in user profile
        List<CalendarException> calendarExceptions = calendarService.getCalendarExceptions(user.getCountry());
        final Map<LocalDate, CalendarException> processedCalendarException = calendarService.prepCalendarExceptions(calendarExceptions);


        ListView<CalendarWeek> view = new ListView<CalendarWeek>("weeks", weeks) {
            public void populateItem(final ListItem<CalendarWeek> item) {
                CalendarWeek week = item.getModelObject();
                Calendar renderDate = (Calendar) week.getWeekStart().clone();

                boolean isCurrentWeek = highlightWeekStartingAt != null && DateUtil.isDateWithinRange(week.getWeekStart().getTime(), highlightWeekStartingAt);


                for (int dayInWeek = 1; dayInWeek <= 7; dayInWeek++) {
                    boolean weekend = DateUtil.isWeekend(renderDate);

                    int currentDay = renderDate.get(DAY_OF_WEEK);
                    CalendarDay day = week.getDay(currentDay);

                    LocalDate localDate = new LocalDate(renderDate.getTime());
                    CalendarException exceptionInfo = processedCalendarException.get(localDate);

                    Label label = createLabel(dayInWeek, week, day, weekend, exceptionInfo);
                    item.add(label);

                    renderDate.add(Calendar.DATE, 1);
                }

                item.setOutputMarkupId(true);

                if (fireWeekClicks) {
                    fireWeekClicks(item, week, isCurrentWeek);
                } else {
                    item.add(AttributeModifier.replace("class", "CalendarWeek other"));
                    item.add(AttributeModifier.replace("style", "cursor:default"));
                }

                if (item.getIndex() == weeks.size() - 1) {
                    item.add(AttributeModifier.append("class", "LastWeek"));
                }

                // Override background of day for holiday and exceptional working day

            }

            private void fireWeekClicks(ListItem<CalendarWeek> item, CalendarWeek week, boolean isCurrentWeek) {
                if (isCurrentWeek) {
                    item.add(AttributeModifier.replace("class", "CalendarWeek selectedWeek"));
                } else {
                    item.add(AttributeModifier.replace("class", "CalendarWeek other"));
                    item.add(new WeekClick("onclick", week.getWeek(), week.getYear()));
                }
            }

            private Label createLabel(int dayOfWeek, CalendarWeek week, CalendarDay day, boolean weekend,
                                      CalendarException calendarException) {
                String id = "day" + dayOfWeek;

                // when day is null the date is in the next/previous month
                Label label = day == null ? HtmlUtil.getNbspLabel(id) : new Label(id, new PropertyModel<Integer>(day, "monthDay"));

                Boolean hasCalendarException = (calendarException != null);
                CalendarExceptionType calendarExceptionType = CalendarExceptionType.NORMAL_DAY;
                if (hasCalendarException) {
                    calendarExceptionType = CalendarExceptionType.getCalendarExceptionTypeByValue(calendarException.getExceptionType());
                }


                // determine css class
                StringBuilder cssClass = new StringBuilder();

                // Is it a normal day? no exception? apply appropriate style
                switch (calendarExceptionType) {
                    case NORMAL_DAY:
//                        cssClass.append(weekend ? "WeekendDay" : "");
                        break;
                    case NON_WORKING_DAY:
                        cssClass.append("HolidayDay");
                        break;
                    case WORKING_DAY:
                        cssClass.append("ExceptionalWorkingDay");
                        break;
                    default:
                        break;
                }

                cssClass.append(weekend ? " WeekendDay" : "");

                // booked days are bold
                if (day != null && day.isBooked()) {
                    if (!day.isOverBooked()) {
                        cssClass.append(" Booked");
                    }
                    else {
                        // Mark overbooked day in RED
                        cssClass.append(" OverBooked");
                    }
                }

                // first day doesn't have margin-left
                if (dayOfWeek == 1) {
                    cssClass.append(" FirstDay");
                }

                if (dayOfWeek == 7) {
                    if (week.getLocation() == ElementLocation.LAST) {
                        cssClass.append(" LastDayOfLastRow");
                    }
                }

                label.add(AttributeModifier.replace("class", cssClass.toString()));

                return label;
            }
        };

        container.add(view);
    }

    /**
     * Changes the month
     *
     * @author Thies
     */
    private class ChangeMonthLink extends AjaxLink<Void> {
        private static final long serialVersionUID = 1L;
        private int monthChange;

        public ChangeMonthLink(String id, int monthChange) {
            super(id);

            this.monthChange = monthChange;
        }

        @Override
        public void onClick(AjaxRequestTarget target) {
            EhourWebSession session = EhourWebSession.getSession();
            Calendar month = session.getNavCalendar();
            month.add(Calendar.MONTH, monthChange);
            month.set(DAY_OF_MONTH, 1);
            session.setNavCalendar(month);

            // do it before it gets replaced, otherwise getPage is null due to new instantiation of links
            EventPublisher.publishAjaxEvent(ChangeMonthLink.this, new AjaxEvent(CalendarAjaxEventType.MONTH_CHANGE));

            refreshCalendar(target);

            this.setEnabled(false);
        }

        @Override
        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
            super.updateAjaxAttributes(attributes);
            List<IAjaxCallListener> listeners = attributes.getAjaxCallListeners();
            listeners.add(new GuardFormAjaxCallListener());
            listeners.add(new LoadingSpinnerDecorator());
        }
    }

    private class WeekClick extends AjaxEventBehavior {
        private static final long serialVersionUID = 9164386260367481606L;

        private int week, year;

        public WeekClick(String id, int week, int year) {
            super(id);
            this.week = week;
            this.year = year;
        }

        @Override
        protected void onEvent(AjaxRequestTarget target) {
            EhourWebSession session = EhourWebSession.getSession();
            Calendar cal = DateUtil.getCalendar(session.getEhourConfig());

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.WEEK_OF_YEAR, week);
            DateUtil.dayOfWeekFix(cal);
            cal.set(DAY_OF_WEEK, config.getFirstDayOfWeek());

            session.setNavCalendar(cal);

            EventPublisher.publishAjaxEvent(CalendarPanel.this, new PayloadAjaxEvent<Calendar>(CalendarAjaxEventType.WEEK_CLICK, cal));
        }

        @Override
        protected void updateAjaxAttributes(AjaxRequestAttributes attributes) {
            super.updateAjaxAttributes(attributes);
            List<IAjaxCallListener> listeners = attributes.getAjaxCallListeners();
            listeners.add(new GuardFormAjaxCallListener());
            listeners.add(new LoadingSpinnerDecorator());
        }
    }

    public void setHighlightWeekStartingAt(DateRange highlightWeekStartingAt) {
        this.highlightWeekStartingAt = highlightWeekStartingAt;
    }
}
