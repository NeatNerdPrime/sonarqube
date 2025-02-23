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
package org.sonar.db.sca;

import java.util.List;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ScaDependencyReleaseDtoTest {

  @Test
  void test_primaryDependencyFilePath() {
    ScaDependencyReleaseDto withUserDependencyFilePath = newScaDependencyReleaseDto("manifest");
    assertEquals("manifest", withUserDependencyFilePath.primaryDependencyFilePath());
    ScaDependencyReleaseDto withoutUserDependencyFilePath = newScaDependencyReleaseDto(null);
    assertEquals("lockfileDependencyFilePath", withoutUserDependencyFilePath.primaryDependencyFilePath());
  }

  private ScaDependencyReleaseDto newScaDependencyReleaseDto(@Nullable String userDependencyFilePath) {
    return new ScaDependencyReleaseDto("dependencyUuid",
      "releaseUuid",
      "componentUuid",
      true,
      "scope",
      userDependencyFilePath,
      "lockfileDependencyFilePath",
      List.of(List.of("pkg:npm/foo@1.0.0")),
      "packageUrl",
      PackageManager.MAVEN,
      "packageName",
      "version",
      "licenseExpression",
      true);
  }
}
