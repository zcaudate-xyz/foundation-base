(ns code.tool.java
  (:require [std.lib :as h]
            [code.tool.java.compile :as java]))

(h/intern-in java/javac)
