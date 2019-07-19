package pm.kee.vault.data.source.local.dao;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import pm.kee.vault.MainActivity;
import pm.kee.vault.model.AutofillDataset;
import pm.kee.vault.model.AutofillHint;
import pm.kee.vault.model.DatasetWithFilledAutofillFields;
import pm.kee.vault.model.FieldType;
import pm.kee.vault.model.FieldTypeWithHints;
import pm.kee.vault.model.FilledAutofillField;

public class capDao {
    /**
     * Fetches a list of datasets associated to autofill fields on the page.
     *
     * @param allAutofillHints Filtering parameter; represents all of the hints associated with
     *                         all of the views on the page.
     */
    public List<DatasetWithFilledAutofillFields> getDatasets(String url) {
        return getAllDatasets();
    }

    public List<DatasetWithFilledAutofillFields> getAllDatasets() {

        MainActivity.getPlugin();

        return Arrays.asList(new DatasetWithFilledAutofillFields() {{
            filledAutofillFields = Arrays.asList(
                    new FilledAutofillField("1", "username", "my username 1"),
                    new FilledAutofillField("1", "password", "my password 1"));
            autofillDataset = new AutofillDataset("1", "2", "3");
        }},
                new DatasetWithFilledAutofillFields() {{
                    filledAutofillFields = Arrays.asList(
                            new FilledAutofillField("2", "username", "my username 2"),
                            new FilledAutofillField("2", "password", "my password 2"));
                    autofillDataset = new AutofillDataset("2", "4", "5");
                }}
                );
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
        return getAllDatasets();
    }

    public List<FieldTypeWithHints> getFieldTypesWithHints() {
        List<FieldTypeWithHints> list = Arrays.asList(
                new FieldTypeWithHints() {{
                    fieldType = new FieldType("password", Arrays.asList(1), 1, 0);
                            autofillHints = Arrays.asList(new AutofillHint("password", "password"),
                                    new AutofillHint("current-password", "password"));
                }},
                new FieldTypeWithHints() {{
                    fieldType = new FieldType("username", Arrays.asList(1), 1, 0);
                    autofillHints = Arrays.asList(new AutofillHint("username", "username"),
                            new AutofillHint("user", "username"));
                }}, //TODO: probably want to match on email hints too and get rid of below type?
                new FieldTypeWithHints() {{
                    fieldType = new FieldType("emailAddress", Arrays.asList(1,3), 16, 2);
                    autofillHints = Arrays.asList(new AutofillHint("emailAddress", "emailAddress"),
                            new AutofillHint("email", "emailAddress"));
                }}
        );
        return list;
        //return null;
    }

    public List<FieldTypeWithHints> getFieldTypesForAutofillHints(List<String> autofillHints) {
        return getFieldTypesWithHints().stream()
                .filter(f -> f.autofillHints.stream().anyMatch(h1 -> autofillHints.stream().anyMatch(h2 -> h2 == h1.mFieldTypeName)))
                        .collect(Collectors.toList());
    }

    public DatasetWithFilledAutofillFields getAutofillDatasetWithId(String datasetId) {
        return null;
    }

    public FilledAutofillField getFilledAutofillField(String datasetId, String fieldTypeName) {
        return null;
    }

    public FieldType getFieldType(String fieldTypeName) {
        //TODO: Maybe this should be mapped to existing field types in KPRPC?
        switch (fieldTypeName) {
            case "emailAddress": return new FieldType(fieldTypeName, Arrays.asList(1,3), 16, 2);
            case "password": return new FieldType(fieldTypeName, Arrays.asList(1), 1, 0);
            case "name": return new FieldType(fieldTypeName, Arrays.asList(1,3), 0, 0);
            case "new-password": return new FieldType(fieldTypeName, Arrays.asList(1), 1, 0);
            case "username": return new FieldType(fieldTypeName, Arrays.asList(1,3), 8, 0);
        }
        return null;
    }

    /**
     * @param autofillFields Collection of autofill fields to be saved to the db.
     */
    public void insertFilledAutofillFields(Collection<FilledAutofillField> autofillFields) {

    }

    public void insertAutofillDataset(AutofillDataset datasets) {

    }

//    public void insertAutofillHints(List<AutofillHint> autofillHints) {
//
//    }
//
//    public void insertResourceIdHeuristic(ResourceIdHeuristic resourceIdHeuristic) {
//
//    }
//
//    public void insertFieldTypes(List<FieldType> fieldTypes) {
//
//    }

    public void clearAll() {

    }
}