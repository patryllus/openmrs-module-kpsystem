/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.kenyaemr.reporting.builder.hts;

import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttributeType;
import org.openmrs.module.kenyacore.report.ReportDescriptor;
import org.openmrs.module.kenyacore.report.ReportUtils;
import org.openmrs.module.kenyacore.report.builder.AbstractReportBuilder;
import org.openmrs.module.kenyacore.report.builder.Builds;
import org.openmrs.module.kenyaemr.metadata.CommonMetadata;
import org.openmrs.module.kenyaemr.metadata.KpMetadata;
import org.openmrs.module.kenyaemr.reporting.cohort.definition.HTSRegisterCohortDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.EverTestedForHIVDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.FinalResultDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.FinalResultGivenDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.HIVTestOneDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.HIVTestTwoDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.HTSDiscordanceDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.HTSMonthsSinceLastTestDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.HTSProviderDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.HTSRemarksDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.HTSSelfTestDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.HTSTBScreeningDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.HTSTestStrategyDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.IndividualORCoupleTestDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.KenyaEMRMaritalStatusDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.PatientConsentDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.PatientDisabilityDataDefinition;
import org.openmrs.module.kenyaemr.reporting.data.converter.definition.PopulationTypeDataDefinition;
import org.openmrs.module.metadatadeploy.MetadataUtils;
import org.openmrs.module.reporting.common.SortCriteria;
import org.openmrs.module.reporting.data.DataDefinition;
import org.openmrs.module.reporting.data.converter.BirthdateConverter;
import org.openmrs.module.reporting.data.converter.DataConverter;
import org.openmrs.module.reporting.data.converter.DateConverter;
import org.openmrs.module.reporting.data.converter.ObjectFormatter;
import org.openmrs.module.reporting.data.encounter.definition.EncounterDatetimeDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.ConvertedPatientDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdDataDefinition;
import org.openmrs.module.reporting.data.patient.definition.PatientIdentifierDataDefinition;
import org.openmrs.module.reporting.data.person.definition.AgeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.BirthdateDataDefinition;
import org.openmrs.module.reporting.data.person.definition.ConvertedPersonDataDefinition;
import org.openmrs.module.reporting.data.person.definition.GenderDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PersonAttributeDataDefinition;
import org.openmrs.module.reporting.data.person.definition.PreferredNameDataDefinition;
import org.openmrs.module.reporting.dataset.definition.DataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.EncounterDataSetDefinition;
import org.openmrs.module.reporting.evaluation.parameter.Mapped;
import org.openmrs.module.reporting.evaluation.parameter.Parameter;
import org.openmrs.module.reporting.report.definition.ReportDefinition;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Component
@Builds({"kenyaemr.hiv.report.htsRegister"})
public class HTSRegisterReportBuilder extends AbstractReportBuilder {
    public static final String ENC_DATE_FORMAT = "yyyy/MM/dd";
    public static final String DATE_FORMAT = "dd/MM/yyyy";

    @Override
    protected List<Parameter> getParameters(ReportDescriptor reportDescriptor) {
        return Arrays.asList(
                new Parameter("startDate", "Start Date", Date.class),
                new Parameter("endDate", "End Date", Date.class),
                new Parameter("dateBasedReporting", "", String.class)
        );
    }

    @Override
    protected List<Mapped<DataSetDefinition>> buildDataSets(ReportDescriptor reportDescriptor, ReportDefinition reportDefinition) {
        return Arrays.asList(
                ReportUtils.map(datasetColumns(), "startDate=${startDate},endDate=${endDate}")
        );
    }

    protected DataSetDefinition datasetColumns() {
        EncounterDataSetDefinition dsd = new EncounterDataSetDefinition();
        dsd.setName("HTSInformation");
        dsd.setDescription("Visit information");
        dsd.addSortCriteria("Visit Date", SortCriteria.SortDirection.ASC);
        dsd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        dsd.addParameter(new Parameter("endDate", "End Date", Date.class));

        String paramMapping = "startDate=${startDate},endDate=${endDate}";

        DataConverter nameFormatter = new ObjectFormatter("{familyName}, {givenName} {middleName}");
        DataDefinition nameDef = new ConvertedPersonDataDefinition("name", new PreferredNameDataDefinition(), nameFormatter);
        PatientIdentifierType upn = MetadataUtils.existing(PatientIdentifierType.class, KpMetadata._PatientIdentifierType.UNIQUE_PATIENT_NUMBER);
        DataConverter identifierFormatter = new ObjectFormatter("{identifier}");
        DataDefinition identifierDef = new ConvertedPatientDataDefinition("identifier", new PatientIdentifierDataDefinition(upn.getName(), upn), identifierFormatter);

        PersonAttributeType phoneNumber = MetadataUtils.existing(PersonAttributeType.class, CommonMetadata._PersonAttributeType.TELEPHONE_CONTACT);

        dsd.addColumn("Name", nameDef, "");
        dsd.addColumn("id", new PatientIdDataDefinition(), "");
        dsd.addColumn("Date of Birth", new BirthdateDataDefinition(), "", new BirthdateConverter(DATE_FORMAT));
        dsd.addColumn("Age", new AgeDataDefinition(), "");
        dsd.addColumn("Sex", new GenderDataDefinition(), "");
        dsd.addColumn("Telephone No", new PersonAttributeDataDefinition(phoneNumber), "");
        dsd.addColumn("Marital Status", new KenyaEMRMaritalStatusDataDefinition(), null);
        dsd.addColumn("Unique Patient Number", identifierDef, null);

        dsd.addColumn("Visit Date", new EncounterDatetimeDataDefinition(),"", new DateConverter(ENC_DATE_FORMAT));
        // new columns
        dsd.addColumn("Population Type", new PopulationTypeDataDefinition(), null);
        dsd.addColumn("everTested", new EverTestedForHIVDataDefinition(), null);
        dsd.addColumn("disability", new PatientDisabilityDataDefinition(), null);
        dsd.addColumn("consent", new PatientConsentDataDefinition(), null);
        dsd.addColumn("clientTestedAs", new IndividualORCoupleTestDataDefinition(), null);
        dsd.addColumn("monthsSinceLastTest", new HTSMonthsSinceLastTestDataDefinition(), null);
        dsd.addColumn("testingStrategy", new HTSTestStrategyDataDefinition(), null);
        dsd.addColumn("hivTest1", new HIVTestOneDataDefinition(), null);
        dsd.addColumn("hivTest2", new HIVTestTwoDataDefinition(), null);
        dsd.addColumn("finalResult", new FinalResultDataDefinition(), null);
        dsd.addColumn("finalResultGiven", new FinalResultGivenDataDefinition(), null);
        dsd.addColumn("coupleDiscordant", new HTSDiscordanceDataDefinition(), null);
        dsd.addColumn("tbScreening", new HTSTBScreeningDataDefinition(), null);
        dsd.addColumn("everHadHIVSelfTest", new HTSSelfTestDataDefinition(), null);
        dsd.addColumn("provider", new HTSProviderDataDefinition(), null);
        dsd.addColumn("remarks", new HTSRemarksDataDefinition(), null);

        HTSRegisterCohortDefinition cd = new HTSRegisterCohortDefinition();
        cd.addParameter(new Parameter("startDate", "Start Date", Date.class));
        cd.addParameter(new Parameter("endDate", "End Date", Date.class));

        dsd.addRowFilter(cd, paramMapping);
        return dsd;

    }
}