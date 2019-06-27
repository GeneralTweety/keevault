package pm.kee.vault.data.source.local.dao;

import java.util.Collection;
import java.util.List;

import pm.kee.vault.model.AutofillDataset;
import pm.kee.vault.model.AutofillHint;
import pm.kee.vault.model.DatasetWithFilledAutofillFields;
import pm.kee.vault.model.FieldType;
import pm.kee.vault.model.FieldTypeWithHeuristics;
import pm.kee.vault.model.FilledAutofillField;
import pm.kee.vault.model.ResourceIdHeuristic;

public class capDao {
    /**
     * Fetches a list of datasets associated to autofill fields on the page.
     *
     * @param allAutofillHints Filtering parameter; represents all of the hints associated with
     *                         all of the views on the page.
     */
    public List<DatasetWithFilledAutofillFields> getDatasets(List<String> allAutofillHints) {
        return null;
    }

    public List<DatasetWithFilledAutofillFields> getAllDatasets() {
        return null;
    }

    /**
     * Fetches a list of datasets associated to autofill fields. It should only return a dataset
     * if that dataset has an autofill field associate with the view the user is focused on, and
     * if that dataset's name matches the name passed in.
     *
     * @param fieldTypes  Filtering parameter; represents all of the field types associated with
     *                    all of the views on the page.
     * @param datasetName Filtering parameter; only return datasets with this name.
     */
    public List<DatasetWithFilledAutofillFields> getDatasetsWithName(
            List<String> fieldTypes, String datasetName) {
        return null;
    }

    public List<FieldTypeWithHeuristics> getFieldTypesWithHints() {
        return null;
    }

    public List<FieldTypeWithHeuristics> getFieldTypesForAutofillHints(List<String> autofillHints) {
        return null;
    }

    public DatasetWithFilledAutofillFields getAutofillDatasetWithId(String datasetId) {
        return null;
    }

    public FilledAutofillField getFilledAutofillField(String datasetId, String fieldTypeName) {
        return null;
    }

    public FieldType getFieldType(String fieldTypeName) {
        return null;
    }

    /**
     * @param autofillFields Collection of autofill fields to be saved to the db.
     */
    public void insertFilledAutofillFields(Collection<FilledAutofillField> autofillFields) {

    }

    public void insertAutofillDataset(AutofillDataset datasets) {

    }

    public void insertAutofillHints(List<AutofillHint> autofillHints) {

    }

    public void insertResourceIdHeuristic(ResourceIdHeuristic resourceIdHeuristic) {

    }

    public void insertFieldTypes(List<FieldType> fieldTypes) {

    }

    public void clearAll() {

    }
}