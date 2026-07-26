/*
 * Copyright 2018 Transposit Corporation. All Rights Reserved.
 */

package hara.lib.graal;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;

public class Require {
  // This overload registers the require function globally in the engine scope
  public static Module enable(Context context, Folder folder) throws PolyglotException {
    Value global = context.getBindings("js");
    return enable(context, folder, global);
  }

  // This overload registers the require function in a specific Binding. It is useful when re-using
  // the
  // same script engine across multiple threads (each thread should have his own global scope
  // defined
  // through the binding that is passed as an argument).
  public static Module enable(Context context, Folder folder, Value bindings)
      throws PolyglotException {
    Value module = context.eval("js", "({})");
    Value exports = context.eval("js", "({})");

    Module created =
        new Module(context, folder, new ModuleCache(), "<main>", module, exports, null, null);
    created.setLoaded();

    bindings.putMember("require", created);
    bindings.putMember("module", module);
    bindings.putMember("exports", exports);

    return created;
  }
}
