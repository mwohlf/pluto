package net.wohlfart.pluto.scene.lang;

import java.util.HashMap;
import java.util.Map;

// TODO: use optionals
public class Scope {

    //private static final Logger LOGGER = LoggerService.forClass(Scope.class);

    private Scope parent;
    private Map<String, Value<?>> variables;

    public Scope() {
        this(null);
    }

    public Scope(Scope parent) {
        this.parent = parent;
        this.variables = new HashMap<>();
    }

    // assign in this scope
    public void assignInScope(String name, Value<?> value) {
        variables.put(name, value);
    }

    public void assign(String identifier, Value<?> value) {
        //LOGGER.info("<assign> " + identifier + " = " + value);
        if (resolve(identifier) != null) {
            // if there is already such a variable, re-assign it
            this.assignExisting(identifier, value);
        } else {
            assignInScope(identifier, value);
        }
    }

    public void assignExisting(String identifier, Value<?> value) {
        if (variables.containsKey(identifier)) {
            variables.put(identifier, value);
        } else if (!isGlobalScope()) {
            parent.assignExisting(identifier, value);
        } else {
            throw new IllegalStateException("variable not found for reassigning, name war " + identifier + " value: " + value);
        }
    }

    // used for recursive function calls
    @Deprecated // not used yet
    public Scope copy() {
        final Scope scope = new Scope();
        scope.variables = new HashMap<>(this.variables);
        scope.parent = this.parent;
        return scope;
    }

    public boolean isGlobalScope() {
        return parent == null;
    }

    public Scope parent() {
        return parent;
    }

    // resolve in this scope or in parent
    public Value<?> resolve(String identifier) {
        final Value<?> value = variables.get(identifier);
        if (value != null) {
            return value;
        } else if (!isGlobalScope()) {
            return parent.resolve(identifier);
        } else {
            return null;
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("Scope:");
        for (final Map.Entry<String, Value<?>> var : variables.entrySet()) {
            sb.append(System.lineSeparator() + "   " + var.getKey() + "->" + var.getValue());
        }
        return sb.toString();
    }
}
