(ns sky-observer.sightings
  (:require [sky-observer.db :as db]))

(defn select-all []
  (db/select-all-from "sightings"))

(defn insert [data]
  (db/insert :sightings, data))

(defn update [id new-value]
  (db/update :sightings, new-value, id))

(defn delete [id]
  (db/delete :sightings, id))