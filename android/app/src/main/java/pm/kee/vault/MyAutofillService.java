/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pm.kee.vault;

import android.app.assist.AssistStructure;
import android.content.Context;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.os.CancellationSignal;
import android.service.autofill.AutofillService;
import android.service.autofill.Dataset;
import android.service.autofill.FillCallback;
import android.service.autofill.FillContext;
import android.service.autofill.FillRequest;
import android.service.autofill.FillResponse;
import android.service.autofill.SaveCallback;
import android.service.autofill.SaveInfo;
import android.service.autofill.SaveRequest;
import android.support.annotation.NonNull;
import android.view.autofill.AutofillId;
import android.view.autofill.AutofillValue;
import android.widget.RemoteViews;

import java.util.HashMap;
import java.util.List;

import pm.kee.vault.model.DalCheck;
import pm.kee.vault.model.DalInfo;
import pm.kee.vault.util.Util;

import static java.util.stream.Collectors.toList;
import static pm.kee.vault.util.Util.bundleToString;
import static pm.kee.vault.util.Util.dumpStructure;
import static pm.kee.vault.util.Util.logVerboseEnabled;
import static pm.kee.vault.util.Util.logd;
import static pm.kee.vault.util.Util.loge;
import static pm.kee.vault.util.Util.logv;
import static pm.kee.vault.util.Util.logw;

public class MyAutofillService extends AutofillService {

    @Override
    public void onFillRequest(FillRequest request, CancellationSignal cancellationSignal, FillCallback callback) {
        // Get the structure from the request
        List<FillContext> context = request.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        // Traverse the structure looking for nodes to fill out.
//        ParsedStructure parsedStructure = new ParsedStructure ();
//        parsedStructure.usernameId = AutofillId.forText();
//        , passwordId: "sd"};
//
//        // Fetch user data that matches the fields.
//        UserData userData = fetchUserData(parsedStructure);
        UserData user1Data = new UserData();
        user1Data.username = "user1";
        user1Data.password = "pass1";
        UserData user2Data = new UserData();
        user2Data.username = "user";
        user2Data.password = "pass2";

        // Build the presentation of the datasets
        RemoteViews username1Presentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        username1Presentation.setTextViewText(android.R.id.text1, "my_username 1");
        RemoteViews password1Presentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        password1Presentation.setTextViewText(android.R.id.text1, "Password for my_username 1");
        RemoteViews username2Presentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        username2Presentation.setTextViewText(android.R.id.text1, "my_username 2");
        RemoteViews password2Presentation = new RemoteViews(getPackageName(), android.R.layout.simple_list_item_1);
        password2Presentation.setTextViewText(android.R.id.text1, "Password for my_username 2");

        // Add multiple datasets to the response
        FillResponse fillResponse = new FillResponse.Builder()
                .addDataset(new Dataset.Builder()
                        .setValue(parsedStructure.usernameId,
                                AutofillValue.forText(user1Data.username), username1Presentation)
                        .setValue(parsedStructure.passwordId,
                                AutofillValue.forText(user1Data.password), password1Presentation)
                        .build())
                .addDataset(new Dataset.Builder()
                        .setValue("usernameId",
                                AutofillValue.forText(user2Data.username), username2Presentation)
                        .setValue(parsedStructure.passwordId,
                                AutofillValue.forText(user2Data.password), password2Presentation)
                        .build())
                .setSaveInfo(new SaveInfo.Builder(
                        SaveInfo.SAVE_DATA_TYPE_USERNAME | SaveInfo.SAVE_DATA_TYPE_PASSWORD,
                        new AutofillId[] {parsedStructure.usernameId, parsedStructure.passwordId})
                        .build())

                .build();


        // If there are no errors, call onSuccess() and pass the response
        callback.onSuccess(fillResponse);
    }

    @Override
    public void onSaveRequest(SaveRequest request, SaveCallback callback) {
        // Get the structure from the request
        List<FillContext> context = request.getFillContexts();
        AssistStructure structure = context.get(context.size() - 1).getStructure();

        // Traverse the structure looking for data to save
        traverseStructure(structure);

        // Persist the data, if there are no errors, call onSuccess()
        callback.onSuccess();
    }

    class ParsedStructure {
        AutofillId usernameId;
        AutofillId passwordId;
    }

    class UserData {
        String username;
        String password;
    }


    public void traverseStructure(AssistStructure structure) {
        int nodes = structure.getWindowNodeCount();

        for (int i = 0; i < nodes; i++) {
            AssistStructure.WindowNode windowNode = structure.getWindowNodeAt(i);
            AssistStructure.ViewNode viewNode = windowNode.getRootViewNode();
            traverseNode(viewNode);
        }
    }

    public void traverseNode(AssistStructure.ViewNode viewNode) {
        if(viewNode.getAutofillHints() != null && viewNode.getAutofillHints().length > 0) {
            // If the client app provides autofill hints, you can obtain them using:
            //viewNode.getAutofillHints();
            viewNode.getAutofillId();
        } else {
            // Or use your own heuristics to describe the contents of a view
            // using methods such as getText() or getHint().
        }

        for(int i = 0; i < viewNode.getChildCount(); i++) {
            AssistStructure.ViewNode childNode = viewNode.getChildAt(i);
            traverseNode(childNode);
        }
    }


//
//    private LocalAutofillDataSource mLocalAutofillDataSource;
//    private DigitalAssetLinksRepository mDalRepository;
//    private PackageVerificationDataSource mPackageVerificationRepository;
//    private AutofillDataBuilder mAutofillDataBuilder;
//    private ResponseAdapter mResponseAdapter;
//    private ClientViewMetadata mClientViewMetadata;
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//    }
//
//    @Override
//    public void onFillRequest(@NonNull FillRequest request,
//            @NonNull CancellationSignal cancellationSignal, @NonNull FillCallback callback) {
//        List<FillContext> fillContexts = request.getFillContexts();
//        List<AssistStructure> structures =
//                fillContexts.stream().map(FillContext::getStructure).collect(toList());
//        AssistStructure latestStructure = fillContexts.get(fillContexts.size() - 1).getStructure();
//        ClientParser parser = new ClientParser(structures);
//
//        boolean responseAuth = true;
//        boolean datasetAuth = true;
//        boolean manual = (request.getFlags() & FillRequest.FLAG_MANUAL_REQUEST) != 0;
//        mLocalAutofillDataSource.getFieldTypeByAutofillHints(
//                new DataCallback<HashMap<String, FieldTypeWithHeuristics>>() {
//                    @Override
//                    public void onLoaded(HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint) {
//                        DatasetAdapter datasetAdapter = new DatasetAdapter(parser);
//                        ClientViewMetadataBuilder clientViewMetadataBuilder =
//                                new ClientViewMetadataBuilder(parser, fieldTypesByAutofillHint);
//                        mClientViewMetadata = clientViewMetadataBuilder.buildClientViewMetadata();
//                        mResponseAdapter = new ResponseAdapter(MyAutofillService.this,
//                                mClientViewMetadata, getPackageName(), datasetAdapter);
//                        String packageName = latestStructure.getActivityComponent().getPackageName();
//                        if (!mPackageVerificationRepository.putPackageSignatures(packageName)) {
//                            callback.onFailure(getString(R.string.invalid_package_signature));
//                            return;
//                        }
//                        if (logVerboseEnabled()) {
//                            logv("onFillRequest(): clientState=%s",
//                                    bundleToString(request.getClientState()));
//                            dumpStructure(latestStructure);
//                        }
//                        cancellationSignal.setOnCancelListener(() ->
//                                logw("Cancel autofill not implemented in this sample.")
//                        );
//                        fetchDataAndGenerateResponse(fieldTypesByAutofillHint, responseAuth,
//                                datasetAuth, manual, callback);
//                    }
//
//                    @Override
//                    public void onDataNotAvailable(String msg, Object... params) {
//
//                    }
//                });
//    }
//
//    private void fetchDataAndGenerateResponse(
//            HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint, boolean responseAuth,
//            boolean datasetAuth, boolean manual, FillCallback callback) {
//        if (responseAuth) {
//            // If the entire Autofill Response is authenticated, AuthActivity is used
//            // to generate Response.
//            IntentSender sender = AuthActivity.getAuthIntentSenderForResponse(this);
//            RemoteViews remoteViews = RemoteViewsHelper.viewsWithAuth(getPackageName(),
//                    getString(R.string.autofill_sign_in_prompt));
//            FillResponse response = mResponseAdapter.buildResponse(sender, remoteViews);
//            if (response != null) {
//                callback.onSuccess(response);
//            }
//        } else {
//            mLocalAutofillDataSource.getAutofillDatasets(mClientViewMetadata.getAllHints(),
//                    new DataCallback<List<DatasetWithFilledAutofillFields>>() {
//                        @Override
//                        public void onLoaded(List<DatasetWithFilledAutofillFields> datasets) {
//                            if ((datasets == null || datasets.isEmpty()) && manual) {
//                                IntentSender sender = ManualActivity
//                                        .getManualIntentSenderForResponse(MyAutofillService.this);
//                                RemoteViews remoteViews = RemoteViewsHelper.viewsWithNoAuth(
//                                        getPackageName(),
//                                        getString(R.string.autofill_manual_prompt));
//                                FillResponse response = mResponseAdapter.buildManualResponse(sender,
//                                        remoteViews);
//                                if (response != null) {
//                                    callback.onSuccess(response);
//                                }
//                            } else {
//                                FillResponse response = mResponseAdapter.buildResponse(
//                                        fieldTypesByAutofillHint, datasets, datasetAuth);
//                                callback.onSuccess(response);
//                            }
//                        }
//
//                        @Override
//                        public void onDataNotAvailable(String msg, Object... params) {
//                            logw(msg, params);
//                            callback.onFailure(String.format(msg, params));
//                        }
//                    });
//        }
//    }
//
//    @Override
//    public void onSaveRequest(@NonNull SaveRequest request, @NonNull SaveCallback callback) {
//        List<FillContext> fillContexts = request.getFillContexts();
//        List<AssistStructure> structures =
//                fillContexts.stream().map(FillContext::getStructure).collect(toList());
//        AssistStructure latestStructure = fillContexts.get(fillContexts.size() - 1).getStructure();
//        ClientParser parser = new ClientParser(structures);
//        mLocalAutofillDataSource.getFieldTypeByAutofillHints(
//                new DataCallback<HashMap<String, FieldTypeWithHeuristics>>() {
//                    @Override
//                    public void onLoaded(
//                            HashMap<String, FieldTypeWithHeuristics> fieldTypesByAutofillHint) {
//                        mAutofillDataBuilder = new ClientAutofillDataBuilder(
//                                fieldTypesByAutofillHint, getPackageName(), parser);
//                        ClientViewMetadataBuilder clientViewMetadataBuilder =
//                                new ClientViewMetadataBuilder(parser, fieldTypesByAutofillHint);
//                        mClientViewMetadata = clientViewMetadataBuilder.buildClientViewMetadata();
//                        String packageName = latestStructure.getActivityComponent().getPackageName();
//                        if (!mPackageVerificationRepository.putPackageSignatures(packageName)) {
//                            callback.onFailure(getString(R.string.invalid_package_signature));
//                            return;
//                        }
//                        if (logVerboseEnabled()) {
//                            logv("onSaveRequest(): clientState=%s",
//                                    bundleToString(request.getClientState()));
//                        }
//                        dumpStructure(latestStructure);
//                        checkWebDomainAndBuildAutofillData(packageName, callback);
//                    }
//
//                    @Override
//                    public void onDataNotAvailable(String msg, Object... params) {
//                        loge("Should not happen - could not find field types.");
//                    }
//                });
//    }
//
//    private void checkWebDomainAndBuildAutofillData(String packageName, SaveCallback callback) {
//        String webDomain;
//        try {
//            webDomain = mClientViewMetadata.getWebDomain();
//        } catch (SecurityException e) {
//            logw(e.getMessage());
//            callback.onFailure(getString(R.string.security_exception));
//            return;
//        }
//        if (webDomain != null && webDomain.length() > 0) {
//            Util.DalCheckRequirement req = Util.DalCheckRequirement.AllUrls;
//            mDalRepository.checkValid(req, new DalInfo(webDomain, packageName),
//                    new DataCallback<DalCheck>() {
//                        @Override
//                        public void onLoaded(DalCheck dalCheck) {
//                            if (dalCheck.linked) {
//                                logd("Domain %s is valid for %s", webDomain, packageName);
//                                buildAndSaveAutofillData();
//                            } else {
//                                loge("Could not associate web domain %s with app %s",
//                                        webDomain, packageName);
//                                callback.onFailure(getString(R.string.dal_exception));
//                            }
//                        }
//
//                        @Override
//                        public void onDataNotAvailable(String msg, Object... params) {
//                            logw(msg, params);
//                            callback.onFailure(getString(R.string.dal_exception));
//                        }
//                    });
//        } else {
//            logd("no web domain");
//            buildAndSaveAutofillData();
//        }
//    }
//
//    private void buildAndSaveAutofillData() {
//        int datasetNumber = mLocalAutofillDataSource.getDatasetNumber();
//        List<DatasetWithFilledAutofillFields> datasetsWithFilledAutofillFields =
//                mAutofillDataBuilder.buildDatasetsByPartition(datasetNumber);
//        mLocalAutofillDataSource.saveAutofillDatasets(datasetsWithFilledAutofillFields);
//    }

    @Override
    public void onConnected() {
        logd("onConnected");
    }

    @Override
    public void onDisconnected() {
        logd("onDisconnected");
    }
}
