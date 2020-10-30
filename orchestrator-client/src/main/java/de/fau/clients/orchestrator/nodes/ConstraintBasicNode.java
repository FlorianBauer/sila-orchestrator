package de.fau.clients.orchestrator.nodes;

import de.fau.clients.orchestrator.ctx.FeatureContext;
import java.util.function.Supplier;
import javax.swing.JComponent;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import sila_java.library.core.models.BasicType;
import sila_java.library.core.models.Constraints;

/**
 * A <code>BasicNode</code> with additional restrictions given by the SiLA-Constraint object.
 *
 * @see BasicNode
 * @see BasicNodeFactory
 * @see ConstraintBasicNodeFactory
 */
@Slf4j
public class ConstraintBasicNode extends BasicNode {

    private final FeatureContext featCtx;
    private final Constraints constraints;

    protected ConstraintBasicNode(
            @NonNull final BasicType type,
            @NonNull final JComponent component,
            @NonNull final Supplier<String> valueSupplier,
            @NonNull final Constraints constraints
    ) {
        super(type, component, valueSupplier, true);
        this.featCtx = null;
        this.constraints = constraints;
    }

    protected ConstraintBasicNode(
            @NonNull final FeatureContext featCtx,
            @NonNull final BasicType type,
            @NonNull final JComponent component,
            @NonNull final Supplier<String> valueSupplier,
            @NonNull final Constraints constraints
    ) {
        super(type, component, valueSupplier, true);
        this.featCtx = featCtx;
        this.constraints = constraints;
    }

    @Override
    public BasicNode cloneNode() {
        return ConstraintBasicNodeFactory.create(this.featCtx, this.type, this.constraints);
    }

    @NonNull
    @Override
    public String toString() {
        return "(" + this.type + ", "
                + this.component.getClass() + ", "
                + this.valueSupplier.getClass() + ", "
                + this.constraints + ")";
    }

    protected Constraints getConstaint() {
        return this.constraints;
    }
}
