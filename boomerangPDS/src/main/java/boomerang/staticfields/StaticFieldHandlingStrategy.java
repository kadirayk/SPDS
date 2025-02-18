package boomerang.staticfields;

import boomerang.scene.ControlFlowGraph.Edge;
import boomerang.scene.StaticFieldVal;
import boomerang.scene.Val;
import java.util.Set;
import wpds.interfaces.State;

public interface StaticFieldHandlingStrategy {

  void handleForward(Edge storeStmt, Val storedVal, StaticFieldVal staticVal, Set<State> out);

  void handleBackward(Edge curr, Val leftOp, StaticFieldVal staticField, Set<State> out);
}
