(ns std.lang.base.emit-rewrite)

(defn stage-transforms
  "returns rewrite transforms for a compile stage"
  {:added "4.1"}
  [grammar stage]
  (vec (get-in grammar [:rewrite stage])))

(defn rewrite-stage
  "applies language rewrite transforms for a compile stage"
  {:added "4.1"}
  [stage form grammar mopts]
  (reduce (fn [acc transform]
            (transform acc {:stage stage
                            :grammar grammar
                            :mopts mopts}))
          form
          (stage-transforms grammar stage)))
