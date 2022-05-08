package de.firemage.codelinter.core.spoon.analysis;

import java.util.Set;

public class ObjectValueSet implements ValueSet<ObjectValueSet> {
    private boolean containsNull;
    private boolean containsInstance;

    public ObjectValueSet(boolean empty) {
        if (empty) {
            this.containsInstance = false;
            this.containsNull = false;
        } else {
            this.containsInstance = true;
            this.containsNull = true;
        }
    }

    public boolean containsNull() {
        return this.containsNull;
    }

    public boolean containsInstance() {
        return this.containsInstance;
    }

    public void includeNull() {
        this.containsNull = true;
    }

    public void includeInstance() {
        this.containsInstance = true;
    }

    @Override
    public ObjectValueSet intersect(ObjectValueSet other) {
        ObjectValueSet result = new ObjectValueSet(true);
        result.containsNull = this.containsNull && other.containsNull;
        result.containsInstance = this.containsInstance && other.containsInstance;
        return result;
    }

    @Override
    public ObjectValueSet combine(ObjectValueSet other) {
        ObjectValueSet result = new ObjectValueSet(true);
        result.containsNull = this.containsNull || other.containsNull;
        result.containsInstance = this.containsInstance || other.containsInstance;
        return result;
    }
}
