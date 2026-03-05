(ns bb-compat)

(defmacro if-bb
  [then else]
  (if (System/getProperty "babashka.version")
    then
    else))

(defmacro if-clj
  [form]
  (if-not (System/getProperty "babashka.version")
    form))
