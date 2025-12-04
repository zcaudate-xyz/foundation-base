(ns code.test.base.context)

(defonce ^:dynamic *eval-fact* false)

(defonce ^:dynamic *eval-mode* true)

(defonce ^:dynamic *eval-meta* nil)

(defonce ^:dynamic *eval-global* nil)

(defonce ^:dynamic *eval-check* nil)

(defonce ^:dynamic *eval-current-ns* nil)

(defonce ^:dynamic *run-id* true)

(defonce ^:dynamic *registry* (atom {}))

(defonce ^:dynamic *accumulator* (atom nil))

(defonce ^:dynamic *errors* nil)

(defonce ^:dynamic *settings* {:test-paths ["test"]})

(defonce ^:dynamic *root* ".")

(defonce ^:dynamic *results* nil)

(defonce ^:dynamic *timeout-global* 20000)

(defonce ^:dynamic *timeout* nil)

(defonce ^:dynamic *print* #{:print-throw :print-failed :print-timeout :print-bulk})

(defn new-context
  []
  {:eval-fact false
   :eval-mode false
   :eval-meta  nil
   :eval-global nil
   :eval-check nil
   :eval-current-ns nil
   :run-id true
   :registry    (atom {})
   :accumulator (atom {})
   :errors nil
   :results nil
   :timeout 60000
   :print #{:print-throw :print-failed :print-timeout :print-bulk}})

(defmacro with-new-context
  [m & body]
  `(let [ctx# (merge (new-context)
                     ~m)]
     (binding [*eval-fact*       (:eval-fact ctx#)
               *eval-mode*       (:eval-mode ctx#)
               *eval-meta*       (:eval-meta ctx#)
               *eval-global*     (:eval-global ctx#)
               *eval-check*      (:eval-check ctx#)
               *eval-current-ns* (:eval-current-ns ctx#)
               *run-id*          (:run-id ctx#)
               *registry*        (:registry ctx#)
               *accumulator*     (:accumulator ctx#)
               *errors*          (:errors ctx#)
               *settings*        (:settings ctx#)
               *root*            (:root ctx#)
               *results*         (:results ctx#)
               *print*           (:print ctx#)]
       ~@body)))
