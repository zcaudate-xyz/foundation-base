(ns code.tool.java
  (:require [code.tool.java.compile :as java]
            [std.lib.foundation :as f]))

(f/intern-in java/javac)
