(defproject sky-observer "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url  "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [org.clojure/java.jdbc "0.7.11"]
                 [mysql/mysql-connector-java "8.0.22"]
                 [compojure "1.6.2"]
                 [ring "1.8.2"]
                 [ring/ring-defaults "0.3.2"]
                 [ring/ring-json "0.5.0"]
                 [http-kit "2.5.0"]
                 [cheshire "5.10.0"]
                 [org.orekit/orekit "10.2"]
                 [org.clojure/core.async "1.3.610"]]

  :java-source-paths ["src/sky_observer/space"]
  :main sky-observer.core/-main
  :repl-options {:init-ns sky-observer.core})
