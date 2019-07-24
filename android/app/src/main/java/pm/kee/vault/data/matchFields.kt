package pm.kee.vault.data

import pm.kee.vault.model.DatasetWithFilledAutofillFields
import pm.kee.vault.model.vault.Entry

data class keeLoginField (

    // "name" attribute on the HTML form element
    val name: String?,

    // "value" attribute on the HTML form element
    val value: String?,

    // "id" attribute on the HTML form element
    val fieldId: String?,

    // The HTML form element DOM objects - transient (not sent to KeePass)
    //DOMInputElement: HTMLInputElement;
    //DOMSelectElement: HTMLSelectElement;

    // "type" attribute on the HTML form element
    val type: String, // "password" | "text" | "radio" | "select-one" | "checkbox";

    //val formFieldPage: number;

    // Best score for any potential entry that is being considered for selection - transient (not sent to KeePass)
    val highestScore: Int? // maybe use for sorting list of datasets?
)

// This is maybe just the same as a dataset? so where used, just create a new DataSet instead?
data class FilledField {
    id: string;
    autofillid: ...
    name: string;
    value: string;
}


fun matchFields(matchedEntries: List<Entry>, clientViewMetadata: ClientViewMetadata): List<DatasetWithFilledAutofillFields> {
    return null
}









// TODO: convert formField: keeLoginField into clientField: ClientField

fun calculateFieldMatchScore(formField: keeLoginField, dataField: keeLoginField, isVisible: Boolean): Int
{
    // Default score is 1 so that bad matches which are at least the correct type
    // have a chance of being selected if no good matches are found
    var score = 1;

    // Do not allow any match if field types are significantly mismatched (e.g. checkbox vs text field)
    if ( !( formUtils.isATextFormFieldType(formField.type) && (dataField.type == "username" || dataField.type == "text") )
            && !(formField.type == "password" && dataField.type == "password")
            && !(formField.type == "radio" && dataField.type == "radio")
            && !(formField.type == "checkbox" && dataField.type == "checkbox")
            && !(formField.type == "select-one" && dataField.type == "select-one")
    )
        return 0

    // If field IDs match +++++
    if (formField.fieldId != null && formField.fieldId != undefined
            && formField.fieldId != "" && formField.fieldId == dataField.fieldId
    ) {
        score += 50;
    } else if (dataField.fieldId == null) {
        score -= 5;
    }

    // If field names match ++++
    // (We do not treat ID and NAME as mutually exclusive because some badly written
    // websites might have duplicate IDs but different names so this combined approach
    // might allow them to work correctly)
    if (formField.name != null && formField.name != undefined
            && formField.name != "" && formField.name == dataField.name
    ) {
        score += 40;
    } else if (dataField.name) {
        score -= 5;
    }

    // Radio buttons have their values set by the website and hence can provide
    // a useful cue when both id and name matching fails
    if (formField.type == "radio" && formField.value != null && formField.value != undefined
            && formField.value != "" && formField.value == dataField.value
    )
        score += 30;

    if (isVisible === undefined && this.formUtils.isDOMElementVisible(formField.DOMInputElement || formField.DOMSelectElement))
        isVisible = true;

    score += isVisible ? 35 : 0;

    return score;
}

fun fillMatchedFields (fieldScoreMatrix, dataFields, formFields, automated: Boolean)
{
    // We want to make sure each data field is matched to only one form field but we
    // don't know which field will be the best match and we don't want to ignore
    // less accurate matches just because they happen to appear later.

    // We have a matrix of objects representing each possible combination of data field
    // and form field and the score for that match.
    // We choose what to fill by sorting that list by score.
    // After filling a field we remove all objects from the list which are for the
    // data field we just filled in and the form field we filled in.

    // This means we always fill each form field only once, with the best match
    // selected from all data fields that haven't already been selected for another form field

    // The above algorithm could maybe be tweaked slightly in order to auto-fill
    // a "change password" form if we ever manage to make that automated

    // (score is reduced by one for each position we find in the form - this gives
    // a slight priority to fields at the top of a form which can be useful occasionally)

    fieldScoreMatrix.sort(function (a, b) {
        return b.score - a.score;
    });

    // Remember what we've filled in so we can make more accurate decisions when
    // the form is submitted later. We resist the urge the index by element ID or
    // the DOMelement itself because some websites do not specify an ID and some
    // may remove the DOMelement before we submit the form (sometimes under user
    // direction but occasionally automatically too)
    val filledFields: FilledField[] = [];

    // Keep filling in fields until we find no more with a positive score
    while (fieldScoreMatrix.length > 0 && fieldScoreMatrix[0].score > 0)
    {
        const ffi = fieldScoreMatrix[0].formFieldIndex;
        const dfi = fieldScoreMatrix[0].dataFieldIndex;
        let DOMelement;

        if (formFields[ffi].type === "select-one")
            DOMelement = formFields[ffi].DOMSelectElement;
        else
            DOMelement = formFields[ffi].DOMInputElement;

        const currentValue = this.getFormFieldCurrentValue(DOMelement, formFields[ffi].type);

        if (automated && currentValue && currentValue !== DOMelement.keeInitialDetectedValue) {
            this.Logger.info("Not filling field because it's not empty and was edited by user since last load/fill");
        } else {
            this.Logger.info("We will populate field " + ffi + " (id:" + formFields[ffi].fieldId + ")");
            this.fillASingleField(DOMelement, formFields[ffi].type, dataFields[dfi].value);
        }

        filledFields.push({
            id: formFields[ffi].fieldId,
            DOMelement: DOMelement,
            name: formFields[ffi].name,
            value: dataFields[dfi].value
        });

        fieldScoreMatrix = fieldScoreMatrix.filter(function (element, index, array) {
            return (element.dataFieldIndex != dfi && element.formFieldIndex != ffi);
        });

        fieldScoreMatrix.sort(function (a, b) {
            return b.score - a.score;
        });
    }
    return filledFields;
}




fun fillManyFormFields (formFields, dataFields, currentPage, scoreConfig: FieldMatchScoreConfig, automated: boolean)
{

    if (formFields == null || formFields == undefined || dataFields == null || dataFields == undefined)
        return;


    // we try to fill every form field. We try to match by id first and then name before just guessing.
    // Generally we'll only fill if the matched field is of the same type as the form field but
    // we are flexible RE text and username fields because that's an artificial difference
    // for the sake of the Kee password management software. However, usernames will be chosen above
    // text fields if all else is equal
    const fieldScoreMatrix = [];

    for (let i = 0; i < formFields.length; i++)
    {
        for (let j = 0; j < dataFields.length; j++)
        {
            const score = this.calculateFieldMatchScore(
                formFields[i], dataFields[j], currentPage, scoreConfig);
            this.Logger.debug("Suitability of putting data field "+j+" into form field "+i
                    +" (id: "+formFields[i].fieldId + ") is " + score);
            fieldScoreMatrix.push({score: score, dataFieldIndex: j, formFieldIndex: i});
        }
    }

    return this.fillMatchedFields(fieldScoreMatrix, dataFields, formFields, automated);
}