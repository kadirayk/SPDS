package boomerang.framework.sootup;

import boomerang.scene.Field;
import boomerang.scene.IfStatement;
import boomerang.scene.InvokeExpr;
import boomerang.scene.Pair;
import boomerang.scene.Statement;
import boomerang.scene.StaticFieldVal;
import boomerang.scene.Val;
import java.util.Arrays;
import java.util.Collection;
import sootup.core.jimple.common.constant.StringConstant;
import sootup.core.jimple.common.expr.JCastExpr;
import sootup.core.jimple.common.expr.JNewMultiArrayExpr;
import sootup.core.jimple.common.ref.JArrayRef;
import sootup.core.jimple.common.ref.JCaughtExceptionRef;
import sootup.core.jimple.common.ref.JInstanceFieldRef;
import sootup.core.jimple.common.ref.JStaticFieldRef;
import sootup.core.jimple.common.stmt.JAssignStmt;
import sootup.core.jimple.common.stmt.JIdentityStmt;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.JReturnStmt;
import sootup.core.jimple.common.stmt.JThrowStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.java.core.JavaSootField;

public class JimpleUpStatement extends Statement {

  private final Stmt delegate;
  private final JimpleUpMethod method;

  private JimpleUpStatement(Stmt delegate, JimpleUpMethod method) {
    super(method);

    if (delegate == null) {
      throw new RuntimeException("Statement must not be null");
    }

    this.delegate = delegate;
    this.method = method;
  }

  public static Statement create(Stmt delegate, JimpleUpMethod method) {
    return new JimpleUpStatement(delegate, method);
  }

  @Override
  public boolean containsStaticFieldAccess() {
    if (delegate instanceof JAssignStmt) {
      JAssignStmt assignStmt = (JAssignStmt) delegate;

      return assignStmt.getLeftOp() instanceof JStaticFieldRef
          || assignStmt.getRightOp() instanceof JStaticFieldRef;
    }
    return false;
  }

  @Override
  public boolean containsInvokeExpr() {
    return delegate.isInvokableStmt() && delegate.asInvokableStmt().containsInvokeExpr();
  }

  @Override
  public Field getWrittenField() {
    assert isAssign();

    JAssignStmt assignStmt = (JAssignStmt) delegate;
    SootUpFrameworkScope scopeInstance = SootUpFrameworkScope.getInstance();
    if (assignStmt.getLeftOp() instanceof JStaticFieldRef) {
      JStaticFieldRef staticFieldRef = (JStaticFieldRef) assignStmt.getLeftOp();
      JavaSootField sootField = scopeInstance.getSootField(staticFieldRef.getFieldSignature());
      return new JimpleUpField(sootField);
    }

    if (assignStmt.getLeftOp() instanceof JArrayRef) {
      return Field.array(getArrayBase().getY());
    }

    JInstanceFieldRef ifr = (JInstanceFieldRef) assignStmt.getLeftOp();
    JavaSootField sootField = scopeInstance.getSootField(ifr.getFieldSignature());
    return new JimpleUpField(sootField);
  }

  @Override
  public boolean isFieldWriteWithBase(Val base) {
    if (isAssign() && isFieldStore()) {
      Pair<Val, Field> instanceFieldRef = getFieldStore();
      return instanceFieldRef.getX().equals(base);
    }

    if (isAssign() && isArrayStore()) {
      Pair<Val, Integer> arrayBase = getArrayBase();
      return arrayBase.getX().equals(base);
    }

    return false;
  }

  @Override
  public Field getLoadedField() {
    JAssignStmt as = (JAssignStmt) delegate;
    JInstanceFieldRef ifr = (JInstanceFieldRef) as.getRightOp();

    JavaSootField sootField =
        SootUpFrameworkScope.getInstance().getSootField(ifr.getFieldSignature());
    return new JimpleUpField(sootField);
  }

  @Override
  public boolean isFieldLoadWithBase(Val base) {
    if (isAssign() && isFieldLoad()) {
      return getFieldLoad().getX().equals(base);
    }
    return false;
  }

  @Override
  public boolean isAssign() {
    return delegate instanceof JAssignStmt;
  }

  @Override
  public Val getLeftOp() {
    assert isAssign();

    JAssignStmt assignStmt = (JAssignStmt) delegate;
    return new JimpleUpVal(assignStmt.getLeftOp(), method);
  }

  @Override
  public Val getRightOp() {
    assert isAssign();

    JAssignStmt assignStmt = (JAssignStmt) delegate;
    return new JimpleUpVal(assignStmt.getRightOp(), method);
  }

  @Override
  public boolean isInstanceOfStatement(Val fact) {
    if (isAssign()) {
      if (getRightOp().isInstanceOfExpr()) {
        Val instanceOfOp = getRightOp().getInstanceOfOp();
        return instanceOfOp.equals(fact);
      }
    }
    return false;
  }

  @Override
  public boolean isCast() {
    if (delegate instanceof JAssignStmt) {
      JAssignStmt assignStmt = (JAssignStmt) delegate;

      return assignStmt.getRightOp() instanceof JCastExpr;
    }
    return false;
  }

  @Override
  public boolean isPhiStatement() {
    return false;
  }

  @Override
  public InvokeExpr getInvokeExpr() {
    assert containsInvokeExpr();
    assert delegate.isInvokableStmt();
    assert delegate.asInvokableStmt().getInvokeExpr().isPresent();
    return new JimpleUpInvokeExpr(delegate.asInvokableStmt().getInvokeExpr().get(), method);
  }

  @Override
  public boolean isReturnStmt() {
    return delegate instanceof JReturnStmt;
  }

  @Override
  public boolean isThrowStmt() {
    return delegate instanceof JThrowStmt;
  }

  @Override
  public boolean isIfStmt() {
    return delegate instanceof JIfStmt;
  }

  @Override
  public IfStatement getIfStmt() {
    assert isIfStmt();

    JIfStmt ifStmt = (JIfStmt) delegate;
    return new JimpleUpIfStatement(ifStmt, method);
  }

  @Override
  public Val getReturnOp() {
    assert isReturnStmt();

    JReturnStmt returnStmt = (JReturnStmt) delegate;
    return new JimpleUpVal(returnStmt.getOp(), method);
  }

  @Override
  public boolean isMultiArrayAllocation() {
    return (delegate instanceof JAssignStmt)
        && ((JAssignStmt) delegate).getRightOp() instanceof JNewMultiArrayExpr;
  }

  @Override
  public boolean isStringAllocation() {
    return delegate instanceof JAssignStmt
        && ((JAssignStmt) delegate).getRightOp() instanceof StringConstant;
  }

  @Override
  public boolean isFieldStore() {
    return delegate instanceof JAssignStmt
        && ((JAssignStmt) delegate).getLeftOp() instanceof JInstanceFieldRef;
  }

  @Override
  public boolean isArrayStore() {
    return delegate instanceof JAssignStmt
        && ((JAssignStmt) delegate).getLeftOp() instanceof JArrayRef;
  }

  @Override
  public boolean isArrayLoad() {
    return delegate instanceof JAssignStmt
        && ((JAssignStmt) delegate).getRightOp() instanceof JArrayRef;
  }

  @Override
  public boolean isFieldLoad() {
    return delegate instanceof JAssignStmt
        && ((JAssignStmt) delegate).getRightOp() instanceof JInstanceFieldRef;
  }

  @Override
  public boolean isIdentityStmt() {
    return delegate instanceof JIdentityStmt;
  }

  @Override
  public Pair<Val, Field> getFieldStore() {
    JAssignStmt assignStmt = (JAssignStmt) delegate;
    JInstanceFieldRef val = (JInstanceFieldRef) assignStmt.getLeftOp();
    return new Pair<>(
        new JimpleUpVal(val.getBase(), method),
        new JimpleUpField(
            SootUpFrameworkScope.getInstance().getSootField(val.getFieldSignature())));
  }

  @Override
  public Pair<Val, Field> getFieldLoad() {
    JAssignStmt assignStmt = (JAssignStmt) delegate;
    JInstanceFieldRef val = (JInstanceFieldRef) assignStmt.getRightOp();
    return new Pair<>(
        new JimpleUpVal(val.getBase(), method),
        new JimpleUpField(
            SootUpFrameworkScope.getInstance().getSootField(val.getFieldSignature())));
  }

  @Override
  public boolean isStaticFieldLoad() {
    return delegate instanceof JAssignStmt
        && ((JAssignStmt) delegate).getRightOp() instanceof JStaticFieldRef;
  }

  @Override
  public boolean isStaticFieldStore() {
    return delegate instanceof JAssignStmt
        && ((JAssignStmt) delegate).getLeftOp() instanceof JStaticFieldRef;
  }

  @Override
  public StaticFieldVal getStaticField() {
    JStaticFieldRef v;
    if (isStaticFieldLoad()) {
      v = (JStaticFieldRef) ((JAssignStmt) delegate).getRightOp();
    } else if (isStaticFieldStore()) {
      v = (JStaticFieldRef) ((JAssignStmt) delegate).getLeftOp();
    } else {
      throw new RuntimeException("Statement does not have a static field");
    }
    return new JimpleUpStaticFieldVal(
        new JimpleUpField(SootUpFrameworkScope.getInstance().getSootField(v.getFieldSignature())),
        method);
  }

  @Override
  public boolean killAtIfStmt(Val fact, Statement successor) {
    return false;
  }

  @Override
  public Collection<Val> getPhiVals() {
    throw new RuntimeException("Not supported!");
  }

  @Override
  public Pair<Val, Integer> getArrayBase() {
    if (isArrayLoad()) {
      Val rightOp = getRightOp();
      return rightOp.getArrayBase();
    }

    if (isArrayStore()) {
      Val rightOp = getLeftOp();
      return rightOp.getArrayBase();
    }

    throw new RuntimeException("Statement does not deal with an array base");
  }

  @Override
  public int getStartLineNumber() {
    return delegate.getPositionInfo().getStmtPosition().getFirstLine();
  }

  @Override
  public int getStartColumnNumber() {
    return delegate.getPositionInfo().getStmtPosition().getFirstCol();
  }

  @Override
  public int getEndLineNumber() {
    return delegate.getPositionInfo().getStmtPosition().getLastLine();
  }

  @Override
  public int getEndColumnNumber() {
    return delegate.getPositionInfo().getStmtPosition().getLastCol();
  }

  @Override
  public boolean isCatchStmt() {
    return delegate instanceof JIdentityStmt
        && ((JIdentityStmt) delegate).getRightOp() instanceof JCaughtExceptionRef;
  }

  public Stmt getDelegate() {
    return delegate;
  }

  @Override
  public int hashCode() {
    return Arrays.hashCode(new Object[] {delegate});
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (!super.equals(obj)) return false;
    if (getClass() != obj.getClass()) return false;

    JimpleUpStatement other = (JimpleUpStatement) obj;
    if (delegate == null) {
      return other.delegate == null;
    } else return delegate.equals(other.delegate);
  }

  @Override
  public String toString() {
    return delegate.toString();
  }
}