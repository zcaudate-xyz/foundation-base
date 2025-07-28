(ns std.lang.model.spec-rust-test
  (:use code.test)
  (:require [std.lang.model.spec-rust :refer :all]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lang :as l]
            [std.lib :as h]
            [std.fs :as fs]
            [std.string]))

(script/script- :rust
  {;;:require [[std :as h]]
   :import  [["std::mem"]]
   })

(script/script- :js)

(comment
  (!.rs
    (var [:> Cell :i32] i
         ($ Cell "new" 0))
    (match [maybe_digit]
           (| 0 1) => "not many"
           (:to 1 2)
           (:to 2 9)
           _ => "lots")
    
    (assert_eq! message "a few"))

  (!.rs
    (var integer := (:as decimal :u8))))

"
enum List<T> {
    Nil,
    Cons(T, Box<List<T>>)
}

let a: List<i32> = List::Cons(7, Box::new(List::Cons(13, Box::new(List::Nil))));"


(defenum.rs [:> List T]
  [Nil
   (Cons T [:> Box [:> List T]])])

(deftrait.rs ^{:% ["#[allow(dead_code)]"]}
  Animal
  
  (fn ^{:- [:Self]}
    new  [:&'static :str name])

  (fn ^{:- [:&'static :str]}
    name [])

  (fn ^{:- [:&'static :str]}
    noise [])
  
  (fn talk []
    (println! "{} says {}",
              (. self (name)),
              (. self (noise)))))

(!.rs
  (deftrait ;;^{:% ["#[allow(dead_code)]"]}
    Animal
    
    (fn ^{:- [:Self]}
      new  [:&'static :str name])

    (fn ^{:- [:&'static :str]}
      name [&self])
    
    (fn ^{:- [:&'static :str]}
      noise  [^:&mut self])
    
    (fn talk [^:& self]
      (println! "{} says {}",
                (. self (name)),
                (. self (noise))))))


(!.rs
  (unsafe
   (print! " 300.0 as u8 is : {}", (. (cast 300.0 :f32)
                                      ("to_int_unchecked::<u8>")))))

(!.rs
  (var :i32 five (exec (fn-call)
                       5)))

(defstruct.rs Sheep)

(defstruct.rs Sheep
  [[:bool naked]
   [:&'static :str name]])

(defimpl.rs Sheep<>Base
  [-/Sheep]
  (fn ^{:- [:bool]}
    is-naked []
    (. self naked))

  (fn ^{:- [:bool]
        :mut true}
    is-naked []
    (if (. self (is-naked))
      (println! "{} is already naked...", (. self (name)))
      (do (println! "{} gets a haircut", (. self name))
          (:= (. self naked) true)))))

(defimpl.rs Animal<>Sheep
  [-/Animal :for -/Sheep]
  
  (fn ^{:- [-/Sheep]} new [:&'static :str name]
    (new Sheep :name name :naked false))
  
  (fn ^{:- [:&'static :str]}
    name [&self]
    (. self name))

  (fn ^{:- [:&'static :str]}
    noise [&self]
    (if (. self (is-naked))
      "baaaaah?"
      "baaaaah!")))

(comment

  
  (defn.rs ^{:- [[:> Hello :i32]]}
    combine-vecs
    [[:> Vec :i32] u
     [:> Vec :i32] v])
  
  
  (defn.rs ^{:- [[:> "iter::Cycle"
                  [:> "iter::Chain"
                   [:> IntoIter :i32]
                   [:> IntoIter :i32]]]]}
    combine-vecs
    [[:> Vec :i32] v
     [:> Vec :i32] u]
    ($ iter "Cycle"))
  
  (defn.rs ^{:- [:impl [:> "Iterator" "Item=i32"]]}
    combine-vecs
    [[:> Vec :i32] u
     [:> Vec :i32] v]
    (. v
       (into-iter)
       (chain (. u (into-iter)))
       (cycle)))

  (defn.rs ^{:- ["impl Fn(i32) -> i32"]}
    combine-vecs
    [[:> Vec :i32] u
     [:> Vec :i32] v]
    (. v
       (into-iter)
       (chain (. u (into-iter)))
       (cycle)))
  
  (defn.rs ^{:- [:impl [:> Iterator :Item=i32]]}
    combine-vecs
    [[:> Vec :i32] u
     [:> Vec :i32] v]
    (. v
       (into-iter)
       (chain (. u (into-iter)))
       (cycle)))
  
  )

(comment

  (into {} List)
  
  (!.rs
    (defimpl Animal<>Sheep
      [-/Animal :for -/Sheep]
      
      (fn ^{:- [-/Sheep]} new [:&'static :str name]
        (new Sheep :name name :naked false))
      
      (fn ^{:- [:&'static :str]}
        name [&self]
        (. self name))

      (fn ^{:- [:&'static :str]}
        noise [&self]
        (if (. self (is-naked))
          "baaaaah?"
          "baaaaah!"))))
  
  
  (!.rs
    (defenum [:> List T]
      [Nil
       (Cons T [:> Box [:> List T]])]))
  
  (!.rs
    (deftrait [:> List T]
      [Nil
       (Cons T [:> Box [:> List T]])]))
  
  
  (!.rs
    (defenum [:> List T]
      Nil
      (Cons T [:> Box [:> List T]])))
  
  (!.rs
    (var [:> List :i32] a
         ($ List "Cons" 7 ($ Box "new"
                             ($ List "Cons" 13
                                ($ Box "new"
                                   ($ List "Nil")))))))
  
  (!.rs
    (var [:> List :i32] a
         ($ List "Cons" 7 ($ Box "new"
                             ($ List "Cons" 13
                                ($ Box "new"
                                   ($ List "Nil")))))))
  
  (!.rs
    (var [:> List A B] a "hello")
    )
  
  (!.rs
    [:> Box [:i32]])
  
  (!.rs :i32)
  
  (!.rs
    (var [:> List :i32] a
         ($ List "Cons" 7 ($ Box "new"
                             ($ List "Cons" 13
                                ($ Box "new"
                                   ($ List :Nil)))))))
  
  (!.rs
    ($ Box "new" ($ List :Nil)))

  (!.rs
    (:& hello))

  (!.rs
    (:&' hello))
  
  (!.rs
    (:& hello))
  
  (!.rs
    (fn ^{:- [T]} newe [:&'static name]))
  
  (!.rs
    (fn ^{:- [:String]} get [&self]))
  
  (!.rs
    (fn ^{:- [:String]}
      get [&self]))
  
  (!.rs
    (defn ^{:- [:String]}
      get [&self]))
  
  (!.rs
    &'hello)
  
  (!.rs
    (defenum [:> List T]
      [Nil
       (Cons T [:> Box [:> List T]])]))
  )

(comment
  

  (!.rs ^{:# [[lang := "owned_box"]
              [fundamental]
              [(stable :feature "rust1" :since "1.0.0")]]}
        (+ 1 2 3))
  => "1 + 2 + 3;")

(fact "various test cases for rust code"
  ^:hidden
  
  (!.rs
    (defstruct ^{:- [[T Sized]]}
      Box))
  => "struct Box;"
  
  (!.rs
    (defimpl Box<>Base
      [Box]
      
      
      (fn ^{:- [:String]} get [])))

  (!.rs
    (fn [:> double-positive :'a]))
  

  (!.rs
    (deftrait UsernameWidget
      
      (fn ^{:- [:String]} get [&self]))))

(fact "various test cases for rust code"
  ^:hidden

  (!.rs
    (defn main []
      (println! "{}" 123)))
  => (std.string/|
      "fn main() {"
      "  println!(\"{}\",123);"
      "}")

  (!.js
   (var (:int a) 3))
  => "let int a = 3;"
  
  (!.rs
    (var :mutable (:f64 number) 1.0))
  => "let mutable number: f64 = 1.0;"
  
  (!.rs
    (var (:f64 number) 1.0))
  => "let number: f64 = 1.0;"

  (!.rs
    (var (:f64 n) 1.0))
  => "let n: f64 = 1.0;"
  
  (!.rs
    (:to 0 3))
  => "0..3;"
  
  (!.rs
    ($ Class "new"))
  => "Class::new();"
  
  (!.rs
    ($ Class :new))
  => "Class::new;"


  
  
  (!.rs
    (defn main []
      (println! "{subject} {verb} {object}"
                :object "the lazy dog"
                :verb   "jumps over"
                :subject "the quick brown fox")))
  => (std.string/|
      "fn main() {"
      "  println!("
      "    \"{subject} {verb} {object}\","
      "    object=\"the lazy dog\","
      "    verb=\"jumps over\","
      "    subject=\"the quick brown fox\""
      "  );"
      "}")
  
  (!.rs
    (defn is-divisible-by [:u32 lhs
                           :u32 rhs]
      (if (== rhs 0)
        (return false))
      
      (return
       (== 0 (mod lhs rhs)))))
  => (std.string/|
      "fn is_divisible_by(lhs: u32, rhs: u32) {"
      "  if(rhs == 0){"
      "    return false;"
      "  }"
      "  return 0 == (lhs % rhs);"
      "}")

  (!.rs
    (defn main []
      (println! "Base 10:    {}"    69420)
      (println! "Base 2:     {:b}"  69420)
      (println! "Base 8:     {:o}"  69420)
      (println! "Base 16:    {:x}"  69420)

      (println! "{number:>5}"  :number 1)
      (println! "{number:0>5}"  :number 1)
      (println! "{number:0<5}"  :number 1)
      (println! "{number:0>width$}"
                :number 1
                :width  3)

      (println! "My name is {0}, {1} {0}", "Bond" "James")))
  => (std.string/|
      "fn main() {"
      "  println!(\"Base 10:    {}\",69420);"
      "  println!(\"Base 2:     {:b}\",69420);"
      "  println!(\"Base 8:     {:o}\",69420);"
      "  println!(\"Base 16:    {:x}\",69420);"
      "  println!(\"{number:>5}\",number=1);"
      "  println!(\"{number:0>5}\",number=1);"
      "  println!(\"{number:0<5}\",number=1);"
      "  println!(\"{number:0>width$}\",number=1,width=3);"
      "  println!(\"My name is {0}, {1} {0}\",\"Bond\",\"James\");"
      "}"))

(comment
  (l/rt:restart)
  (do (spit "hello.rs"
            (!.rs
              (defn main []
                (println! "Base 10:    {}"    69420)
                (println! "Base 2:     {:b}"  69420)
                (println! "Base 8:     {:o}"  69420)
                (println! "Base 16:    {:x}"  69420)

                (println! "{number:>5}"  :number 1)
                (println! "{number:0>5}"  :number 1)
                (println! "{number:0<5}"  :number 1)
                (println! "{number:0>width$}"
                          :number 1
                          :width  3)

                (println! "My name is {0}, {1} {0}", "Bond" "James"))))
      (h/sh {:args ["rustc" "hello.rs"]})
      (h/sh {:args ["hello"]})))

(comment

  (defenum WebEvent
    [[PageLoad]
     [PageUnload]
     [KeyPress [:char]]
     [Paste [:String]]
     [Click [[:i64 x]
             [:i64 y]]]])
  
  
  (defstruct Person
    [[:String name]
     [:u8 age]])
  
  (defstruct Unit)

  (defstruct Pair [:i32 :f32])

  (defstruct Point
    [[:f32 x]
     [:f32 y]])
  
  (defstruct Rectangle
    [[-/Point top-left]
     [-/Point bottom-right]])
  
  "
enum WebEvent {
    // An `enum` variant may either be `unit-like`,
    PageLoad,
    PageUnload,
    // like tuple structs,
    KeyPress(char),
    Paste(String),
    // or c-like structures.
    Click { x: i64, y: i64 },
}
"

  "
fn inspect(event: WebEvent) {
    match event {
        WebEvent::PageLoad => println!(\"page loaded\"),
        WebEvent::PageUnload => println!(\"page unloaded\"),
        // Destructure `c` from inside the `enum` variant.
        WebEvent::KeyPress(c) => println!(\"pressed '{}'.\", c),
        WebEvent::Paste(s) => println!(\"pasted \\\"{}\\\".\", s),
        // Destructure `Click` into `x` and `y`.
        WebEvent::Click { x, y } => {
            println!(\"clicked at x={}, y={}.\", x, y);
        },
    }
}
"
  
  (do (spit "hello.rs"
            "
#[derive(Debug)]
struct Person<'a> {
    name: &'a str,
    age: u8
}

fn main() {
    let name = \"Peter\";
    let age = 27;
    let peter = Person { name, age };

    // Pretty print
    println!(\"{:#?}\", peter);
}")
      (h/sh {:args ["rustc" "hello.rs"]})
      (h/sh {:args ["hello"]})))

  
  
  


(comment

  (!.rs
    (^{:- [:i32]} fn [:i32 i]
     (+ i 10)))
  
  (!.rs
    (mut (:f64 n) 1.0))
  
  (!.rs
    (var (:f64 n)))
  
  (!.rs
    (var (:f64  number) 1.0)
    (var (:bool number) 1.0)
    (var :mut number 1.0))
  
  (!.rs
    #_#_
    (var (:f64  number) 1.0)
    (var (:bool number) 1.0)
    (var :mut number 1.0))
  
  (!.rs
    (defn main []
      (println! "Base 10:    {}"    69420)
      (println! "Base 2:     {:b}"  69420)
      (println! "Base 8:     {:o}"  69420)
      (println! "Base 16:    {:x}"  69420)))
  
  
  
  )


^{:refer std.lang.model.spec-rust/rst-typesystem :added "4.0"}
(fact "TODO")

^{:refer std.lang.model.spec-rust/rst-vector :added "4.0"}
(fact "TODO")

^{:refer std.lang.model.spec-rust/rst-attributes :added "4.0"}
(fact "TODO")

^{:refer std.lang.model.spec-rust/rst-defenum :added "4.0"}
(fact "TODO")

^{:refer std.lang.model.spec-rust/rst-deftrait :added "4.0"}
(fact "TODO")

^{:refer std.lang.model.spec-rust/rst-defimpl :added "4.0"}
(fact "TODO")

^{:refer std.lang.model.spec-rust/rst-new :added "4.0"}
(fact "TODO")

^{:refer std.lang.model.spec-rust/rst-exec :added "4.0"}
(fact "TODO")

^{:refer std.lang.model.spec-rust/rst-defstruct :added "4.0"}
(fact "TODO")