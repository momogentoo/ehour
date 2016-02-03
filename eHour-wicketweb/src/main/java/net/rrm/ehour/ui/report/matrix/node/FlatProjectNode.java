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

package net.rrm.ehour.ui.report.matrix.node;

import net.rrm.ehour.report.reports.element.ReportElement;
import net.rrm.ehour.ui.report.matrix.DetailedMatrixReportElement;
import net.rrm.ehour.ui.report.model.ReportNode;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * Flat project report node
 */

public class FlatProjectNode extends ReportNode {
    private static final long serialVersionUID = -9117536402550375613L;
    protected static final Logger logger = Logger.getLogger(FlatProjectNode.class);

    public FlatProjectNode(DetailedMatrixReportElement element) {

        super(element.getProjectId(), element.isEmptyEntry());

        if (element.getOrder() == 0) {
            this.columnValues = new String[]{element.getProjectName()};
        }
        else {
            this.columnValues = new String[] { "" };
        }
    }

    @Override
    protected Serializable getElementId(ReportElement element) {
        return ((DetailedMatrixReportElement) element).getProjectId();
    }
}
