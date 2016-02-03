package net.rrm.ehour.ui.report.matrix

import net.rrm.ehour.report.criteria.ReportCriteria
import net.rrm.ehour.report.reports.ReportData
import net.rrm.ehour.ui.common.panel.AbstractBasePanel
import net.rrm.ehour.ui.common.report.DetailedMatrixReportConfig
import net.rrm.ehour.ui.report.cache.ReportCacheService
import net.rrm.ehour.ui.report.detailed.{AggregateByChangedEvent, AggregateByDatePanel, DetailedReportChartContainer}
import net.rrm.ehour.ui.report.model.{TreeReportData, TreeReportModel}
import net.rrm.ehour.ui.report.panel.{TreeReportDataPanel, UpdateReportDataEvent}
import org.apache.wicket.event.{Broadcast, IEvent}
import org.apache.wicket.markup.html.WebMarkupContainer
import org.apache.wicket.model.PropertyModel
import org.apache.wicket.spring.injection.annot.SpringBean

object DetailedMatrixReportPanel {
}

class DetailedMatrixReportPanel(id: String, report: DetailedMatrixReportModel) extends AbstractBasePanel[DetailedMatrixReportModel](id) {
  val Self = this

  setDefaultModel(report)
  setOutputMarkupId(true)

  @SpringBean
  var reportCacheService: ReportCacheService = _

  protected override def onBeforeRender() {
    val reportConfig = DetailedMatrixReportConfig.DETAILED_REPORT_BY_DAY

    val excel = new DetailedMatrixReportExcel(new PropertyModel[ReportCriteria](report, "reportCriteria"))

    addOrReplace(new TreeReportDataPanel("reportTable", report, reportConfig, excel) {
      //protected override def createAdditionalOptions(id: String): WebMarkupContainer = new AggregateByDatePanel(id, report.getReportCriteria.getUserSelectedCriteria)
    })

    val reportData: ReportData = rawReportData()
    val cacheKey = storeReportData(reportData)

    val chartContainer = new DetailedReportChartContainer("chart", cacheKey)
    chartContainer.setVisible(!reportData.isEmpty)
    addOrReplace(chartContainer)

    super.onBeforeRender()
  }

  private def rawReportData():ReportData = {
    val reportModel = getDefaultModel.asInstanceOf[TreeReportModel]
    val treeReportData = reportModel.getReportData.asInstanceOf[TreeReportData]
    treeReportData.getRawReportData
  }

  private def storeReportData(data: ReportData) = reportCacheService.storeReportData(data)

  override def onEvent(event: IEvent[_]) = {
    event.getPayload match {
      case aggregateByChangedEvent: AggregateByChangedEvent =>
        val cacheKey = storeReportData(rawReportData())

        val reportDataEvent = new UpdateReportDataEvent(aggregateByChangedEvent.target, cacheKey, aggregateByChangedEvent.reportConfig)

        send(Self, Broadcast.BREADTH, reportDataEvent)
      case _ =>
    }
  }
}

