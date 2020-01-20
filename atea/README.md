# ATEA

ATEA stands for Abbreviated Text Expansion Algorithm. ATEA parses text, finds where abbreviations are used and predicts what they stand for.

## Set Up

[Setup database steps here]

## Usage
### Create an Atea object.
`Atea atea = new Atea("host_address", "username", "password");`

### Training ATEA

The more ATEA is trained, the better the predictions it makes. ATEA is trained by feeding it examples of abbreviations being used in text along with what they stand for. You can also train ATEA on examples of when a word isn't being used as an abbreviation. For example, the word "it" is sometimes used as a word (It is sunny today.), and is sometimes used as an abbreviation for Information Technology. By showing ATEA examples of both cases, it will better be able to predict when "it" is being used to stand for Information Technology.

To help you find abbreviations (or words like "it" that may not be an abbreviation) to train ATEA with you can use the `findPotentialAbbreviations` method.

`ArrayList<Abbreviation> potentialAbbrs = atea.findPotentialAbbreviations(myInputString);`

You can now loop through the ArrayList and ask  the user to verify if each item is an abbreviation and if so, what it stands for.

Abbreviation object useful public methods during training:  
`getValue()` - Returns the abbreviation.  
`getText()` - Returns the text the abbreviation was found in.  
`getExpansions()` - Returns an ArrayList of Expansion objects, sorted from the most likely (highest confidence score) to least likely expansion.

Expansion object useful public methods during trainging:  
`getValue()` - Returns the value of the expansion.  
`getConfidence()` - Returns the confidence score (from 0-1) as a Double.

Once you have an example to show ATEA, use the `addExample` method.

`atea.addExample(abbr, expansion);`

# Documentation

## Atea Class
### Constructor
`Atea(String host, String username, String password)`

|Parameter|Description|
|:---|:---|
|String host|Database host|
|String username|Database username|
|String password|Database password|

### addExample(Abbreviation, Expansion)
Adds an example of an abbreviation being used to the database.

|Parameter|Description|
|:---|:---|
|Abbreviation|An Abbreviation object|
|Expansion|An Expansion object|

#### Returns
|Type|Description|
|:---|:---|
|boolean|True on success, False on failure

### expand(String)
Returns the String with the most likely expansion for each abbreviation substituted for the abbreviation.

|Parameter|Description|
|:---|:---|
|String|The String to look for abbreviations in|

#### Returns
|Type|Description|
|:---|:---|
|String|The expanded String|

### explain(String)
Returns the String with the most likely expansion for each abbreviation put in parenthesis next to the abbreviation.

|Parameter|Description|
|:---|:---|
|String|The String to look for abbreviations in|

#### Returns
|Type|Description|
|:---|:---|
|String|The explained String|

### findPotentialAbbreviations(String)
Looks in the String for any word that may be an abbreviation. That is, any word whose characters match an abbreviation found in the abbreviations table in the database. This method makes no predicts on whether or not these words are actually abbreviations. It only returns a list of potential abbreviations.

|Parameter|Description|
|:---|:---|
|String|The String to look for abbreviations in|

#### Returns
|Type|Description|
|:---|:---|
|ArrayList\<Abbrivation\>|A list of Abbreviation objects for potential abbreviations found in the String|

### predictAbbreviations(String)
Looks in the String for abbreviations, predicting which words are abbreviations and what they stand for.

|Parameter|Description|
|:---|:---|
|String|The String to look for abbreviations in|

#### Returns
|Type|Description|
|:---|:---|
|ArrayList\<Abbrivation\>|A list of Abbreviation objects for abbreviations found in the String|