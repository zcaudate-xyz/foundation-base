(ns hara.model.annex.spec-octave
  (:require [clojure.string]
            [hara.lang.book :as book]
            [hara.common.emit :as emit]
            [hara.common.grammar :as grammar]
            [hara.lang.impl :as impl]
            [hara.lang.script :as script]
            [hara.model.spec-xtalk]
            [std.lib.collection :as collection])
  (:refer-clojure :exclude [for while]))

(defn octave-token-boolean
  "emits true/false"
  {:added "4.0"}
  [bool]
  (if bool "true" "false"))

(defn tf-defn
  "transforms defn to a raw octave function definition"
  {:added "4.0"}
  [[_ sym args & body]]
  (let [arg-str   (clojure.string/join ", " (map name args))
        body-strs (mapv #(impl/emit-as :octave [%]) body)
        body-lines (concat (butlast body-strs)
                           [(str (name sym) " = " (last body-strs) ";")])]
    (list ':- (str "function " (name sym) "(" arg-str ")\n"
                   (clojure.string/join "\n" body-lines)
                   "\nend"))))

(def +features+
  (-> (merge (grammar/build :include [:builtin
                                      :builtin-global
                                      :builtin-module
                                      :builtin-helper
                                      :free-control
                                      :free-literal
                                      :math
                                      :compare
                                      :logic
                                      :return
                                      :block
                                      :data-shortcuts
                                      :data-range
                                      :vars
                                      :fn
                                      :control-base
                                      :control-general
                                      :top-base
                                      :top-global
                                      :top-declare
                                      :for
                                      :macro
                                      :macro-arrow
                                      :macro-let
                                      :macro-xor])
             (grammar/build-xtalk))
      (grammar/build:override
       {:mod   {:raw "mod" :emit :invoke}
        :pow   {:raw "^"}
        :neq   {:raw "~="}
        :and   {:raw "&&"}
        :or    {:raw "||"}
        :not   {:raw "~"}
        :defn  {:macro #'tf-defn :emit :macro}})))

(def +template+
  (->> {:banned #{:set :keyword}
        :allow  {:assign #{:symbol}}
        :highlight '#{return break end for if while function}
        :default {:comment   {:prefix "%"}
                  :common    {:statement ";"
                              :assign "="}
                  :invoke    {:space ""}
                  :function  {:raw "function"
                              :body {:start ""
                                     :end "end"}}
                  :index     {:offset 1
                              :end-inclusive true
                              :start "("
                              :end ")"}
                  :block     {:parameter {:start " ("
                                          :end ")"}
                              :body {:start ""
                                     :end "end"}}}
         :token  {:nil       {:as "NA"}
                  :boolean   {:as #'octave-token-boolean}
                  :string    {:quote :double}
                  :symbol    {:replace {\- "_"
                                        \? "p"
                                        \! "f"}}}
         :data   {:vector    {:start "[" :end "]" :sep ", "}
                  :map       {:start "struct(" :end ")" :sep ", "}
                  :map-entry {:start "" :end "" :space "" :assign ", " :keyword :string}}
         :define {:def       {:raw ""}
                  :defn      {:raw ""}
                  :shorthand true}}
        (collection/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :octave
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-current (fn [])
    :module-import  (fn [name {:keys [as]} opts]
                      (if as
                        (list 'pkg 'load name)
                        (list 'pkg 'load name)))
    :module-export  (fn [name {:keys [as]} opts])}))

(def +book+
  (book/book {:lang :octave
              :parent :xtalk
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))
