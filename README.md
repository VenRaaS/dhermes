## TOC
* [Overview](#overview)
* [Service Tiers](#overview---service-tiers)
* [High Availability](#high-availability-by-distributed-mapping-table)
* [A Hermes instance](#a-hermes-instance)
* [Routing & Mapping](#routing--mapping)
  * [routing - overview](#routing---overview)
  * [routing - traffic unit](#routing---traffic-unit)
  * [routing - routing hash](#routing-hash)
  * [mapping](#mapping)
* [Usage Guide - restFul APIs](#usage-guide---restful-apis)
  * [Management APIs](#management-apis)
  * [Request bypass APIs](#request-bypass-apis)

## Overview
DHermes is a distributed RESTful API A/B/n testing application.  
It splits incoming traffic into pre-defined restful API group with the specified percentage.  
An API group comprises a set of APIs which represents a service embodiment, e.g. recommendation APIs for an EC site.

![](https://storage.googleapis.com/venraas-github/img/hermes_overview.PNG)

## Overview - Service Tiers
![](https://storage.googleapis.com/venraas-github/img/hermes_service_tier.PNG)

In the right side tier, a recommender is represented by a restful API, i.e. http://

## High Availability by Distributed mapping table

![](https://storage.googleapis.com/venraas-github/img/hermes_distributed_mapping.PNG)

## A Hermes instance
A Hermes instance is comprised of a RESTful web app and an ES node.  
The RESTful web app consists of following parts.
* RestFul API: Invoked by upstream and configuration.
* Routing Hash: dispatchs traffic into group (channel) as random as possible.
* Local Cache: acceleration of mapping query.
* Mappings: tables for linking input parameters and destination api url.

![](https://storage.googleapis.com/venraas-github/img/hermes_an_instance_z150.PNG)

## Routing & Mapping 
### Routing - Overview
![](https://storage.googleapis.com/venraas-github/img/hermes_routing_overview.PNG)

### Routing - Traffic Unit
* client_id = $token + $ven_guid + $ven_session
![](https://storage.googleapis.com/venraas-github/img/hermes_traffic_unit.PNG)

### Routing Hash
* Input = client_id + interval(current_datetime)
* Output = $long_integer => $GroupID
![](https://storage.googleapis.com/venraas-github/img/hermes_routing_hash.PNG)

### Mapping 
* Input = parameters given by client (consumber browser)
* Output = service URL and auxiliary parameters for the destination API

![](https://storage.googleapis.com/venraas-github/img/hermes_params_mapping.PNG)

#### Reference
* [State of the hash functions](http://blog.reverberate.org/2012/01/state-of-hash-functions-2012.html)

## Usage Guide - restFul APIs
### Management APIs
* root path
  ```
  /hermes/mgmt/
  ```

* register_normal  
  Note, all value should be Double-quoted as a String
  ```
  POST /hermes/mgmt/register_normal

  {
    "token":"${token}"
    "rec_pos":"categTop",
    "rec_code":"ClickStream",
    "rec_type":"cs",
    "api_url":["http://104.199.205.141/cupid/api/showinputparam"],
    "in_keys2recomder":["rec_pos"],
    "out_aux_params":["rec_code", "rec_type"]
  }
  ```

* register_test  
  Note, all value should be Double-quoted as a String
  ```
  POST /hermes/mgmt/register_test
  
  {
    "token":"${token}"
    "group_key":"test-1",
    "rec_pos":"categTop",
    "rec_code":"ClickStream_COOC",
    "rec_type":"ClickStream",
    "page_type":"1",
    "api_url":["http://fake-cupid/cupid/api/showinputparam"],
    "in_keys2recomder":["page_type"],
    "out_aux_params":["rec_code","rec_pos", "rec_type"]
  }
  ```

* set_traffic_pct_normal
  ```
  GET /hermes/mgmt/set_traffic_pct_normal?token=${token}&pct=0.66
  ```

* set_routing_reset_interval
  ```
  GET /hermes/mgmt/set_routing_reset_interval?token=${token}&interval=HOUR
  ```

* ls_grp
  ```
  GET /hermes/mgmt/ls_grp?token=${token}
  ```

* rm_grp
  ```
  DELETE /hermes/mgmt/rm_grp?token=${token}&grpkey=${group_key}
  ```

* rm_mapping
  ```
  DELETE /hermes/mgmt/rm_mapping?token=${token}&mid=${_id}
  ```

* set_jumper
  * uid (uid will be priority consideration if user is login.)
    ```
    GET /hermes/mgmt/set_jumper?token=${token}&grpkey=test-1&uid=${uid}
    ```
  * guid  
    ```
    GET /hermes/mgmt/set_jumper_guid?token=${token}&grpkey=test-1&ven_guid=${ven_guid}
    ```

* ls_forward_headers  
  Shows the setting http headers which forwards to the the backend API, e.g. cookie, origin, referer ...  
  ```
  GET /hermes/mgmt/ls_forward_headers?token=${token}
  ```  

* add_forward_headers  
  Adds the http header keys which would be forwarded to the backend API, e.g. cookie, origin, referer ...  
  The input json should be a JsonArray, i.e. [].  
  ```
  GET /hermes/mgmt/add_forward_headers?token=${token}&json=["Cookie"]
  ```

* set_forward_headers  
  Sets the http header keys which would be forwarded to the backend API, e.g. cookie, origin, referer ...  
  The input json should be a JsonArray and allow empty for clearing this setting, i.e. [].
  ```
  GET /hermes/mgmt/set_forward_headers?token=${token}&json=["Referer"]
  ```

### Request bypass APIs 
* root path and syntax
  ```
  /hermes/api/${subject}/${action}
  ```
  
* `${subject}` and `${action}` are able to be customized according to the regular expression, i.e. `[0-9A-Za-z_]+`
  * /hermes/api/`goods/rank`
  * /hermes/api/`category/rank`
