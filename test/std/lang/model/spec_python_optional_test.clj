(ns std.lang.model.spec-python-optional-test
  (:require [std.lang :as l]
            [std.lang.base.emit-preprocess :as preprocess])
  (:use code.test))

(fact "python emission inherits module context for optional xtalk args"
  (preprocess/with:macro-opts [{:module {:id 'xt.lang.event-view}}]
    (l/emit-as :python
               '[(defn get-output [view dest-key]
                   (return dest-key))]))
  => "def get_output(view,dest_key = None):\n  return dest_key")
