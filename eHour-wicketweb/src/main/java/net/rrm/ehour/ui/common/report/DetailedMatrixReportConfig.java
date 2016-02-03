package net.rrm.ehour.ui.common.report;

public enum DetailedMatrixReportConfig implements ReportConfig {
    // constructors like these might be a bit over the top..
    // take note, the columnResourceKey is used for serie creation with trend charts (pardon my English, it's late.. or early in the morning)
    DETAILED_REPORT_BY_DAY(ReportType.SHOW_ZERO_BOOKINGS, "report.criteria.zerobookings.detailed",
            new ReportColumn("userReport.report.project"),
            new ReportColumn("userReport.report.user"));


    private final String zeroBookingsMessageKey;
    private ReportColumn[] reportColumns;
    private Boolean showZeroBookings;

    private DetailedMatrixReportConfig(ReportType zeroBookings, String zeroBookingsMessageKey, ReportColumn... reportColumns) {
        this.zeroBookingsMessageKey = zeroBookingsMessageKey;
        this.reportColumns = reportColumns;
        this.showZeroBookings = zeroBookings == ReportType.SHOW_ZERO_BOOKINGS;
    }

    public ReportColumn[] getReportColumns() {
        return reportColumns;
    }

    public Boolean isShowZeroBookings() {
        return showZeroBookings;
    }

    public String getZeroBookingsMessageKey() {
        return zeroBookingsMessageKey;
    }
}