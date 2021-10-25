(ns clj-restaurants.server
  (:require [clojure.data.json :refer [write-str]]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET]]
            [ring.adapter.jetty :refer [run-jetty]]

            [clj-restaurants.coerce  :refer [->int]]
            [clj-restaurants.service :as service]
            [clj-restaurants.utils :refer [count-dislikes count-likes]]))

(defn make-handler [service]
  (routes
   (GET "/cities" [] (write-str (service/find-cities service)))
   (GET "/restaurants/:id" [id] (write-str
                                 (as-> (service/find-restaurant service (->int id)) r
                                   (assoc r :restaurants/dislikes (count-dislikes (:restaurants/likes r)))
                                   (assoc r :restaurants/likes (count-likes (:restaurants/likes r))))))
   (GET "/restaurants" [] (write-str (service/find-all-restaurants service)))))

(defrecord Server [config
                   service] ;; dependency

  component/Lifecycle
  (start [this]
    (assoc this :server (run-jetty (#'make-handler service)
                                   {:join? false
                                    :port (get config :port 3000)})))
  (stop [this]
    (let [{:keys [server]} this]
      (when server
        (.stop (:server this))))
    this))
