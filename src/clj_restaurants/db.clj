(ns clj-restaurants.db
  (:require [clj-restaurants.config :as config]
            [clojure.java.jdbc :as jdbc]
            [seql.helpers :refer [make-schema entity field ident has-one has-many transform inline-condition]]
            [seql.core :refer [query]])
  (:import [com.zaxxer.hikari HikariConfig HikariDataSource]))

(def hikari-config
  (doto (HikariConfig.)
    (.setJdbcUrl (:jdbc-url config/config))))

(def datasource
  (HikariDataSource. hikari-config))

(def db-spec {:datasource datasource})

(defn like [s] (str "%" s "%"))

(def schema
  "Database schema for SEQL"
  (make-schema
   (entity :restaurants
           (field :numero (ident))
           (field :nom)
           (field :adresse)
           (field :description)
           (field :site-web)
           (has-one :type-gastronomique [:fk_type :types-gastronomiques/numero])
           (has-one :ville [:fk_vill :villes/numero])
           (has-many :likes [:numero :likes/fk_rest])
           (has-many :commentaires [:numero :commentaires/fk_rest])
           (inline-condition :nom-like [n] [:like :restaurants/nom (like n)])
           (inline-condition :ville-like [n] [:like :ville/nom-ville (like n)])
           (inline-condition :type-gastronomique-like [n] [:like :type-gastronomiques/libelle (like n)]))
   (entity :types-gastronomiques
           (field :numero (ident))
           (field :libelle)
           (field :description)
           (has-many :restaurants [:numero :restaurants/fk-type]))
   (entity :villes
           (field :numero (ident))
           (field :code-postal)
           (field :nom-ville)
           (has-many :restaurants [:numero :restaurants/fk-vill]))
   (entity :likes
           (field :numero (ident))
           (field :appreciation (transform #(= % "T") #(if % "T" "F")))
           (field :adresse-ip)
           (field :date_eval))
   (entity :commentaires
           (field :numero (ident))
           (field :date-eval)
           (field :commentaire)
           (field :nom-utilisateur)
           (has-many :notes [:numero :notes/fk_comm]))
   (entity :notes
           (field :numero (ident))
           (field :note)
           (has-one :critere-evaluation [:fk_crit :criteres-evaluation/numero]))
   (entity :criteres-evaluation
           (field :numero (ident))
           (field :nom)
           (field :description))))

(defn insert-commentaire! [tx {:keys [date-eval commentaire nom-utilisateur fk-rest]}]
  (->> (jdbc/insert! tx
                     :commentaires
                     {:date_eval date-eval
                      :commentaire commentaire
                      :nom_utilisateur nom-utilisateur
                      :fk_rest fk-rest})
       first))

(defn insert-note! [tx {:keys [note fk-comm fk-crit]}]
  (jdbc/insert! tx
                :notes
                {:note note
                 :fk_comm fk-comm
                 :fk_crit fk-crit}))

(defn insert-ville! [tx {:keys [code-postal nom-ville]}]
  (->> (jdbc/insert! tx
                     :villes
                     {:code_postal code-postal
                      :nom_ville nom-ville})
       first
       :numero
       (assoc {} :villes/numero)))

(defn insert-restaurant! [tx {:keys [nom adresse description nom-utilisateur fk-rest]}]
  (jdbc/insert! tx
                :restaurants
                {:nom nom
                 :adresse adresse
                 :description description
                 :nom_utilisateur nom-utilisateur
                 :fk_rest fk-rest}))

(defn update-restaurant! [tx {:restaurants/keys [numero name description site_web type-gastronomique]}]
  (jdbc/update! tx
                :restaurants
                {:nom name
                 :description description
                 :site_web site_web
                 :fk_type (:types-gastronomiques/numero type-gastronomique)}
                ["numero = ?" numero]))

(def env {:schema schema :jdbc datasource})

(defn find-cities []
  (query env
         :villes
         [:villes/numero
          :villes/code-postal
          :villes/nom-ville]))

(defn find-evaluations-criteria []
  (query env
         :criteres-evaluation
         [:criteres-evaluation/numero
          :criteres-evaluation/nom
          :criteres-evaluation/description]))

(defn find-types-gastronomiques []
  (query env
         :types-gastronomiques
         [:types-gastronomiques/numero
          :types-gastronomiques/libelle
          :types-gastronomiques/description]))

(defn find-restaurants
  ([] (find-restaurants []))
  ([where] (query env
                  :restaurants
                  [:restaurants/numero
                   :restaurants/nom
                   :restaurants/adresse
                   {:restaurants/ville [:villes/code-postal :villes/nom-ville]}
                   {:restaurants/type-gastronomique [:types-gastronomiques/numero]}] where)))

(defn find-all-restaurants []
  (find-restaurants))

(defn find-restaurants-by-name [nom]
  (find-restaurants [[:restaurants/nom-like nom]]))

(defn find-restaurants-by-nom-ville [nom-ville]
  (find-restaurants [[:restaurants/ville-like nom-ville]]))

(defn find-restaurants-by-type-gastronomique [type]
  (find-restaurants [[:restaurants/type-gastronomique-like type]]))

(defn find-restaurant [id]
  (query env
         [:restaurants/numero (int id)]
         [:restaurants/numero
          :restaurants/nom
          :restaurants/description
          :restaurants/adresse
          :restaurants/site-web
          {:restaurants/ville [:villes/code-postal :villes/nom-ville]}
          {:restaurants/type-gastronomique [:types-gastronomiques/libelle]}
          {:restaurants/likes [:likes/numero :likes/appreciation]}
          {:restaurants/commentaires [:commentaires/nom-utilisateur
                                      :commentaires/commentaire
                                      {:commentaires/notes [:notes/numero
                                                            :notes/note
                                                            {:notes/critere-evaluation [:criteres-evaluation/nom]}]}]}]))
