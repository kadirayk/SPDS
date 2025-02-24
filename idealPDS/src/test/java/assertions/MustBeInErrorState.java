package assertions;

import boomerang.scope.Statement;
import boomerang.scope.Val;
import typestate.finiteautomata.State;

import java.util.Collection;

public class MustBeInErrorState extends StateResult {

    private boolean unsound;
    private boolean checked;

    public MustBeInErrorState(Statement statement, Val seed) {
        super(statement, seed);

        this.unsound = false;
        this.checked = false;
    }

    @Override
    public void computedStates(Collection<State> states) {
        // Check if any state is not an error state
        for (State state : states) {
            unsound |= !state.isErrorState();
        }
        checked = true;
    }

    @Override
    public boolean isUnsound() {
        return !checked || unsound;
    }

    @Override
    public String getAssertedMessage() {
        if (checked) {
            return seed.getVariableName() + " must be in an error state @ " + statement + " @ line " + statement.getStartLineNumber();
        } else {
            return statement + " @ line " + statement.getStartLineNumber() + " has not been checked";
        }
    }
}
