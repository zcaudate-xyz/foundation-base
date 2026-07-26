tahto/model/spec_python_optional_test.clj:1:(ns tahto.model.spec-python-optional-test
  (:require [tahto.core :as l]
tahto/model/spec_python_optional_test.clj:3:            [tahto.common.emit-preprocess :as preprocess] [tahto.common.preprocess-base :as preprocess-base])
  (:use code.test))

(fact "python emission inherits module context for optional xtalk args"
  (preprocess/with:macro-opts [{:module {:id 'xt.event.base-model}}]
    (l/emit-as :python
               '[(defn get-output [view dest-key]
                   (return dest-key))]))
  => "def get_output(view,dest_key = None):\n  return dest_key")
