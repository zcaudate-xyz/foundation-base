(ns rt.postgres.grammar
  (:require [rt.postgres.grammar.meta :as meta]
            [rt.postgres.grammar.common :as common]
            [rt.postgres.grammar.tf :as tf]
            [rt.postgres.grammar.form-let :as form-let]
            [rt.postgres.grammar.form-defn :as form-defn]
            [rt.postgres.grammar.form-defconst :as form-defconst]
            [rt.postgres.grammar.form-defrole :as form-defrole]
            [rt.postgres.grammar.form-deftype :as form-deftype]
            [rt.postgres.grammar.form-vec :as form-vec]
            [std.lang.base.emit :as emit]
            [std.lang.base.emit-common :as emit-common]
            [std.lang.base.emit-fn :as emit-fn]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.util :as ut]
            [std.lang.base.book :as book]
            [std.lang.base.script :as script]
            [std.string :as str]
            [std.lib :as h]))

(defn- pg-tf-free-data
  [[_ data]]
  (list 'quote (list (list 'quote (vec data)))))

(defn- pg-tf-free-vec
  [[_ data]]
  (list 'quote (vec data)))

(defn- pg-vector
  [arr grammar mopts]
  (cond (:js (meta arr))
        (emit-common/*emit-fn* (tf/pg-tf-js [nil arr]) grammar mopts)

        :else (form-vec/pg-section arr grammar mopts)))

(def +features+
  (-> (grammar/build :include [:builtin
                               :builtin-lang
                               :free-control
                               :free-literal
                               :math
                               :compare
                               :logic
                               :return
                               :vars
                               :block
                               :control-base
                               [:control-general :include [:for :branch]]
                               :control-try-catch
                               :top-base
                               :macro])
      (grammar/build:override
       {:mul       {:value true}
        :add       {:value true}
        :sub       {:value true}
        :div       {:value true}
        :eq        {:raw "="}
        :not       {:raw "NOT " :wrap true}
        :or        {:raw "OR" :wrap true}
        :and       {:raw "AND" :wrap true}
        :seteq     {:raw ":=" :value true}
        :ret       {:raw "RETURN"}
        :defrun    {:hydrate #'common/pg-hydrate
                    :static/return [:block]}
        :defn      {:hydrate #'common/pg-hydrate
                    :format  #'form-defn/pg-defn-format
                    :emit    #'form-defn/pg-defn
                    :static/dbtype :function}
        :def       {:emit :macro
                    :format  #'common/pg-format
                    :hydrate #'common/pg-hydrate
                    :macro   #'common/pg-defblock}
        :for       {:macro #'tf/pg-tf-for  :emit :macro}})

      ;;
      ;; OPS
      ;;
      (grammar/build:extend
       {:doblk   {:op :doblk   :symbol #{'do:block}    :emit  #'form-let/pg-do-block :type :block}
        :dosup   {:op :dosup   :symbol #{'do:suppress} :emit  #'form-let/pg-do-suppress :type :block}
        :doast   {:op :doast   :symbol #{'do:assert}   :emit  #'common/pg-do-assert :type :block :style/indent 1}
        :letblk  {:op :letblk  :symbol #{'let:block}   :macro #'form-let/pg-tf-let-block :emit :macro :type :block}
        :let     {:op :let     :symbol #{'let}         :macro #'form-let/pg-tf-let :emit :macro :type :block}
        :forech  {:op :forech  :symbol #{'for:each}    :macro #'tf/pg-tf-foreach :emit :macro :type :block}
        :fdata   {:op :fdata   :symbol #{'>-<}        :macro #'pg-tf-free-data :emit :macro :type :block}
        :fvec    {:op :fdata   :symbol #{'---}        :macro #'pg-tf-free-vec :emit :macro :type :block}
        :concat  {:op :concat  :symbol #{'||}          :value true :raw "||" :emit :infix}
        :cast    {:op :cast    :symbol #{'++}          :emit #'common/pg-typecast}
        :idxt    {:op :idxt    :symbol #{:->>}         :value true :raw "->>" :emit :infix}
        :idxj    {:op :idxj    :symbol #{:->}          :value true :raw "->"  :emit :infix}
        :remc    {:op :remc    :symbol #{'re}          :raw "~"    :emit :bi}  
        :remi    {:op :remi    :symbol #{'re:*}        :raw "~*"   :emit :bi}  
        :array   {:op :array   :symbol #{'array}       :emit  #'common/pg-array}
        :js      {:op :js      :symbol #{'js}          :macro #'tf/pg-tf-js :type :macro}})
      
      ;;
      ;; TOP LEVEL
      ;;
      (grammar/build:extend
       {:defenum   {:op :defenum :symbol '#{defenum}
                    :type :def :section :code :emit :macro
                    :hydrate #'common/pg-hydrate
                    :macro   #'common/pg-defenum
                    :static/dbtype :enum}

        :deftype   {:op :deftype :symbol '#{deftype}
                    :type :def :section :code :emit :macro
                    :format       #'form-deftype/pg-deftype-format
                    :hydrate      #'form-deftype/pg-deftype-hydrate
                    :hydrate-hook #'form-deftype/pg-deftype-hydrate-hook
                    :macro        #'form-deftype/pg-deftype
                    :static/dbtype :table}

        :defconst  {:op :defconst :symbol '#{defconst}
                    :type :def :section :code :emit :macro
                    :hydrate      #'form-defconst/pg-defconst-hydrate
                    :macro        #'form-defconst/pg-defconst
                    :static/dbtype :const}

        :defrole  {:op :defrole :symbol '#{defrole}
                   :type :def :section :code :emit :macro
                   :format       #'common/pg-format
                   :hydrate      #'common/pg-hydrate
                   :macro        #'form-defrole/pg-defrole
                   :static/dbtype :access}})))

(def +template+
  (->> {:banned #{}
        :highlight '#{return break do:assert}
        :default {:comment   {:prefix "--"}
                  :common    {:line-spacing 1
                              :space  " "
                              :assign ":="}
                  :function  {:raw ""
                              :args {:multiline true}}
                  :invoke    {:reversed true
                              :type {:uppercase true}
                              :keyword-fn #'common/pg-invoke-typecast}
                  :block     {:parameter {:start " " :end " " :space " " :statement "" :sep ""}
                              :body      {:start "" :end ""}}}
        :data    {:map       {:custom #'common/pg-map}
                  :set       {:custom #'common/pg-set}
                  :vector    {:custom #'pg-vector}}
        :token   {:nil       {:as "null"}
                  :string    {:custom #'common/pg-string}
                  :symbol    {:replace {\- "_" \: "." \? "p_"}
                             :link-fn #'common/pg-linked-token}}
        :block   {:branch  {:wrap    {:start "" :end "END IF;"}
                            :control {:default {:parameter  {:start " " :end " THEN"}
                                                :body {:append true
                                                       :start "" :end ""}}
                                      :if      {:raw "IF"}
                                      :elseif  {:raw "ELSIF"}
                                      :else    {:raw "ELSE"}}}}
        :function {:defn    {:args  {:assign "DEFAULT"}}}}
       (h/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :pg
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta meta/+fn+))

(def +book+
  (book/book {:lang :postgres
              :meta +meta+
              :grammar +grammar+
              :file {:suffix "pg.sql"}}))

(def +init+
  (script/install +book+))
