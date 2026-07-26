/*
 * Copyright 2018 Transposit Corporation. All Rights Reserved.
 */

package hara.lib.graal;

import java.util.HashMap;
import java.util.Map;

public class ModuleCache {
  private Map<String, Module> modules = new HashMap<>();

  public Module get(String fullPath) {
    return modules.get(fullPath);
  }

  public void put(String fullPath, Module module) {
    modules.put(fullPath, module);
  }
}
