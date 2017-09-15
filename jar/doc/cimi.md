# CIMI API

The SlipStream API is migrating from a custom REST API to the
[CIMI](https://www.dmtf.org/standards/cloud) standard from
[DMTF](http://dmtf.org).  All recent additions to the resource model
already follow the CIMI REST interface.

## Usage

The CIMI search (search), create (add), read (get), update (edit), and
delete (delete) operations are defined in the `cimi` protocol in the
`sixsq.slipstream.client.api.cimi` namespace.  The protocol also
contains several functions that simplify the authentication process.

To use the CIMI protocol functions, you must instantiate either a
synchronous or asynchronous implementation of that protocol.  The
synchronous implementation directly returns the results of the
functions, while the asynchronous implementation always returns a
core.async channel.

```clojure
(ns my.namespace
 (:require
   [sixsq.slipstream.client.api.cimi :as cimi]
   [sixsq.slipstream.client.api.cimi.async :as async]
   [sixsq.slipstream.client.api.cimi.sync :as sync]))

;; Create an asynchronous client context.  Note that the
;; asynchronous client can be used from Clojure or ClojureScript.
(def client-async (async/create-cimi-async))

;; Returns a channel on which a document with directory of available
;; resource is pushed. User does not need to be authenticated.
(cimi/cloud-entry-point client-async)

;; Returns a channel with login status (HTTP code).
(cimi/login client-async {:href "session-template/internal"
                          :username "user"
                          :password "pass"})

;; Returns channel with document containing list of events.
(cimi/search client-async "events")

;; Same can be done with synchronous client, but in this case
;; all values are directly returned, rather than being pushed
;; onto a channel.
;; NOTE: The synchronous client is only available in Clojure!

(def client-sync (sync/create-cimi-sync))
(cimi/login client-sync {:href "session-template/internal"
                         :username "user"
                         :password "pass"})
(cimi/search client-sync "events")
```

When creating the client context without specific endpoints, then
the endpoints for the Nuvla service will be used.  See the API
documentation for details on specifying the endpoints or other
options. 
