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
package org.sonar.ce.task.step;

import java.util.Objects;
import javax.annotation.CheckForNull;
import javax.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.sonar.ce.task.CeTaskInterrupter;
import org.sonar.ce.task.telemetry.MutableStepsTelemetryHolder;
import org.sonar.ce.task.telemetry.StepsTelemetryUnavailableHolderImpl;
import org.sonar.core.util.logs.Profiler;
import org.springframework.beans.factory.annotation.Autowired;

import static com.google.common.base.Preconditions.checkArgument;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public final class ComputationStepExecutor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ComputationStepExecutor.class);

  private final ComputationSteps steps;
  private final CeTaskInterrupter taskInterrupter;
  private final MutableStepsTelemetryHolder stepsTelemetryHolder;
  @CheckForNull
  private final Listener listener;

  /**
   * This constructor is used when running Portfolio Refresh, Project Import or App Refresh tasks.
   * @param steps computation steps
   * @param taskInterrupter task interrupter
   */
  public ComputationStepExecutor(ComputationSteps steps, CeTaskInterrupter taskInterrupter) {
    this(steps, taskInterrupter, new StepsTelemetryUnavailableHolderImpl(), null);
  }

  @Autowired
  public ComputationStepExecutor(ComputationSteps steps, CeTaskInterrupter taskInterrupter,
    MutableStepsTelemetryHolder stepsTelemetryHolder, @Nullable Listener listener) {
    this.steps = steps;
    this.taskInterrupter = taskInterrupter;
    this.stepsTelemetryHolder = stepsTelemetryHolder;
    this.listener = listener;
  }

  public void execute() {
    Profiler stepProfiler = Profiler.create(LOGGER).logTimeLast(true);
    boolean allStepsExecuted = false;
    try {
      executeSteps(stepProfiler);
      allStepsExecuted = true;
    } finally {
      if (listener != null) {
        executeListener(allStepsExecuted);
      }
    }
  }

  private void executeSteps(Profiler stepProfiler) {
    StepStatisticsImpl statistics = new StepStatisticsImpl(stepProfiler);
    ComputationStep.Context context = new StepContextImpl(statistics, stepsTelemetryHolder);
    for (ComputationStep step : steps.instances()) {
      executeStep(stepProfiler, context, step);
    }
  }

  private void executeStep(Profiler stepProfiler, ComputationStep.Context context, ComputationStep step) {
    String status = "FAILED";
    stepProfiler.start();
    try {
      taskInterrupter.check(Thread.currentThread());
      step.execute(context);
      status = "SUCCESS";
    } finally {
      stepProfiler.addContext("status", status);
      stepProfiler.stopInfo(step.getDescription());
    }
  }

  private void executeListener(boolean allStepsExecuted) {
    try {
      listener.finished(allStepsExecuted);
    } catch (Throwable e) {
      // any Throwable throws by the listener going up the stack might hide an Exception/Error thrown by the step and
      // cause it be swallowed. We don't wan't that => we catch Throwable
      LOGGER.error("Execution of listener failed", e);
    }
  }

  @FunctionalInterface
  public interface Listener {
    void finished(boolean allStepsExecuted);
  }

  private static class StepStatisticsImpl implements ComputationStep.Statistics {
    private final Profiler profiler;

    private StepStatisticsImpl(Profiler profiler) {
      this.profiler = profiler;
    }

    @Override
    public ComputationStep.Statistics add(String key, Object value) {
      requireNonNull(key, "Statistic has null key");
      requireNonNull(value, () -> format("Statistic with key [%s] has null value", key));
      checkArgument(!key.equalsIgnoreCase("time"), "Statistic with key [time] is not accepted");
      checkArgument(!profiler.hasContext(key), "Statistic with key [%s] is already present", key);
      profiler.addContext(key, value);
      return this;
    }
  }

  private static class StepContextImpl implements ComputationStep.Context {
    private final ComputationStep.Statistics statistics;
    private final MutableStepsTelemetryHolder stepsTelemetryHolder;

    private StepContextImpl(ComputationStep.Statistics statistics, MutableStepsTelemetryHolder stepsTelemetryHolder) {
      this.statistics = statistics;
      this.stepsTelemetryHolder = stepsTelemetryHolder;
    }

    private static String prependPrefix(@Nullable String prefix, String key) {
      if (prefix == null) {
        return key;
      } else {
        return prefix + "." + key;
      }
    }

    @Override
    public ComputationStep.Statistics getStatistics() {
      return statistics;
    }

    @Override
    public void addTelemetryMetricOnly(@Nullable String telemetryPrefix, String key, Object value) {
      stepsTelemetryHolder.add(prependPrefix(telemetryPrefix, key), value);
    }

    @Override
    public void addTelemetryWithStatistic(String telemetryPrefix, String key, Object value) {
      Objects.requireNonNull(telemetryPrefix);
      stepsTelemetryHolder.add(prependPrefix(telemetryPrefix, key), value);
      statistics.add(key, value);
    }
  }
}
