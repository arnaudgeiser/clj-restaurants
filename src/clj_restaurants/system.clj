(ns clj-restaurants.system
  (:require [clj-restaurants.datasource :as datasource]
            [clj-restaurants.migrations :as migrations]
            [clj-restaurants.service :as service]
            [clj-restaurants.cli :as cli]
            [com.stuartsierra.component :refer [system-map using]]))

(defn build-system [{{:keys[creds migrations]} :db}]
  (system-map
   :datasource (datasource/map->Datasource {:config creds})
   :migrations (using (migrations/map->Migrations {:config migrations}) [:datasource])
   :service (using (service/map->RestaurantService {}) [:datasource :migrations])
   :cli (using (cli/map->CLI {}) [:service])))
