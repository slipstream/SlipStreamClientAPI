(ns sixsq.slipstream.client.api.cimi
  (:refer-clojure :exclude [get]))

(defprotocol cimi
  "This protocol (interface) defines all the SCRUD (search, create, read,
   update, and delete) actions for CIMI resources. It also defines convenience
   functions for authenticating with the server and a function to execute
   specialized operations on a given resource or collection.

   Note that the concrete return types will depend on the implementation. For
   example, an asynchronous implementation may return channels from all of the
   functions."

  (login
    [this login-params]
    [this login-params options]
    "Uses the given login-params to log into the SlipStream server. The
     login-params must be a map containing an :href element giving the id of
     the sessionTemplate resource and any other attributes required for the
     login method. Returns a map with the response.  Successful responses will
     contain a status code of 201 and the resource-id of the session.")

  (logout
    [this]
    [this options]
    "Performs a logout of the client by deleting the current session. Returns
     a map of the request response or nil if the user is not currently logged
     in.")

  (authenticated?
    [this]
    [this options]
    "Returns true if the client has an active session; returns false otherwise
     (even for errors).")

  (cloud-entry-point
    [this]
    [this options]
    "Retrieves the cloud entry point. The cloud entry point (CEP) acts as a
     directory of the available resources within the CIMI server. This function
     does not require authentication. The result is returned in EDN format.
     Implementations may cache the cloud entry point to avoid unnecessary
     requests to the server.")

  (add
    [this resource-type data]
    [this resource-type data options]
    "Creates a new CIMI resource of the given type. The data will be converted
     into a JSON string before being sent to the server. The data must match
     the schema of the resource type.")

  (edit
    [this url-or-id data]
    [this url-or-id data options]
    "Updates an existing CIMI resource identified by the URL or resource id.
     The data must be the complete, updated data of the resource. Returns the
     updated resource in EDN format.")

  (delete
    [this url-or-id]
    [this url-or-id options]
    "Deletes the CIMI resource identified by the URL or resource id from the
     server. Returns a map with the result.")

  (get
    [this url-or-id]
    [this url-or-id options]
    "Reads the CIMI resource identified by the URL or resource id. Returns the
     resource as EDN data.")

  (search
    [this resource-type]
    [this resource-type options]
    "Search for CIMI resources of the given type, returning a list of the
     matching resources. The list will be wrapped within an envelope containing
     the metadata of the collection and search. The returned document is in EDN
     format.")

  (operation
    [this url-or-id operation]
    [this url-or-id operation data]
    [this url-or-id operation data options]
    "Executes the chosen operation on the resource identified to the
     url-or-id. The operation must specify the full URI. If the data is
     provided, then it will be sent as a JSON document as the body of the POST
     request. Standard options can also be provided."))
