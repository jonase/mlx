(ns mlx.risk.gui
  (:import
   [javax.swing JFrame JPanel JLabel JSpinner SpinnerNumberModel JRadioButton ButtonGroup]
   [javax.swing.event ChangeListener]
   [java.awt FlowLayout BorderLayout]
   [java.awt.event ActionListener]
   [org.w3c.dom.events EventListener]
   [org.apache.batik.swing JSVGCanvas]
   [org.apache.batik.dom.svg SAXSVGDocumentFactory]
   [org.apache.batik.util XMLResourceDescriptor])
  (:use
   [mlx.risk.graph :only [kernel shell territory-graph]]
   [clojure.java.io :only [reader]]))

(defn svg-document [reader]
  (.createDocument (SAXSVGDocumentFactory.
		    (XMLResourceDescriptor/getXMLParserClassName))
		   nil
		   reader))

;; Layout
(def frame (JFrame. "RISK"))
(def main-panel (JPanel. (BorderLayout.)))
(def svg-canvas (JSVGCanvas.))
(def worldmap (svg-document (reader "resources/worldmap.svg")))

(.setDocumentState svg-canvas JSVGCanvas/ALWAYS_DYNAMIC)
(.setDocument svg-canvas worldmap)
(.add main-panel svg-canvas)

(def top-panel (JPanel. (FlowLayout.)))
(def nspinner (JSpinner. (SpinnerNumberModel. 3 0 10 1)))
(def shell-button (JRadioButton. "shell" true))
(def kernel-button (JRadioButton. "kernel"))
(def button-group (ButtonGroup.))

(-> nspinner .getEditor .getTextField (.setEditable false))

(.add button-group shell-button)
(.add button-group kernel-button)
(.add top-panel "center" (JLabel. "n:"))
(.add top-panel "center" nspinner)
(.add top-panel "center" shell-button)
(.add top-panel "center" kernel-button)
(.add main-panel top-panel BorderLayout/NORTH)

;; State
(def selected (atom nil))
(def highlighted (atom #{}))
(def fun (atom shell))
(def n (atom 3))


;; Click events
(doseq [territory (keys territory-graph)]
  (.addEventListener
   (.getElementById worldmap (name territory))
   "click"
   (reify EventListener
     (handleEvent [_ _]
       (let [prevsel @selected]
	 (if (= prevsel territory)
	   (do (reset! selected nil)
	       (reset! highlighted #{}))
	   (do (reset! selected territory)
	       (reset! highlighted (@fun territory @n territory-graph)))))))
   false))

;; Top panel events
(.addChangeListener
 nspinner
 (reify ChangeListener
   (stateChanged [_ _]
     (reset! n (.getValue nspinner)))))

(.addActionListener
 shell-button
 (reify ActionListener
   (actionPerformed [_ _]
     (reset! fun shell))))

(.addActionListener
 kernel-button
 (reify ActionListener
   (actionPerformed [_ _]
     (reset! fun kernel))))

;; Reacting to state changes
(declare add-class remove-class)
(defn select [old-terr new-terr]
  (-> svg-canvas
      .getUpdateManager
      .getUpdateRunnableQueue
      (.invokeLater (fn []
		      (when old-terr (remove-class old-terr  "selected"))
		      (when new-terr (add-class new-terr "selected"))))))

(defn highlight [old-terrs new-terrs]
  (-> svg-canvas
      .getUpdateManager
      .getUpdateRunnableQueue
      (.invokeLater (fn []
		      (doseq [terr old-terrs]
			(remove-class terr "highlight"))
		      (doseq [terr new-terrs]
			(add-class terr "highlight"))))))

(add-watch selected
	   :gui-watcher
	   (fn [_ _ old-sel new-sel]
	     (select old-sel new-sel)))

(add-watch highlighted
	   :gui-watcher
	   (fn [_ _ old-set new-set]
	     (highlight old-set new-set)))
(add-watch fun
	   :gui-watcher
	   (fn [_ _ _ new-fun]
	     (reset! highlighted (new-fun @selected @n territory-graph))))
(add-watch n
	   :gui-watcher
	   (fn [_ _ _ new-n]
	     (reset! highlighted (@fun @selected new-n territory-graph))))

;; DOM manipulation
(defn class-attr-set [element]
  (-> (.getAttribute element "class")
      (.split " ")
      set))
      
(defn class-attr-str [cset]
  (->> (interpose " " cset)
       (apply str)))
       
(defn add-class [territory class-name]
  (let [element (.getElementById worldmap (name territory))]
    (if (.hasAttribute element "class")
      (.setAttribute element "class" (class-attr-str (conj (class-attr-set element) class-name)))
      (.setAttribute element "class" class-name))))

(defn remove-class [territory class-name]
  (let [element (.getElementById worldmap (name territory))]
    (when (.hasAttribute element "class")
      (.setAttribute element "class" (class-attr-str (disj (class-attr-set element) class-name))))))

;; Show the gui
(defn start []
  (doto frame
    (-> .getContentPane (.add main-panel))
    .pack
    (.setVisible true)))
