
## TOC
* [Overview](#overview)
* [Service Tier](#overview---service-tier)
* [A Hermes instance](#a-hermes-instance)
* [Routing Hash](#routing-hash)
* Usage Guide
  * [restFul APIs](#usage-guide---restful-apis)

## Overview
DHermes is a distributed A/B testing application.  
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
  Note, value should be Double-Auoted as a String
  ```
/hermes/mgmt/register_normal?token=${token}&json=

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
  Note, value should be Double-Auoted as a String
  ```
/hermes/mgmt/register_test?token=${token}&json=

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
/hermes/mgmt/set_traffic_pct_normal?token=${token}&pct=0.66
```

* set_routing_reset_interval
  ```
/hermes/mgmt/set_routing_reset_interval?token=${token}&interval=HOUR
```

* ls_grp
  ```
/hermes/mgmt/ls_grp?token=${token}
```

* rm_grp
  ```
/hermes/mgmt/rm_grp?token=${token}&key=${group_key}
```

* rm_mapping
  ```
/hermes/mgmt/rm_mapping?token=${token}&mid=${_id}
```

* set_jumper
  ```
/hermes/mgmt/set_jumper?token=${token}&uid=u0806449&grpkey=test-1
```

### Request bypass APIs (/hermes/api/)

