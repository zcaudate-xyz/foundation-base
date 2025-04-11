(ns rt.postgres.client-impl
  (:require [std.lang.base.pointer :as ptr]
            [std.lang.base.util :as ut]
            [std.lang.base.impl :as impl]
            [std.lang.base.library :as lib]
            [std.lang.base.book :as book]
            [lib.jdbc :as jdbc]
            [lib.postgres :as libpg]
            [lib.postgres.connection :as conn]
            [std.json :as json]
            [std.lib :as h :refer [defimpl]]))

(defn raw-eval-pg-return
  "returns a regularised result"
  {:added "4.0"}
  [ret]
  (let [ret  (if (and (coll? (first ret))
                      (= 1 (count (first ret)))) 
               (map (comp second first) ret)
               ret)
        out (if (= 1 (count ret))
              (first ret)
              ret)]
    (if (string? out)
      (try (let [data (json/read out json/+keyword-case-mapper+)]
             (if (number? data)
               (h/parse-double out))
             data)
           (catch Throwable t
             out))
      out)))

(defn raw-eval-pg
  "executes a raw value"
  {:added "4.0"}
  [{:keys [instance] :as rt} body]
  (h/prn instance body)
  (conn/conn-execute @instance body))

(defn init-ptr-pg
  "initiates a pointer in the runtime
 
   (base/init-ptr-pg -pg- scratch/addf)
   => -2"
  {:added "4.0"}
  [{:keys [mode] :as pg} ptr]
  (if (and (= :dev mode)
           (= :code (:section @ptr)))
    (let [body (ptr/ptr-display ptr {})]
      (ptr/ptr-invoke pg
                      raw-eval-pg
                      body
                      {}
                      raw-eval-pg-return))))

(defn prepend-select-check-form
  "checks if form needs a `SELECT` prepended"
  {:added "4.0"}
  [[f :as form] book]
  (or (keyword? f)
      (if-let [reserved (get-in book [:grammar :reserved f])]
        (or (not (#{:block :macro :special} (:type reserved))))
        (and (symbol? f)
             (let [entry (if-let [var (resolve f)]
                           (and (h/pointer? @var)
                                (let [{:keys [module section id]} @var]
                                  (get-in book [:modules module section id])))
                           (let [[sym-module sym-id] (ut/sym-pair f)]
                             (and sym-module
                                  (or (get-in book [:modules sym-module :code sym-id])
                                      (get-in book [:modules sym-module :fragment sym-id])))))]
               (and entry
                    (not= [:block] (:static/return entry))))))))

(defn prepend-select-check
  "checks if values needs a `SELECT` prepended"
  {:added "4.0"}
  [ptr args]
  (let [book (lib/get-book (impl/runtime-library)
                           :postgres)
        {:keys [module section id]} ptr]
    (or (and id
             (not= [:block] (get-in book [:modules module section id :static/return])))
        (and (not (:id ptr))
             (= 1 (count args))
             
             (let [x (first args)]
               (and (not (vector? x))
                    (or (string? x)
                        (number? x)
                        (map? x)
                        (and (h/form? x)
                             (prepend-select-check-form x book)))))))))

(defn invoke-ptr-pg
  "invokes a pointer in runtime"
  {:added "4.0"}
  [{:keys [instance] :as pg} ptr args]
  (let [{:keys [bulk]} (meta args)
        add-select (prepend-select-check ptr args)
        body (ptr/ptr-invoke-string ptr args {:lang :postgres
                                              :layout :module
                                              :emit {:transform (fn [forms {:keys [bulk] :as opts}]
                                                                  (if (not bulk)
                                                                    forms
                                                                    (if add-select
                                                                      (first forms)
                                                                      (apply list 'do forms))))}})]
    (binding [conn/*execute* (if (and bulk
                                      (not add-select))
                               jdbc/execute
                               jdbc/fetch)]
      (ptr/ptr-invoke pg
                      raw-eval-pg
                      body
                      (if add-select {:in (fn [s] (str "select " s ";"))})
                      raw-eval-pg-return))))
