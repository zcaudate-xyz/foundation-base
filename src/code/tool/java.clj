(ns code.tool.java
  (:require [code.tool.java.compile :as java]
            [std.lib.foundation]))

(std.lib.foundation/intern-in java/javac)
