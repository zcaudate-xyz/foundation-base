(ns js.cell.kernel.base-link-eval
  (:require [std.lang :as l]
            [std.lang.typed.xtalk :refer [defspec.xt]]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.walk :as walk]
            [xt.lang.common-notify :as notify]))

(l/script :js
  {:require [[js.cell.kernel.base-link :as link]
             [xt.lang.common-repl :as repl]]})


(defspec.xt post-eval
  [:fn [js.cell.kernel.spec/LinkRecord
        :xt/any
        [:xt/maybe :xt/bool]
        [:xt/maybe :xt/str]]
   :xt/any])

;;
;; TESTING
;;

(def ^:dynamic *temp-id* nil)

(defmacro ^{:style/indent 1}
  wait-post
  "posts code to worker"
  {:added "4.0"}
  [worker body & [action opts id]]
  (let [[worker timeout] (if (vector? worker)
                           worker
                           [worker 1000])
        [op input] (cond (string? action)
                         [action body]
                         
                         :else
                         ["eval" (list '@! (list 'std.lang/with:input (list '!.js body)))])]
    (list 'xt.lang.common-notify/wait-on [:js timeout]
          (list '. worker (list 'postMessage (merge {:op op
                                                     :id id
                                                     :body input}
                                                    opts))))))

(defmacro async-post
  "helper for async post"
  {:added "4.0"}
  [value]
  (list 'postMessage
        {:op "eval"
         :id (or *temp-id* (f/error "No return id"))
         :status "ok"
         :body (list 'JSON.stringify {:type "data"
                                      :value value})}))

(defn- async-post-transform
  [body id]
  (walk/prewalk (fn [form]
               (cond (and (collection/form? form)
                          (symbol? (first form))
                          (resolve (first form))
                          (= (f/var-sym (resolve (first form)))
                             `async-post))
                     (list 'postMessage
                           {:op "eval"
                            :id id
                            :status "ok"
                            :body (list 'JSON.stringify {:type "data"
                                                         :value (second form)})})
                     (and (symbol? form)
                          (resolve form)
                          (= (f/var-sym (resolve form))
                             `async-post))
                     (list 'fn '[value]
                           (list 'postMessage
                                 {:op "eval"
                                  :id id
                                  :status "ok"
                                  :body (list 'JSON.stringify {:type "data"
                                                               :value 'value})}))
                     
                     :else form))
             body))  

(defmacro.js ^{:style/indent 1}
  post-eval
  "posts to worker, works in conjuction with async-post"
  {:added "4.0"}
  [link body & [async id]]
  (let [id    (or id (str "eval-" (rand-int 10000000)))
        body  (async-post-transform body id)
        input (eval (list 'std.lang/with:input (list '!.js body)))]
    (list 'js.cell.kernel.base-link/call link {:op "eval"
                                               :id id
                                               :async async
                                               :body input})))

(defmacro ^{:style/indent 1}
  wait-eval
  "posts code to worker with eval"
  {:added "4.0"}
  [link body & [async id]]
  (let [[link timeout] (if (vector? link)
                         link
                         [link 1000])
        id    (or id (str "eval-" (rand-int 10000000)))
        body  (async-post-transform body id)
        input (list '@! (list 'std.lang/with:input (list '!.js body)))]  
    (list 'xt.lang.common-notify/wait-on [:js timeout]
          (list '. (list 'js.cell.kernel.base-link/call link {:op "eval"
                                                              :id id
                                                              :async async
                                                              :body input})
                (list 'then '(xt.lang.common-repl/>notify))))))
