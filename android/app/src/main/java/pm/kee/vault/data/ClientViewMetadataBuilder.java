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

package pm.kee.vault.data;


import android.app.assist.AssistStructure;
import android.util.MutableInt;
import android.view.autofill.AutofillId;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import pm.kee.vault.ClientParser;
import pm.kee.vault.model.FieldTypeWithHints;

import static pm.kee.vault.util.Util.logd;

public class ClientViewMetadataBuilder {
    private ClientParser mClientParser;
    private HashMap<String, FieldTypeWithHints> mFieldTypesByAutofillHint;

    public ClientViewMetadataBuilder(ClientParser parser,
            HashMap<String, FieldTypeWithHints> fieldTypesByAutofillHint) {
        mClientParser = parser;
        mFieldTypesByAutofillHint = fieldTypesByAutofillHint;
    }

    public ClientViewMetadata buildClientViewMetadata() {
        List<String> allHints = new ArrayList<>();
        MutableInt saveType = new MutableInt(0);
        List<AutofillId> autofillIds = new ArrayList<>();
        StringBuilder webDomainBuilder = new StringBuilder();
        List<AutofillId> focusedAutofillIds = new ArrayList<>();
        mClientParser.parse((node) -> parseNode(node, allHints, saveType, autofillIds, focusedAutofillIds));
        mClientParser.parse((node) -> parseWebDomain(node, webDomainBuilder));
        String webDomain = webDomainBuilder.toString();
        AutofillId[] autofillIdsArray = autofillIds.toArray(new AutofillId[autofillIds.size()]);
        AutofillId[] focusedIds = focusedAutofillIds.toArray(new AutofillId[focusedAutofillIds.size()]);
        return new ClientViewMetadata(allHints, saveType.value, autofillIdsArray, focusedIds, webDomain);
    }

    private void parseWebDomain(AssistStructure.ViewNode viewNode, StringBuilder validWebDomain) {
        String webDomain = viewNode.getWebDomain();
        if (webDomain != null) {
            logd("child web domain: %s", webDomain);
            if (validWebDomain.length() > 0) {
                if (!webDomain.equals(validWebDomain.toString())) {
                    throw new SecurityException("Found multiple web domains: valid= "
                            + validWebDomain + ", child=" + webDomain);
                }
            } else {
                validWebDomain.append(webDomain);
            }
        }
    }

    private void parseNode(AssistStructure.ViewNode root, List<String> allHints,
            MutableInt autofillSaveType, List<AutofillId> autofillIds,
            List<AutofillId> focusedAutofillIds) {
        String[] hints = root.getAutofillHints();
        if (hints != null) {
            for (String hint : hints) {
                FieldTypeWithHints fieldTypeWithHints = mFieldTypesByAutofillHint.get(hint);
                if (fieldTypeWithHints != null && fieldTypeWithHints.fieldType != null) {
                    allHints.add(hint);
                    autofillSaveType.value |= fieldTypeWithHints.fieldType.getSaveInfo();
                    autofillIds.add(root.getAutofillId());
                }
            }
        }
        if (root.isFocused()) {
            focusedAutofillIds.add(root.getAutofillId());
        }
/*
        getHtmlInfo().getAttributes() // id, class, type, etc
        getImportantForAutofill() // ignore all View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS and View.IMPORTANT_FOR_AUTOFILL_NO

        getInputType() //

username/text:
TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_EMAIL_ADDRESS
TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_NORMAL
TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_WEB_EDIT_TEXT
TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_WEB_EMAIL_ADDRESS

password:
TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_PASSWORD
TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_WEB_PASSWORD

OTP?:
TYPE_CLASS_NUMBER | TYPE_NUMBER_VARIATION_NORMAL
TYPE_CLASS_NUMBER | TYPE_NUMBER_VARIATION_PASSWORD

TYPE_NULL (unknown)

post-MVP maybe want to implement a blacklist:
TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_MULTI_LINE
TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_IME_MULTI_LINE
TYPE_CLASS_TEXT | TYPE_TEXT_FLAG_AUTO_COMPLETE
TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_FILTER
TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_EMAIL_SUBJECT
TYPE_CLASS_TEXT | TYPE_TEXT_VARIATION_POSTAL_ADDRESS

        getMaxTextLength() // integer
        getVisibility() // VISIBLE
        getWebScheme() // Pie only

*/
    }
}
