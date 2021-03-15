(ns clj-restaurants.migrations
  (:require [com.stuartsierra.component :as component]
            [ragtime.jdbc :as rag]
            [ragtime.repl :as repl]))

(defrecord Migrations [datasource]
  component/Lifecycle
  (start [this]
    (print "Starting migrations")
    (let [datastore (rag/sql-database datasource)
          migrations (rag/load-resources "migrations")]
      (repl/migrate {:datastore datastore
                     :migrations migrations}))
    true)
  (stop [this]))
