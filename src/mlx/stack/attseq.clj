(ns mlx.stack.attseq
  (:import
   [java.io InputStream]
   [javax.xml.stream XMLInputFactory XMLStreamReader]))

(set! *warn-on-reflection* true)

(defn- attribute-seq* [^XMLStreamReader stream pred attribute-parser]
  (lazy-seq
   (when (and (.hasNext stream)
	      (.next stream))
     (if (and (.isStartElement stream)
	      (pred stream))
       (cons (attribute-parser stream)
	     (attribute-seq* stream pred attribute-parser))
       (attribute-seq* stream pred attribute-parser)))))

(defn- xml-stream-reader [^InputStream input-stream]
  (.createXMLStreamReader ^XMLInputFactory (XMLInputFactory/newInstance)
			  input-stream))

(defn attribute-seq [input-stream pred attribute-parser]
  (attribute-seq* (xml-stream-reader input-stream)
		  pred
		  attribute-parser))