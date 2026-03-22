(ns std.make.makefile
  (:require [std.lib.env]
            [std.lib.foundation]
            [std.string.common]
            [std.string.prose]))

(def ^:dynamic *indent* 0)

(defn- block?
  ([v]
   (or (map? v)
       (and (vector? v)
            (vector? (first v))))))

(defn- nested-block?
  ([v]
   (and (vector? v)
        (block? (last v)))))

(defn emit-headers
  "emits makefile headers
 
   (emit-headers {:CC :gcc})
   => \"CC = gcc\""
  {:added "4.0"}
  ([m]
   (->> (map (fn [[k v]]
               (std.lib.foundation/strn k " = " (std.string.prose/write-line v)))
             m)
        (std.string.common/join "\n"))))

(defn emit-target
  "emits all makefile targets"
  {:added "4.0"}
  ([[tag & more]]
   (let [[deps commands] (if (map? (first more))
                           [(:- (first more)) (rest more)]
                           [nil more])]
     (str (std.lib.foundation/strn tag ":")
          (if deps (str " " (std.string.prose/write-line deps))) "\n\t"
          (std.string.common/join "\n\t" (map std.string.prose/write-line commands))))))

(defn write
  "link to `std.lang.compile/compile-ext-fn`"
  {:added "4.0"}
  ([v]
   (let [[headers & targets] (if (map? (first v))
                               v
                               (cons nil v))]
     (str (when headers
            (str (emit-headers headers) "\n\n"))
          (->> (map emit-target targets)
               (std.string.common/join "\n\n"))))))

(comment

  (std.lib.env/pl (write
       [{:CC     :gcc
         :CFLAGS "-I."
         :DEPS   ["hellomake.h"]
         :OBJ    ["hellomake.h"]}
        [:.PHONY {:- "clean"}]
        [:clean
         '[rm -f "$(ODIR)/*.o" *- core "$(INCDIR)/*~"]]])))
