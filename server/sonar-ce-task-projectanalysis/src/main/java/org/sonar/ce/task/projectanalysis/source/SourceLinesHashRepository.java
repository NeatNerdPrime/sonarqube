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
package org.sonar.ce.task.projectanalysis.source;

import java.util.List;
import org.sonar.ce.task.projectanalysis.component.Component;

/**
 * Generates line hashes from source code included in the report.
 * Line hashes are versioned. Currently there are 2 possible versions: Hashes created using the entire line, or hashes created using
 * only the "significant code" part of the line. The "significant code" can be optionally provided by code analyzers, meaning that 
 * the line hash for a given file can be of either versions.
 * We always persist line hashes taking into account "significant code", if it's provided. 
 * When the line hashes are used for comparison with line hashes stored in the DB, we try to generate them using the same version 
 * as the ones in the DB. This ensures that the line hashes are actually comparable.
 */
public interface SourceLinesHashRepository {
  /**
   * Read from the report the line hashes for a file.
   * The line hashes will have the version matching the version of the line hashes existing in the report, if possible.
   */
  List<String> getLineHashesMatchingDBVersion(Component component);

  /**
   * Get a line hash computer that can be used when persisting the line hashes in the DB.
   * The version of the line hashes that are generated by the computer will be the one that takes into account significant code,
   * if it was provided by a code analyzer.
   */
  SourceLinesHashRepositoryImpl.LineHashesComputer getLineHashesComputerToPersist(Component component);

  /**
   * Get the version of the line hashes for a given component in the report
   */
  int getLineHashesVersion(Component component);
}
