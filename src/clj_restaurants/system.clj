(ns clj-restaurants.system
  (:require [clj-restaurants.datasource :as datasource]
            [clj-restaurants.migrations :as migrations]
            [clj-restaurants.service :as service]
            [clj-restaurants.cli :as cli]
            [com.stuartsierra.component :refer [system-map using]]))

(def system
  (system-map
   :datasource (datasource/map->Datasource {})
   :migrations (using (migrations/map->Migrations {}) [:datasource])
   :service (using (service/map->RestaurantService {}) [:datasource :migrations])
   :cli (using (cli/map->CLI {}) [:service])))
