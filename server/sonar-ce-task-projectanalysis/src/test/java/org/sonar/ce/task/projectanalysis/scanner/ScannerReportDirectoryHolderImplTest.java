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
package org.sonar.ce.task.projectanalysis.scanner;

import java.io.File;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ScannerReportDirectoryHolderImplTest {

  @Test
  public void getDirectory_throws_ISE_if_holder_is_empty() {
    assertThatThrownBy(() ->  new ScannerReportDirectoryHolderImpl().getDirectory())
      .isInstanceOf(IllegalStateException.class);
  }

  @Test
  public void getDirectory_returns_File_set_with_setDirectory() {
    File file = new File("");
    ScannerReportDirectoryHolderImpl holder = new ScannerReportDirectoryHolderImpl();
    holder.setDirectory(file);

    assertThat(holder.getDirectory()).isSameAs(file);
  }
}
