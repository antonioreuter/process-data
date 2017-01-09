# Batch Processing

An example of batch processing application.

#### Technologies
- Gradle
- JDK 1.8
- Spring Batch
- Mockito
- Junit
- H2 (Database)

### Installation
#### Requirements
- JDK 1.8

Executing the command below, it'll install all the project dependencies and build the package.

```
    ./gradlew shadowJar
```

### Running

```
    java -jar build/libs/process-data-1.0.0.jar -s=<source file path> -o=<output format>
```

### Parameters

The supported params to perform the data processing.

**Source file:**
```
    -s or -source :
        The source file path where is the data that you want to process.
        Restrictions:
            * Support just for CSV files;
            * Processes just hotel information that use this header:
                name,address,stars,contact,phone,uri

        ex: -s=/tmp/hotel-data.csv
```

**Output format:**
```
    -o or -output :
        The output format supported:
            * CSV
            * JSON
            * XML
            * DB

        ex: -o=json
```

**Sort by**
```
    -sort :
        You are able to sort the output data through any field presented on header.

       ex: -sort=name,stars
```

**Complete example**
```
    java -jar build/libs/process-data-1.0.0.jar -s=/tmp/hotel-data.csv -o=xml -sort=name,stars
```


### Processing

During the processing stage we evaluate each record, hotel, through some criteria.
   - A hotel name may only contain UTF-8 characters.
   - The hotel URL must be valid (please come up with a good definition of "valid").
   - Hotel ratings are given as a number from 0 to 5 stars. There may be no negative numbers.

To establish the quality of each record processed, we define a new field that holds a *grade*.
For each criteria that the record don't accomplish we reduce his grade. All records starts with the
maximum grade, 10.

    * Invalid URL (-3):
        The URL must be well written, besides that, we check with a http client if the page is still available.
        So if the response after a request return something different than 200 (OK), the URL is considered invalid.

    * Star (-1):
        The star cannot be negative otherwise, we decrease the grade by 1 digit.

**Observation**

You can use the grade field to sort the output results.
 ```
   java -jar build/libs/process-data-1.0.0.jar -s=/tmp/hotel-data.csv -o=xml -sort=grade,name,stars
 ```