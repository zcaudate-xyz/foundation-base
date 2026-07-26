/*
 * Copyright 2018 Transposit Corporation. All Rights Reserved.
 */

package hara.lib.graal;

public class Paths {
  public static String[] splitPath(String path) {
    return path.split("[\\\\/]");
  }
}
