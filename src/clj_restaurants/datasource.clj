(ns clj-restaurants.datasource
  (:require [com.stuartsierra.component :as component]
            [clj-restaurants.config :as config]
            [clj-restaurants.db :as db])
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
    (let [datasource (HikariDataSource. hikari-config)]
      (merge this {:datasource datasource
                   :schema db/schema
                   :jdbc datasource})))
  (stop [{:keys [datasource]}]
    (.close datasource)))
