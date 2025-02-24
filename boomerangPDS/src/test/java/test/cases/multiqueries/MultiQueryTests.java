/**
 * ***************************************************************************** Copyright (c) 2018
 * Fraunhofer IEM, Paderborn, Germany. This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * <p>SPDX-License-Identifier: EPL-2.0
 *
 * <p>Contributors: Johannes Spaeth - initial API and implementation
 * *****************************************************************************
 */
package test.cases.multiqueries;

import org.junit.Test;
import test.core.MultiQueryBoomerangTest;
import test.core.selfrunning.AllocatedObject;
import test.core.selfrunning.AllocatedObject2;

public class MultiQueryTests extends MultiQueryBoomerangTest {

  private final String target = MultiQueryTargets.class.getName();

  @Test
  public void twoQueriesTest() {
    analyze(target, testName.getMethodName());
  }

  @Test
  public void withFieldsTest() {
    analyze(target, testName.getMethodName());
  }
}
