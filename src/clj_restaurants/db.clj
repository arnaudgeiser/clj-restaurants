(ns clj-restaurants.db
  (:require
   [clojure.java.jdbc :as jdbc]
   [seql.helpers :refer [make-schema entity field ident has-one has-many transform inline-condition]]
   [seql.core :refer [query]]))

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

(defn update-restaurant! [tx {:restaurants/keys [numero name description site_web type-gastronomique]}]
  (jdbc/update! tx
                :restaurants
                {:nom name
                 :description description
                 :site_web site_web
                 :fk_type (:types-gastronomiques/numero type-gastronomique)}
                ["numero = ?" numero]))

(defn find-cities [datasource]
  (query datasource
         :villes
         [:villes/numero
          :villes/code-postal
          :villes/nom-ville]))

(defn find-evaluations-criteria [datasource]
  (query datasource
         :criteres-evaluation
         [:criteres-evaluation/numero
          :criteres-evaluation/nom
          :criteres-evaluation/description]))

(defn find-types-gastronomiques [datasource]
  (query datasource
         :types-gastronomiques
         [:types-gastronomiques/numero
          :types-gastronomiques/libelle
          :types-gastronomiques/description]))

(defn find-restaurants
  ([datasource] (find-restaurants datasource []))
  ([datasource where] (query datasource
                             :restaurants
                             [:restaurants/numero
                              :restaurants/nom
                              :restaurants/adresse
                              {:restaurants/ville [:villes/code-postal :villes/nom-ville]}
                              {:restaurants/type-gastronomique [:types-gastronomiques/numero]}] where)))

(defn find-all-restaurants [datasource]
  (find-restaurants datasource))

(defn find-restaurants-by-name [datasource nom]
  (find-restaurants datasource [[:restaurants/nom-like nom]]))

(defn find-restaurants-by-nom-ville [datasource nom-ville]
  (find-restaurants datasource [[:restaurants/ville-like nom-ville]]))

(defn find-restaurants-by-type-gastronomique [datasource type]
  (find-restaurants datasource [[:restaurants/type-gastronomique-like type]]))

(defn find-restaurant [datasource id]
  (prn id)
  (query datasource
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
