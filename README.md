## Overview
DHermes is a distributed A/B testing application.  
It splits incoming traffic into pre-defined restful API group.  
An API group comprises a set of APIs which represents a service embodiment, e.g. recommendation APIs for an EC site.

<img src="https://drive.google.com/uc?id=0B78KhWqVkVmtNnZidTZLZkdPY2s" width=700/>

## Overview - Service Tier
<img src= https://drive.google.com/uc?id=0B78KhWqVkVmtS0poNkJUZ2N0OGc />
In the right side tier, a recommender is represented by a restful API, i.e. http://

## A Hermes instance
TODO...
* RestFul API:
* Routing Hash:
* Local Cache:
* Mappings:

![](https://drive.google.com/uc?id=0B78KhWqVkVmtbngtQUF3U0ltS3M)

## Routing Hash
![](https://drive.google.com/uc?id=0B78KhWqVkVmteWQ0YXJHdTliQ2M)

#### Reference
* [State of the hash functions](http://blog.reverberate.org/2012/01/state-of-hash-functions-2012.html)

## Usage Guide - restFul APIs
### Management APIs 
* register_normal  
  ```hermes/mgmt/register_normal?token=${token}&json=

{
	"rec_pos":"categTop",
    "rec_code":"ClickStream",
    "rec_type":"cs",
    "api_url":[
        "http://140.96.83.32:8080/cupid/api/goods/rank"
    ],
    "in_keys2recomder":[
        "rec_pos"
    ],
    "out_aux_params":[
        "rec_code",
        "rec_type"
    ]
}
```

* register_test  
  ```
hermes/mgmt/register_test?token=${token}&json=

{
  "group_key":"test-1",
  "rec_pos":"categTop",
  "rec_code":"ClickStream",
  "rec_type":"cs",
  "api_url":[
      "http://140.96.83.32:8080/cupid/api/goods/rank"
  ],
  "in_keys2recomder":[
      "rec_pos"
  ],
  "out_aux_params":[
      "rec_code",
      "rec_type"
  ]
}
```


### Request bypass APIs

