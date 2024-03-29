(defproject clj-restaurants "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "EPL-2.0 OR GPL-2.0-or-later WITH Classpath-exception-2.0"
            :url "https://www.eclipse.org/legal/epl-2.0/"}
  :dependencies [[org.clojure/clojure "1.10.0"]
                 [compojure "1.6.2"]
                 [ring "1.9.4"]
                 [ragtime "0.8.0"]
                 [org.clojure/data.json "2.4.0"]
                 [exoscale/seql "0.1.26"]
                 [com.stuartsierra/component "1.0.0"]
                 [com.h2database/h2 "1.4.200"]
                 [com.zaxxer/HikariCP "4.0.2"]]
  :profiles {:dev {:dependencies [[reloaded.repl "0.2.4"]]}}
  :aot :all
  :main clj-restaurants.main
  :repl-options {:init-ns clj-restaurants.main})
