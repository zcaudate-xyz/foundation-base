(ns hara.runtime.basic.impl.process-glsl
  (:require [clojure.string]
            [hara.runtime.basic.type-common :as common]
            [hara.runtime.basic.type-oneshot :as oneshot]
            [hara.lang.impl :as impl]
            [hara.lang.pointer :as ptr]
            [hara.lang.runtime :as runtime]
            [hara.lang.book :as book]
            [hara.common.util :as ut]
            [std.fs :as fs]
            [std.lib.collection :as collection]
            [std.lib.foundation :as f]
            [std.lib.impl :as impl-std]
            [std.lib.os :as os]
            [std.protocol.component :as protocol.component]
            [std.protocol.context :as protocol.context]))

;;
;; PROGRAM OPTIONS
;;

(def +program-init+
  (common/put-program-options
   :glsl  {:default  {:oneshot :gcc-egl-frag
                      :verify  :glslang-vert}
           :env      {:glslang-vert  {:exec   "glslangValidator"
                                      :pipe   true
                                      :stderr true
                                      :flags  {:oneshot      false
                                               :verify       ["--stdin" "-S" "vert"]
                                               :interactive  false
                                               :json         false
                                               :ws-client    false}}
                      :glslang-frag  {:exec   "glslangValidator"
                                      :pipe   true
                                      :stderr true
                                      :flags  {:oneshot      false
                                               :verify       ["--stdin" "-S" "frag"]
                                               :interactive  false
                                               :json         false
                                               :ws-client    false}}
                      :glslang-geom  {:exec   "glslangValidator"
                                      :pipe   true
                                      :stderr true
                                      :flags  {:oneshot      false
                                               :verify       ["--stdin" "-S" "geom"]
                                               :interactive  false
                                               :json         false
                                               :ws-client    false}}
                      :glslang-comp  {:exec   "glslangValidator"
                                      :pipe   true
                                      :stderr true
                                      :flags  {:oneshot      false
                                               :verify       ["--stdin" "-S" "comp"]
                                               :interactive  false
                                               :json         false
                                               :ws-client    false}}
                      :gcc-egl-vert  {:exec   "gcc"
                                      :extension   "c"
                                      :stderr true
                                      :flags  {:oneshot     []
                                               :interactive false
                                               :json        false
                                               :ws-client   false}}
                      :gcc-egl-frag  {:exec   "gcc"
                                      :extension   "c"
                                      :stderr true
                                      :flags  {:oneshot     []
                                               :interactive false
                                               :json        false
                                               :ws-client   false}}}}))

;;
;; VERIFY (validation only)
;;

(defn transform-form-verify
  "prepends a #version directive to emitted GLSL source for validation"
  {:added "4.0"}
  [form opts]
  (let [body-forms (ptr/free-form-body form)
        body (cond (empty? body-forms)
                   '(do)

                   (= 1 (count body-forms))
                   (first body-forms)

                   :else
                   (cons 'do body-forms))]
    ['(:- "#version 460")
     body]))

;;
;; RUN (execute shader via EGL/GL harness)
;;

(def ^:private default-vs
  (str "#version 120\n"
       "attribute vec2 vPos;\n"
       "void main(){ gl_Position = vec4(vPos, 0.0, 1.0); }\n"))

(def ^:private default-fs
  (str "#version 120\n"
       "void main(){ gl_FragColor = vec4(1.0, 0.0, 0.0, 1.0); }\n"))

(defn- emit-glsl-string
  "emits the GLSL body to a plain string"
  {:added "4.0"}
  [form]
  (let [body-forms (ptr/free-form-body form)
        body (cond (empty? body-forms)
                   '(do)

                   (= 1 (count body-forms))
                   (first body-forms)

                   :else
                   (cons 'do body-forms))]
    (impl/emit-as :glsl ['(:- "#version 120")
                         body])))

(defn- c-string-literal
  "converts a multi-line string into a series of C string literals"
  {:added "4.0"}
  [s]
  (->> (clojure.string/split-lines s)
       (map #(-> %
                 (clojure.string/replace "\\" "\\\\")
                 (clojure.string/replace "\"" "\\\"")
                 (clojure.string/replace #"\t" "\\t")))
       (map #(str "\"" % "\\n\""))
       (clojure.string/join "\n")))

(defn- host-c-source
  "generates a C/EGL harness that runs the user shader"
  {:added "4.0"}
  [user-glsl stage]
  (let [user (c-string-literal user-glsl)
        vs (if (= stage :vert) user (c-string-literal default-vs))
        fs (if (= stage :frag) user (c-string-literal default-fs))]
    (str "#define GL_GLEXT_PROTOTYPES\n"
         "#include <stdio.h>\n"
         "#include <EGL/egl.h>\n"
         "#include <GL/gl.h>\n"
         "#include <GL/glext.h>\n\n"
         "static const char *user_vs =\n" vs ";\n\n"
         "static const char *user_fs =\n" fs ";\n\n"
         "static GLuint compile_shader(GLenum type, const char *src) {\n"
         "  GLuint s = glCreateShader(type);\n"
         "  glShaderSource(s, 1, &src, NULL);\n"
         "  glCompileShader(s);\n"
         "  GLint ok;\n"
         "  glGetShaderiv(s, GL_COMPILE_STATUS, &ok);\n"
         "  if (!ok) {\n"
         "    char log[1024];\n"
         "    glGetShaderInfoLog(s, 1024, NULL, log);\n"
         "    fprintf(stderr, \"shader error: %s\\n\", log);\n"
         "  }\n"
         "  return s;\n"
         "}\n\n"
         "int main() {\n"
         "  EGLDisplay dpy = eglGetDisplay(EGL_DEFAULT_DISPLAY);\n"
         "  eglInitialize(dpy, NULL, NULL);\n"
         "  EGLint attribs[] = {EGL_SURFACE_TYPE, EGL_PBUFFER_BIT, EGL_RENDERABLE_TYPE, EGL_OPENGL_BIT, EGL_NONE};\n"
         "  EGLConfig cfg; EGLint n;\n"
         "  eglChooseConfig(dpy, attribs, &cfg, 1, &n);\n"
         "  EGLint pbuf_attribs[] = {EGL_WIDTH, 64, EGL_HEIGHT, 64, EGL_NONE};\n"
         "  EGLSurface surf = eglCreatePbufferSurface(dpy, cfg, pbuf_attribs);\n"
         "  eglBindAPI(EGL_OPENGL_API);\n"
         "  EGLContext ctx = eglCreateContext(dpy, cfg, EGL_NO_CONTEXT,\n"
         "    (EGLint[]){EGL_CONTEXT_MAJOR_VERSION, 2, EGL_CONTEXT_MINOR_VERSION, 1, EGL_NONE});\n"
         "  eglMakeCurrent(dpy, surf, surf, ctx);\n\n"
         "  GLuint prog = glCreateProgram();\n"
         "  glAttachShader(prog, compile_shader(GL_VERTEX_SHADER, user_vs));\n"
         "  glAttachShader(prog, compile_shader(GL_FRAGMENT_SHADER, user_fs));\n"
         "  glBindAttribLocation(prog, 0, \"vPos\");\n"
         "  glLinkProgram(prog);\n"
         "  GLint ok;\n"
         "  glGetProgramiv(prog, GL_LINK_STATUS, &ok);\n"
         "  if (!ok) {\n"
         "    char log[1024];\n"
         "    glGetProgramInfoLog(prog, 1024, NULL, log);\n"
         "    fprintf(stderr, \"link error: %s\\n\", log);\n"
         "    return 1;\n"
         "  }\n\n"
         "  GLfloat verts[] = {-1.0f, -1.0f, 3.0f, -1.0f, -1.0f, 3.0f};\n"
         "  glEnableVertexAttribArray(0);\n"
         "  glVertexAttribPointer(0, 2, GL_FLOAT, GL_FALSE, 0, verts);\n"
         "  glUseProgram(prog);\n"
         "  glViewport(0, 0, 64, 64);\n"
         "  glDrawArrays(GL_TRIANGLES, 0, 3);\n\n"
         "  GLfloat px[4];\n"
         "  glReadPixels(32, 32, 1, 1, GL_RGBA, GL_FLOAT, px);\n"
         "  printf(\"pixel: %.3f %.3f %.3f %.3f\\n\", px[0], px[1], px[2], px[3]);\n"
         "  return 0;\n"
         "}\n")))

(defn- resolve-glsl-form
  "resolves a bare symbol to the body of a defrun entry"
  {:added "4.0"}
  [form {:keys [book module] :as opts}]
  (if (and (symbol? form) book module)
    (let [module-id (ut/module-id module)
          id (symbol (name module-id) (name form))]
      (try (let [entry (book/get-code-entry book id)]
             (if (= :defrun (:op-key entry))
               (apply list 'do (drop 2 (:form entry)))
               form))
           (catch Throwable _
             form)))
    form))

(defn transform-form-run
  "generates a C/EGL harness for the emitted GLSL shader"
  {:added "4.0"}
  [form opts]
  (let [stage (or (-> opts :emit :runtime :process :stage)
                  :frag)
        user-glsl (emit-glsl-string (resolve-glsl-form form opts))]
    `(:- ~(host-c-source user-glsl stage))))

(defn glsl-sh-exec
  "compile and run helper that puts linker flags after the source file"
  {:added "4.0"}
  [input-args input-body {:keys [trim stderr raw root extension libs]
                          :as opts
                          :or {trim clojure.string/trim-newline}}]
  (let [tmp-exec (java.io.File/createTempFile "tmp" "")
        tmp-file (str tmp-exec
                      "."
                      (or extension
                          (f/error "Requires File Extension"
                                   opts)))
        root-dir   (str (or root (fs/parent tmp-file)))
        compile-args (vec (concat input-args
                                  ["-o" (str tmp-exec) (str tmp-file)]
                                  libs))
        run-args   [(str tmp-exec)]
        run!       (fn [args]
                     (let [proc (os/sh {:wait false
                                        :output false
                                        :args args
                                        :root root-dir})]
                       (os/sh-wait proc)
                       (os/sh-output proc)))
        raw-output (fn [{:keys [exit out err]}]
                     (let [out-lines (->> (clojure.string/split-lines (trim out))
                                          (remove empty?)
                                          seq)
                           err-lines (->> (clojure.string/split-lines (trim err))
                                          (remove empty?)
                                          seq)]
                       [exit (or out-lines err-lines [])]))
        stderr-output (fn [{:keys [out err]}]
                        (trim (or (not-empty err)
                                  out
                                  "")))]
    (try
      (spit tmp-file input-body)
      (let [compile-ret (run! compile-args)]
        (cond raw
              (if (zero? (:exit compile-ret))
                (raw-output (run! run-args))
                (raw-output compile-ret))

              (not (zero? (:exit compile-ret)))
              (if stderr
                (stderr-output compile-ret)
                (f/error "GLSL run compile failed"
                         {:args compile-args
                          :root root-dir
                          :file tmp-file
                          :exec (str tmp-exec)
                          :result compile-ret}))

              :else
              (let [run-ret (run! run-args)]
                (if (zero? (:exit run-ret))
                  (trim (:out run-ret))
                  (if stderr
                    (stderr-output run-ret)
                    (f/error "GLSL run execution failed"
                             {:args run-args
                              :root root-dir
                              :file tmp-file
                              :exec (str tmp-exec)
                              :result run-ret}))))))
      (catch Throwable t
        (if stderr
          (trim (.getMessage t))
          (throw t)))
      (finally
        (doseq [path [tmp-file (str tmp-exec)]]
          (try (fs/delete path)
               (catch Throwable _)))))))

(def +glsl-run-config+
  (common/set-context-options
   [:glsl :run :default]
   {:main    {}
    :emit    {:body {:transform #'transform-form-run}}
    :json    false
    :exec-fn #'glsl-sh-exec
    :libs    ["-lEGL" "-lGL"]}))

(defn- rt-glsl-string [{:keys [lang runtime program]}]
  (str "#rt.glsl" [lang runtime program]))

(defn raw-eval-glsl
  "compiles and runs the generated C/EGL harness"
  {:added "4.0"}
  [{:keys [exec process] :as rt} body]
  (glsl-sh-exec exec body process))

(defn invoke-ptr-glsl
  "invokes a glsl pointer through the run harness"
  {:added "4.0"}
  [{:keys [process lang layout] :as rt :or {layout :full}} ptr args]
  (runtime/default-invoke-script (assoc rt :layout layout)
                                 ptr args
                                 raw-eval-glsl
                                 process))

(impl-std/defimpl RuntimeGlsl [id]
  :string rt-glsl-string
  :protocols [protocol.context/IContext
              :prefix "runtime/default-"
              :method {-raw-eval    raw-eval-glsl
                       -invoke-ptr  invoke-ptr-glsl}
              protocol.component/IComponent
              :body {-start  component
                     -stop   component
                     -kill   component}])

(defn rt-glsl:create
  "creates a glsl run runtime"
  {:added "4.0"}
  [{:keys [id lang runtime exec program process] :as m
    :or {runtime :oneshot}}]
  (let [program (common/get-program-default lang runtime program)
        process (collection/merge-nested (common/get-options lang runtime program)
                                         process)
        exec    (or exec
                    (common/get-program-exec lang runtime program))]
    (map->RuntimeGlsl (assoc m
                             :id (or id (f/sid))
                             :runtime runtime
                             :program program
                             :exec exec
                             :process process))))

(def +glsl-oneshot-config+
  (common/set-context-options
   [:glsl :oneshot :default]
   {:main    {}
    :emit    {:body {:transform #'transform-form-run}}
    :json    false
    :exec-fn #'glsl-sh-exec
    :libs    ["-lEGL" "-lGL"]}))

(def +glsl-verify-config+
  (common/set-context-options
   [:glsl :verify :default]
   {:main  {}
    :emit  {:body  {:transform #'transform-form-verify}}
    :json  false}))

(def +glsl-oneshot+
  [(runtime/install-type!
    :glsl :oneshot
    {:type :hara/rt.glsl
     :instance {:create rt-glsl:create}})])

(def +glsl-verify+
  [(runtime/install-type!
    :glsl :verify
    {:type :hara/rt.oneshot
     :instance {:create oneshot/rt-oneshot:create}})])

(comment
  (./create-tests))
