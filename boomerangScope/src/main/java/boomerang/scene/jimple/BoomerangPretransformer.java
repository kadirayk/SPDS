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
package boomerang.scene.jimple;

import sootup.core.model.Body;
import sootup.core.transform.BodyInterceptor;
import sootup.core.views.View;

public class BoomerangPretransformer implements BodyInterceptor {
  @Override
  public void interceptBody(Body.BodyBuilder bodyBuilder, View view) {}

  //  public static boolean TRANSFORM_CONSTANTS = true;
  //  public static String UNITIALIZED_FIELD_TAG_NAME = "UnitializedField";
  //  public static Tag UNITIALIZED_FIELD_TAG =
  //      new Tag() {
  //
  //        @Override
  //        public String getName() {
  //          return UNITIALIZED_FIELD_TAG_NAME;
  //        }
  //
  //        @Override
  //        public byte[] getValue() throws AttributeValueException {
  //          return new byte[0];
  //        }
  //      };
  //  private static BoomerangPretransformer instance;
  //  private int replaceCounter;
  //  private boolean applied;
  //
  //  @Override
  //  protected void internalTransform(Body b, String phaseName, Map<String, String> options) {
  //    addNopStmtToMethods(b);
  //    if (TRANSFORM_CONSTANTS) transformConstantAtFieldWrites(b);
  //  }
  //
  //  private void transformConstantAtFieldWrites(Body body) {
  //    Set<Unit> cwnc = getStmtsWithConstants(body);
  //    for (Unit u : cwnc) {
  //      if (u instanceof AssignStmt) {
  //        AssignStmt assignStmt = (AssignStmt) u;
  //        if (isFieldRef(assignStmt.getLeftOp())
  //            && assignStmt.getRightOp() instanceof Constant
  //            && !(assignStmt.getRightOp() instanceof ClassConstant)) {
  //          String label = "varReplacer" + new Integer(replaceCounter++).toString();
  //          Local paramVal = new JimpleLocal(label, assignStmt.getRightOp().getType());
  //          AssignStmt newUnit = new JAssignStmt(paramVal, assignStmt.getRightOp());
  //          body.getLocals().add(paramVal);
  //          body.getUnits().insertBefore(newUnit, u);
  //          AssignStmt other = new JAssignStmt(assignStmt.getLeftOp(), paramVal);
  //          other.addAllTagsOf(u);
  //          body.getUnits().insertBefore(other, u);
  //          body.getUnits().remove(u);
  //        }
  //      }
  //      if (u instanceof Stmt
  //          && ((Stmt) u).containsInvokeExpr()
  //          && !u.toString().contains("test.assertions.Assertions:")
  //          && !u.toString().contains("intQueryFor")) {
  //        Stmt stmt = (Stmt) u;
  //        if (stmt.getInvokeExpr()
  //            .getMethod()
  //            .getSignature()
  //            .equals("<java.math.BigInteger: java.math.BigInteger valueOf(long)>")) {
  //          continue;
  //        }
  //        List<ValueBox> useBoxes = stmt.getInvokeExpr().getUseBoxes();
  //        for (Value v : stmt.getInvokeExpr().getArgs()) {
  //          if (v instanceof Constant && !(v instanceof ClassConstant)) {
  //            String label = "varReplacer" + new Integer(replaceCounter++).toString();
  //            Local paramVal = new JimpleLocal(label, v.getType());
  //            AssignStmt newUnit = new JAssignStmt(paramVal, v);
  //            newUnit.addAllTagsOf(u);
  //            body.getLocals().add(paramVal);
  //            body.getUnits().insertBefore(newUnit, u);
  //            for (ValueBox b : useBoxes) {
  //              backPropagateSourceLineTags(b, newUnit);
  //              if (b.getValue().equals(v)) {
  //                b.setValue(paramVal);
  //              }
  //            }
  //          }
  //        }
  //      }
  //      if (u instanceof ReturnStmt) {
  //        ReturnStmt returnStmt = (ReturnStmt) u;
  //        String label = "varReplacer" + new Integer(replaceCounter++).toString();
  //        Local paramVal = new JimpleLocal(label, returnStmt.getOp().getType());
  //        AssignStmt newUnit = new JAssignStmt(paramVal, returnStmt.getOp());
  //        newUnit.addAllTagsOf(u);
  //        body.getLocals().add(paramVal);
  //        body.getUnits().insertBefore(newUnit, u);
  //        JReturnStmt other = new JReturnStmt(paramVal);
  //        body.getUnits().insertBefore(other, u);
  //        body.getUnits().remove(u);
  //      }
  //    }
  //  }
  //
  //  /**
  //   * Propagates back the line number tags from the constant value box to the newly created
  //   * AssignStmt, to revert the forward propagation done in {@link
  //   * soot.jimple.toolkits.scalar.CopyPropagator}
  //   *
  //   * @param valueBox
  //   * @param assignStmt
  //   */
  //  private void backPropagateSourceLineTags(ValueBox valueBox, AssignStmt assignStmt) {
  //    Tag tag = valueBox.getTag(SourceLnPosTag.IDENTIFIER);
  //    if (tag != null) {
  //      // in case that we copied a line number tag from the original statement, we want to remove
  //      // that now since the valueBox contains the correct lin number tag for the assign
  // statement as
  //      // it was before copy propagation
  //      assignStmt.removeTag(SourceLnPosTag.IDENTIFIER);
  //      assignStmt.addTag(tag);
  //    }
  //
  //    tag = valueBox.getTag(LineNumberTag.IDENTIFIER);
  //    if (tag != null) {
  //      // same as for the above case
  //      assignStmt.removeTag(LineNumberTag.IDENTIFIER);
  //      assignStmt.addTag(tag);
  //    }
  //  }
  //
  //  /**
  //   * The first statement of a method must be a nop statement, because the call-flow functions do
  //   * only map parameters to arguments. If the first statement of a method would be an assign
  //   * statement, the analysis misses data-flows.
  //   */
  //  private void addNopStmtToMethods(Body b) {
  //    JNopStmt nopStmt = new JNopStmt();
  //    for (Unit u : b.getUnits()) {
  //      if (u.getJavaSourceStartLineNumber() > 0) {
  //        nopStmt.addAllTagsOf(u);
  //        break;
  //      }
  //    }
  //    b.getUnits().insertBefore(nopStmt, b.getUnits().getFirst());
  //    Set<IfStmt> ifStmts = Sets.newHashSet();
  //    for (Unit u : b.getUnits()) {
  //      if (u instanceof IfStmt) {
  //        // ((IfStmt) u).getTarget();
  //        ifStmts.add((IfStmt) u);
  //      }
  //    }
  //
  //    // After all if-stmts we add a nop-stmt to make the analysis
  //    for (IfStmt ifStmt : ifStmts) {
  //      nopStmt = new JNopStmt();
  //      nopStmt.addAllTagsOf(ifStmt);
  //      b.getUnits().insertAfter(nopStmt, ifStmt);
  //      Unit target = ifStmt.getTarget();
  //      nopStmt = new JNopStmt();
  //      nopStmt.addAllTagsOf(target);
  //      b.getUnits().insertBefore(nopStmt, target);
  //      ifStmt.setTarget(nopStmt);
  //    }
  //  }
  //
  //  private Set<Unit> getStmtsWithConstants(Body methodBody) {
  //    Set<Unit> retMap = Sets.newHashSet();
  //    for (Unit u : methodBody.getUnits()) {
  //      if (u instanceof AssignStmt) {
  //        AssignStmt assignStmt = (AssignStmt) u;
  //        if (isFieldRef(assignStmt.getLeftOp()) && assignStmt.getRightOp() instanceof Constant) {
  //          retMap.add(u);
  //        }
  //      }
  //      if (u instanceof Stmt && ((Stmt) u).containsInvokeExpr()) {
  //        Stmt stmt = (Stmt) u;
  //        for (Value v : stmt.getInvokeExpr().getArgs()) {
  //          if (v instanceof Constant) {
  //            retMap.add(u);
  //          }
  //        }
  //      }
  //      if (u instanceof ReturnStmt) {
  //        ReturnStmt assignStmt = (ReturnStmt) u;
  //        if (assignStmt.getOp() instanceof Constant) {
  //          retMap.add(u);
  //        }
  //      }
  //    }
  //    return retMap;
  //  }
  //
  //  private boolean isFieldRef(Value op) {
  //    return op instanceof InstanceFieldRef || op instanceof StaticFieldRef || op instanceof
  // ArrayRef;
  //  }
  //
  //  public void apply() {
  //    if (applied) return;
  //    ReachableMethods reachableMethods = Scene.v().getReachableMethods();
  //    QueueReader<MethodOrMethodContext> listener = reachableMethods.listener();
  //    while (listener.hasNext()) {
  //      SootMethod method = listener.next().method();
  //      if (method.hasActiveBody()) {
  //        if (method.isConstructor()) {
  //          addNulliefiedFields(method);
  //        }
  //        internalTransform(method.getActiveBody(), "", new HashMap<>());
  //      }
  //    }
  //    applied = true;
  //  }
  //
  //  private static void addNulliefiedFields(SootMethod cons) {
  //    Chain<SootField> fields = cons.getDeclaringClass().getFields();
  //    UnitPatchingChain units = cons.getActiveBody().getUnits();
  //    Set<SootField> fieldsDefinedInMethod = getFieldsDefinedInMethod(cons, Sets.newHashSet());
  //    for (SootField f : fields) {
  //      if (fieldsDefinedInMethod.contains(f)) continue;
  //      if (f.isStatic()) continue;
  //      if (f.isFinal()) continue;
  //      if (f.getType() instanceof RefType) {
  //        JAssignStmt jAssignStmt =
  //            new JAssignStmt(
  //                new JInstanceFieldRef(cons.getActiveBody().getThisLocal(), f.makeRef()),
  //                NullConstant.v());
  //
  //        jAssignStmt.addTag(new LineNumberTag(2));
  //        jAssignStmt.addTag(UNITIALIZED_FIELD_TAG);
  //        Unit lastIdentityStmt = findLastIdentityStmt(units);
  //        if (lastIdentityStmt != null) {
  //          units.insertAfter(jAssignStmt, lastIdentityStmt);
  //        } else {
  //          units.addFirst(jAssignStmt);
  //        }
  //      }
  //    }
  //  }
  //
  //  private static Unit findLastIdentityStmt(UnitPatchingChain units) {
  //    for (Unit u : units) {
  //      if (u instanceof IdentityStmt && u instanceof AssignStmt) {
  //        continue;
  //      }
  //      return u;
  //    }
  //    return null;
  //  }
  //
  //  private static Set<SootField> getFieldsDefinedInMethod(SootMethod cons, Set<SootMethod>
  // visited) {
  //    Set<SootField> res = Sets.newHashSet();
  //    if (!visited.add(cons)) return res;
  //    if (!cons.hasActiveBody()) return res;
  //    for (Unit u : cons.getActiveBody().getUnits()) {
  //      if (u instanceof AssignStmt) {
  //        AssignStmt as = (AssignStmt) u;
  //        Value left = as.getLeftOp();
  //        if (left instanceof InstanceFieldRef) {
  //          InstanceFieldRef ifr = (InstanceFieldRef) left;
  //          res.add(ifr.getField());
  //        }
  //      }
  //      if (u instanceof Stmt) {
  //        Stmt stmt = (Stmt) u;
  //        if (stmt.containsInvokeExpr()) {
  //          if (stmt.getInvokeExpr().getMethod().isConstructor()) {
  //            res.addAll(getFieldsDefinedInMethod(stmt.getInvokeExpr().getMethod(), visited));
  //          }
  //        }
  //      }
  //    }
  //    return res;
  //  }
  //
  //  public boolean isApplied() {
  //    return applied;
  //  }
  //
  //  public static BoomerangPretransformer v() {
  //    if (instance == null) {
  //      instance = new BoomerangPretransformer();
  //    }
  //    return instance;
  //  }
  //
  //  public void reset() {
  //    instance = null;
  //  }
}
