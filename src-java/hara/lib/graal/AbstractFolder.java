/*
 * Copyright 2018 Transposit Corporation. All Rights Reserved.
 */

package hara.lib.graal;

public abstract class AbstractFolder implements Folder {
  private Folder parent;
  private String path;

  public Folder getParent() {
    return parent;
  }

  public String getPath() {
    return path;
  }

  protected AbstractFolder(Folder parent, String path) {
    this.parent = parent;
    this.path = path;
  }
}
