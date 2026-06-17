(ns ocaml.core.builtin
  "Curated outline of common OCaml standard library builtins."
  (:require [hara.lang :as l]
            [std.lib.foundation :as f]))

(l/script :ocaml
  ocaml.core
  {:macro-only true})

(def +list+
  "List module functions from the OCaml standard library."
  [
    {:name "List.length" :signature "List.length : 'a list -> int"}
    {:name "List.hd" :signature "List.hd : 'a list -> 'a"}
    {:name "List.tl" :signature "List.tl : 'a list -> 'a list"}
    {:name "List.nth" :signature "List.nth : 'a list -> int -> 'a"}
    {:name "List.map" :signature "List.map : ('a -> 'b) -> 'a list -> 'b list"}
    {:name "List.filter" :signature "List.filter : ('a -> bool) -> 'a list -> 'a list"}
    {:name "List.fold_left" :signature "List.fold_left : ('a -> 'b -> 'a) -> 'a -> 'b list -> 'a"}
    {:name "List.fold_right" :signature "List.fold_right : ('a -> 'b -> 'b) -> 'a list -> 'b -> 'b"}
    {:name "List.rev" :signature "List.rev : 'a list -> 'a list"}
    {:name "List.append" :signature "List.append : 'a list -> 'a list -> 'a list"}
    {:name "List.concat" :signature "List.concat : 'a list list -> 'a list"}
    {:name "List.iter" :signature "List.iter : ('a -> unit) -> 'a list -> unit"}
    {:name "List.mem" :signature "List.mem : 'a -> 'a list -> bool"}
    {:name "List.sort" :signature "List.sort : ('a -> 'a -> int) -> 'a list -> 'a list"}])

(def +string+
  "String module functions from the OCaml standard library."
  [
    {:name "String.length" :signature "String.length : string -> int"}
    {:name "String.get" :signature "String.get : string -> int -> char"}
    {:name "String.sub" :signature "String.sub : string -> int -> int -> string"}
    {:name "String.concat" :signature "String.concat : string -> string list -> string"}
    {:name "String.trim" :signature "String.trim : string -> string"}
    {:name "String.split_on_char" :signature "String.split_on_char : char -> string -> string list"}
    {:name "String.make" :signature "String.make : int -> char -> string"}
    {:name "String.uppercase_ascii" :signature "String.uppercase_ascii : string -> string"}
    {:name "String.lowercase_ascii" :signature "String.lowercase_ascii : string -> string"}])

(def +array+
  "Array module functions from the OCaml standard library."
  [
    {:name "Array.length" :signature "Array.length : 'a array -> int"}
    {:name "Array.get" :signature "Array.get : 'a array -> int -> 'a"}
    {:name "Array.set" :signature "Array.set : 'a array -> int -> 'a -> unit"}
    {:name "Array.make" :signature "Array.make : int -> 'a -> 'a array"}
    {:name "Array.map" :signature "Array.map : ('a -> 'b) -> 'a array -> 'b array"}
    {:name "Array.fold_left" :signature "Array.fold_left : ('a -> 'b -> 'a) -> 'a -> 'b array -> 'a"}
    {:name "Array.to_list" :signature "Array.to_list : 'a array -> 'a list"}
    {:name "Array.of_list" :signature "Array.of_list : 'a list -> 'a array"}])

(def +int+
  "Integer functions from the OCaml standard library."
  [
    {:name "abs" :signature "abs : int -> int"}
    {:name "max_int" :signature "max_int : int"}
    {:name "min_int" :signature "min_int : int"}
    {:name "succ" :signature "succ : int -> int"}
    {:name "pred" :signature "pred : int -> int"}])

(def +bool+
  "Boolean functions from the OCaml standard library."
  [
    {:name "not" :signature "not : bool -> bool"}
    {:name "&&" :signature "(&&) : bool -> bool -> bool"}
    {:name "||" :signature "(||) : bool -> bool -> bool"}])

(def +all+
  "All curated OCaml builtins."
  (vec (concat +list+ +string+ +array+ +int+ +bool+)))

(def +count+
  "Number of curated OCaml builtins."
  (count +all+))
