(ns std.lang.base.script-lint
  (:require [clojure.set]
            [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lib.collection]
            [std.lib.env]
            [std.lib.foundation]
            [std.lib.invoke :refer [definvoke]]
            [std.lib.walk]
            [std.string.common]))

;;
;; dumb linter
;;

(defn get-reserved-raw
  "gets all reserved symbols in the grammar"
  {:added "4.0"}
  [lang]
  (set (keys (:reserved (impl/grammar lang)))))

(def get-reserved (memoize get-reserved-raw))

(defn collect-vars
  "collects all vars"
  {:added "4.0"}
  [form]
  (let [vars (volatile! #{})
        _ (std.lib.walk/prewalk (fn [form]
                       (cond (symbol? form)
                             (do (vswap! vars conj form)
                                 form)
                             
                             :else form))
                     form)]
    @vars))

(definvoke collect-module-globals
  "collects global symbols from module"
  {:added "4.0"}
  [:recent {:key :id
            :compare std.lib.foundation/hash-code}]
  ([module]
   (let [{:keys [native static]} module]
     (disj (clojure.set/union
            (set (mapcat #(mapcat std.lib.collection/seqify (vals %))
                         (vals native)))
            (:lang/lint-globals static))
           '*))))

(defn collect-sym-vars
  "collect symbols and vars"
  {:added "4.0"}
  ([entry module]
   (collect-sym-vars entry module #{}))
  ([entry module lang-globals]
   (let [globals  (or (clojure.set/union lang-globals
                               (collect-module-globals module)
                               (:static/lint-globals entry))
                      #{})
         {:keys [form form-input lang op-key]} entry
         form (or form form-input)
         reserved (get-reserved lang)
         [vars lint-input] (case op-key 
                             (:defn
                              :defgen) [(volatile! (collect-vars (nth form 2)))
                                        (drop 3 form)]
                             (:def
                              :defglobal)  [(volatile! #{})
                                            (drop 2 form)])
         syms (volatile! #{})
         sym-fn (fn [form]
                  (let [[ftag] form
                        form (if (and (= ftag 'fn)
                                      (symbol? (second form)))
                               (do (vswap! vars clojure.set/union (collect-vars (second form)))
                                   (cons 'fn (drop 2 form)))
                               form)]
                    (cond (#{'var 'const 'fn:> 'fn 'local} ftag)
                          (do (vswap! vars clojure.set/union (collect-vars (second form)))
                              (drop 2 form))

                          (= '. ftag)
                          (let [[_ sym & body] form]
                            [sym (map (fn [form]
                                        (cond (std.lib.collection/form? form)
                                              (drop 1 form)

                                              (symbol? form)
                                              nil
                                              
                                              :else form))
                                      body)])
                          
                          (or (std.string.common/starts-with? (str ftag) "for:")
                              (= (str ftag) "forange"))
                          (let [[_ bindings & body] form]
                            (do (vswap! vars clojure.set/union (collect-vars (first bindings)))
                                [(second bindings) body]))
                          
                          (= 'let ftag)
                          (let [[_ bindings & body] form
                                all-bindings (take-nth 2 bindings)
                                all-bound    (take-nth 2 (drop 1 bindings))]
                            (do (vswap! vars clojure.set/union (collect-vars all-bindings))
                                [all-bound body]))
                          
                          ('#{!:G new} ftag)
                          (do (vswap! vars conj (second form))
                              nil)

                          (= 'catch ftag)
                          (let [[_ bindings & body] form]
                            (do (vswap! vars clojure.set/union (collect-vars bindings))
                                body))
                          
                          :else form)))
         _    (std.lib.walk/prewalk
               (fn [form]
                 (cond (std.lib.collection/form? form)
                       (sym-fn form)
                       
                       (symbol? form)
                       (cond (or (= '_ form)
                                 (globals form)
                                 (reserved form)
                                 (namespace form)
                                 (std.string.common/starts-with? (str form)
                                                   "x:")
                                 (std.string.common/starts-with? (str form)
                                                   "..."))
                             form
                             
                             :else
                             (let [s  (first (std.string.common/split (str form)
                                                        #"\."))
                                   s  (if s (symbol s))]
                               (cond (or (nil? s)
                                         (globals s)
                                         (reserved s))
                                     form
                                     
                                     :else
                                     (do (vswap! syms conj s)
                                         form))))
                       
                       :else form))
               lint-input)]
     {:vars @vars
      :syms @syms})))

(defn sym-check-linter
  "checks the linter"
  {:added "4.0"}
  ([entry module]
   (sym-check-linter entry module #{}))
  ([entry module lang-globals]
   (sym-check-linter entry module lang-globals {:unknown :print
                                                :unused  :silent}))
  ([entry module lang-globals options]
   (let [{:keys [id op]} entry]
     (cond ('#{defn def defgen defglobal fn} op)
           (let [{:keys [vars syms]} (collect-sym-vars entry module lang-globals)
                 internal (disj (set (vals (:internal module)))
                                '-)
                 unused  (clojure.set/difference (disj vars '_) syms)
                 unknown (clojure.set/difference syms vars)
                 unknown (clojure.set/difference unknown internal)]
             (when (not-empty unused)
               (when (= :print (:unused options))
                 (std.lib.env/p (str "UNUSED VAR @ " (:module entry) "/" id)
                      {:unused unused})))
             (when (not-empty unknown)
               (when (= :print (:unknown options))
                 (std.lib.env/p (str "UNKNOWN VARIABLES @ " (:module entry) "/" id)
                      {:unknown unknown}))
               (when (= :error (:unknown options))
                 (std.lib.foundation/error (str "UNKNOWN VARIABLES @ " (:module entry) "/" id)
                          {:unknown unknown
                           :form (or (:form entry)
                                     (:form-input entry))})))
             :pass)))))

;;
;;
;;

(defonce +registry+
  (atom {}))

(def +settings+
  (atom {:lua   {:linters [sym-check-linter]
                 :globals '#{tonumber
                             unpack
                             error
                             next
                             type
                             pcall
                             table
                             math
                             cjson
                             os
                             io
                             string
                             getmetatable
                             ngx
                             require
                             GLOBAL
                             CONFIG}}
         :xtalk {:linters [sym-check-linter]
                 :globals '#{XT}}
         :solidity {:linters [sym-check-linter]
                    :globals '#{msg
                                require}}
         :js    {:linters [sym-check-linter]
                 :globals '#{Worker
                             Request
                             Response
                             Promise
                             alert
                             setTimeout
                             setInterval
                             clearTimeout
                             clearInterval
                             Date
                             eval
                             WebSocket
                             EventSource
                             self
                             arguments
                             fetch
                             FileReader
                             globalThis
                             JSON
                             BigInt
                             console
                             import
                             document
                             window
                             navigator
                             screen
                             location
                             localStorage
                             sessionStorage
                             Array
                             Object
                             Math
                             Number
                             require
                             process}}}))

(defn lint-set
  "sets the linter for a namespace"
  {:added "4.0"}
  ([ns]
   (lint-set ns true))
  ([ns option]
   (swap! +registry+
          (fn [m]
            (if option
              (assoc m ns true)
              (dissoc m ns))))))

(defn lint-clear
  "clears all linted namespaces"
  {:added "4.0"}
  []
  (reset! +registry+ {}))

(defn lint-needed?
  "checks if lint is needed"
  {:added "4.0"}
  [ns]
  (get @+registry+ ns))

(defn lint-entry
  "lints a single entry"
  {:added "4.0"}
  [entry module]
  (let [{:keys [lang]} entry
        {:keys [linters globals]} (get @+settings+ lang)]
    (when (not (-> module :static :lang/no-lint))
      (doseq [linter linters]
        (linter entry module globals {:unknown :error
                                      :unused  :silent})))))


(comment
  (keys *module*)
  )
