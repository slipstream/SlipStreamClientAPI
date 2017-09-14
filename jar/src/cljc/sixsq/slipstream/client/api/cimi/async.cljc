(ns sixsq.slipstream.client.api.cimi.async
  "Defines a type that implements an asynchronous version of the CIMI
   protocol. All the defined functions return a channel containing the
   result(s). When an application/event-stream result is requested, the stream
   of events will be pushed to the returned channel, which can be closed by
   calling `close!` on the channel."
  (:refer-clojure :exclude [get])
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require
    [sixsq.slipstream.client.api.defaults :as defaults]
    [sixsq.slipstream.client.api.authn :as authn]
    [sixsq.slipstream.client.api.cimi.impl-async :as impl]
    [sixsq.slipstream.client.api.cimi.utils :as u]
    [sixsq.slipstream.client.api.cimi :as cimi]
    [sixsq.slipstream.client.api.pricing :as pricing]
    [sixsq.slipstream.client.api.cimi.impl-pricing-async :as pi]
    [clojure.core.async :refer #?(:clj  [chan >! <! go]
                                  :cljs [chan >! <!])]))

(deftype cimi-async [endpoint state]
  cimi/cimi

  (login
    [this login-params]
    (cimi/login this login-params nil))
  (login
    [this login-params options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[response token] (<! (impl/login @state login-params opts))]
          (u/update-state state :token token)
          response))))

  (logout
    [this]
    (cimi/logout this nil))
  (logout
    [this options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[response token] (<! (impl/logout @state opts))]
          (u/update-state state :token token)
          response))))

  (authenticated?
    [this]
    (cimi/authenticated? this nil))
  (authenticated?
    [this options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[session-id _] (<! (impl/current-session @state opts))]
          (not (nil? session-id))))))

  (cloud-entry-point
    [this]
    (cimi/cloud-entry-point this nil))
  (cloud-entry-point
    [_ options]
    (go
      (or (:cep @state)
          (let [opts (merge (:default-options @state) options)
                [cep token] (<! (impl/cloud-entry-point endpoint opts))]
            (u/update-state state :token token)
            (u/update-state state :cep cep)
            cep))))

  (add [this resource-type data]
    (cimi/add this resource-type data nil))
  (add [this resource-type data options]
    (go
      (<! (cimi/cloud-entry-point this options))
      (let [opts (merge (:default-options @state) options)
            [response token] (<! (impl/add @state resource-type data opts))]
        (u/update-state state :token token)
        response)))

  (edit [this url-or-id data]
    (cimi/edit this url-or-id data nil))
  (edit [this url-or-id data options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[response token] (<! (impl/edit @state url-or-id data opts))]
          (u/update-state state :token token)
          response))))

  (delete [this url-or-id]
    (cimi/delete this url-or-id nil))
  (delete [this url-or-id options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[response token] (<! (impl/delete @state url-or-id opts))]
          (u/update-state state :token token)
          response))))

  (get [this url-or-id]
    (cimi/get this url-or-id nil))
  (get [this url-or-id {:keys [sse?] :as options}]
    (let [opts (merge (:default-options @state) options)]
      (if sse?
        (impl/get-sse @state url-or-id opts)
        (go
          (<! (cimi/cloud-entry-point this opts))
          (let [[response token] (<! (impl/get @state url-or-id opts))]
            (u/update-state state :token token)
            response)))))

  (search [this resource-type]
    (cimi/search this resource-type nil))
  (search [this resource-type {:keys [sse?] :as options}]
    (let [opts (merge (:default-options @state) options)]
      (if sse?
        (impl/search-sse @state resource-type opts)
        (go
          (<! (cimi/cloud-entry-point this opts))
          (let [[response token] (<! (impl/search @state resource-type opts))]
            (u/update-state state :token token)
            response)))))

  (operation [this url-or-id operation]
    (cimi/operation this url-or-id operation nil nil))
  (operation [this url-or-id operation data]
    (cimi/operation this url-or-id operation data nil))
  (operation [this url-or-id operation data options]
    (let [opts (merge (:default-options @state) options)]
      (go
        (<! (cimi/cloud-entry-point this opts))
        (let [[response token] (<! (impl/operation @state url-or-id operation data opts))]
          (u/update-state state :token token)
          response))))

  pricing/pricing

  (place-and-rank [this module-uri connectors]
    (go
      (let [{:keys [baseURI]} (<! (cimi/cloud-entry-point this))
            endpoint (second (re-matches #"^(https?://[^/]+)/.*$" baseURI))]
        (<! (pi/place-and-rank @state endpoint module-uri connectors))))))

(defn instance
  "A convenience function for creating an instance that implements the CIMI
   protocol asynchronously. All of the CIMI functions for this client return a
   core.async channel. Use of this function is preferred to the raw
   constructor.

   If the endpoint is not provided or is nil, the default endpoint will be
   used.

   Optionally, you may also provide a default set of options that will be
   applied to all requests. The supported options are:

     * :insecure? - a boolean value to turn off/on the SSL certificate
       checking. This defaults to false. This option is only effective when
       using Clojure.

     * sse? - a boolean value to indicate that a channel of Server Sent Events
       should be returned. Defaults to false. This option is only effective for
       the `get` and `search` functions.

   You can override your provided defaults by specifying options directly on
   the individual CIMI function calls."
  ([]
   (instance nil nil))
  ([cep-endpoint]
   (instance cep-endpoint nil))
  ([cep-endpoint default-options]
   (let [defaults {:insecure? false
                   :sse?      false}]
     (->cimi-async (or cep-endpoint defaults/cep-endpoint)
                   (atom {:default-options (merge defaults default-options)})))))
