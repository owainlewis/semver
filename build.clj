(ns build
  (:require [clojure.tools.build.api :as b]
            [deps-deploy.deps-deploy :as deploy]))

(def lib 'com.owainlewis/semver)
(def version (or (System/getenv "VERSION") "0.2.0-SNAPSHOT"))
(def class-dir "target/classes")
(def jar-file (format "target/%s-%s.jar" (name lib) version))
(def basis (delay (b/create-basis {:project "deps.edn"})))

(defn clean
  "Remove build outputs."
  [_]
  (b/delete {:path "target"}))

(defn jar
  "Build a library jar."
  [_]
  (clean nil)
  (b/write-pom {:basis @basis
                :class-dir class-dir
                :lib lib
                :src-dirs ["src"]
                :version version
                :scm {:url "https://github.com/owainlewis/semver"
                      :tag (str "v" version)}})
  (b/copy-dir {:src-dirs ["src"]
               :target-dir class-dir})
  (b/jar {:class-dir class-dir
          :jar-file jar-file})
  {:jar-file jar-file})

(defn install
  "Build and install the jar to the local Maven repository."
  [_]
  (jar nil)
  (b/install {:basis @basis
              :class-dir class-dir
              :jar-file jar-file
              :lib lib
              :version version})
  {:jar-file jar-file})

(defn deploy
  "Build and deploy the jar to Clojars.

  Requires CLOJARS_USERNAME and CLOJARS_PASSWORD in the environment."
  [_]
  (jar nil)
  (deploy/deploy {:artifact jar-file
                  :installer :remote
                  :pom-file (b/pom-path {:class-dir class-dir
                                         :lib lib})})
  {:jar-file jar-file})
