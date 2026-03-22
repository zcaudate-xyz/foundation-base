(ns std.dom.invoke
  (:require [std.dom.common :as base]
            [std.dom.component :as component]
            [std.dom.impl :as impl]
            [std.dom.type :as type]
            [std.lib.function :as fn]
            [std.lib.invoke :as invoke]
            [std.protocol.invoke :as protocol.invoke]))

(invoke/definvoke invoke-intern-dom
  "constructor for dom
 
   (invoke-intern-dom nil 'hello {:class :react
                                  :tag :test/hello-dom} [['dom '_] 1])
   
   (invoke-intern-dom nil 'hello {:class :value
                                  :tag :test/hello-dom} [['_] 1])"
  {:added "3.0"}
  [:method {:multi protocol.invoke/-invoke-intern
            :val :dom}]
  ([_ name {:keys [class tag protect] :as params} body]
   (let [var    (or (resolve name)
                    (intern *ns* name))
         params (assoc params :var var)
         _   (if-let [props (type/metaprops tag)]
               (cond (and var (= var (:var props)))
                     nil
                     
                     protect
                     (throw (ex-info "dom tag overwritten" {:tag tag
                                                            :var var
                                                            :metaprops props}))))
         name (with-meta name {:arglists ''([props] [props children])})
         definition `(defn ~name
                       ([~'props] (base/dom-compile [~tag ~'props])))]
     (cond (= class :value)
           `(let [~'construct (fn ~@body)]
              (fn/arg-check ~'construct 1 "value class takes 1 argument")
              (type/metaprops-add :dom/value (assoc ~params :construct ~'construct))
              ~definition)

           (= class :element)
           `(do
              (type/metaprops-add ~(:metaclass params) ~params)
              ~definition)
           
           :else
           `(let [~'template (fn ~@body)]
              (fn/arg-check ~'template 2 "template class takes 2 arguments")
              (component/component-install ~tag ~class ~'template ~params)
              ~definition)))))
