package boomerang.framework.sootup;

import boomerang.scene.CallGraph;
import boomerang.scene.Method;
import boomerang.scene.Statement;
import java.util.Collection;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sootup.core.jimple.common.stmt.InvokableStmt;
import sootup.core.signatures.MethodSignature;
import sootup.java.core.JavaSootMethod;

public class SootUpCallGraph extends CallGraph {

  private static final Logger LOGGER = LoggerFactory.getLogger(SootUpCallGraph.class);

  public SootUpCallGraph(sootup.callgraph.CallGraph callGraph, Collection<Method> entryPoints) {

    assert !entryPoints.isEmpty();
    assert !callGraph.getMethodSignatures().isEmpty();

    // TODO: add a convenience method for this(edge collecting) to sootup
    callGraph.getMethodSignatures().stream()
        .flatMap((MethodSignature methodSignature) -> callGraph.callsTo(methodSignature).stream())
        .forEach(
            call -> {
              Optional<JavaSootMethod> sourceOpt =
                  SootUpFrameworkScope.getInstance().getSootMethod(call.getSourceMethodSignature());
              Optional<JavaSootMethod> targetOpt =
                  SootUpFrameworkScope.getInstance().getSootMethod(call.getTargetMethodSignature());

              if (sourceOpt.isEmpty() || targetOpt.isEmpty()) {
                return;
              }

              JavaSootMethod sourceMethod = sourceOpt.get();
              JavaSootMethod targetMethod = targetOpt.get();
              if (!sourceMethod.hasBody() || !targetMethod.hasBody()) {
                return;
              }

              InvokableStmt invokableStmt = call.getInvokableStmt();
              if (!invokableStmt.containsInvokeExpr()) {
                return;
              }

              Statement callSite =
                  JimpleUpStatement.create(invokableStmt, JimpleUpMethod.of(sourceMethod));
              this.addEdge(new Edge(callSite, JimpleUpMethod.of(targetMethod)));

              LOGGER.trace("Added edge {} -> {}", callSite, targetMethod);
            });

    for (Method em : entryPoints) {
      this.addEntryPoint(em);
      LOGGER.trace("Added entry point: {}", em);
    }

    if (getEdges().isEmpty()) {
      throw new IllegalStateException("CallGraph is empty!");
    }
  }
}