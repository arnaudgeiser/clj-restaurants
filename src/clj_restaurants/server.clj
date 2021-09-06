(ns clj-restaurants.server
  (:require [clojure.data.json :refer [write-str]]
            [com.stuartsierra.component :as component]
            [compojure.core :refer [routes GET]]
            [ring.adapter.jetty :refer [run-jetty]]
            [clj-restaurants.service :as service]))

(defn make-handler [service]
  (routes
   (GET "/" [] "<h1>What23ever!<h1>")
   (GET "/cities" [] (write-str (service/find-cities service)))
   (GET "/restaurants" [] (write-str (service/find-all-restaurants service)))))

(defrecord Server [service]
  component/Lifecycle
  (start [this]
    (prn "restart 26:")
    (assoc this :server (run-jetty (#'make-handler service)
                                   {:join? false
                                    :port 3000})))
  (stop [this]
    (let [{:keys [server]} this]
      (when server
        (.stop (:server this))))
    this))

(defn token []
  (let [chars-between #(map char (range (int %1) (inc (int %2))))
        chars (concat (chars-between \0 \9)
                      (chars-between \A \Z))
        password (take 8 (repeatedly #(rand-nth chars)))]
    (reduce str password)))

(repeately token)
