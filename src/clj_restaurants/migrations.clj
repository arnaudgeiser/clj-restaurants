(ns clj-restaurants.migrations
  (:require [com.stuartsierra.component :as component]
            [ragtime.jdbc :as rag]
            [ragtime.repl :as repl]))

(defrecord Migrations [config       ;; config
                       datasource]  ;; dependency
  component/Lifecycle
  (start [this]
    (print "Starting migrations")
    (prn config)
    (let [datastore (rag/sql-database datasource)
          migrations (rag/load-resources config)]
      (repl/migrate {:datastore datastore
                     :migrations migrations}))
    true)
  (stop [this]))
