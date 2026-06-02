(ns hara.model.spec-python-optional-test
  (:require [hara.lang :as l]
            [hara.common.emit-preprocess :as preprocess] [hara.common.preprocess-base :as preprocess-base])
  (:use code.test))

(fact "python emission inherits module context for optional xtalk args"
  (preprocess/with:macro-opts [{:module {:id 'xt.event.base-model}}]
    (l/emit-as :python
               '[(defn get-output [view dest-key]
                   (return dest-key))]))
  => "def get_output(view,dest_key = None):\n  return dest_key")
