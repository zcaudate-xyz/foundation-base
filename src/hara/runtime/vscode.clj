(ns hara.runtime.vscode
  (:require [std.lib :as h]
            [hara.runtime.vscode.impl :as impl])
  (:refer-clojure :exclude [eval]))

(h/intern-in
 impl/vscode
 impl/vscode:create
 impl/raw-eval-vscode)
