/*
 * SonarQube
 * Copyright (C) 2009-2025 SonarSource SA
 * mailto:info AT sonarsource DOT com
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.sonar.ce.task.projectanalysis.component;

import org.sonar.ce.task.projectanalysis.analysis.AnalysisMetadataHolder;
import org.sonar.ce.task.projectanalysis.period.NewCodeReferenceBranchComponentUuids;
import org.sonar.ce.task.projectanalysis.period.PeriodHolder;

import static org.sonar.db.newcodeperiod.NewCodePeriodType.REFERENCE_BRANCH;

public class BranchComponentUuidsDelegate {
  private final AnalysisMetadataHolder analysisMetadataHolder;
  private final PeriodHolder periodHolder;
  private final ReferenceBranchComponentUuids referenceBranchComponentUuids;
  private final NewCodeReferenceBranchComponentUuids newCodeReferenceBranchComponentUuids;

  public BranchComponentUuidsDelegate(AnalysisMetadataHolder analysisMetadataHolder, PeriodHolder periodHolder,
    ReferenceBranchComponentUuids referenceBranchComponentUuids, NewCodeReferenceBranchComponentUuids newCodeReferenceBranchComponentUuids) {
    this.analysisMetadataHolder = analysisMetadataHolder;
    this.periodHolder = periodHolder;
    this.referenceBranchComponentUuids = referenceBranchComponentUuids;
    this.newCodeReferenceBranchComponentUuids = newCodeReferenceBranchComponentUuids;
  }

  public String getComponentUuid(String key) {
    if (analysisMetadataHolder.isPullRequest()) {
      return referenceBranchComponentUuids.getComponentUuid(key);
    }
    if (isNewCodePeriodReferenceBranch()) {
      return newCodeReferenceBranchComponentUuids.getComponentUuid(key);
    }
    if (!analysisMetadataHolder.getBranch().isMain()) {
      return referenceBranchComponentUuids.getComponentUuid(key);
    }
    return null;
  }

  private boolean isNewCodePeriodReferenceBranch() {
    return periodHolder.hasPeriod() && REFERENCE_BRANCH.name().equals(periodHolder.getPeriod().getMode());
  }

  public String getReferenceBranchName() {
    if (isNewCodePeriodReferenceBranch()) {
      return periodHolder.getPeriod().getModeParameter();
    }
    if (!analysisMetadataHolder.getBranch().isMain()) {
      return referenceBranchComponentUuids.getReferenceBranchName();
    }

    return analysisMetadataHolder.getBranch().getName();
  }
}
