(ns clj-restaurants.core
  (:require [clj-restaurants.db :refer :all]
            [clojure.string :refer [includes?]]
            [ragtime.jdbc :as rag]
            [ragtime.repl :as repl]
            [clojure.java.jdbc :as jdbc])
  (:gen-class))

(defn now [] (java.time.LocalDate/now))

(defn ip-address []
  (->> (java.net.Inet4Address/getLocalHost)
       (.getHostAddress)))

(defn choose [] (-> (read-line) (keyword)))

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

(defn count-likes [likes]
  (->> likes
       (filter :likes/appreciation)
       count))

(defn count-dislikes [likes]
  (->> likes
       (filter (complement :likes/appreciation))
       count))

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

(defn get-restaurant-id [name restaurants]
  (->> restaurants
       (filter #(includes? {:restaurants/nom %} name))
       first
       :restaurants/numero))

(defn read-int [] (Integer/parseInt (read-line)))

(defn ask-input-for [msg]
  (println msg)
  (let [r (read-line)]
    (flush)
    r))

(defmulti menu-restaurant-option (fn [option _] option))

(defmethod menu-restaurant-option :default [& _])

(defmethod menu-restaurant-option :1 [_ {fk-rest :restaurants/numero}]
  (jdbc/with-db-transaction [tx db-spec]
    (jdbc/insert! tx :likes {:appreciation "T"
                             :date_eval (now)
                             :adresse_ip (ip-address)
                             :fk_rest fk-rest})))

(defmethod menu-restaurant-option :2 [_ {fk-rest :restaurants/numero}]
  (jdbc/with-db-transaction [tx db-spec]
    (jdbc/insert! tx :likes {:appreciation "F"
                             :date_eval (now)
                             :adresse_ip (ip-address)
                             :fk_rest fk-rest})))

(defmethod menu-restaurant-option :3 [_ {fk-rest :restaurants/numero}]
  (println "Merci d'évaluer ce restaurant !")
  (let [username (ask-input-for "Quel est votre nom d'utilisateur ?")
        comment (ask-input-for "Quel commentaire aimeriez-vous publier ?")]
    (println "Veuillez s'il vous plait donner une notre entre 1 et 5 pour chacun des critères :")
    (let [criteria (find-evaluations-criteria)
          notes (mapv (fn [{:criteres-evaluation/keys [numero nom description]}]
                        (println (str nom " : " description))
                        (let [note (read-int)]
                          {:note note :fk-crit numero}))
                      criteria)]
      (jdbc/with-db-transaction [tx db-spec]
        (let [{fk-comm :numero} (insert-commentaire! tx {:date-eval (now)
                                                         :commentaire comment
                                                         :nom-utilisateur username
                                                         :fk-rest fk-rest})]
          (doseq [{:keys [note fk-crit]} notes]
            (insert-note! tx {:note note
                              :fk-comm fk-comm
                              :fk-crit fk-crit})))))))

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
        new-type-gastronomique (pick-restaurant-type (find-types-gastronomiques))]
    (jdbc/with-db-connection [tx db-spec]
      (update-restaurant! tx
                          (merge restaurant {:restaurants/name new-name
                                             :restaurants/description new-description
                                             :restaurants/ste_web new-website
                                             :restaurants/type-gastronomique new-type-gastronomique})))
    (println "Merci, le restaurant a bien été supprimé")))

(defn add-city []
  (let [code-postal (ask-input-for "Veuillez entrer le NPA de la nouvelle ville : ")
        nom-ville (ask-input-for "Veuillez entrer le nom de la nouvelle ville : ")]
    (jdbc/with-db-connection [tx db-spec]
      (insert-ville! tx
                     {:code-postal code-postal
                      :nom-ville nom-ville}))))

(defn pick-city []
  (let [cities (find-cities)]
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

(defmethod menu-restaurant-option :5 [_ restaurant]
  (println "Edition de l'adresse du restaurant")
  (let [new-address (ask-input-for "Nouvelle rue: ")
        city (pick-city)]
    (jdbc/with-db-connection [tx db-spec]
      (jdbc/update! tx
                    :restaurants
                    {:adresse new-address
                     :fk_vill (:villes/numero city)}
                    ["numero=?" (:restaurants/numero restaurant)]))))

(defmethod menu-restaurant-option :6 [_ {:restaurants/keys [numero]}]
  (println "Etes-vous sûr de vouloir supprimer ce restaurant ? (O/n)")
  (let [choice (read-line)]
    (when (.equalsIgnoreCase choice "o")
      (jdbc/with-db-connection [tx db-spec]
        (jdbc/delete! tx :notes ["numero in (select(n.numero)
                                   from notes n
                                   join commentaires c
                                   on n.fk_comm=c.numero
                                   where c.fk_rest=?)" numero])
        (jdbc/delete! tx :commentaires ["fk_rest=?" numero])
        (jdbc/delete! tx :likes ["fk_rest=?" numero])
        (jdbc/delete! tx :restaurants ["numero=?" numero])))))

(defn pick-restaurants [restaurants]
  (display-restaurants restaurants)
  (if (seq restaurants)
    (let [rest-name (read-line)]
      (loop [choice nil]
        (let [restaurant (find-restaurant (get-restaurant-id rest-name restaurants))]
          (show-restaurant restaurant)
          (print-restaurant-menu)
          (let [choice (choose)]
            (when-not (= :0 choice)
              (menu-restaurant-option choice restaurant)
              (when-not (= :6 choice)
                (recur choice)))))))))

(defn pick-restaurant [restaurants]
  (display-restaurants restaurants)
  (if (seq restaurants)
    (let [rest-name (read-line)]
      (loop [choice nil]
        (let [restaurant (find-restaurant (get-restaurant-id rest-name restaurants))]
          (show-restaurant restaurant)
          (print-restaurant-menu)
          (let [choice (choose)]
            (when-not (= :0 choice)
              (menu-restaurant-option choice restaurant)
              (when-not (= :6 choice)
                (recur choice)))))))))

(defmulti menu-option identity)

(defmethod menu-option :default [_])

(defmethod menu-option :1 [_]
  (let [restaurants (find-all-restaurants)]
    (pick-restaurant restaurants)))

(defmethod menu-option :2 [_]
  (println "Veuillez entrer une partie du nom recherché : ")
  (let [name (read-line)
        restaurants (find-restaurants-by-name name)]
    (pick-restaurant restaurants)))

(defmethod menu-option :3 [_]
  (println "Veuillez entrer une partie du nom de la ville désirée : ")
  (let [name (read-line)
        restaurants (find-restaurants-by-nom-ville name)]
    (pick-restaurant restaurants)))

(defmethod menu-option :4 [_]
  (println "Veuillez entrer une partie du type de restaurant recherché : ")
  (let [type (read-line)
        restaurants (find-restaurants-by-type-gastronomique type)]
    (pick-restaurant restaurants)))

(defmethod menu-option :5 [_]
  (println "Vous allez ajouter un nouveau restaurant !")
  (let [name (ask-input-for "Quel est son nom ?")
        description (ask-input-for "Veuillez entrer une courte description : ")
        site-web (ask-input-for "Veuillez entrer l'adresse de son site internet : ")
        adresse (ask-input-for "Rue :")
        city (pick-city)
        type-gastronomique (pick-restaurant-type (find-types-gastronomiques))]
    (jdbc/with-db-connection [tx db-spec]
      (jdbc/insert! tx
                    :restaurants
                    {:nom name
                     :description description
                     :site_web site-web
                     :adresse adresse
                     :fk_vill (:villes/numero city)
                     :fk_type (:types-gastronomiques/numero type-gastronomique)}))))

(defmethod menu-option :default [_])

(defn -main
  [& args]

  (repl/migrate {:datastore (rag/sql-database db-spec)
                 :migrations (rag/load-resources "migrations")})
  (loop [choice nil]
    (when-not (= :0 choice)
      (print-main-menu)
      (let [choice (choose)]
        (menu-option choice)
        (recur choice)))))

(comment
  (-main))
