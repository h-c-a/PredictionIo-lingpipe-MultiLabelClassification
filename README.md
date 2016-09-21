##Multi Label Text Category Classifier Template

This engine template is an almost-complete implementation of an engine meant to used with PredictionIO.

This muli label Classification Engine Template has integrated Lingpipe (http://alias-i.com/lingpipe/) algorithm by default.

## Quick Start
Check the prerequisites below before setup, it will inform choices made.

 1. [Install the PredictionIO framework](https://docs.prediction.io/install/) **be sure to choose HBase and Elasticsearch** for storage. This template requires Elasticsearch.
 2. Make sure the PIO console and services are running, check with `pio status`
 3. [Install this template](git pull TEMPLATEFOLDERLOCATION)

### Overview
This engine template utilizes the Lingpipe library (DynamicLMClassifier) to classify text based off of training data.

read more at:  http://alias-i.com/lingpipe/demos/tutorial/classify/read-me.html

LingPipe Licensing : http://alias-i.com/lingpipe/web/download.html  

### Import Sample Data

1. Create a new app name, change `appName` in `engine.json`
2. Run `pio app new **your-new-app-name**`
4. Import sample events by running `pio import --appid **your-app-id** --input **your-eventfile.json**` where the appid can be retrieved with `pio app list`
3. The engine.json file in the root directory of your new config template is set up for the data you just imported (make sure to create a new one for your data) Edit this file and change the `appName` parameter to match what you called the app in step #2
5. Perform `pio build`, `pio train`, and `pio deploy`
6. To execute some sample queries run `curl -H "Content-Type: application/json" -d '{"text": "this is a good one", "locale":"EN"}' http://localhost:8000/queries.json`


### Usage
**Event Data Requirements**

ensure to read : https://docs.prediction.io/datacollection/eventapi/

**Events**
***Training Data***
```

    1. eventTime : String
    2. entityId: GUID
    3. event : String
    4  properties
        * Query: String,
        * Category: String,
        * Locale: String
    5. entityType: String

```
```
**Example**:
{"eventTime":"2016-03-02T09:52:49+0000","entityId": "5ec59686-84fe-4fe0-b343-27794f6a2645","event":"Autocategory","properties":{"Query":"Apple","Category":"Fruit","Locale":"en"},"entityType":"content"}
```


***StopWords data***
```

    1. eventTime : String
    2. entityId: GUID
    3. event : String
    4  properties
        * word: String,
        * Locale: String
    5. entityType: String

```
```
**Example**:
{"eventTime":"2016-03-16T10:25:02+0000","entityId":"fae01e9e-b00e-46d3-becd-f91a6b3b140a","event":"stopwords","properties":{"word":"and","locale":"EN"},"entityType":"resource"}
```


**Input Query**
```
* text: String
* locale : String
```
```
**Example**: {"text": "Apple", "locale":"EN"}'
```
**Output: A List of PredictedResult**
```
* Category : String
* Score : String
```
```
**Example**:
items ":[{" Category ":" Fruit "," Score ":" 0.845175688166216 "},{" Category ":" Tree "," Score ":" 0.7922164503364719 "]}
 ```

### Engine.json

This file allows the user to describe and set parameters that control the engine operations. Many values have defaults so the following can be seen as the minimum.Reasonable defaults are used so try this first and add tunings or new event types and item property fields as you become more familiar.

#### Simple Default Values
    {
      "id": "default",
      "description": "Default settings",
      "engineFactory": "io.prediction.lingpipe.AutoCatEngine",
      "datasource": {
        "params": {
          "appName": "AutoCat",
          "appId": 1,
          "entityType": "content",
          "eventType": "Autocategory",
          "categoryField": "Category",
          "queryField": "Query",
          "localeField": "Locale",
          "stopwordentityType": "resource",
          "stopwordeventType": "stopwords",
          "stopwordField": "word",
          "stopwordlocaleField": "locale"
        }
      },
      "algorithms": [
        {
          "name": "algo",
          "params": {
            "maxresults":5,
            "ngramsize":3,
            "modelbuildfilepath": "/opt/Templates/BuildModels/",
            "modelbuildfilename": "Lingpipe_Model"
          }
        }
      ]
    }