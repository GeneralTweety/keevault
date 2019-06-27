/*
 * Copyright (C) 2018 The Android Open Source Project
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

package pm.kee.vault.model;

import java.util.List;

public class FieldType {
    private final String mTypeName;

    private final List<Integer> mAutofillTypes;

    private final Integer mSaveInfo;

    private final Integer mPartition;
//
//    private final FakeData mFakeData;
//
//    public FieldType(String typeName, IntList autofillTypes,
//            Integer saveInfo, Integer partition, FakeData fakeData) {
//        mTypeName = typeName;
//        mAutofillTypes = autofillTypes;
//        mSaveInfo = saveInfo;
//        mPartition = partition;
//        mFakeData = fakeData;
//    }

    public FieldType(String typeName, List<Integer> autofillTypes,
            Integer saveInfo, Integer partition) {
        mTypeName = typeName;
        mAutofillTypes = autofillTypes;
        mSaveInfo = saveInfo;
        mPartition = partition;
    }

    public String getTypeName() {
        return mTypeName;
    }

    public List<Integer> getAutofillTypes() {
        return mAutofillTypes;
    }

    public Integer getSaveInfo() {
        return mSaveInfo;
    }

    public Integer getPartition() {
        return mPartition;
    }

//    public FakeData getFakeData() {
//        return mFakeData;
//    }
}