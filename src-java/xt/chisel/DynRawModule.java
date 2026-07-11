package xt.chisel;

import chisel3.RawModule;
import scala.Function1;

/**
 * {@link RawModule} counterpart of {@link DynModule}: a reusable module with
 * explicit clock and reset (no implicit clock/reset), whose body is supplied by
 * the caller as a callback and runs inside the constructor.
 */
public class DynRawModule extends RawModule {

    /** Top-level IO, assigned by the body so Chisel can reflect its name. */
    public chisel3.Data io;

    private final String desiredName;

    public DynRawModule(String desiredName, Function1 build) {
        super();
        this.desiredName = desiredName;
        build.apply(this);
    }

    @Override
    public String desiredName() {
        return desiredName;
    }
}
