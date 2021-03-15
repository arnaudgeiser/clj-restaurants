(ns clj-restaurants.cli
  (:require [clj-restaurants.service :as service]
            [clj-restaurants.utils :refer [now
                                           count-likes
                                           count-dislikes
                                           get-restaurant-id]]
            [clojure.string :refer [includes?]]
            [com.stuartsierra.component :as component])
  (:gen-class))

(declare service)

(defn choose
  "Choose a menu-option and convert it into a keyword"
  [] (-> (read-line) (keyword)))

(defn read-int
  "Read an int from stdin"
  [] (Integer/parseInt (read-line)))

(defn ask-input-for
  "Ask a value with a specific message"
  [msg]
  (println msg)
  (read-line))

(defn print-main-menu []
  (println "======================================================")
  (println "1. Afficher la liste de tous les restaurants")
  (println "2. Rechercher un restaurant par son nom")
  (println "3. Rechercher un restaurant par ville")
  (println "4. Rechercher un restaurant par son type de cuisine")
  (println "5. Saisir un nouveau restaurant")
  (println "0. Quitter l'application"))

(defn print-restaurant-menu []
  (println "======================================================")
  (println "Que souhaitez-vous faire ?")
  (println "1. J'aime ce restaurant")
  (println "2. Je n'aime pas ce restaurant")
  (println "3. Faire une évaluation complète de ce restaurant !")
  (println "4. Editer ce restaurant")
  (println "5. Editer l'adresse du restaurant")
  (println "6. Supprimer ce restaurant")
  (println "0. Revenir au menu principal"))

(defn display-restaurant [{:restaurants/keys [nom adresse ville]}]
  (let [{:villes/keys [code-postal nom-ville]} ville]
    (println (format "\"%s\" - %s - %s %s" nom adresse code-postal nom-ville))))

(defn display-restaurants [restaurants]
  (if (seq restaurants)
    (do
      (println "Liste des restaurants : ")
      (doseq [r restaurants]
        (display-restaurant r))
      (println "Veuillez saisir le nom exact du restaurant dont vous voulez voir le détail, ou appuyez sur Enter pour revenir en arrière"))
    (println "Aucun restaurant n'a été trouvé")))

(defn show-note [{:notes/keys [note critere-evaluation] :as n}]
  (let [{critere :criteres-evaluation/nom} critere-evaluation]
    (when critere-evaluation
      (println (str critere " : " (int note) "/5")))))

(defn show-commentaire [{:commentaires/keys [nom-utilisateur commentaire notes]}]
  (println "Evaluation de : " nom-utilisateur)
  (println "Commentaire " commentaire)
  (println "")
  (doseq [n notes] (show-note n))
  (println ""))

(defn show-restaurant [{:restaurants/keys [nom description adresse type-gastronomique site-web ville likes commentaires] :as restaurant}]
  (let [{:villes/keys [code-postal nom-ville]} ville
        {type :types-gastronomiques/libelle} type-gastronomique]
    (println nom)
    (println description)
    (println type)
    (println site-web)
    (println adresse)
    (println code-postal " " nom-ville)
    (println "Nombre de likes : " (count-likes likes))
    (println "Nombre de dislikes " (count-dislikes likes))
    (println "Evaluations reçues : ")
    (println "")
    (doseq [c commentaires] (show-commentaire c))))

(defn add-city []
  (let [code-postal (ask-input-for "Veuillez entrer le NPA de la nouvelle ville : ")
        nom-ville (ask-input-for "Veuillez entrer le nom de la nouvelle ville : ")]
    (service/insert-ville! service
                           {:code-postal code-postal
                            :nom-ville nom-ville})))

(defn pick-city []
  (let [cities (service/find-cities service)]
    (println "Voici la liste des villes possibles, veuillez entrer le NPA de la ville désirée")
    (doseq [{:villes/keys [code-postal nom-ville]} cities]
      (println code-postal " " nom-ville))
    (println "Entrez \"NEW\" pour créer une nouvelle ville")
    (let [choice (read-line)]
      (if (= "NEW" choice)
        (add-city)
        (if-let [city (first (filter #(includes? (:villes/code-postal %) choice) cities))]
          city
          (recur))))))

(defmulti menu-restaurant-option (fn [option _] option))

(defmethod menu-restaurant-option :default [& _])

(defmethod menu-restaurant-option :1 [_ {fk-rest :restaurants/numero}]
  (service/insert-like! service fk-rest))

(defmethod menu-restaurant-option :2 [_ {fk-rest :restaurants/numero}]
  (service/insert-dislike! service fk-rest))

(defmethod menu-restaurant-option :3 [_ {fk-rest :restaurants/numero}]
  (println "Merci d'évaluer ce restaurant !")
  (let [username (ask-input-for "Quel est votre nom d'utilisateur ?")
        comment (ask-input-for "Quel commentaire aimeriez-vous publier ?")]
    (println "Veuillez s'il vous plait donner une notre entre 1 et 5 pour chacun des critères :")
    (let [criteria (service/find-evaluations-criteria service)
          notes (mapv (fn [{:criteres-evaluation/keys [numero nom description]}]
                        (println (str nom " : " description))
                        (let [note (read-int)]
                          {:note note :fk-crit numero}))
                      criteria)
          evaluation {:date-eval (now)
                      :commentaire comment
                      :nom-utilisateur username
                      :fk-rest fk-rest}]
      (service/insert-complete-evaluation! service {:evaluation evaluation :notes notes}))))

(defn display-types [types]
  (->> types
       (map (fn [{:types-gastronomiques/keys [libelle description]}] (str "\"" libelle "\" : " description)))
       (clojure.string/join "\n")
       (println)))

(defn pick-restaurant-type [types]
  (println "Voici la liste des types possibles, veuillez entrer le libellé exact du type désiré :")
  (display-types types)
  (let [choice (read-line)]
    (->> types
         (filter #(clojure.string/includes? (:types-gastronomiques/libelle %) choice))
         first)))

(defmethod menu-restaurant-option :4 [_ restaurant]
  (let [new-name (ask-input-for "Nouveau nom :")
        new-description (ask-input-for "Nouvelle description: ")
        new-website (ask-input-for "Nouveau site web:")
        new-type-gastronomique (pick-restaurant-type (service/find-types-gastronomiques service))]
    (service/update-restaurant! service
                                (merge restaurant {:restaurants/name new-name
                                                   :restaurants/description new-description
                                                   :restaurants/ste_web new-website
                                                   :restaurants/type-gastronomique new-type-gastronomique}))
    (println "Merci, le restaurant a bien été supprimé")))


(defmethod menu-restaurant-option :5 [_ restaurant]
  (println "Edition de l'adresse du restaurant")
  (let [new-address (ask-input-for "Nouvelle rue: ")
        city (pick-city)]
    (service/update-restaurant-address! service
                                        {:numero (:restaurants/numero restaurant)
                                         :adresse new-address
                                         :fk_vill (:villes/numero city)})))

(defmethod menu-restaurant-option :6 [_ {:restaurants/keys [numero]}]
  (println "Etes-vous sûr de vouloir supprimer ce restaurant ? (O/n)")
  (let [choice (read-line)]
    (when (.equalsIgnoreCase choice "o")
      (service/delete-restaurant! service numero))))

(defn pick-restaurant [restaurants]
  (display-restaurants restaurants)
  (when (seq restaurants)
    (let [rest-name (read-line)]
      (loop []
        (let [restaurant (service/find-restaurant service (get-restaurant-id rest-name restaurants))]
          (when restaurant
            (show-restaurant restaurant)
            (print-restaurant-menu)
            (let [choice (choose)]
              (when-not (= :0 choice)
                (menu-restaurant-option choice restaurant)
                (when-not (= :6 choice)
                  (recur))))))))))

(defmulti menu-option identity)

(defmethod menu-option :default [_])

(defmethod menu-option :1 [_]
  (let [restaurants (service/find-all-restaurants service)]
    (pick-restaurant restaurants)))

(defmethod menu-option :2 [_]
  (println "Veuillez entrer une partie du nom recherché : ")
  (let [name (read-line)
        restaurants (service/find-restaurants-by-name service name)]
    (pick-restaurant restaurants)))

(defmethod menu-option :3 [_]
  (println "Veuillez entrer une partie du nom de la ville désirée : ")
  (let [name (read-line)
        restaurants (service/find-restaurants-by-nom-ville service name)]
    (pick-restaurant restaurants)))

(defmethod menu-option :4 [_]
  (println "Veuillez entrer une partie du type de restaurant recherché : ")
  (let [type (read-line)
        restaurants (service/find-restaurants-by-type-gastronomique service type)]
    (pick-restaurant restaurants)))

(defmethod menu-option :5 [_]
  (println "Vous allez ajouter un nouveau restaurant !")
  (let [name (ask-input-for "Quel est son nom ?")
        description (ask-input-for "Veuillez entrer une courte description : ")
        site-web (ask-input-for "Veuillez entrer l'adresse de son site internet : ")
        adresse (ask-input-for "Rue :")
        city (pick-city)
        type-gastronomique (pick-restaurant-type (service/find-types-gastronomiques service))]
    (service/insert-restaurant! service
                                {:nom name
                                 :description description
                                 :site_web site-web
                                 :adresse adresse
                                 :fk_vill (:villes/numero city)
                                 :fk_type (:types-gastronomiques/numero type-gastronomique)})))

(defmethod menu-option :default [_])

(defrecord CLI [service]
  component/Lifecycle
  (start [this]
    (def service service)
    (loop [choice nil]
      (when-not (= :0 choice)
        (print-main-menu)
        (let [choice (choose)]
          (menu-option choice)
          (recur choice))))))
