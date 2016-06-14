(ns sixsq.slipstream.client.api.impl.cimi
  "Provides utilities that support the SCRUD actions for CIMI resources.
   Although these functions are public, they are not part of the public
   API and may change without notice."
  (:refer-clojure :exclude [read])
  (:require
    [sixsq.slipstream.client.api.utils.http :as http]
    [clojure.walk :as w]
    #?(:clj
    [clojure.data.json :as json])
    [superstring.core :as s]))

(def action-uris {:add    "http://sixsq.com/slipstream/1/Action/add"
                  :delete "http://sixsq.com/slipstream/1/Action/delete"
                  :edit   "http://sixsq.com/slipstream/1/Action/edit"})

(def action-keywords {"http://sixsq.com/slipstream/1/Action/add"    :add
                      "http://sixsq.com/slipstream/1/Action/delete" :delete
                      "http://sixsq.com/slipstream/1/Action/edit"   :edit})

(def std-opts {:type             :json
               :accept           :json
               :follow-redirects false})

(defn assoc-token [m token]
  (if token
    (assoc m :headers {:cookie token})
    m))

(defn assoc-body [m body]
  (if body
    (assoc m :body body)
    m))

(defn req-opts
  ([token]
   (req-opts token nil))
  ([token body]
   (-> std-opts
       (assoc-token token)
       (assoc-body body))))

(defn parse-json [s]
  (w/keywordize-keys
    #?(:clj  (json/read-str s)
       :cljs (JSON.parse s))))

(defn parse-raw-json [s]
  #?(:clj  (json/read-str s)
     :cljs (JSON.parse s)))

(defn kw->string [kw]
  (if (keyword? kw) (name kw) kw))

(defn edn->json [json]
  (let [json (w/postwalk kw->string json)]
    #?(:clj  (json/write-str json)
       :cljs (JSON.stringify json))))

(defn ensure-url [cep url-or-id]
  (if (re-matches #"^((http://)|(https://))" url-or-id)
    url-or-id
    (str (:baseURI cep) url-or-id)))

(defn get-collection-url [cep collection-name]
  (let [collection-name (keyword collection-name)
        baseURI (:baseURI cep)]
    (str baseURI (-> cep collection-name :href))))

(defn action-keyword [s]
  (or (get action-keywords s) (keyword s)))

(defn get-collection-operations [token cep collection-name]
  (let [baseURI (:baseURI cep)
        url (get-collection-url cep collection-name)
        req (req-opts token)]
    (into {}
          (->> (http/get url req)
               :body
               parse-json
               :operations
               (map (juxt :rel :href))
               (map (fn [[k v]] [(action-keyword k) (str baseURI v)]))))))

(defn get-resource-operations [token cep url-or-id]
  (let [baseURI (:baseURI cep)
        url (ensure-url cep url-or-id)
        req (req-opts token)]
    (into {}
          (->> (http/get url req)
               :body
               parse-json
               :operations
               (map (juxt :rel :href))
               (map (fn [[k v]] [(action-keyword k) (str baseURI v)]))))))
