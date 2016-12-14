
## TOC
* [Overview](#overview)
* [Service Tier](#overview---service-tier)
* [A Hermes instance](#a-hermes-instance)
* [Routing Hash](#routing-hash)
* Usage Guide
  * [restFul APIs](#usage-guide---restful-apis)

## Overview
DHermes is a distributed [RESTful](https://en.wikipedia.org/wiki/Representational_state_transfer) API A/B testing application.  
It splits incoming traffic into pre-defined restful API group.  
An API group comprises a set of APIs which represents a service embodiment, e.g. recommendation APIs for an EC site.

<img src="https://drive.google.com/uc?id=0B78KhWqVkVmtNnZidTZLZkdPY2s" width=100%/>

## Overview - Service Tier
<img src= https://drive.google.com/uc?id=0B78KhWqVkVmtS0poNkJUZ2N0OGc width=100%/>

In the right side tier, a recommender is represented by a restful API, i.e. http://

## A Hermes instance
TODO...
* RestFul API:
* Routing Hash:
* Local Cache:
* Mappings:

![](https://drive.google.com/uc?id=0B78KhWqVkVmtaWU1Z0FVYWVJSUk)

## Routing Hash Overview
![](https://drive.google.com/uc?id=0B78KhWqVkVmtRlJ5OHFSbkFNOTg)

#### Reference
* [State of the hash functions](http://blog.reverberate.org/2012/01/state-of-hash-functions-2012.html)

## Usage Guide - restFul APIs
### Management APIs (/hermes/mgmt/)
* register_normal  
  Note, all value should be Double-quoted as a String
  ```
GET /hermes/mgmt/register_normal?token=${token}&json=

{
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
GET /hermes/mgmt/register_test?token=${token}&json=

{
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
DELETE /hermes/mgmt/rm_grp?token=${token}&key=${group_key}
```

* rm_mapping
  ```
DELETE /hermes/mgmt/rm_mapping?token=${token}&mid=${_id}
```

* set_jumper
  ```
GET /hermes/mgmt/set_jumper?token=${token}&uid=u0806449&grpkey=test-1
```

* ls_forward_headers
  ```
GET /hermes/mgmt/ls_forward_headers?token=${token}
```

* add_forward_headers  
  The input json should be a JsonArray.  
  Note, empty input isn't acceptably, i.e. [].
  ```
GET /hermes/mgmt/add_forward_headers?token=${token}&json=["Cookie"]
```

* set_forward_headers  
  The input json should be a JsonArray and allow empty for clearing this setting, i.e. [].
  ```
GET /hermes/mgmt/set_forward_headers?token=${token}&json=["Referer"]
```

### Request bypass APIs (/hermes/api/)

