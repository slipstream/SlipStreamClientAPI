(ns ^{:no-doc true} sixsq.slipstream.client.impl.utils.json
  "Utilities for handling JSON data."
  (:require
    #?(:clj
    [clojure.data.json :as json])
    [sixsq.slipstream.client.impl.utils.error :as e]))

(defn kw->str
  "Converts a keyword to the equivalent string without the leading colon and
   **preserving** any namespace."
  [kw]
  (subs (str kw) 1))

(defn str->json [s]
  #?(:clj  (json/read-str s :key-fn keyword)
     :cljs (js->clj (js/JSON.parse s) :keywordize-keys true)))

(defn edn->json [json]
  #?(:clj  (json/write-str json)
     :cljs (js/JSON.stringify (clj->js json :keyword-fn kw->str))))

(defn json->edn [s]
  (cond
    (nil? s) {}
    (e/error? s) s
    :else (str->json s)))

(defn body-as-json
  "transducer that extracts the body of a response and parses
   the result as JSON"
  []
  (comp
    (map e/throw-if-error)
    (map :body)
    (map json->edn)))
