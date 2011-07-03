(ns mlx.comptags
  (:use
   [mlx.attseq :only [attribute-seq]]
   [clojure.set :only [intersection select]]
   [clojure.pprint :only [print-table]]
   [clojure.java.io :only [input-stream]])
  (:import
   [java.util Calendar]
   [javax.xml.bind DatatypeConverter]
   [javax.xml.stream XMLStreamReader]))

(set! *warn-on-reflection* true)

(defn get-attr [name ^XMLStreamReader stream]
  (.getAttributeValue stream nil name))

(defn date-parser [stream]
  (DatatypeConverter/parseDate (get-attr "CreationDate" stream)))

(defn tags-parser [stream]
  (set (re-seq #"[\w#\\Q+-.\\E]+" (get-attr "Tags" stream))))

(defn parse-question [stream]
  {:creation-date (date-parser stream)
   :tags (tags-parser stream)})

(defn question? [stream]
  (= (get-attr "PostTypeId" stream) "1"))

(defn question-seq [input-stream]
  (attribute-seq input-stream question? parse-question))

;; Can this be written as a one-liner with HOFs?
;; Exercise: Rewrite with drop-while
(defn monotonic-inc-by [key coll]
  (lazy-seq
   (let [[x y & more] coll]
     (if y
       (if-not (pos? (compare (key x) (key y)))
	 (cons x (monotonic-inc-by key (cons y more))) ;; x <= y
	 (monotonic-inc-by key (cons x more))) ;; y > x, drop y (or drop x)
       [x]))))

(defn element-count
  ([sets]
     (element-count (constantly true) sets))
  ([pred sets]
     (reduce #(merge-with + %1 %2)
	     (map (fn [set] (into {} (map #(vector % 1) (select pred set))))
		  sets))))

(defn tagged [& tags]
  (fn [question]
    (seq (intersection (set tags)
		       (:tags question)))))

(defn make-report [tags questions]
  (let [tag-count (element-count (set tags) (map :tags questions))
	volume (reduce + (vals tag-count))]
    (into {"month" (format "%1$tb %1$tY" (:creation-date (first questions)))
	   "volume" volume}
	  (map (fn [tag] [tag (* (/ (get tag-count tag 0) volume) 100.0)])
	       tags))))

(defn comptags [input & tags]
  (->> (question-seq input) 
       (filter (apply tagged tags))
       (monotonic-inc-by :creation-date)
       (partition-by #(.get ^Calendar (:creation-date %) Calendar/MONTH))
       (map #(make-report tags %))))

(defn print-reports [reports]
  (doseq [report reports]
    (printf "%s  volume: %5d" (report "month") (report "volume"))
    (doseq [[tag percent] (sort (dissoc report "month" "volume"))]
      (printf "  %s: %5.1f%%" tag percent))
    (println)))
