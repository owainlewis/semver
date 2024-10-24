(defproject com.owainlewis/semver "0.2.0-SNAPSHOT"
  :description "Parser for semantic version strings"
  :url "https://github.com/owainlewis/semver"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-cljfmt "0.7.0"]
            [lein-cljsbuild "1.1.8"]]
  :profiles
  {:provided {:dependencies [[org.clojure/clojurescript "1.11.132"]
                             [org.clojure/clojure       "1.12.0"]]}}
  :hooks [leiningen.cljsbuild]
  :cljsbuild
  {:test-commands {"node" ["node" "target/test.js"]}
   :builds
   [{:id :main
     :source-paths ["src"]
     :compiler
     {:output-to "target/main.js"
      :optimizations :advanced}}

    {:id :test
     :source-paths ["src" "test"]
     :compiler
     {:output-to "target/test.js"
      :target :nodejs
      :optimizations :simple}}]})
