(ns std.lang.model.spec-xtalk.com-r
  (:require [std.lib :as h]))

;;
;; COM
;;

(defn r-tf-x-return-encode
  ([[_ out id key]]
   (h/$ (do* (library "jsonlite")
             (tryCatch (block (toJSON {:type "data"
                                       :value ~out
                                       :id ~id
                                       :key ~key}
                                      :auto-unbox true :null "null"))
                       :error (fn [err]
                                (toJSON {:type "raw"
                                         :value (toString out)
                                         :id ~id
                                         :key ~key}
                                        :auto-unbox true :null "null")))))))

(defn r-tf-x-return-wrap
  ([[_ f encode-fn]]
   (h/$ (do* (library "jsonlite")
             (tryCatch
              (block
               (:= out (~f))
               (~encode-fn out nil nil))
              :error   (fn [err]
                         (paste "{\"type\":\"error\",\"value\":"
                                (toJSON (toString err))
                                "}")))))))

(defn r-tf-x-return-eval
  ([[_ s wrap-fn]]
   (h/$ (return (~wrap-fn
                 (fn []
                   (eval (parse :text ~s))))))))

(def +r-return+
  {:x-return-encode  {:macro #'r-tf-x-return-encode   :emit :macro}
   :x-return-wrap    {:macro #'r-tf-x-return-wrap     :emit :macro}
   :x-return-eval    {:macro #'r-tf-x-return-eval     :emit :macro}})

(defn r-tf-x-socket-connect
  ([[_ host port opts]]
   (h/$ (do* (return (socketConnection :port ~port :blocking true))))))

(defn r-tf-x-socket-send
  ([[_ conn s]]
   (h/$ (writeLines ~s ~conn :sep "\n"))))

(defn r-tf-x-socket-close
  ([[_ conn]]
   (h/$ (close ~conn))))

(def +r-socket+
  {:x-socket-connect      {:macro #'r-tf-x-socket-connect      :emit :macro}
   :x-socket-send         {:macro #'r-tf-x-socket-send         :emit :macro}
   :x-socket-close        {:macro #'r-tf-x-socket-close        :emit :macro}})

(def +r-ws+
  {})

(def +r-notify+
  {})

(def +r-client+
  {})

(defn r-tf-x-print
  ([[_ & args]]
   (apply list 'print args)))

(defn r-tf-x-shell
  ([[_ s cm]]
   (h/$ (system ~s))))

(def +r-shell+
  {:x-print    {:macro #'r-tf-x-print         :emit :macro}
   :x-shell    {:macro #'r-tf-x-shell         :emit :macro}})

(def +r-com+
  (merge +r-return+
         +r-socket+
         +r-ws+
         +r-notify+
         +r-client+
         +r-shell+))
