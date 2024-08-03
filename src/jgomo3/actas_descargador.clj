(ns jgomo3.actas-descargador
  (:require [clojure.data.csv :as csv]
            [clojure.java.io :as io]
            [clojure.string :as str]
            [clj-http.client :as http])
  (:import (java.net URL)
           (java.nio.file Paths)
           (org.apache.http ConnectionClosedException))
  (:gen-class))


(defn trim-char [s char-to-trim]
  (let [pattern (re-pattern (str "^" (java.util.regex.Pattern/quote (str char-to-trim)) "+|" 
                                 (java.util.regex.Pattern/quote (str char-to-trim)) "+$"))]
    (str/replace s pattern "")))

(defn join-paths [base subpath]
  (str (.resolve (Paths/get base (make-array String 0)) subpath)))

(defn csv-data->maps [csv-data]
  (map zipmap
       (->> (first csv-data) ;; First row is the header
            (map keyword)    ;; Drop if you want string keys instead
            repeat)
       (rest csv-data)))

(defn url-path [url]
  (let [url-obj (URL. url)]
    (.getPath url-obj)))

(defn download-file [url output-path]
  (try
    (let [response (http/get url {:as :byte-array})]
      (with-open [out (io/output-stream output-path)]
        (.write out (:body response))))
    (catch ConnectionClosedException e
      (println (str "Error while downloading: " url)))))

(def storage "storage")
(defn storage-path [path]
  (join-paths storage (trim-char path "/")))

(defn url->storage-path [url]
  (->> url
       url-path
       storage-path))

(defn list-files [dir]
  (let [directory (io/file dir)]
    (map #(str dir "/" (.getName %))
         (file-seq directory))))

(def results-file "resources/RESULTADOS_2024_CSV_V1.csv")

(def files-done (set (list-files storage)))

(def results (with-open [reader (io/reader results-file)]
               (doall
                (->> reader
                     csv/read-csv
                     csv-data->maps
                     #_(take 5)))))

(defn descargar [& args]
  (doseq [[url path] (->> results
                          (map :URL)
                          (map #(list % (url->storage-path %))))
          :when (not (files-done path))]
    (download-file url path)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (descargar))

(comment

  (count results)

  (->> results
       (map :URL)
       (filter #(= )))
  
  (descargar)

  (list-files "storage")
  
  (url->storage-path "https://static.resultadosconvzla.com/408106_387187_0096Acta0096.jpg")
  
  (storage-path "/hola/que/tal")
  
  (map #(list % (url->storage-path %)) (map :URL results))

  (download-file url "storage/acta.jpg")

)
  
(defn greet
    "Callable entry point to the application."
    [data]
    (println (str "Hello, " (or (:name data) "World") "!")))




