/*
 * Copyright 2018 Transposit Corporation. All Rights Reserved.
 */

package hara.lib.graal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.HostAccess;
import org.graalvm.polyglot.PolyglotException;
import org.graalvm.polyglot.Value;
import org.graalvm.polyglot.proxy.ProxyArray;

public class Module implements RequireFunction {
  private Context context;
  private Value jsonConstructor;

  private Folder folder;
  private ModuleCache cache;

  private Module mainModule;
  @HostAccess.Export public Value main;
  private Value module;
  private List<Value> children = new ArrayList<>();
  private Value exports;
  private static ThreadLocal<Map<String, Value>> refCache = new ThreadLocal<>();

  public Module(
      Context context,
      Folder folder,
      ModuleCache cache,
      String filename,
      Value module,
      Value exports,
      Module parent,
      Module root)
      throws PolyglotException {

    this.context = context;

    if (parent != null) {
      this.jsonConstructor = parent.jsonConstructor;
    } else {
      this.jsonConstructor = context.eval("js", "JSON");
    }

    this.folder = folder;
    this.cache = cache;
    this.mainModule = root != null ? root : this;
    this.module = module;
    this.exports = exports;

    this.main = this.mainModule.module;

    module.putMember("exports", exports);
    module.putMember("children", new WrappedList(children));
    module.putMember("filename", filename);
    module.putMember("id", filename);
    module.putMember("loaded", false);
    module.putMember("parent", parent != null ? parent.module : null);
  }

  void setLoaded() {
    module.putMember("loaded", true);
  }

  @Override
  @HostAccess.Export
  public Value require(String module) throws PolyglotException {
    if (module == null) {
      throwModuleNotFoundException("<null>");
    }

    String[] parts = Paths.splitPath(module);
    if (parts.length == 0) {
      throwModuleNotFoundException(module);
    }

    String[] folderParts = Arrays.copyOfRange(parts, 0, parts.length - 1);

    String filename = parts[parts.length - 1];

    Module found = null;

    Folder resolvedFolder = resolveFolder(folder, folderParts);

    // Let's make sure each thread gets its own refCache
    if (refCache.get() == null) {
      refCache.set(new HashMap<>());
    }

    String requestedFullPath = null;
    if (resolvedFolder != null) {
      requestedFullPath = resolvedFolder.getPath() + filename;
      Value cachedExports = refCache.get().get(requestedFullPath);
      if (cachedExports != null) {
        return cachedExports;
      } else {
        // We must store a reference to currently loading module to avoid circular requires
        refCache.get().put(requestedFullPath, newObject());
      }
    }

    try {
      // If not cached, we try to resolve the module from the current folder, ignoring node_modules
      if (isPrefixedModuleName(module)) {
        found = attemptToLoadFromThisFolder(resolvedFolder, filename);
      }

      // Then, if not successful, we'll look at node_modules in the current folder and then
      // in all parent folders until we reach the top.
      if (found == null) {
        found = searchForModuleInNodeModules(folder, folderParts, filename);
      }

      if (found == null) {
        throwModuleNotFoundException(module);
      }

      assert found != null;
      children.add(found.module);

      return found.exports;

    } finally {
      // Finally, we remove the successful resolved module from the refCache
      if (requestedFullPath != null) {
        refCache.get().remove(requestedFullPath);
      }
    }
  }

  private Module searchForModuleInNodeModules(
      Folder resolvedFolder, String[] folderParts, String filename) throws PolyglotException {
    Folder current = resolvedFolder;
    while (current != null) {
      Folder nodeModules = current.getFolder("node_modules");

      if (nodeModules != null) {
        Module found =
            attemptToLoadFromThisFolder(resolveFolder(nodeModules, folderParts), filename);
        if (found != null) {
          return found;
        }
      }

      current = current.getParent();
    }

    return null;
  }

  private Module attemptToLoadFromThisFolder(Folder resolvedFolder, String filename)
      throws PolyglotException {

    if (resolvedFolder == null) {
      return null;
    }

    String requestedFullPath = resolvedFolder.getPath() + filename;

    Module found = cache.get(requestedFullPath);
    if (found != null) {
      return found;
    }

    // First we try to load as a file, trying out various variations on the path
    found = loadModuleAsFile(resolvedFolder, filename);

    // Then we try to load as a directory
    if (found == null) {
      found = loadModuleAsFolder(resolvedFolder, filename);
    }

    if (found != null) {
      // We keep a cache entry for the requested path even though the code that
      // compiles the module also adds it to the cache with the potentially different
      // effective path. This avoids having to load package.json every time, etc.
      cache.put(requestedFullPath, found);
    }

    return found;
  }

  private Module loadModuleAsFile(Folder parent, String filename) throws PolyglotException {

    String[] filenamesToAttempt = getFilenamesToAttempt(filename);
    for (String tentativeFilename : filenamesToAttempt) {

      String code = parent.getFile(tentativeFilename);
      if (code != null) {
        String fullPath = parent.getPath() + tentativeFilename;
        return compileModuleAndPutInCache(parent, fullPath, code);
      }
    }

    return null;
  }

  private Module loadModuleAsFolder(Folder parent, String name) throws PolyglotException {
    Folder fileAsFolder = parent.getFolder(name);
    if (fileAsFolder == null) {
      return null;
    }

    Module found = loadModuleThroughPackageJson(fileAsFolder);

    if (found == null) {
      found = loadModuleThroughIndexJs(fileAsFolder);
    }

    if (found == null) {
      found = loadModuleThroughIndexJson(fileAsFolder);
    }

    return found;
  }

  private Module loadModuleThroughPackageJson(Folder parent) throws PolyglotException {
    String packageJson = parent.getFile("package.json");
    if (packageJson == null) {
      return null;
    }

    String mainFile = getMainFileFromPackageJson(packageJson);
    if (mainFile == null) {
      return null;
    }

    String[] parts = Paths.splitPath(mainFile);
    String[] folders = Arrays.copyOfRange(parts, 0, parts.length - 1);
    String filename = parts[parts.length - 1];
    Folder folder = resolveFolder(parent, folders);
    if (folder == null) {
      return null;
    }

    Module module = loadModuleAsFile(folder, filename);

    if (module == null) {
      folder = resolveFolder(parent, parts);
      if (folder != null) {
        module = loadModuleThroughIndexJs(folder);
      }
    }

    return module;
  }

  private String getMainFileFromPackageJson(String packageJson) throws PolyglotException {
    Value parsed = parseJson(packageJson);
    Value main = parsed.getMember("main");
    return main == null ? null : main.asString();
  }

  private Module loadModuleThroughIndexJs(Folder parent) throws PolyglotException {
    String code = parent.getFile("index.js");
    if (code == null) {
      return null;
    }

    return compileModuleAndPutInCache(parent, parent.getPath() + "index.js", code);
  }

  private Module loadModuleThroughIndexJson(Folder parent) throws PolyglotException {
    String code = parent.getFile("index.json");
    if (code == null) {
      return null;
    }

    return compileModuleAndPutInCache(parent, parent.getPath() + "index.json", code);
  }

  private Module compileModuleAndPutInCache(Folder parent, String fullPath, String code)
      throws PolyglotException {

    Module created;
    String lowercaseFullPath = fullPath.toLowerCase();
    if (lowercaseFullPath.endsWith(".js")) {
      created = compileJavaScriptModule(parent, fullPath, code);
    } else if (lowercaseFullPath.endsWith(".json")) {
      created = compileJsonModule(parent, fullPath, code);
    } else {
      // Unsupported module type
      return null;
    }

    // We keep a cache entry for the compiled module using it's effective path, to avoid
    // recompiling even if module is requested through a different initial path.
    cache.put(fullPath, created);

    return created;
  }

  private Module compileJavaScriptModule(Folder parent, String fullPath, String code)
      throws PolyglotException {

    Value module = newObject();

    // If we have cached bindings, use them to rebind exports instead of creating new ones
    Value exports = refCache.get().get(fullPath);
    if (exports == null) {
      exports = newObject();
    }

    Module created =
        new Module(context, parent, cache, fullPath, module, exports, this, this.mainModule);

    String[] split = Paths.splitPath(fullPath);
    String filename = split[split.length - 1];
    String dirname = fullPath.substring(0, Math.max(fullPath.length() - filename.length() - 1, 0));

    // This mimics how Node wraps module in a function. I used to pass a 2nd parameter
    // to eval to override global context, but it caused problems Object.create.
    //
    // The \n at the end is to take care of files ending with a comment
    Value function =
        context.eval(
            "js", "(function (exports, require, module, __filename, __dirname) {" + code + "\n})");
    function.execute(created.exports, created, created.module, filename, dirname);

    // Scripts are free to replace the global exports symbol with their own, so we
    // reload it from the module object after compiling the code.
    created.exports = created.module.getMember("exports");

    created.setLoaded();
    return created;
  }

  private Module compileJsonModule(Folder parent, String fullPath, String code)
      throws PolyglotException {
    Value module = newObject();
    Value exports = newObject();
    Module created =
        new Module(context, parent, cache, fullPath, module, exports, this, this.mainModule);
    created.exports = parseJson(code);
    created.setLoaded();
    return created;
  }

  private Value parseJson(String json) throws PolyglotException {
    // Pretty lame way to parse JSON but hey...
    return jsonConstructor.getMember("parse").execute(json);
  }

  private void throwModuleNotFoundException(String module) throws GraalGuestException {
    throw new GraalGuestException("Module not found: " + module);
  }

  private Folder resolveFolder(Folder from, String[] folders) {
    Folder current = from;
    for (String name : folders) {
      switch (name) {
        case "":
          throw new IllegalArgumentException();
        case ".":
          continue;
        case "..":
          current = current.getParent();
          break;
        default:
          current = current.getFolder(name);
          break;
      }

      // Whenever we get stuck we bail out
      if (current == null) {
        return null;
      }
    }

    return current;
  }

  private Value newObject() throws PolyglotException {
    return context.eval("js", "({})");
  }

  private static boolean isPrefixedModuleName(String module) {
    return module.startsWith("/") || module.startsWith("../") || module.startsWith("./");
  }

  private static String[] getFilenamesToAttempt(String filename) {
    return new String[] {filename, filename + ".js", filename + ".json"};
  }

  static class WrappedList implements ProxyArray {
    private final List<Value> list;

    public WrappedList(List<Value> list) {
      this.list = list;
    }

    @Override
    public Object get(long index) {
      return list.get(Math.toIntExact(index));
    }

    @Override
    public void set(long index, Value value) {
      list.set(Math.toIntExact(index), value);
    }

    @Override
    public long getSize() {
      return list.size();
    }
  }
}
