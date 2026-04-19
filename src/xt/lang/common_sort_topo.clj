(ns xt.lang.common-sort-topo
  (:require [std.lang :as l :refer [defspec.xt]]
            [xt.lang.common-data :as common-data]))

(l/script :xtalk
  {:require [[xt.lang.common-spec :as xt]]})


;;
;; SORT TOPO
;;

(defn.xt sort-edges-build
  "builds an edge with links"
  {:added "4.0"}
  [nodes edge]
  (var n-from (xt/x:first edge))
  (var n-to   (xt/x:second edge))
  (if (not (xt/x:has-key? nodes n-from))
    (xt/x:set-key nodes n-from {:id n-from
                                :links []}))
  (if (not (xt/x:has-key? nodes n-to))
    (xt/x:set-key nodes n-to {:id n-to
                              :links []}))
  (var links (. nodes [n-from] ["links"]))
  (xt/x:arr-push links n-to))

(defn.xt sort-edges-visit
  "walks over the list of edges"
  {:added "4.0"}
  [nodes visited sorted id ancestors]
  (if (== true (xt/x:get-key visited id))
    (return))
  (var node (. nodes [id]))
  (if (xt/x:nil? node)
    (xt/x:err (xt/x:cat "Not available: " id)))
  (do (:= ancestors (:? (xt/x:nil? ancestors) [] ancestors))
      (xt/x:arr-push ancestors id)
      (xt/x:set-key visited id true)
      (var input (. node ["links"]))
      (xt/for:array [link input]
        (-/sort-edges-visit nodes visited sorted link (xt/x:arr-clone ancestors))))
  (xt/x:arr-push-first sorted id))

(defn.xt sort-edges
  "sort edges given a list"
  {:added "4.0"}
  [edges]
  (var nodes   {})
  (var sorted  [])
  (var visited {})
  (xt/for:array [e edges]
    (-/sort-edges-build nodes e))
  (xt/for:object [[id _] nodes]
    (-/sort-edges-visit nodes visited sorted id nil))
  (return sorted))

(defn.xt sort-topo
  "sorts in topological order"
  {:added "4.0"}
  [input]
  (var edges [])
  (xt/for:array [link input]
    (var root (xt/x:first link))
    (var deps (xt/x:second link))
    (xt/for:array [d deps]
      (xt/x:arr-push edges [root d])))
  (return (xt/x:arr-reverse (-/sort-edges edges))))
