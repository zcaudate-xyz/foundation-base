(ns std.lang.model.spec-glsl
  (:require [std.lang.base.book :as book]
            [std.lang.base.emit :as emit]
            [std.lang.base.grammar :as grammar]
            [std.lang.base.script :as script]
            [std.lang.base.util :as ut]
            [std.lib.collection :as collection]
            [std.lib.template :as template]))

(def +types+
  [:bool :int :float :double :uint
   :vec2 :vec3 :vec4
   :dvec2 :dvec3 :dvec4
   :ivec2 :ivec3 :ivec4
   :uvec2 :uvec3 :uvec4
   :bvec2 :bvec3 :bvec4
   :mat2 :mat3 :mat4
   :mat2x3 :mat2x4
   :mat3x2 :mat3x4
   :mat4x2 :mat4x3
   :sampler1D :sampler2D :sampler3D :samplerCube
   :sampler1DShadow :sampler2DShadow :samplerCubeShadow
   :isampler2D :usampler2D
   :dmat2 :dmat3 :dmat4
   :dmat2x3 :dmat2x4
   :dmat3x2 :dmat3x4
   :dmat4x2 :dmat4x3])

(def +typeops+
  (collection/map-juxt [identity
               (fn [k]
                 {:op k    :symbol #{k}  :raw (name k) :emit :invoke})]
              +types+))

(def +features+
  (-> (grammar/build :exclude [:data-shortcuts
                               :control-try-catch
                               :class])
      (grammar/build:extend +typeops+)))

(def +template+
  (->> {:banned #{:set :map :regex}
        :highlight '#{return break}
        :default {:function  {:raw ""}}
        :data    {:vector    {:start "{" :end "}" :space ""}
                  :tuple     {:start "(" :end ")" :space ""}}
        :block  {:for       {:parameter {:sep ","}}}
        :define {:def       {:raw ""}}}
       (collection/merge-nested (emit/default-grammar))))

(def +grammar+
  (grammar/grammar :gl
    (grammar/to-reserved +features+)
    +template+))

(def +meta+
  (book/book-meta
   {:module-import    (fn [name _ opts]  
                        (template/$ (:- "#include" ~name)))}))

(def +book+
  (book/book {:lang :glsl
              :meta +meta+
              :grammar +grammar+}))

(def +init+
  (script/install +book+))













(comment
  {:defrun
        {:op :def  :symbol '#{def} :spec :def  :type :def  :section :code
         :emit gl-def 
         :arglists '([sym & body])}
        :def     {:op :def :raw ""}
        :vec2    {:op :vec2    :symbol '#{:vec2}  :raw "vec2" :emit :invoke}
        :vec3    {:op :vec3    :symbol '#{:vec3}  :raw "vec3" :emit :invoke}
        :addr    {:op :addr    :symbol '#{:&}  :raw "&" :emit :pre}
        :ptr     {:op :ptr     :symbol '#{:*}  :raw "*" :emit :pre}}
  )
