(ns clj-restaurants.system
  (:require [com.stuartsierra.component :as component]))

(defn system []
  (-> (component/system-map
       :cli (CLI->map {})
       :datasource (Datasource->map {})
       ())
      (component/system-using
       {:cli [:datasource]})))
