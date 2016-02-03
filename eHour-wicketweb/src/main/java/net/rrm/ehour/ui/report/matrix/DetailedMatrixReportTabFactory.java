package net.rrm.ehour.ui.report.matrix;

import com.google.common.base.Optional;
import net.rrm.ehour.report.criteria.ReportCriteria;
import net.rrm.ehour.ui.common.model.KeyResourceModel;
import net.rrm.ehour.ui.report.builder.ReportFactory;
import net.rrm.ehour.ui.report.builder.ReportTabFactory;
import org.apache.wicket.extensions.markup.html.tabs.AbstractTab;
import org.apache.wicket.extensions.markup.html.tabs.ITab;
import org.apache.wicket.markup.html.panel.Panel;

@ReportFactory
public class DetailedMatrixReportTabFactory implements ReportTabFactory {
    @Override
    public Optional<ITab> createReportTab(final ReportCriteria criteria) {
        return Optional.<ITab>of(new AbstractTab(new KeyResourceModel("report.title.matrix")) {
            @Override
            public Panel getPanel(String panelId) {
                return getDetailedMatrixReportPanel(panelId, criteria);
            }
        });
    }

    @Override
    public int getRenderPriority() {
        return 5;
    }

    private Panel getDetailedMatrixReportPanel(String id, ReportCriteria reportCriteria) {
        DetailedMatrixReportModel detailedReport = new DetailedMatrixReportModel(reportCriteria);
        return new DetailedMatrixReportPanel(id, detailedReport);
    }
}
