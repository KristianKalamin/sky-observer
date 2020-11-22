(ns sky-observer.db
  (:require [clojure.java.jdbc :as my-sql]))

(def db {:subprotocol "mysql"
         :subname     "//localhost:3306/sky_observer_db"
         :user        "root"
         :password    "1234"})

(defn insert [table, row-data]
  (my-sql/insert! db table row-data))

(defn select-all-from [table]
  (my-sql/query db [(str "SELECT * FROM " table)]))

(defn update [table, new-value, id]
  (my-sql/update! db table new-value ["id = ?" id]))

(defn delete [table, id]
  (my-sql/delete! db table ["id = ?" id]))