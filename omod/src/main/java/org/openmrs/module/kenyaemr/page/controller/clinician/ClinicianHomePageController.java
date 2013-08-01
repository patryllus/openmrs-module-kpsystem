/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.kenyaemr.page.controller.clinician;

import org.openmrs.module.kenyaemr.EmrConstants;
import org.openmrs.module.kenyaui.annotation.AppPage;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Homepage for the clinician app
 */
@AppPage(EmrConstants.APP_CLINICIAN)
public class ClinicianHomePageController {

	public String controller(UiUtils ui, @RequestParam(required=false, value="patientId") Integer patientId) {

		if (patientId != null) {
			return "redirect:" + ui.pageLink(EmrConstants.MODULE_ID, "clinician/clinicianViewPatient", SimpleObject.create("patientId", patientId));
		} else {
			return null;
		}
	}
}