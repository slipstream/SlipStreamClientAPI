(ns com.sixsq.slipstream.clj-client.run
  (:require
    [com.sixsq.slipstream.clj-client.utils :as u]
    [com.sixsq.slipstream.clj-client.run-util :as ru]
    [superstring.core :as s]
    [clojure.tools.logging  :as log]
    [clj-http.client :as http]
    [clj-json.core :as json])
  (:use [slingshot.slingshot :only [try+]]))

; Add cookie store. Example:
; (with-connection-pool {:timeout 5 :threads 4 :insecure? false :default-per-route 10}
;   (get "http://aoeu.com/1")
;   (post "http://aoeu.com/2")
;   (get "http://aoeu.com/3")
;   (get "http://aoeu.com/999"))

;; Default set of request parameters.
(def ^:private  rtp-req-params (conj
                     ru/base-req-params
                     {:content-type "text/plain"
                      :accept "text/plain;charset=utf-8"
                      :query-params {:ignoreabort "true"}}))
(def ^:private scale-req-params (assoc rtp-req-params :accept "application/json"))

;;
;; Public library

(defn get-rtp
  "Get runtime paramter.
  "
  [node-name id param]
  (:body
    (http/get
      (ru/build-rtp-url node-name id param)
      rtp-req-params)))

(defn set-rtp
  "Set runtime parameter.
  "
  [node-name id param value]
  (http/put
    (ru/build-rtp-url node-name id param)
    (merge rtp-req-params {:body value})))

(defn get-state
  []
  (:body
    (http/get
      ru/run-state-url
      rtp-req-params)))

(defn get-abort
  []
  (try+
    (:body (http/get ru/run-abort-url rtp-req-params))
    (catch [:status 412] {} "")))

(defn get-node-multiplicity
  [node-name]
  (Integer/parseInt (get-rtp node-name nil "multiplicity")))

(defn get-node-ids
  "IDs of the node as list."
  [node-name]
  (->> (-> (get-rtp node-name nil "ids")
           (u/split  #",")
           (sort))
       (remove #(= (count %) 0))))

;; Predicates.
(defn aborted?
  []
  (not (empty? (get-abort))))

; TODO: process XML and extract "scalable" attribute of the root element.
(defn scalable?
  []
  ;(http/get
  ;  ru/run-url
  ;  scale-req-params)
  true)

(defn- in-final-states?
  ([]
   (in-final-states? (get-state)))
  ([state]
   (u/in? state ru/non-scalable-final-states)))

(defn can-scale?
  []
  (and
    (scalable?)
    (not (aborted?))
    (u/in? (get-state) ru/scalable-states)))

;; Actions on the run.
(defn cancel-abort
  "Cancel abort of the run.
  "
  []
  (http/delete
    ru/run-abort-url
    rtp-req-params))

(defn terminate
  "Synchronous termination.
  "
  []
  (http/delete
    ru/run-url
    ru/base-req-params))

(defn scale-up
  "Scale up node-name by n. Allow to set rtps RTPs on the new node instances.
  "
  ([node-name n]
    (http/post
      (ru/build-node-url node-name)
      (merge scale-req-params {:body (str "n=" n)})))
  ([node-name n rtps]
    (let [added-node-instances (scale-up node-name n)]
      (doseq [[k v] rtps]
        (doseq [id (ru/extract-ids added-node-instances)]
          (set-rtp node-name id k v)))
      added-node-instances)))

(defn scale-down
  "Scale down node-name by terminating node instances defined by ids vector.
  "
  [node-name ids]
  (http/delete
    (ru/build-node-url node-name)
    (merge scale-req-params {:body (str "ids=" (s/join "," ids))})))

(defn- wait-state
  [state & [{:keys [timeout-s interval-s] :or {timeout-s 60 interval-s 5}}]]
  (log/debug "Waiting for" state "state for" timeout-s "sec.")
  (u/wait-for #(= state (get-state)) timeout-s interval-s))

(defn wait-ready
  "Waits for Ready state on the run. Returns true on success."
  [& [{:keys [timeout-s] :or {timeout-s 600}}]]
  (wait-state "Ready" :timeout-s timeout-s :interval-s 5))

;; Composite actions.
(def action-success "success")
(def action-failure "failure")

(defn- reason-from-error
  [error]
  (let [e (:error error)]
    (format "%s. %s. %s" (:code e) (:reason e) (:detail e))))
(defn- reason-from-exc
  [ex]
  (println ex)
  (-> ex
      :body
      json/parse-string
      u/keywordize-keys
      reason-from-error))

(def action-result
  {:state action-success
   :reason nil
   :action nil
   :node-name nil
   :multiplicity nil})

(defn- run-scale-action
  [a node-name n [& rtps]]
  (cond
    (= a "up") (scale-up node-name n rtps)
    (= a "down") (scale-down node-name n)
    :else (throw (Exception. (str "Wrong scale action requested: " a)))))

(defn- action-scale
  "Call scale action by name."
  [a node-name n & [{:keys [rtps timeout-s] :or {rtps {} timeout-s 600}}]]
  (let [res (assoc action-result
              :action "scale-up"
              :node-name node-name)]
    (log/info (format "Scaling %s." a) node-name n rtps)
    (try+
      (let [ret (run-scale-action a node-name n rtps)
            _   (wait-ready timeout-s)]
        (log/info (format "Success. Finished scaling ." a) node-name n rtps)
        (assoc res
          :reason ret
          :multiplicity (get-node-multiplicity node-name)))
      (catch Object o
        (log/error (format "Failure. Finished scaling %s." a) node-name n rtps o)
        (assoc res
          :state action-failure
          :reason (reason-from-exc o))))))

(defn action-scale-up
  "Scale up node-name by n VMs and wait for the action completion.
  Optionally provide map of RTPs as rtps.
  "
  [node-name n & [{:keys [rtps timeout-s] :or {rtps {} timeout-s 600}}]]
  (action-scale "up" node-name n :rtps rtps :timeout-s timeout-s))

(defn action-scale-down-by
  "Scale down node-name by n VMs and wait for the action completion.
  "
  [node-name n & [{:keys [timeout-s] :or {timeout-s 600}}]]
  (action-scale "down" node-name n :timeout-s timeout-s))

