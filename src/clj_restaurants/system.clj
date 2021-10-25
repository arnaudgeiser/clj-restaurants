(ns clj-restaurants.system
  (:require  [com.stuartsierra.component :refer [system-map using]]

             [clj-restaurants.datasource :as datasource]
             [clj-restaurants.migrations :as migrations]
             [clj-restaurants.server     :as server]
             [clj-restaurants.service    :as service]
             [clj-restaurants.cli        :as cli]))

(defn build-system [{{:keys[creds migrations]} :db}]
  (system-map
   :datasource (datasource/map->Datasource {:config creds})
   :migrations (using (migrations/map->Migrations {:config migrations}) [:datasource])
   :service (using (service/map->RestaurantService {}) [:datasource :migrations])))

(defn build-cli-system [config]
  (merge (build-system config)
         {:cli (using (cli/map->CLI {}) [:service])}))

(defn build-server [{:keys [server] :as config}]
  (merge (build-system config)
         {:server (using (server/map->Server {:config server}) [:service])}))
