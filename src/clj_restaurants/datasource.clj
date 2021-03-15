(ns clj-restaurants.datasource
  (:require [com.stuartsierra.component :as component]
            [clj-restaurants.config :as config])
  (:import [com.zaxxer.hikari HikariConfig HikariDataSource]))

(def hikari-config
  (let [{:keys [jdbc-url user password]} config/config]
    (doto (HikariConfig.)
      (.setJdbcUrl jdbc-url)
      (.setUsername user)
      (.setPassword password))))

(defrecord Datasource []
  component/Lifecycle
  (start [this]
    assoc this :datasource (HikariDataSource. hikari-config))
  (stop [{:keys [datasource]}]
    (.close datasource)))

(def a (map->Datasource {}))
(.start a)
