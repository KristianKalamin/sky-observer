(defproject sky-observer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [compojure "1.6.2"]
                 [ring "1.8.2"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [ring-cors "0.1.13"]
                 [http-kit "2.5.0"]
                 [cheshire "5.10.0"]
                 [org.orekit/orekit "10.2"]
                 [org.clojure/core.async "1.3.610"]
                 [com.novemberain/monger "3.5.0"]]

  :profiles {:dev {:dependencies [[midje "1.9.9"]
                                  [ring/ring-mock "0.4.0"]]
                   :plugins      [[lein-midje "3.2.2"]]}}


  :java-source-paths ["src/java"]
  :source-paths ["src/clojure"]
  :main sky-observer.core/-main
  :repl-options {:init-ns sky-observer.core})
