package xt.chisel;

import chisel3.Module;
import scala.Function1;

/**
 * A reusable Chisel {@link Module} whose constructor body is supplied by the
 * caller (Clojure) as a callback.
 *
 * <p>Chisel requires that a module's ports and wiring be created while the
 * module's constructor is executing (the Builder context is only open then).
 * JVM languages that cannot inject statements into a superclass constructor
 * (e.g. Clojure) therefore subclass this class and pass the body in.</p>
 *
 * <p>The callback receives the module instance; the body should build {@code io}
 * (and any wiring) and assign the top-level IO to the {@link #io} field so that
 * Chisel's port discovery can name it.</p>
 */
public class DynModule extends Module {

    /** Top-level IO, assigned by the body so Chisel can reflect its name. */
    public chisel3.Data io;

    private final String desiredName;

    public DynModule(String desiredName, Function1 build) {
        super();
        this.desiredName = desiredName;
        // Body runs here, inside the constructor, with the Builder context open.
        build.apply(this);
    }

    @Override
    public String desiredName() {
        return desiredName;
    }
}
