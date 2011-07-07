(ns mlx.risk.graph
  (:use [clojure.set :only (difference union)]))

(defn shell [node n graph]
  (when (graph node)
    (case n
      0 #{node}
      1 (graph node)
      (let [shell-1 (shell node (- n 1) graph)
	    shell-2 (shell node (- n 2) graph)]
	(difference (reduce union (map graph shell-1))
		    shell-1
		    shell-2)))))

(alter-var-root #'shell memoize)

(defn kernel [node n graph]
  (->> #{node}
       (iterate #(reduce union (map graph %)))
       (take n)
       (reduce union)))

(defn shell [node n graph]
  (if (zero? n)
    #{node}
    (let [kernel-nodes (kernel node n graph)]
      (difference (reduce union (map graph kernel-nodes))
		  kernel-nodes))))
  
(defn graph
  {:a #{:b :c :d :f :i}
   :b #{:a :d}
   :c #{:a :e}
   :d #{:a :b :e}
   :e #{:d :c}
   :f #{:a :g}
   :g #{:f :h}
   :h #{:g}
   :i #{:a :j}
   :j #{:i}})


(def territory-graph
     {;; North America
      :alaska #{:northwest-territory
		:alberta
		:kamchatka}

      :alberta #{:alaska
		 :northwest-territory
		 :ontario
		 :western-united-states}

      :central-america #{:western-united-states
			 :eastern-united-states
			 :venezuela}

      :eastern-united-states #{:central-america
			       :western-united-states
			       :ontario
			       :quebec}

      :greenland #{:northwest-territory
		   :ontario
		   :quebec
		   :iceland}

      :northwest-territory #{:alaska
			     :alberta
			     :greenland
			     :ontario}

      :ontario #{:alberta
		 :eastern-united-states
		 :greenland
		 :northwest-territory
		 :quebec
		 :western-united-states}

      :quebec #{:eastern-united-states
		:greenland
		:ontario}

      :western-united-states #{:alberta
			       :central-america
			       :eastern-united-states
			       :ontario}

      ;; South America
      :argentina #{:brazil
		   :peru}

      :brazil #{:north-africa
		:argentina
		:peru
		:venezuela}

      :peru #{:brazil
	      :venezuela
	      :argentina}

      :venezuela #{:peru
		   :brazil
		   :central-america}

      ;; Europe
      :great-britain #{:iceland
		       :northern-europe
		       :scandinavia
		       :western-europe}

      :iceland #{:greenland
		 :great-britain
		 :scandinavia}

      :northern-europe #{:scandinavia
			 :ukraine
			 :southern-europe
			 :western-europe
			 :great-britain}

      :scandinavia #{:ukraine
		     :northern-europe
		     :great-britain
		     :iceland}

      :southern-europe #{:egypt
			 :western-europe
			 :northern-europe
			 :ukraine
			 :middle-east}

      :ukraine #{:ural
		 :afghanistan
		 :middle-east
		 :southern-europe
		 :northern-europe
		 :scandinavia}

      :western-europe #{:great-britain
			:north-africa
			:southern-europe
			:northern-europe}

      ;; Africa
      :congo #{:east-africa
	       :south-africa
	       :north-africa}

      :east-africa #{:egypt
		     :middle-east
		     :madagascar
		     :south-africa
		     :congo
		     :north-africa}

      :egypt #{:southern-europe
	       :middle-east
	       :east-africa
	       :north-africa}

      :madagascar #{:east-africa
		    :south-africa}

      :north-africa #{:western-europe
		      :egypt
		      :east-africa
		      :congo
		      :brazil}

      :south-africa #{:congo
		      :east-africa
		      :madagascar}

      ;; Asia
      :afghanistan #{:ukraine
		     :ural
		     :china
		     :india
		     :middle-east}

      :china #{:siam
	       :india
	       :afghanistan
	       :siberia
	       :ural
	       :mongolia}

      :india #{:china
	       :siam
	       :middle-east
	       :afghanistan}

      :irkutsk #{:yakutsk
		 :mongolia
		 :siberia
		 :kamchatka}

      :japan #{:kamchatka
	       :mongolia}

      :kamchatka #{:japan
		   :alaska
		   :irkutsk
		   :mongolia
		   :yakutsk}

      :middle-east #{:east-africa
		     :egypt
		     :ukraine
		     :afghanistan
		     :india
		     :southern-europe}

      :mongolia #{:japan
		  :china
		  :siberia
		  :irkutsk
		  :kamchatka}

      :siam #{:indonesia
	      :india
	      :china}

      :siberia #{:irkutsk
		 :yakutsk
		 :mongolia
		 :china
		 :ural}

      :ural #{:siberia
	      :china
	      :afghanistan
	      :ukraine}

      :yakutsk #{:kamchatka
		 :irkutsk
		 :siberia}

      ;; Oceania
      :eastern-australia #{:new-guinea
			   :western-australia}

      :indonesia #{:siam
		   :new-guinea
		   :western-australia}

      :new-guinea #{:eastern-australia
		    :indonesia
		    :western-australia}

      :western-australia #{:eastern-australia
			   :indonesia
			   :new-guinea}})