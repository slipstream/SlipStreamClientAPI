(ns ^{:no-doc true} sixsq.slipstream.client.impl.utils.modules
  "Utilities specific to working with modules.")

(defn select-child-fields [child]
  (select-keys child #{:name :description :category :version}))

(defn process-children [children]
  (when children
    (let [children (if (map? children) [children] children)] ;; may be single item!
      (map select-child-fields children))))

(defn extract-children [module]
  (process-children (get-in module [:projectModule :children :item])))

(defn extract-root-children [root]
  (process-children (get-in root [:list :item])))

(defn fix-module-name [mname]
  (first (map second (re-seq #"module/(.+)/[\d+]+" mname))))

(defn extract-xml-children [xml]
  (->> (re-seq #"resourceUri=\"([^\"]*)\"" xml)
       (map second)
       (map fix-module-name)))
