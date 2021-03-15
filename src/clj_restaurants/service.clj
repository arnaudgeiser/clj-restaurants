(ns clj-restaurants.service
  (:require [clj-restaurants.db :as db]
            [clojure.java.jdbc :as jdbc]
            [clj-restaurants.utils :refer [ip-address now]]))

(defprotocol IRestaurantService
  (find-restaurant [this id])
  (find-all-restaurants [this])
  (find-restaurants-by-name [this name])
  (find-restaurants-by-nom-ville [this name])
  (find-restaurants-by-type-gastronomique [this name])
  (find-cities [this])
  (find-evaluations-criteria [this])
  (find-types-gastronomiques [this])
  (insert-restaurant! [this restaurant])
  (insert-ville! [this ville])
  (insert-complete-evaluation! [this m])
  (insert-like! [this fk-rest])
  (insert-dislike! [this fk-rest])
  (update-restaurant! [this restaurant])
  (update-restaurant-address! [this restaurant])
  (delete-restaurant! [this! id]))

(defrecord RestaurantService [datasource]
  IRestaurantService
  (find-restaurant [_ id]
    (db/find-restaurant datasource id))
  (find-all-restaurants [_]
    (db/find-all-restaurants datasource))
  (find-restaurants-by-name [_ name]
    (db/find-restaurants-by-name datasource name))
  (find-restaurants-by-nom-ville [_ name]
    (db/find-restaurants-by-nom-ville datasource name))
  (find-restaurants-by-type-gastronomique [_ type]
    (db/find-restaurants-by-type-gastronomique datasource type))
  (find-cities [_]
    (db/find-cities datasource))
  (find-evaluations-criteria [_]
    (db/find-evaluations-criteria datasource))
  (find-types-gastronomiques [this]
    (db/find-types-gastronomiques datasource))
  (insert-restaurant! [this restaurant]
    (jdbc/insert! datasource :restaurants restaurant))
  (insert-ville! [this ville]
    (db/insert-ville! datasource ville))
  (insert-complete-evaluation! [_ {:keys [evaluation notes]}]
    (jdbc/with-db-transaction [tx datasource]
      (let [{fk-comm :numero} (db/insert-commentaire! tx evaluation)]
        (doseq [{:keys [note fk-crit]} notes]
          (db/insert-note! tx {:note note
                               :fk-comm fk-comm
                               :fk-crit fk-crit})))))
  (insert-like! [_ fk-rest]
    (jdbc/insert! datasource :likes {:appreciation "T"
                                     :date_eval (now)
                                     :adresse_ip (ip-address)
                                     :fk_rest fk-rest}))
  (insert-dislike! [_ fk-rest]
    (jdbc/insert! datasource :likes {:appreciation "F"
                                     :date_eval (now)
                                     :adresse_ip (ip-address)
                                     :fk_rest fk-rest}))
  (update-restaurant! [_ restaurant]
    (db/update-restaurant! datasource restaurant))
  (update-restaurant-address! [this restaurant-address]
    (jdbc/update! datasource
                  :restaurants
                  restaurant-address
                  ["numero=?" (:numero restaurant-address)]))
  (delete-restaurant! [_ id]
    (jdbc/with-db-connection [tx datasource]
      (jdbc/delete! tx :notes ["numero in (select(n.numero)
                                   from notes n
                                   join commentaires c
                                   on n.fk_comm=c.numero
                                   where c.fk_rest=?)" id])
      (jdbc/delete! tx :commentaires ["fk_rest=?" id])
      (jdbc/delete! tx :likes ["fk_rest=?" id])
      (jdbc/delete! tx :restaurants ["numero=?" id]))))
