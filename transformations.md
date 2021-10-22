<!-- BEGINPARSE -->
# Transformations

## Introduction

A transformation is an operation in order to modify the DataFrame by applying row and column transformations. These modifications can be processed by sql functions, algorithms, data cleaning functions, among others.

In Kirby the transformation configuration comprises an array of transformation objects which are executed in sequential order. There are two types of transformations:
1. Row Transformation: transformations that can be applied to all rows.  
2. Column Transformation: transformations that can be applied to one column. 

<a name="regexExpr"></a>
## Using Regex for Transformations

The *field* attribute of transformations accepts regular java expressions, meaning thereby that a transformation can be applied to all fields matching the regex expression. In order to leverage this feature add the configuration
```conf
regex = true
```
to the transformation. Doing so, this attribute ensures that the transformation will be applied to all fields matching the regex expression.

##### Example
In order to change values of a field, whose name matches with the regex expression '.\*FIELD.\*' the following transformation can be applied:
```hocon
{
  field = ".*FIELD.*"
  regex = true
  type = "replace"
  replace = {
    "old": "new"
  }
}
```
Example input:

| id     | otherField | test_FIELD_1 | FIELD_2 |
|--------|------------|--------------|---------|
| earth  | old        | old          | old     |
| mars   | none       | none         | none    |

Example output:

| id    | otherField | test_FIELD_1 | FIELD_2 |
|-------|------------|--------------|---------|
| earth | old        | new          | new     |
| mars  | none       | none         | none    |

## Transformations List

Column Transformations
- [base64](#base64)
- [Caseletter](#CaseLetter)
- [Catalog](#Catalog)
- [CharacterTrimmer](#CharacterTrimmer)
- [CommaDelimiter](#CommaDelimiter)
- [CopyColumn](#CopyColumn)
- [DateFormatter](#DateFormatter)
- [ExtractInfoFromDate](#ExtractInfoFromDate)
- [Formatter](#Formatter)
- ~~[Hash](#Hash)~~
- [InsertLiteral](#InsertLiteral)
- [Integrity](#Integrity)
- [Literal](#Literal)
- ~~[Mask](#Mask)~~
- [PartialInfo](#PartialInfo)
- [Replace](#Replace)
- [LeftPadding](#LeftPadding)
- [Tokenizer](#Tokenizer)
- [Trim](#Trim)
- [UpdateTime](#UpdateTime)

Row Transformations
- [ArithmeticOperation](#ArithmeticOperation)
- [Autocast](#Autocast)
- [CleanNulls](#CleanNulls)
- [Conditional](#Conditional)
- [DropColumns](#DropColumns)
- [DropDuplicates](#DropDuplicates)
- [Filter](#Filter)
- [InitNulls](#InitNulls)
- [Join](#Join)
- [OffsetColumn](#OffsetColumn)
- [RegexCaseColumn](#RegexCaseColumn)
- [RegexColumn](#RegexColumn)
- [RenameColumns](#RenameColumns)
- [SelectColumns](#SelectColumns)
- [SqlFilter](#SqlFilter)
- [PreserveOrder](#PreserveOrder)
- [Checkpoint](#Checkpoint)

## Column Transformations
Column transformations are applied over a data frame field. Use the *field* attribute in the transformation configuration to identify the field on which the transformation will be applied. If the column not exists a column will be created. Alternatively you can use [regex expressions](#regexExpr). 

<a name="base64"></a>
### Base64 (type = base64)

This transformation casts a string type column into a column with base64.

| Attribute | Type    | Optional | Value Domain | Default | Description                                                                                                      |
|-----------|---------|----------|--------------|---------|------------------------------------------------------------------------------------------------------------------|
| type      | String  | False    | "base64"     |         | Transformation type                                                                                              |
| field     | String  | False    |              |         | field name                                                                                                       |
| encrypted | Boolean | True     |              | False   | If setted by true, this transformation decodes a BASE64 encoded string column and returns it as a string column. |

It is important to know that the transformation is case sensitive. Uppercase letters are traduced differently in base64 than lowercase letters.

##### Example

In order to transform the field *test* into base64, the following configuration can be used:

```hocon
{
   type = "base64"
   field = "test"
}
```
Example input:

| test |
|------|
|spark |
|gerson|

Example output:

| test   |
|--------|
|c3Bhcms=|
|Z2Vyc29u|

<a name="CaseLetter"></a>
### Case letter (type = caseletter)

Letter values of a selected field can be transformed to be uniformly written in uppercase or lowercase.

| Attribute | Type   | Optional | Value Domain     | Default | Description                                              |
|-----------|--------|----------|------------------|---------|----------------------------------------------------------|
| type      | String | False    | "caseletter"     |         | Transformation type                                      |
| field     | String | False    |                  |         | field name                                               |
| operation | String | False    | "upper", "lower" |         | Operations to apply to strings: Uppercase and lowercase. |

##### Example

In order to convert in the field *key* characters to uppercase the following transformation can be applied:

```hocon
{
   type = "caseletter"
   field = "key"
   operation = "upper"
}
```
Example input:

| key |
|-----|
|earth|
|EARTH|
|mars |

Example output:

| key |
|-----|
|EARTH|
|EARTH|
|MARS |

<a name="Catalog"></a>
### Catalog (type = catalog)
Given a catalog stored in a csv formatted HDFS with *(key,value)* structure, the field content is modified by replacing the character subsequence matching *key* by the corresponding *value* determined in the catalog.

| Attribute | Type   | Optional | Value Domain | Default | Description                                                 |
|-----------|--------|----------|--------------|---------|-------------------------------------------------------------|
| type      | String | False    | "catalog"    |         | Transformation type                                         |
| field     | String | False    |              |         | field name                                                  |
| path      | String | False    |              |         | Path to HDFS of catalog that contains key value csv format. |

##### Example
In order to replace in the field *description* the subsequence *ref1* by *world* and *ref2* by *world2*, the dictionary file
```csv
ref1,world
ref2,world2
```
and the following transformation can be applied:

```hocon
{
   type = "catalog"
   path = "{PATH}/dictionary.csv"
   field = "description"
}
 ```

Example input:

| key  | description |
|------|-------------|
|earth |ref1         |
|mars  |none         |

Example output:

| key | description |
|-----|-------------|
|earth|world        |
|mars |none         |

<a name="CharacterTrimmer"></a>
### Character Trimmer (type = charactertrimmer)

This transformation removes a character sequence of a string if the character sequence is located at the beginning or the end of the string.

| Attribute        | Type   | Optional | Value Domain          | Default | Description                                            |
|------------------|--------|----------|-----------------------|---------|--------------------------------------------------------|
| type             | String | False    | "charactertrimmer"    |         | Transformation type                                    |
| field            | String | False    |                       |         | field name                                             |
| charactertrimmer | String | False    |                       | '0'     | Character to trim.                                     |
| trimType         | String | False    | "left","right","both" | 'left'  | Indicator of where string sequences should be removed. |

##### Example
In order to remove the character '0' at the beginning of a value in field *description* the following transformation can be applied:
```hocon
config
{
   type = "charactertrimmer"
   charactertrimmer = "0"
   trimType = "left"
   field = "description"
}
```

Example input:

| key | description |
|-----|-------------|
|earth|00000world   |
|mars |000none      |

Example output:

| key | description |
|-----|-------------|
|earth|world        |
|mars |none         |

<a name="CommaDelimiter"></a>
### Comma Delimiter (type = commadelimiter)
Include a decimal separator at a selected position from the end of a string.

| Attribute        | Type    | Optional | Value Domain     | Default | Description         |
|------------------|---------|----------|------------------|---------|---------------------|
| type             | String  | False    | "commaDelimiter" |         | Transformation type |
| field            | String  | False    |                  |         | Field name          |
| lengthDecimal    | Integer | False    |                  | 2       | Decimal part size.  |
| separatorDecimal | String  | False    |                  | '.'     | Separator to use.   |


##### Example
In order to transform integers to decimals with 3 decimal places, the following transformation can be applied:
```hocon
config
{
   type = "commaDelimiter"
   separatorDecimal = "."
   lengthDecimal = 3
   field = "currency"
}
```
Example input:

| currency |
|----------|
|18903     |
|123       |
|1         |

Example output:

| currency |
|----------|
|18.903    |
|0.123     |
|0.001     |

<a name="CopyColumn"></a>
### CopyColumn (type = copycolumn)
This transformation generates a copy of the column defined in *copyField* and renames it to the name defined in *field*. By default, datatype of the new field concurs with the datatype of the original field. Is desired, the datatype can me changed manually to any compatible type by specifying the *defaultType* attribute.

| Attribute   | Type   | Optional | Value Domain | Default                           | Description                           |
|-------------|--------|----------|--------------|-----------------------------------|---------------------------------------|
| type        | String | False    | "copyColumn" |                                   | Transformation type                   |
| field       | String | False    |              |                                   | Field name                            |
| copyField   | String | False    |              |                                   | Name of field that have to be copied. |
| defaultType | String | True     |              | Same data type as original field. | data type to cast new column.         |


##### Example
In order to copy the field *description* in a new one called *copydescription* the following transformation can be applied:
```hocon
{
  type = "copycolumn"
  field = "copydescription"
  copyField = "description"
}
```

Example input:

| key | description |
|-----|-------------|
|earth|ref1         |
|mars |none         |

Example output:

| key | description | copydescription |
|-----|-------------|-----------------|
|earth|ref1         |ref1             |
|mars |none         |none             |

<a name="DateFormatter"></a>
### DateFormatter (type = dateformatter)
This transformation transforms a field with string type format to a field with date type format, and viceversa. Furthermore this transformation can change the date format on a field of string type (string to string).

| Attribute | Type   | Optional | Value Domain                                    | Default      | Description                                        |
|-----------|--------|----------|-------------------------------------------------|--------------|----------------------------------------------------|
| type      | String | False    | "dateformatter"                                 |              | Transformation type                                |
| field     | String | False    |                                                 |              | field name                                         |
| format    | String | False    |                                                 |              | Name of field that have to be copied (see Format). |
| reformat  | String | True     |                                                 | ''           | see Reformat                                       |
| locale    | String | True     |                                                 | 'empty'      | see Locate                                         |
| relocale  | String | True     |                                                 | 'empty'      | see Relocate                                       |
| operation | String | True     | "parse", "parseTimestamp", "format", "reformat" | "parse"      | see Operation                                      |
| castMode  | String | True     | "permissive", "notPermissive"                   | "permissive" | see CastMode                                       |
| chronoFields | List [ChronoField] | True | [(HOUR_OF_DAY, 0),(MINUTE_OF_HOUR,0),(SECOND_OF_MINUTE,0),(INSTANT_SECONDS,0)] | Allow to configure de DateTimeFormatter with defaulting values. The allowed field names can be obtained from this https://docs.oracle.com/javase/8/docs/api/java/time/temporal/ChronoField.html A ChronoField is composed by 'name' (string, mandatory) and a value (long, optionally). If value is set, the chrono field override the default of kirby else the kirby chrono field configuration will be unset. By example, zoned patterns (patterns with z, Z, V, x, or X) should remove the chrono field INSTANT_SECONDS |
<a name="dateformatter_Format"></a>
##### Format
Format used to parse the data of the column when using operations 'parse' and 'reformat', and the target format when using the 'format' operation. For more information about formats read https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html.

<a name="dateformatter_Reformat"></a>
##### Reformat
Format to apply to the field when 'reformat' operation is selected. For more information about formats read https://docs.oracle.com/javase/8/docs/api/java/time/format/DateTimeFormatter.html.

<a name="dateformatter_Locale"></a>
##### Locale
Locale used with the format pattern to interpret the date. For more information about locales see language tag in http://www.oracle.com/technetwork/java/javase/java8locales-2095355.html

<a name="dateformatter_Relocale"></a>
##### Relocale
Locale used with the reformat pattern to interpret the date. For more information about locales see language tag in http://www.oracle.com/technetwork/java/javase/java8locales-2095355.html

<a name="dateformatter_Operation"></a>
##### Operation
Default option is 'parse'.The available options are:

| Operation      | Description                                                                                                                                                                                                                                                    |
|----------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| parse          | Parse the field using the format value specified in 'format' and cast the datatype from string to Date.                                                                                                                                                        |
| parseTimestamp | Parse the field using the format value specified in 'format' and cast the datatype from string to Timestamp.                                                                                                                                                   |
| format         | Parse the field using the format value specified in 'format' and cast the datatype to string.                                                                                                                                                                  |
| reformat       | Parse the field using the format value specified in 'format' and cast the datatype to string using the reformat value 'reformat'. If you need to specify the locale you can use the "locale" field for the source locale and "relocale" for the target locale. |

<a name="dateformatter_CastMode"></a>
##### CastMode
Allowed values are permissive (by default) and notPermissive.
  - permissive: Malformed values ​​found in the data do not cause the process to stop, the malformed values will be nullified.
  - notPermissive: Malformed values ​​found in the data cause the process to stop throwing a Exception.

##### Example
In order to format a date with 'HH:mm:ss' the following transformation can be applied:
```hocon
{
  field = last_date
  type = "dateformatter"
  format = "HH:mm:ss"
  operation = "parse"
}
```
Example input:

| id | last_date       |
|----|-----------------|
|jhon|31/12/16 22:00:59|
|paul|31:12:16 21:59:00|

Example output:

| id | last_date |
|----|-----------|
|jhon|22:00:59   |
|paul|21:59:00   |


##### Example
In order to format a date with 'dd/MM/yyyy HH:mm:ss.SSSSSS' the following transformation can be applied:
```hocon
{
  field = last_date
  type = "dateformatter"
  format = "dd/MM/yyyy HH:mm:ss.SSSSSS"
  operation = "parse"
}
```
Example input:

| id | last_date                |
|----|--------------------------|
|jhon|26/04/2017 11:59:59.999999|
|paul|31/12/2017 23:59:59.667112|

Example output:

| id | last_date                |
|----|--------------------------|
|jhon|26/04/2017 11:59:59.999999|
|paul|31/12/2017 23:59:59.667112|


##### Example
In order to format a date with 'dd/MM/yyyy HH:mm:ss' into a timestamp datatype field the following transformation can be applied:
```hocon
{
  field = "date"
  type = "dateformatter"
  format = "dd/MMM/yy HH:mm:ss"
  operation = parseTimestamp
  locale = es
}
```
Example input:

| id  | last_date          |
|-----|--------------------|
|jhon |"31/Dic/16 12:11:59"|
|paul |"01/Dic/16 12:11:59"|
|jesus|"01/Ene/17 12:11:59"|

Example output:

| id  | last_date           |
|-----|---------------------|
|jhon |2016-12-31 12:11:59.0|
|paul |2016-12-01 12:11:59.0|
|jesus|2017-01-01 12:11:59.0|


##### Example
In order to format a string to 'dd/MMM/yy"' the following transformation can be applied:
```hocon
{
  field = "date"
  type = "dateformatter"
  format = "dd/MMM/yy"
  operation = format
  locale = es
}
```
Example input:

| id  | last_date  |
|-----|------------|
|jhon |"2016-12-31"|
|jesus|"2017-01-21"|

Example output:

| id  | last_date |
|-----|-----------|
|jhon |"31/dic/16"|
|jesus|"21/ene/17"|


##### Example
In order to change a string with format "dd/MMM/yy HH:mm:ss" to a string with format "yyyy-MMM-dd HH:mm:ss" the following transformation can be applied:
```hocon
{
  field = "date"
  type = "dateformatter"
  format = "dd/MMM/yy HH:mm:ss"
  reformat = "yyyy-MMM-dd HH:mm:ss"
  operation = reformat
  locale = "es_ES"
  relocale = "de_DE"
}
```
Example input:

| id  | last_date          |
|-----|--------------------|
|john |"31/Dic/16 21:07:11"|
|jesus|"01/Dic/16 17:07:11"|

Example output:

| id  | last_date            |
|-----|----------------------|
|john |"2016-Dez-31 21:07:11"|
|jesus|"2016-Dez-01 17:07:11"|


##### Example
In order to change a string with format "yyyy-MM-dd HH:mm:ss[.SSS][.SS][.S] xx xxx" to a string with format "yyyy-MM-dd HH:mm:ss.SSS"s" the following transformation can be applied:
```hocon
{
    field = "date"
    "type" : "dateformatter",
    "format" : "yyyy-MM-dd HH:mm:ss[.SSS][.SS][.S] xx xxx",
    "reformat" : "yyyy-MM-dd HH:mm:ss.SSS",
    "operation" : "reformat",
    "locale" : "es",
    "chronoFields": [
      {"name": "INSTANT_SECONDS"}
    ]
}
```
Example input:

| date          |
|--------------------|
| "2019-03-23 05:07:01 +0100 +01:00"|
| "2019-03-23 05:07:02.1 +0100 +01:00"|
| "2019-03-23 05:07:03.12 +0100 +01:00"|
| "2019-03-23 05:07:04.123 +0100 +01:00"|
| "2019-03-23 05:07:05 +0100 +01:00"|
| "2019-03-23 05:07:06.1 +0100 +01:00"|
| "2019-03-23 05:07:07.12 +0100 +01:00"|
| "2019-03-23 05:07:08.123 +0100 +01:00"|

Example output:

| date            |
|----------------------|
| "2019-03-23 05:07:01.000" |
| "2019-03-23 05:07:02.100" |
| "2019-03-23 05:07:03.120" |
| "2019-03-23 05:07:04.123" |
| "2019-03-23 05:07:05.000" |
| "2019-03-23 05:07:06.100" |
| "2019-03-23 05:07:07.120" |
| "2019-03-23 05:07:08.123" |

<a name="ExtractInfoFromDate"></a>
### ExtractInfoFromDate (type = extractinfofromdate)
Day, month or year can be extracted from some date type field. The new field will be stored in string type. If the transformation fail to extract the required data, increment metric fails.

| Attribute | Type   | Optional | Value Domain          | Default | Description                                               |
|-----------|--------|----------|-----------------------|---------|-----------------------------------------------------------|
| type      | String | False    | "extractinfofromdate" |         | Transformation type                                       |
| field     | String | False    |                       |         | Field name                                                |
| dateField | String | False    |                       |         | Field name that contains date whose want to extract info. |
| info      | String | True     | "day","month","year"  |         |                                                           |


##### Example
In order to extract the year from the field *last_date* and store it into the new field *year* the following transformation can be applied:
```hocon
{
  type= "extractinfofromdate"
  field = year
  datefield = "last_date"
  info = year
}
```
Example input:

| id | last_date       |
|----|-----------------|
|jhon|31/12/16 22:00:59|
|paul|31:12:16 21:59:00|

Example input:

| id | last_date       | year |
|----|-----------------|------|
|jhon|31/12/16 22:00:59|2016  |
|paul|31:12:16 21:59:00|2016  |

<a name="Formatter"></a>
### Formatter (type = formatter)
Data type conversion of a field. The target type should be compliant with the data types accepted in the schema.

| Attribute    | Type   | Optional | Value Domain    | Default | Description                                                        |
|--------------|--------|----------|-----------------|---------|--------------------------------------------------------------------|
| type         | String | False    | "formatter"     |         | Transformation type                                                |
| field        | String | False    |                 |         | field name                                                         |
| typeToCast   | String | False    | See @TypeToCast |         | Desired field datatype. The field will be casted to this datatype. |
| replacements | String | True     |                 |         |                                                                    |


<a name="Formatter_TypeToCast"></a> 
##### TypeToCast
It specify the target data type (string, int, long, double, float, boolean, date, time_millis, decimal(p), decimal(p,s)). Where decimal type p=precision s=scale.

##### Replacements
A list of replacements can be provided that will replace patterns by a string before castings. Doing so, it must be specified:
  - pattern (string): The regex pattern that should be replaced,
  - replacement (string): The value for the replacement.

##### Example
In order to cast a 'String' type column to an 'Integer' type column the following transformation can be applied:
```hocon
{
  field = "group"
  type = "formatter"
  typeToCast = "int"
}
```
Example input:

| id | group |
|----|-------|
|jhon|00033  |
|paul|00032  |

Example output:

| id | group |
|----|-------|
|jhon|33     |
|paul|32     |


##### Example
In order to remove letters and cast the field *value* to a field with float datatype, the following transformation can be applied:
```
{
  field = "value"
  type = "formatter"
  typeToCast = "float"
  replacements = [
  {
    pattern = "[a-zA-Z]"
    replacement = ""
  }
  ]
}
```

Example input:

| id  | value |
|-----|-------|
|jhon |A1.b   |
|paul |1.fe0  |
|frank|QQ1    |


Example output:

| id  | group |
|-----|-------|
|jhon |1.0    |
|paul |1.0    |
|frank|1.0    |

<a name="Hash"></a>
### Hash (type = hash)
#####Deprecated
Hash functions can be leveraged in order to map data of arbitrary size to data of a fixed size. Kirby provides the hash algorithms MD5, SHA1 and SHA2.

| Attribute  | Type    | Optional | Value Domain          | Default | Description                                                       |
|------------|---------|----------|-----------------------|---------|-------------------------------------------------------------------|
| type       | String  | False    | "hash"                |         | Transformation type                                               |
| field      | String  | False    |                       |         | field name                                                        |
| hashType   | String  | False    | "md5", "sha1", "sha2" |         | The hashing algorithm to apply.                                   |
| hashLength | Integer | True     | 0, 224, 256, 384, 512 | 256     | Lenght of output hash in bits (applicable only to sha2 hashType). |


##### Example
In order to hash a set of ids the following transformation can be applied:
```hocon
{
  type = "hash"
  field = "id"
  hashType = "md5"
}
```
Example input:

| id   | group |
|------|-------|
|jhon  |0001   |
|paul  |null   |
|martha|0002   |
|fred  |0003   |
|jhon  |0005   |

Example output:

| id                              | group |
|---------------------------------|-------|
|4c25b32a72699ed712dfa80df77fc582 | 0001  |
|6c63212ab48e8401eaf6b59b95d816a9 | null  |
|3003051f6d158f6687b8a820c46bfa80 | 0002  |
|570a90bfbf8c7eab5dc5d4e26832d5b1 | 0003  |
|4c25b32a72699ed712dfa80df77fc582 | 0005  |


<a name="InsertLiteral"></a>
### InsertLiteral (type = insertLiteral)
Insert a string literal into a field at a given position.

| Attribute  | Type    | Optional | Value Domain    | Default | Description                                           |
|------------|---------|----------|-----------------|---------|-------------------------------------------------------|
| type       | String  | False    | "insertLiteral" |         | Transformation type                                   |
| field      | String  | False    |                 |         | field name                                            |
| value      | String  | False    |                 |         | literal value to insert.                              |
| offset     | Integer | False    |                 |         | Position to insert the value at. First position is 0. |
| offsetFrom | String  | False    | "left", "right" | "left"  | side of the string the position is calculated from.   |

##### Example
In order to insert the string "id:" into id the following transformation can be applied:
```hocon
{
  type = "insertLiteral"
  field = "id"
  value = "id:"
  offset = 0
  offsetFrom = "left"
}
```

Example input:

| id   | city    |
|------|---------|
|jhon  |Madrid   |
|paul  |Coruña   |
|martha|Bilbao   |
|fred  |Barcelona|

Example output:

| id | city         |
|----|--------------|
|id:jhon  |Madrid   |
|id:paul  |Coruña   |
|id:martha|Bilbao   |
|id:fred  |Barcelona|

<a name="Integrity"></a>
### Integrity (type = integrity)
Compare value of fields with catalog stored in HDFS in csv format. (key, value)

| Attribute | Type   | Optional | Value Domain | Default | Description                                                                                                    |
|-----------|--------|----------|--------------|---------|----------------------------------------------------------------------------------------------------------------|
| type      | String | False    | "integrity"  |         | Transformation type                                                                                            |
| field     | String | False    |              |         | Field name                                                                                                     |
| path      | String | False    |              |         | Path to file with key value format for checking integrity. The path can be any valid path: local or HDFS path. |
| default   | String | False    |              |         | Default value to use when field does not exist in the key value file                                           |

##### Example
In order to replace all invalid cities for 'Invalid' the following transformation can be applied:
```hocon
{
  type = "integrity"
  path = "hdfs://hadoop:9000/tests/flow/csv/masterization_catalog_data.csv"
  default = "Invalid"
  field = "city"
}
```

Example masterization_catalog_data.csv:
```csv
Madrid
Barcelona
Coruña
Bilbao
```
Example input:

| id   | city    |
|------|---------|
|jhon  |Madrid   |
|paul  |         |
|martha|0002     |
|fred  |Barcelona|

Example output:

| id   | city    |
|------|---------|
|jhon  |Madrid   |
|paul  |Invalid  |
|martha|Invalid  |
|fred  |Barcelona|

<a name="LeftPadding"></a> 
### LeftPadding (type = leftpadding)
Previously called ToAlphanumeric(type=toalphanumeric). Cast to String and prepend n times a character. This is useful to standardize columns between different tables. For example, an office in Table A is "0182" (String) and in Table B 182 (Integer). You should apply toalphanumeric masterization in order to cast to String and prepend a zero.


| Attribute     | Type    | Optional | Value Domain  | Default | Description                                                       |
|---------------|---------|----------|---------------|---------|-------------------------------------------------------------------|
| type          | String  | False    | "partialinfo" |         | Transformation type                                               |
| field         | String  | False    |               |         | field name                                                        |
| lengthDest    | Integer | False    |               | 4       | length of the target field.                                       |
| fillCharacter | String  | True     |               | '0'     | character to prepend n times until field get 'lengthDest' length. |


##### Example
In order to unify office number in different tables the following transformation can be applied:
```hocon
{
  field = "bank_office"
  type = "leftpadding"
  lengthDest = 4
  fillCharacter = "0"
}
```

Example input:

| id   | bank_office |
|------|-------------|
|jhon  |1            |
|paul  |32           |
|martha|1            |

Example output:

| id   | bank_office |
|------|-------------|
|jhon  |0001         |
|paul  |0032         |
|martha|0001         |

<a name="Literal"></a>
### Literal (type = literal)
Create or change a field with an specific default value.


| Attribute   | Type   | Optional | Value Domain     | Default | Description                                                      |
|-------------|--------|----------|------------------|---------|------------------------------------------------------------------|
| type        | String | False    | "literal"        |         | Transformation type                                              |
| field       | String | False    |                  |         | field name                                                       |
| default     | String | False    |                  |         | Value that have to be used for literal field in all records.     |
| defaultType | String | True     | @See defaultType |         | If defined the field will be casted to the defaultType type.     |

<a name="Literal_defaultType"></a> 
#### defaultType
Optional value that indicates type to cast. It could be:
  - case "string" => StringType
  - case "int32" | "int" | "integer" => IntegerType
  - case "int64" | "long" => LongType
  - case "double" => DoubleType
  - case "float" => FloatType
  - case "boolean" => BooleanType
  - case "date" => DateType
  - case "int96" | "time_millis" | "timestamp_millis" => TimestampType
  - case "decimal(x,y)" => DecimalType

##### Example
In order to add a new column called *evaluation* with default value 'ok' the following transformation can be applied:
```hocon
{
  field = "evaluation"
  type = "literal"
  default = "ok"
  defaultType = "string"
}
```
Example input:

| id   | city    |
|------|---------|
|jhon  |Madrid   |
|paul  |Invalid  |
|martha|Invalid  |
|fred  |Barcelona|

Example output:

| id   | city    | evaluation |
|------|---------|------------|
|jhon  |Madrid   |ok          |
|paul  |Invalid  |ok          |
|martha|Invalid  |ok          |
|fred  |Barcelona|ok          |

<a name="Mask"></a>
### Mask (type = mask)
#####Deprecated
Apply a a non reversible algorythm of encrypt. 

| Attribute | Type   | Optional | Value Domain | Default                         | Description                |
|-----------|--------|----------|--------------|---------------------------------|----------------------------|
| type      | String | False    | "mask"       |                                 | Transformation type        |
| field     | String | False    |              |                                 | field name                 |
| dataType  | String | True     |              | Datatype in Kirby output schema | Datatype of the new field. |

##### DataType
DataType to Cast. If it's not set take it from schema. We recommend set it.
  - case "string" => StringType
  - case "int32" | "int" | "integer" => IntegerType
  - case "int64" | "long" => LongType
  - case "double" => DoubleType
  - case "float" => FloatType
  - case "boolean" => BooleanType
  - case "date" => DateType
  - case "int96" | "time_millis" | "timestamp_millis" => TimestampType
  - case decimalMatcher(precision, scale) => DecimalType(precision.toInt, scale.toInt)
  - case decimalOnlyPrecisionMatcher(precision) => DecimalType(precision.toInt, 0)


##### Example
In order to mask the field *creditcard* the following transformation can be applied:
```hocon
{
  field = "pan"
  type = "mask"
  dataType = "string"
}
```

Example input:

| id | city  | pan            |
|----|-------|----------------|
|jhon|Madrid |1234567891235678|
|paul|Invalid|6789123567812345|

Example output:

| id | city  | pan |
|----|-------|-----|
|jhon|Madrid |X    |
|paul|Invalid|X    |

<a name="PartialInfo"></a>
### PartialInfo (type = partialinfo)
Stores an extracted field subsequence in a new field.

| Attribute | Type    | Optional | Value Domain  | Default | Description                                             |
|-----------|---------|----------|---------------|---------|---------------------------------------------------------|
| type      | String  | False    | "partialinfo" |         | Transformation type                                     |
| field     | String  | False    |               |         | Field name of new field                                 |
| start     | Integer | False    |               |         | Start position of data extraction. First position is 0. |
| length    | Integer | False    |               |         | Length of substring extraction.                         |
| fieldInfo | String  | False    |               |         | Name of field where data is extracted from.             |


##### Example
In order to extract the age from a fixed width field *text* the following transformation can be applied:
```hocon
{
  field = "age"
  fieldInfo = "text"
  type = "partialinfo"
  start = 8
  length = 3
}
```

Example input:

| id   | text           |
|------|----------------|
|jhon  |' 0034MAD033456'|
|paul  |' 0034MAD054456'|
|martha|' 0000BCN021456'|

Example output:

| id        | text      | age |
|------|----------------|-----|
|jhon  |' 0034MAD033456'|033  |
|paul  |' 0034MAD054456'|054  |
|martha|' 0000BCN021456'|021  |

<a name="Replace"></a>
### Replace (type = replace)
Alter the value in case the key is found in the field. The key value catalog is determined in form of an JSON object.


| Attribute | Type               | Optional | Value Domain | Default | Description                                                     |
|-----------|--------------------|----------|--------------|---------|-----------------------------------------------------------------|
| type      | String             | False    | "replace"    |         | Transformation type                                             |
| field     | String             | False    |              |         | field name                                                      |
| replace   | Map[String,String] | False    |              |         | Object which contains a key value structure for replacing data. |

##### Example
In order to correct a malformed data the following transformation can be applied:
```hocon
{
  field = "text"
  type = "replace"
  replace : {
    "MD" : "MAD"
    "BB" : "BLB"
  }
}
```
Example input:

| id   | text |
|------|------|
|jhon  |MD    |
|paul  |BCN   |
|martha|BB    |

Example output:

| id   | text |
|------|------|
|jhon  |MAD   |
|paul  |MCN   |
|martha|BLB   |

<a name="Tokenizer"></a>
### Tokenizer (type=token)
Mask a field with a reversible algorythm. Configuration of mode of token is always by default.

##### Deprecation notice
This transformation can no longer be used to encrypt a field, but the decryption functionality is still in place. Trying
to use this transformation configured to encrypt, will only produce a warning message.

| Attribute                | Type    | Optional | Value Domain | Default                  | Description                                                                                              |
|--------------------------|---------|----------|--------------|--------------------------|----------------------------------------------------------------------------------------------------------|
| type                     | String  | False    | "token"      |                          | Transformation type                                                                                      |
| field                    | String  | False    |              |                          | field name                                                                                               |
| typeEncrypt              | String  | False    |              |                          | indicates mode of tokenizer.                                                                             |
| mode                     | String  | False    |              |                          | Mode to catch errors when using "typeEncrypt"="pan".                                                     |
| invalidTypeValue         | String  | True     |              | '0000000000000000'       | value to use when pan tokenization catches a type error and mode is "MASK_FAILED"                        |
| invalidTokenizationValue | Integer | True     |              | '9999999999999999'       | value to use when pan tokenization catches a library error and mode is "MASK_FAILED".                    |
| formatDate               | String  | True     |              | 'dd/MM/yyyy'             | used with 'typeEncrypt' = 'date_extended' or 'birth', define the format of the date in SimpleDateFormat. |
| locale                   | String  | True     |              | (System default)         | used with 'typeEncrypt' = 'date_extended' or 'birth', define the locale of the date in SimpleDateFormat. |
| isDecrypt                | String  | True     |              | False                    | flag in order to identify if the column should be encrypted (false) or decrypted (true)                  |
| origin                   | String  | True     |              | Default from environment | Chameleon data origin.                                                                                   |
| contextEndpoint          | String  | True     |              | Default from environment | Chameleon context endpoint.                                                                              |
| encryptionMethod         | String  | True     | "ff1", "aes" | 'ff1'                    | ff1 or aes encryption method.                                                                            |

##### TypeEncrypt
Indicates mode of tokenizer. Possible values are:
 - "pan"
 - "cclient"
 - "mail"
 - "phone"
 - "nif"
 - "alphanumeric"
 - "alphanumeric_extended"
 - "numeric"
 - "numeric_extended"
 - "date_extended"
 - "birth"
There is a special value called "generic" which will encrypt a valid value for any caas configuration with a different value from those previously mentioned.

##### Mode
Mode to catch errors when using "typeEncrypt"="pan". Possible values are:
- FAIL_FAST: default value, transformation fail when pan tokenization fails. Note that if field is null, tokenization won't fail and will return empty string as tokenized value.
- MASK_FAILED: transformation replace the value of field row when pan tokenization fails by:
    - type errors: the field will be replaced by "invalidTypeValue"
    - tokenizer library error: the field will be replaced by "invalidTokenizationValue"

##### Example
In order to decrypt fields the following transformation can be applied:
```hocon
{
  type = "token"
  field = "in_force_pan"
  typeEncrypt = "pan"
  mode = MASK_FAILED
  origin = "BBVA"
  isDecrypt = true
},
{
  type = "token"
  field = "email"
  typeEncrypt = "email"
  origin = "BBVA"
  isDecrypt = true
  encryptionMethod = "aes"
},
{
  type = "token"
  field = "birthday_date"
  typeEncrypt = "date"
  formatDate = "yyyy-MM-dd"
  origin = "BBVA"
  isDecrypt = true
},
{
  type = "token"
  field = "birthday_date"
  typeEncrypt = "dateslash"
  formatDate = "yyyy/MM/dd"
  origin = "BBVA"
  isDecrypt = true
}

```

<a name="Trim"></a>
### Trim (type=trim)
Trim a field removing whitespaces from left, right or both.


| Attribute | Type   | Optional | Value Domain | Default | Description                                                |
|-----------|--------|----------|--------------|---------|------------------------------------------------------------|
| type      | String | False    | "trim"       |         | Transformation type                                        |
| field     | String | False    |              |         | field name                                                 |
| trimType  | String | True     | "both"       |         | indicates mode of trim, would be "left", "right" or "both" |


##### Example
In order to delete the spaces at the end of the field *text* the following transformation can be applied:
```hocon
{
  field = "text"
  type = "trim"
  trimType = "right"
}
```
Example input:

| id   | text        |
|------|-------------|
|jhon  |0034MAD033456|
|paul  |0034MAD054456|
|martha|0000BCN021456|

Example output:

| id   | text        |
|------|-------------|
|jhon  |0034MAD033456|
|paul  |0034MAD054456|
|martha|0000BCN021456|

<a name="UpdateTime"></a>
### Update Time (type=setCurrentDate)
Create or replace a field with current time in TimestampType.

| Attribute | Type   | Optional | Value Domain     | Default | Description         |
|-----------|--------|----------|------------------|---------|---------------------|
| type      | String | False    | "setCurrentDate" |         | Transformation type |
| field     | String | False    |                  |         | field name          |

##### Example
In order to add a new field called *updateTime* with the current time the following transformation can be applied:
```hocon
{
  type : "setCurrentDate"
  field : "updateTime"
}
```
Example input:

| id   | text        |
|------|-------------|
|jhon  |0034MAD033456|
|paul  |0034MAD054456|
|martha|0000BCN021456|

Example output:

| id   | text        | updateTime            |
|------|-------------|-----------------------|
|jhon  |0034MAD033456|2017-10-10 08:49:08.835|
|paul  |0034MAD054456|2017-10-10 08:49:08.835|
|martha|0000BCN021456|2017-10-10 08:49:08.835|

## Row Transformations

<a name="ArithmeticOperation"></a>
### Arithmetic Operation (type=arithmeticoperation)
Create a new field containing the result of a mathematical operation applied to a list of columns or numeric values.


| Attribute         | Type           | Optional | Value Domain          | Default | Description                                                                           |
|-------------------|----------------|----------|-----------------------|---------|---------------------------------------------------------------------------------------|
| type              | String         | False    | "arithmeticoperation" |         | Transformation type                                                                   |
| field             | String         | False    |                       |         | Name of the new column to be created or overwritten with the result of the operation. |
| valuesToOperateOn | List[(string)] | False    |                       |         | List of field names. At least two fields must be added to the list.                   |
| operators         | List[(string)] | False    |                       |         | Operator to apply on the fields listed in valuesToOperateOn.                          |


##### ValuesToOperateOn
Field list or literal numbers which the mathematical operator will be applied to in the same order as in the list.

##### Operators
Operators to be applied. Available operators are +, -, *, /, %, ^. Operators are applied in the order specified, and the final given operator will be applied to all remaining values if the number of operators is not the number of values - 1. If more operators are provided than columns, as many operators as possible will applied, then the remainder will be ignored.

##### Example
Apply operators *, +, and then + on columns balance, amount, and tax percentage 0.10. The result will be stored in the column named new_balance.

```hocon
{
     type: "arithmeticoperation"
     valuesToOperateOn: ["0.10", "amount", "amount", "balance"]
     field: "new_balance"
     operators: ["*", "+"]
}
```

Example input:

| balance | amount |
|---------|--------|
|1000.00  |-180.50 |
|72.86    |-15.30  |

Example output:

| balance | amount | new_balance |
|---------|--------|-------------|
|1000.00  |-180.50 |801.45       |
|72.86    |-15.30  |56.03        |

<a name="Autocast"></a>
### Autocast (type = autocast)
Cast the datatype for all fields with a specific datatype in the output schema to other datatype. 


| Attribute  | Type   | Optional | Value Domain  | Default | Description                                                                                       |
|------------|--------|----------|---------------|---------|---------------------------------------------------------------------------------------------------|
| type       | String | False    | "autocast"    |         | Transformation type                                                                               |
| fromType   | String | False    | @See datatype |         | Field datatType to cast from.                                                                     |
| toType     | String | False    | @See datatype |         | Field datatType to cast to.                                                                       |
| format     | String | True     |               |         | Mandatory if fromType od toType are date. String format of the dateType column should be cast to. |
| exceptions | String | True     |               |         | List of columns which should be excluded from the transformation.                                 |

<a name="Autocast_datatype"></a> 
##### DataType 
DataType to Cast. If it's not set take it from schema. We recommend set it.
  - case "string" => StringType
  - case "int32" | "int" | "integer" => IntegerType
  - case "int64" | "long" => LongType
  - case "double" => DoubleType
  - case "float" => FloatType
  - case "boolean" => BooleanType
  - case "date" => DateType
  - case "int96" | "time_millis" | "timestamp_millis" => TimestampType
  - case decimalMatcher(precision, scale) => DecimalType(precision.toInt, scale.toInt)
  - case decimalOnlyPrecisionMatcher(precision) => DecimalType(precision.toInt, 0)

##### Example
In order to convert all fields with string type to the date format, the following transformation can be applied:
 ```hocon
 {
   type : "autocast"
   fromType: "string"
   toType: "date"
   format: "yyyy-MM-dd"
 }
 ```

##### Example
In order to convert all fields with date format to string type, the *format* attribute is required. The following transformation can be applied:
 ```hocon
 {
   type : "autocast"
   fromType: "date"
   toType: "string"
   format: "yyyy-MM-dd"
 }
 ```

##### Example
In this case the column actualization will not be casted to a string.
 ```hocon
 {
   type : "autocast"
   fromType: "date"
   toType: "string"
   excluded: ["actualization"]
 }
 ```

<a name="CleanNulls"></a>
### CleanNulls (type = cleannulls)
Remove rows when some field in the *primaryKey* attribute has value null. If the *primaryKey* attribute is not specified, the transformation removes all rows that have a null value in one or more fields.


| Attribute  | Type         | Optional | Value Domain | Default | Description                                   |
|------------|--------------|----------|--------------|---------|-----------------------------------------------|
| type       | String       | False    | "cleannulls" |         | Transformation type                           |
| primaryKey | List[String] | False    |              |         | list of columns that conform the primary key. |


##### Example
```hocon
{
  type : "cleannulls"
  primaryKey = ["code"]
}
```

<a name="Conditional"></a>
### Conditional (type = conditional)
The transformation *conditional* provides a manner to output column values row-wisely if some condition is acomplished. The condition must be related to some column.

| Attribute   | Type   | Optional | Value Domain                                                      | Default | Description                                                                    |
|-------------|--------|----------|-------------------------------------------------------------------|---------|--------------------------------------------------------------------------------|
| type        | String | False    | "conditional"                                                     |         | Transformation type                                                            |
| field       | String | False    |                                                                   |         | Field which wants to be created or overwritten if it already exists.           |
| default     | String | False    |                                                                   |         | Default value in case no condition is satisfied and the column is initialized. |
| dataType    | String | True     | string, boolean, integer, date, timestamp, decimal, float, double | string  | Required datatype for field.                                                   |
| expressions | List   | False    |                                                                   |         | Condition expressions.                                                         |

The attribute *expressions* is a list of possible conditions. Each condition can contain the following attributes:

| Attribute | Type   | Optional | Value Domain | Default | Description                                                       |
|-----------|--------|----------|--------------|---------|-------------------------------------------------------------------|
| condition | String | False    |              |         | Condition which needs to be satisfied.                            |
| field     | String | True     |              |         | If condition is satisfied, the value of this field will be taken. |
| value     | String | True     |              |         | If condition is satisfied, this value will be taken.              |

If the list *expressions* is empty, the column is initialized with the value assigned in the attribute *default* in case the attribute *field* contains a new column name. If the column name already exists in the dataframe, nothing is done.

If both attributes *field* and *value* are assigned, the attribute *value* will be dominant.

Given a row, if some column is used in the condition (as a factor, divisor, summand, etc.) and the value is *null*, the condition will normally be false since *null* values cannot be interpreted correctly.

Conditions can be written in the following manner:

| Sign | Description                        |
|------|------------------------------------|
| AND  |Intersection of conditions.         |
| OR   |Union of conditions.                |
| ==   |equal                               |
| =!   |unequal                             |
| >=   |bigger or equal                     |
| =<   |equal or less                       |
| +    |Sum of columns (row-wise)           |
| -    |Difference of columns (row-wise)    |
| *    |Multiplication of columns (row-wise)|
| /    |Division of columns (row-wise)      |


##### Examples

###### Example 1 - Simple value condition
```hocon
{
  type = "conditional"
  field = "C"
  default="default"
  expressions=[
    {
      condition="B=='carlos'"
      field= "A"
    }
  ]
}
```

with inputs:

| A     | B    |
|-------|------|
|enero  |carlos|
|febrero|julio |

Since no datatype is defined the new column *D* will be of type *string*. The output with the given input and configuration will be:

| A     | B    | C     |
|-------|------|-------|
|enero  |carlos|enero  |
|febrero|julio |default|



###### Example 2 - Multi-conditions
```hocon
{
  type = "conditional"
  field = "C"
  default="default"
  expressions=[
  {
    condition="B='carlos'"
    field= "A"
  },
  {
    condition="B='julio'"
    field= "B"
  }
  ]
}
```

with inputs:

| A     | B    |
|-------|------|
|enero  |carlos|
|febrero|julio |

Since no datatype is defined the new column *D* will be of type *string*. The output with the given input and configuration will be:

| A     | B    | C   |
|-------|------|-----|
|enero  |carlos|enero|
|febrero|julio |julio|


###### Example 3 - Intersection conditions
```hocon
  {
    type = "conditional"
    field = "D"
    default="default"
    expressions=[
        {
         condition="B='carlos' AND A='enero'"
         field= "C"
        }
   ]
  }
```

with inputs:

| A     | B    | C  |
|-------|------|----|
|enero  |carlos|1997|
|febrero|carlos|1998|
|febrero|julio |2000|

Since no datatype is defined the new column *D* will be of type *string*. The output with the given input and configuration will be:

| A     | B    | C  | D     |
|-------|------|----|-------|
|enero  |carlos|1997|1997   |
|febrero|carlos|1998|default|
|febrero|julio |2000|default|


###### Example 4 - Union conditions
```hocon
{
  type = "conditional"
  field = "D"
  default="default"
  expressions=[
    {
      condition="B='carlos' OR A='enero'"
      field= "C"
    }
  ]
}
```

with inputs:

| A     | B    | C  |
|-------|------|----|
|enero  |carlos|1997|
|febrero|carlos|1998|
|febrero|julio |2000|

Since no datatype is defined the new column *D* will be of type *string*. The output with the given input and configuration will be:

| A     | B    | C  | D     |
|-------|------|----|-------|
|enero  |carlos|1997|1997   |
|febrero|carlos|1998|1997   |
|febrero|julio |2000|default|


###### Example 5 - Column summation
```hocon
  {
    type = "conditional"
    field = "D"
    default=false
    dataType="boolean"
    expressions=[
        {
         condition="A+B+C==0"
         value=true
         field= ""
        }
   ]
  }
```

with inputs:

| A | B | C |
|---|---|---|
|1  |2  |-3 |
|2  |4  |-6 |
|2  |8  |-6 |

Since the datatype is *boolean* the new column *D* will be of type *boolean*. Both attributes *field* and *value* are assigned, so that the attribute *value* will be dominant. The output with the given input and configuration will be:

| A | B | C | D   |
|---|---|---|-----|
|1  |2  |-3 |true |
|2  |4  |-6 |true |
|2  |8  |-6 |false|

###### Example 6 - Column summation
```hocon
  {
    type = "conditional"
    field = "D"
    default=0
    dataType="integer"
    expressions=[
        {
         condition="A+B+C==0"
         value=1
        }
   ]
  }
```

with inputs:

| A | B | C |
|---|---|---|
|1  |2  |-3 |
|2  |4  |-6 |
|2  |8  |-6 |

Since the datatype is *integer* the new column *D* will be of type *integer*. The output with the given input and configuration will be:

| A | B | C | D |
|---|---|---|---|
|1  |2  |-3 |1  |
|2  |4  |-6 |1  |
|2  |8  |-6 |0  |

###### Example 7 - Column multiplication
```hocon
  {
    type = "conditional"
    field = "D"
    default=0
    dataType="integer"
    expressions=[
        {
         condition="A*B*C==20"
         value=1
        }
   ]
  }
```

with inputs:

| A | B | C |
|---|---|---|
|2  |2  |5  |
|10 |4  |1  |
|2  |8  |0  |

Since the datatype is *integer* the new column *D* will be of type *integer*. The output with the given input and configuration will be:

| A | B | C | D |
|---|---|---|---|
|2  |2  |5  |1  |
|10 |4  |1  |1  |
|2  |8  |0  |0  |



###### Example 8 - Column division
```hocon
  {
    type = "conditional"
    field = "C"
    default=0
    dataType="integer"
    expressions=[
        {
         condition="A/B==1"
         value=1
        }
   ]
  }
```

with inputs:

| A | B |
|---|---|
|2  |2  |
|4  |4  |
|2  |8  |

Since the datatype is *integer* the new column *D* will be of type *integer*. The output with the given input and configuration will be:

| A | B | C |
|---|---|---|
|2  |2  |1  |
|4  |4  |1  |
|2  |8  |0  |

###### Example 9 - Date inequality
```hocon
  {
    type = "conditional"
    field = "B"
    default=false
    dataType=boolean
    expressions=[
        {
         condition=" A > '2017-07-17'"
         value=true
        }
   ]
  }
```

with inputs:

| A        |
|----------|
|07-17-2018|
|07-17-2019|
|07-17-2016|
|07-17-2015|



| A        | B   |
|----------|-----|
|07-17-2018|true |
|07-17-2019|true |
|07-17-2016|false|
|07-17-2015|false|


###### Example 10 - Timestamp inequality
```hocon
  {
    type = "conditional"
    field = "B"
    default=false
    dataType=boolean
    expressions=[
        {
         condition=" A >= '2013-01-01 11:01:11'"
         value=true
        }
   ]
  }
```

with inputs:

| A                 |
|-------------------|
|2013-01-01 11:01:11|
|2013-02-02 11:01:22|
|1999-01-01 11:01:33|


| A                 | B   |
|-------------------|-----|
|2013-01-01 11:01:11|true |
|2013-02-02 11:01:22|true |
|1999-01-01 11:01:33|false|




<a name="DropColumns"></a>
### DropColumns (type = dropcolumns)
Remove fields of a dataframe.

| Attribute     | Type         | Optional | Value Domain  | Default | Description                                   |
|---------------|--------------|----------|---------------|---------|-----------------------------------------------|
| type          | String       | False    | "dropcolumns" |         | Transformation type                           |
| columnsToDrop | List[String] | False    |               |         | list of column names that have to be dropped. |

##### Example
```hocon
{
  type : "dropcolumns"
  columnsToDrop: ["column1", "column2"]
}
```

##### Example
```hocon
{
  type : "dropcolumns"
  columnsToDrop: ["A.A.A"]
}
```

If the column A.A does not have any other substructure beside A, then A.A will be deleted as well, since no data is left for this column. This logic will be applied recursively: If A has no other substructure then A.A, then it will be deleted as well.



<a name="ConcatColumns"></a>
### ConcatColumns (type = concatcolumns)
Concat fields of a dataframe.


| Attribute       | Type   | Optional | Value Domain    | Default | Description                                                                                                                                 |
|-----------------|--------|----------|-----------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------|
| type            | String | False    | "concatcolumns" |         | Transformation type                                                                                                                         |
| columnsToConcat | String | False    |                 |         | list of column names to concat.                                                                                                             |
| columnName      | String | False    |                 |         | Name to set to the concatenated column.                                                                                                     |
| separator       | String | False    |                 |         | Separator to set between concatenated columns (optional, if not set, defaults to empty string).                                             |
| convertNulls    | String | True     |                 |         | Convert null values to the given string before concatenating (optional, if not set, any null will cause the whole concatenation to be null) |


##### Example
```hocon
{
  type : "concatcolumns"
  columnsToConcat : ["first", "middle", "last"]
  columnName : "fullName"
  separator : " "
  convertNulls : ""
}
```

Example input:

| first | last    | middle |
|-------|---------|--------|
|Emilio |Guillot  |null    |
|Carlos |Gutierrez|Santiago|
|John   |Guton    |Samuel  |

Example output:

| first | last    | middle | fullName                 |
|-------|---------|--------|--------------------------|
|Emilio |Guillot  |null    |Emilio Guillot            |
|Carlos |Gutierrez|Santiago|Carlos Santiago Gutierrez |
|John   |Guton    |Samuel  |John Samuel Guton         |

<a name="DropDuplicates"></a>
### DropDuplicates (type = dropDuplicates)
Given the subsequence of the selected fields determined in the *primaryKey* attribute, this transformation will delete all rows that share the same values in the subsequence set. Just the first row in the dataframe encountered with those values will be left, thereby deleting just duplicates.

| Attribute  | Type         | Optional | Value Domain     | Default    | Description                                  |
|------------|--------------|----------|------------------|------------|----------------------------------------------|
| type       | String       | False    | "dropDuplicates" |            | Transformation type                          |
| primaryKey | List[String] | True     |                  | all fields | list of fields that conform the primary key. |

##### Example
```hocon
{
  type : "dropDuplicates"
  primaryKey = ["name"]
}
```

Example input:


| name  | city   |
|-------|--------|
|Antonio| Madrid |
|Paul   | Paris  |
|Jesus  | Cordoba|
|Antonio| Sevilla|
|Andres | Bogota |
|Jesus  | Cordoba|

Example output:

| name  | city  |
|-------|-------|
|Antonio|Madrid |
|Paul   |Paris  |
|Jesus  |Cordoba|
|Andres |Bogota |

##### Example
If the *primaryKey* attribute is not specified, just repeated rows are dropped.
```hocon
{
  type : "dropDuplicates"
}
```

Example input:

| name  | city  |
|-------|-------|
|Antonio|Madrid |
|Paul   |Paris  |
|Jesus  |Cordoba|
|Antonio|Sevilla|
|Andres |Bogota |
|Jesus  |Cordoba|

Example output:

| name  | city  |
|-------|-------|
|Antonio|Madrid |
|Paul   |Paris  |
|Jesus  |Cordoba|
|Antonio|Sevilla|
|Andres |Bogota |

<a name="Filter"></a>
### Filter (type = filterByField)
Filter by criteria in an specific field.

| Attribute | Type   | Optional | Value Domain    | Default | Description                                                                                             |
|-----------|--------|----------|-----------------|---------|---------------------------------------------------------------------------------------------------------|
| type      | String | False    | "filterByField" |         | Transformation type                                                                                     |
| filters   | Array  | False    |                 |         | Array containing a list of filters. A single filter is an object composed by 'field', 'value' and 'op'. |
| field     | String | False    |                 |         | Column name to be filtered.                                                                             |
| op        | String | True     |                 |         | Comparator operations                                                                                   |
| value     | String | True     |                 |         | Column value of parameter field for being filtered.                                                     |
| logicOp   | String | False    | 'and', 'or'     | and'    | Logic operator for filter combinations.                                                                 |


##### op
Comparator operations:
  - eq (equals)
  - neq (not equals)
  - lt (less than)
  - leq (less or equals than)
  - gt (greater than)
  - geq (greater or equals than)
  - like (sql like)
  - rlike (regex like)

##### Example
```hocon
{
  type : "filterByField"
  logicOp : or
  filters: [{
    value : "Antonio"
    field : "name"
    op : "eq"
  },
  {
    value : "Samuel"
    field : "name"
    op : "eq"
  }]
}
```

Example input:

| name  | weight |
|-------|--------|
|Antonio|12      |
|Samuel |1       |
|Maria  |1       |
|Alvaro |2       |
|Antonio|3       |


Example output:

| name  | weight |
|-------|--------|
|Antonio|12      |
|Samuel |1       |
|Antonio|3       |


<a name="InitNulls"></a>
### InitNulls (type = initnulls)
Initialize fields with null value to default value.


| Attribute | Type            | Optional | Value Domain | Default | Description         |
|-----------|-----------------|----------|--------------|---------|---------------------|
| type      | String          | False    | "initnulls"  |         | Transformation type |
| fields    | List of Strings | False    |              |         | field name          |
| default   | String          | True     |              |         |                     |


##### Default
The default value to use when one of the fields is null. In case the field column type is date or timestamp, the required default value format to follow is yyyy-MM-dd HH:mm:ss. If the default parameter is not specified, the corresponding value defined in the schema will be taken.

##### Example
In order to replace all null values of a field for '0000' the following transformation can be applied:
```hocon
{
  fields = ["group","account"]
  type = "initNulls"
  default = "0000"
}
```
Example input:

| id   | group | account |
|------|-------|---------|
|jhon  |0001   |0001     |
|paul  |null   |0001     |
|empty |       |         |
|martha|0002   |         |
|fred  |0003   |null     |

Example output:

| id   | group | account |
|------|-------|---------|
|jhon  |0001   |0001     |
|paul  |0000   |0001     |
|empty |       |         |
|martha|0002   |         |
|fred  |0003   |0000     |


<a name="Join"></a>
### Join (type=join)
A JOIN is used to combine rows from two or more data frames, based on a related column between them.

Ambiguous columns: rename or joins operations could produce columns with same name. Despite being supported by spark, it is a source of failure and is not supported by the output formats we use. In order to solve this ambiguity, Kirby provides aliases to the subtables, which can only be used in the join transformation and work in a similar way to SQL aliases. The main dataframe will internally use the alias 'self'. As previously mentioned, the transformation output prohibits repeated columns. In order to avoid this issue, the columns can be renamed in the SELECT statment using the reserved word AS.

Example (ambiguity on select):
join tables: self[x,y,z], other[x,p,h], join by [x=x]
- select [x] ← ambiguous | solution → select [self.x]
- select [self.x,other.x] ← ambiguous | solution → select [self.x, other.x as other_x]

Example (ambiguity on joinColumns):
join tables: self[x,y], other[x,p], another[x,h], self-other join by [x=x], then join by [x=x] ← ambiguous | solution →join tables: self[x,y], other[x,p], another[x,h], self-other join by [x=x], then join by [self.x=x]

*USING JOIN WITH STRUCT TYPES* Currently, the join transformation only supports dataframes with flattened structures; leveraging nested data will result in a dataframe with flattened structures as well, thereby losing the original structure.


| Attribute            | Type          | Optional | Value Domain                                                                                           | Default | Description                                                                                                                                                                                                                                                   |   |
|----------------------|---------------|----------|--------------------------------------------------------------------------------------------------------|---------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|---|
| type                 | String        | False    | "join"                                                                                                 |         | Transformation type                                                                                                                                                                                                                                           |   |
| joins                | String        | False    |                                                                                                        |         | Element list, each element defining a join operation with attributes 'input', 'alias', 'joinType', 'joincolumns'.                                                                                                                                             |   |
| input                | String        | False    |                                                                                                        |         | Data frame input to join with the main dataframe. This object follows the same rules than a standard kirby input element (documentation: 1. Input v1.0.x). Constant 'self' can replace this object, with this way we can define a data frame join with itself |   |
| alias                | String        | False    |                                                                                                        |         | Name to identify the dataframe fields in 'select' or in following joins                                                                                                                                                                                       |   |
| joinType             | String        | False    | 'inner', 'left','left_outer', 'right','right_outer', 'outer','full','fullouter', 'leftsemi', 'leftant' |         | Join type to perform.                                                                                                                                                                                                                                         |   |
| joinColumns          | List          | False    |                                                                                                        |         | list of objects containing 'self' and 'other' elements that represents the columns related values in the two tables.                                                                                                                                          |   |
| transformations      | List          | True     |                                                                                                        | Seq()   | list of objects containing transformations to apply to the 'other' table before apply the join.                                                                                                                                                               |   |
| self                 | String        | False    |                                                                                                        |         | Contained in joinColumns. Name of the column of the main data frame (or a previous join) to perform the join                                                                                                                                                  |   |
| other                | String        | False    |                                                                                                        |         | Contained in joinColumns. Name of the column of the input dataframe                                                                                                                                                                                           |   |
| select               | Array[String] | True     |                                                                                                        | '*'     | List of columns to output from the join, can use '[alias].' to get all columns from a join data frame and '[column] as [renameColumn]' to rename columns. Default ''                                                                                          |   |
| resolveConflictsAuto | Boolean       | True     |                                                                                                        | False   | Autoresolve possible conflicts of ambiguity on 'select'. The output conflict column will be called '[alias]_[column]'. Default 'false'.                                                                                                                       |   |

With a data frame of users lets get their transactions using another data frame of credit_cards to combine.

```hocon
{
      joins = [
        {
          input {
            type = "parquet"
            paths = [
              "/data/master/uuaa/data/cards/"
            ]
            schema = {}
          }
          alias = "creditCard"
          joinType = "inner"
          joinColumns = [
            {
              self = "user_id"
              other = "owner"
            }
          ]
          transformations = [
            {
              type = "filter"
              filters: [{
                field = "expiry_date"
                value = "2010-01-01"
                op = "neq"
              }]
            }
          ]
        }
        ,
        {
          input {
            type = "parquet"
            paths = [
              "/data/master/uuaa/data/transactions/"
            ]
            schema = {}
          }
          alias = transaction
          joinType = "inner"
          joinColumns = [
            {
              self = "creditCard.id"
              other = "credit_card_id"
            }
          ]
        }
      ]
      select = ["user_id", "name", "creditCard.number", "transaction.amount", "transaction.message", "transaction.timestamp"]
      type = "join"
    }
```
Example input:

| user_id | name |
|---------|------|
|1        |juan  |
|2        |ruben |

credit_cards:

| id | owner | numnber        | expiry_date |
|----|-------|----------------|-------------|
|1   |1      |0000111122223333|2020-12-31   |
|2   |3      |1111000044443333|2020-12-31   |

transactions:

| id | credit_card_id | ammount | message       | timestamp         |
|----|----------------|---------|---------------|-------------------|
|1   |1               |-100     |Amazon         |2017-09-06 11:08:52|
|2   |3               |-12      |Shopp          |2017-10-06 10:08:52|
|3   |3               |-400     |Other concepts |2017-10-06 10:28:52|
|4   |1               |-65      |Coffe          |2017-11-06 10:08:52|

Example output:

| user_id | name | creditCard.number | transaction.amount | transaction.message | transaction.timestamp |
|---------|------|-------------------|--------------------|---------------------|-----------------------|
|1        |juan  |0000111122223333   |-100                |Amazon               |2017-09-06 11:08:52    |
|1        |juan  |0000111122223333   |-65                 |Coffe                |2017-11-06 10:08:52    |

<a name="RenameColumns"></a>
### RenameColumns (type = renamecolumns)
Rename fields in the data frame.

**Warning:** keep in mind that by using this transformation it's possible to generate columns with the same name as existing ones. In that cases expect to see later an exception with a message like "Reference 'A' is ambiguous, could be: A#680, A#681."

| Attribute       | Type               | Optional | Value Domain    | Default | Description                                                            |
|-----------------|--------------------|----------|-----------------|---------|------------------------------------------------------------------------|
| type            | String             | False    | "renamecolumns" |         | Transformation type                                                    |
| columnsToRename | Map[String,String] | False    |                 |         | object of key value that contains all columns to rename, and new name. |

##### Example
```hocon
{
  type : "renamecolumns"
  columnsToRename : {
    field_1 : "field1Renamed",
    field_2 : "field2Renamed"
  }
}
```

##### Nested columns example
```hocon
{
  type : "renamecolumns"
  columnsToRename : {
    A.A.A : "B.A",
  }
}
```

If the column A.A does not have any other substructure beside A, then A.A will be deleted as well, since no data is left for this column. This logic will be applied recursively: If A has no other substructure then A.A, then it will be deleted as well.


<a name="SelectColumns (type = selectcolumns)"></a>
### SelectColumns (type = selectcolumns)
Select fields of the dataframe.

| Attribute       | Type         | Optional | Value Domain    | Default | Description                                   |
|-----------------|--------------|----------|-----------------|---------|-----------------------------------------------|
| type            | String       | False    | "selectcolumns" |         | Transformation type                           |
| columnsToSelect | List[String] | False    |                 |         | list of columns name that have to be selected |

##### Example
select columns 'name' and 'phone' from table
```hocon
{
  type : "selectcolumns"
  columnsToSelect : ["name", "phone"]
}
```

Example input:

| user_id | name | phone    | createdTs | updateTs  |
|---------|------|----------|-----------|-----------|
|1        |juan  |678678678 |2016-01-01 |           |
|2        |ruben |689689680 |2016-12-31 |2017-11-10 |

Example output:

| name | phone   |
|------|---------|
|juan  |678678678|
|ruben |689689680|

<a name="SqlFilter"></a>
### Sql Filter (type=sqlfilter)
Filters rows using a given SQL expression.

| Attribute | Type   | Optional | Value Domain | Default | Description           |
|-----------|--------|----------|--------------|---------|-----------------------|
| type      | String | False    | "sqlfilter"  |         | Transformation type   |
| filter    | String | False    |              |         | sql Filter expression |

##### Example
Get the users created or updated between a dates

```hocon
{
  type = "sqlFilter"
  filter = "createdTS BETWEEN '2017-01-01' AND '2017-01-31' OR updatedTS BETWEEN '2017-01-01' AND '2017-01-31'"
}
```
Example input:

| user_id | name | phone   | createdTs | updateTs |
|---------|------|---------|-----------|----------|
|1        |juan  |678678678|2016-01-01 |          |
|2        |ruben |689689680|2016-12-31 |2017-11-10|

Example output:

| user_id | name | phone   | createdTs | updateTs  |
|---------|------|---------|-----------|-----------|
|2        |ruben |689689680|2016-12-31 |2017-11-10 |

<a name="RegexColumn"></a>
### RegexColumn (type=regexcolumn)
Return or overwrite a field after applying a regex pattern in an origin field.

| Attribute     | Type               | Optional | Value Domain  | Default | Description                                                                                                               |
|---------------|--------------------|----------|---------------|---------|---------------------------------------------------------------------------------------------------------------------------|
| type          | String             | False    | "regexcolumn" |         | Transformation type                                                                                                       |
| columnToRegex | String             | False    |               |         | column in which regex pattern will be applied.                                                                            |
| regexPattern  | String             | False    |               |         | regex sequence.                                                                                                           |
| regexList     | Seq[(int, String)] | False    |               |         | sequence of objects containing 'regexGroup' and 'field' elements that represents the group of regex and the final column. |
| regexGroup    | Integer            | True     |               |         | group defined in regex pattern                                                                                            |
| field         | String             | True     |               |         | name of the column to create or overwrite the result of regex.                                                            |

##### Example
Apply regex pattern -([0-9]+)- with no group from field *date*. The result will be stored in the fields named *year*,*month* and *day*.

```hocon
{
  type : "regexcolumn"
  regexPattern: "-([0-9]+)-"
  columnToRegex: "date"
  regex : [
      {
        regexGroup: 0
        field: "year"
      },
      {
        regexGroup: 1
        field: "month"
      },
      {
        regexGroup: 2
        field: "day"
      }
  ]
}
```

Example input:

| user | name | date     |
|------|------|----------|
|1     |juan  |2016-01-01|
|2     |ruben |2016-12-31|

Example output:

| user | name | date     | year| month | day|
|------|------|----------|-----|-------|----|
|1     |juan  |2016-01-01|2016 |01     |01  |
|2     |ruben |2016-12-31|2016 |12     |31  |

<a name="OffsetColumn"></a>
### Offset (Lead or Lag) Column (type = offsetcolumn)
Lags or leads a field using windowing.

| Attribute       | Type    | Optional | Value Domain   | Default                            | Description                                                                 |
|-----------------|---------|----------|----------------|------------------------------------|-----------------------------------------------------------------------------|
| type            | String  | False    | "offsetcolumn" |                                    | Transformation type                                                         |
| offsetType      | String  | False    |                |                                    | lead, lag (required)"                                                       |
| columnToLag     | String  | False    |                |                                    | source column to offset (will not be dropped; required)"                    |
| columnToOrderBy | String  | False    |                |                                    | which column the source column is ordered by (e.g. a date field; required)" |
| offset          | String  | True     |                | 1                                  | integer offset                                                              |
| newColumnName   | Integer | True     |                | {columnToLag}_{offsetType}{offset} | target column name                                                          |

##### Example: Lag a column with default offset and default target column name

This example illustrates the fact that the offset attribute ensures the vertical downside offset (*lag*) of the given field with default value 1. Furthermore, the created field will have a determined default name (*{columnToLag}_{offsetType}{offset}*).

```hocon
config
{
   type = "offsetcolumn"
   offsetType = "lag"
   columnToLag = "int_field"
   columnToOrderBy = "date_field"
}
```
Example input:

| date_field | int_field |
|------------|-----------|
|20180901    |40.01      |
|20180902    |121.79     |
|20180903    |34.53      |

Example output:

| date_field | int_field | int_field_lag1 |
|------------|-----------|----------------|
|20180901    |40.01      |0.0             |
|20180902    |121.79     |40.01           |
|20180903    |34.53      |121.79          |

##### Example: Lead a column with custom offset and target column name

This example illustrates the fact that the offset attribute ensures the vertical upside offset (*lead*) of the given field. Furthermore, the created field can be given a name regardless of default settings determining the attribute *newColumnName*.

```hocon
config
{
   type = "offsetcolumn"
   offsetType = "lead"
   columnToLag = "int_field"
   columnToOrderBy = "date_field"
   offset = 2
   newColumnName = "lagged_field"
}
```
Example input:

| date_field | int_field |
|------------|-----------|
|20180901    |40.01      |
|20180902    |121.79     |
|20180903    |34.53      |

Example output:

| date_field | int_field | lagged_field |
|------------|-----------|--------------|
|20180901    |40.01      |34.53         |
|20180902    |121.79     |0.0           |
|20180903    |34.53      |0.0           |



<a name="RegexCaseColumn"></a>
### RegexCaseColumn (type=regexcasecolumn)
Return or overwrite a field with specified literal values if the regex pattern matches the specified field.

| Attribute     | Type   | Optional | Value Domain      | Default | Description                                                                                                                                                |
|---------------|--------|----------|-------------------|---------|------------------------------------------------------------------------------------------------------------------------------------------------------------|
| type          | String | False    | "regexcasecolumn" |         | Transformation type                                                                                                                                        |
| field         | String | False    |                   |         | Name of the field to create or overwrite with the literal values.                                                                                          |
| default       | String | False    |                   |         | Literal value to put in place if no regex matches (this option does not matter when overwriting, the default is the previous column value)                 |
| regexList     | String | False    |                   |         | List of regex pattern -> value sets and the columns they operate on, each must contain columnToRegex, pattern, and value. Priority goes from first to last |
| columnToRegex | String | True     |                   |         | Column in which the regex pattern will be searched for.                                                                                                    |
| pattern       | String | True     |                   |         | Regex pattern to search for within columnToRegex.                                                                                                          |
| value         | String | True     |                   |         | Value to place into field if pattern is found within columnToRegex.                                                                                        |


##### Example:
Apply regex pattern [0-9]+ to column username, and the pattern test to column username. If they are found, store the corresponding literal in userType otherwise, userType will be the default.

```hocon
{
     type : "regexcasecolumn"
     field: "user_type"
     default: "basic"
     regexList: [
       {
         columnToRegex: username
         pattern: [0-9]+
         value: "multi"
       },
       {
         columnToRegex: username
         pattern: test
         value: "test"
       }
     ]
}
```

Example input:

| username         |
|------------------|
|google_test       |
|datio_admin_test_1|
|datio_admin_11    |
|george            |

Example output:

| username         | user_type |
|------------------|-----------|
|google_test       |test       |
|datio_admin_test_1|multi      |
|datio_admin_11    |multi      |
|george            |basic      |

##### Example
In order to create a new field *column3* with the 3 different regex expressions, the following transformation can be applied:

```hocon
{
  type = "regexcasecolumn"
  field = "column3"
  default = "String"
  regexList = [
    {
      pattern = "[!|@|#|%|^|&|*|(|)|~]+"
      columnToRegex = "column1"
      value = "Special Chars"
    }
    {
      pattern = "[!|@|#|%|^|&|*|(|)|~]+"
      columnToRegex = "column2"
      value = "Special Chars"
    }
    {
      pattern = "[0-9]+"
      columnToRegex = "column1"
      value = "Numerical"
    }
 ]
}
```

Example input:

| column1 | column2 |
|---------|---------|
|aaa      |aaa!     |
|999      |abaaa!   |
|1258     |arrr     |
|adb!@%   |atat     |
|aaaeaaa  |taaataaa |

Example output:

| column1 | column2 | column3     |
|---------|---------|-------------|
|aaa      |aaa!     |Special Chars|
|999      |abaaa!   |Special Chars|
|1258     |arrr     |Numerical    |
|adb!@%   |atat     |Special Chars|
|aaaeaaa  |taaataaa |String       |

##### Example
In order to override an existing field *column2* with the regex expression *[Hello|World]+*, where whose values without match will be maintained, the following transformation can be applied:

```hocon
{
  type = "regexcasecolumn"
  field = "column2"
  default = "This should not Matter"
  regexList = [
    {
      pattern = "[Hello|World]+"
      columnToRegex = "column1"
      value = "Greetings!"
    }
  ]
}
```


Example input:

| column1   | column2 |
|-----------|---------|
|aaa        |aaa!     |
|Hello World|abaaa!   |
|1258       |arrr     |
|Hello      |atat     |
|World      |taaataaa |

Example output:

| column1   | column2  |
|-----------|----------|
|aaa        |aaa!      |
|Hello World|Greetings!|
|1258       |arrr      |
|Hello      |Greetings!|
|World      |Greetings!|

<a name="PreserveOrder"></a>
### PreserveOrder (type=preserveorder)
Return the dataframe sort in the same order as the columns of validation schema (output schema).
For using this transformation properly, this transformation should be the last one in the list of transformations application.conf
Keep in mind that this transformation make a select with the columns found in validation schema. If there are other columns, they will be removed in the final dataframe.

| Attribute                  | Type                                | Optional | Value Domain                                                   | Default            | Description                                                                                                                                                  |
| :--------------------------|:-----------------------------------:| :------: |:--------------------------------------------------------------:|:------------------:|:-------------------------------------------------------------------------------------------------------------------------------------------------------------|
| type                       | String                              | False    | "preserveorder"                                              |                    | Transformation type                                                                                                                                          |

##### Example:

```hocon
{
     type : "preserveorder"
}
```


<a name="Checkpoint"></a>
### Checkpoint (type=checkpoint)
Creates a checkpointing of the dataframe. It is useful, for instance, for processing nested data which computation time is long.

IMPORTANT: Checkpoint's files wont be removed after execution.

| Attribute                  | Type                                | Optional | Value Domain | Default            | Description              |
| :--------------------------|:-----------------------------------:| :------: |:------------:|:------------------:|:-------------------------|
| type                       | String                              | False    | "checkpoint" |                    | Transformation type      |
| path                       | String                              | False    |              |                    | Path where checkpointing |

##### Example:

```hocon
{
     type : "checkpoint"
     path : "/path/where/checkpointing"
}
```
<!-- ENDPARSE -->
