package net.rrm.ehour.ui.report.matrix;

import net.rrm.ehour.data.DateRange;
import net.rrm.ehour.report.reports.element.ProjectStructuredReportElement;

import java.util.List;

/**
 * A Re-consolidated element for detailed matrix report
 *
 * Daily Unit (hours, comments) of a single user for each project has one such element
 *
 * The element also stores flags to help construct ReportNode
 */
public class DetailedMatrixReportElement implements ProjectStructuredReportElement {
    private Integer customerId;
    private String projectName;
    private String projectCode;
    private Integer projectId;
    private Integer userId;
    private String userName;
    private List<DailyUnit> dailyProjectElements;
    private Number totalHours;
    private String firstName;
    private String lastName;
    private DateRange dateRange;
    private boolean showProjectName;
    private boolean  showZeroHours;
    private Integer order;

    public DetailedMatrixReportElement() {
        totalHours = 0.0;
        showProjectName = false;
        showZeroHours = false;
        order = Integer.valueOf(0);
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<DailyUnit> getDailyProjectElements() {
        return dailyProjectElements;
    }

    public void setDailyProjectElements(List<DailyUnit> dailyProjectElements) {
        this.dailyProjectElements = dailyProjectElements;
    }

    public Number getTotalHours() {
        return totalHours;
    }

    public void setTotalHours(Number totalHours) {
        this.totalHours = totalHours;
    }

    @Override
    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public Boolean getShowProjectName() {
        return showProjectName;
    }

    public void setShowProjectName(Boolean showProjectName) {
        this.showProjectName = showProjectName;
    }


    public String getProjectCode() {
        return projectCode;
    }

    public void setProjectCode(String projectCode) {
        this.projectCode = projectCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public Integer getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Integer customerId) {
        this.customerId = customerId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public boolean isShowZeroHours() {
        return showZeroHours;
    }

    public void setShowZeroHours(boolean showZeroHours) {
        this.showZeroHours = showZeroHours;
    }

    public Integer getOrder() {
        return order;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    @Override
    public Boolean isEmptyEntry() {
        return false;
    }

    @Override
    public String toString() {
        return (showProjectName ? projectName : "") + ", "
                + userName + ", "
                + totalHours;
    }
}
