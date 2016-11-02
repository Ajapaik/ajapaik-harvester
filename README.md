##**Ajapaik.ee public API v1.0 specification**

Endpoint: http://ajapaik.ee:8080/ajapaik-service/AjapaikService.json

API content type is ```application/json```


**Request body:**
```
{
    "method": "search",
    "params": [
        {
            "fullSearch": {
                "value": "555"
            },
            "id": {
                "value": "1"
            },
            "what": {
                "value": "Ajapaik"
            },
            "description": {
                "value": "valimimoodul"
            },
            "who": {
                "value": "Autor"
            },
            "from": {
                "value": "MuIS"
            },
            "number": {
                "value": "123"
            },
            "luceneQuery": null,
            "institutionTypes": [
                "MUSEUM",
                "LIBRARY"
            ],
            "pageSize": 200,
            "digital": true
        }
    ],
    "id": 0
}
```
**Request parameters description:**

- Method – Must be „search“
- Params – List of search parameters
- fullSearch – Full text search parameeter. Optional
- id – Number part of record id. Optional
- what – Title of record. Record title must have all words defined in this parameter. All words must be exact match. Not case sensitiive. Optional
- description – Description of record. Record title must have all words defined in this parameter. All words must be exact match. Not case sensitiive. Optional
- who – Author of record. Record title must have all words defined in this parameter. All words must be exact match. Not case sensitiive. Optional
- from – Institution, where record is from. Optional
- number – Number of Record, ID of institution record
- luceneQuery - ?
- institutionTypes – Array. Possible values are: null, "MUSEUM", "LIBRARY", "DPSACE"
- pageSize – limits number of record data. Record Ids list length is always up to 10 000 rows and cannot be limited. If not specified, no record views are returned
- digital - ?

**Response body:**
```
{
    "jsonrpc": "2.0",
    "id": 0,
    "result": {
        "ids": [
            "oai:muis.ee:1343334_1829478"
        ],
        "firstRecords": null,
        "firstRecordViews": [
            {
                "creators": "fotoateljee nr.2",
                "identifyingNumber": "NLM F 996:4",
                "title": "Jelena Grablevskaja portree",
                "description": "",
                "types": "foto, pilt",
                "materials": "",
                "collections": "Narva Muuseum: Fotokogu",
                "institution": "Narva Muuseum, MuIS",
                "urlToRecord": "http://muis.ee/museaalview/1343334",
                "providerHomepageUrl": "http://www.muis.ee",
                "providerName": "MuIS",
                "imageUrl": "http://opendata.muis.ee/media/1829478",
                "institutionType": "MUSEUM",
                "id": "oai:muis.ee:1343334_1829478",
                "date": "[]",
                "cachedThumbnailUrl": "99173c6e833f0f916b64cf23fcb86840",
                "mediaId": 1829478,
                "mediaOrder": 0
            }
        ],
        "museumIds": null,
        "archiveIds": null,
        "libraryIds": null,
        "mediaIds": null,
        "totalMuseumIds": 0,
        "totalArchiveIds": 0,
        "totalLibraryIds": 0,
        "totalMediaIds": 0,
        "searchTime": 69.265683,
        "totalIds": 0
    }
}
```

**Response parameters description:**

- Jsonrpc – Version of JsonRPC
- Id - unused  
- Result – result object
- Ids – Array of record ids matching query
- firstRecords – unused
- firstRecordViews – list of record views matching Query. Size of list is controlled by request parameter
- creators – Creator of records
- identifyingNumber - Number of Record, ID of institution record
- title – Title of record
- description – Description of record
- types – types of record
- materials – materials of record
- collections – Collection, where record is stored
- institutions – institutions, where record is stored.
- urlToRecord – URL of record view on institution site
- providerHomepageUrl – URL of record institution homepage
- providerName – name of the record institution
- imageUrl – URL of image of record
- institutionType – type of the record institution
- id – id of the record
- date – date of creation (or digitalization) of record 
- cachedThumbnailUrl – part of URL of image thumbnail of record on Ajapaik.ee site.
- mediaid – Ajapaik.ee internal ID of record image
- mediaOrder – index of media
- museumIds - unused,
- archiveIds - unused
- libraryIds - unused
- mediaIds - unused
- totalMuseumIds - unused
- totalArchiveIds - unused
- totalLibraryIds - unused
- totalMediaIds - unused
- searchTime - time of query,
- totalIds - unused



