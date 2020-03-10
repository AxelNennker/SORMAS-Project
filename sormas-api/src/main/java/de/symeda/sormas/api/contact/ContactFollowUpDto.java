package de.symeda.sormas.api.contact;

import java.io.Serializable;
import java.util.Date;

import de.symeda.sormas.api.Disease;
import de.symeda.sormas.api.person.PersonReferenceDto;
import de.symeda.sormas.api.user.UserReferenceDto;
import de.symeda.sormas.api.visit.VisitResult;

public class ContactFollowUpDto implements Serializable {

	private static final long serialVersionUID = -1257025719012862417L;
	
	public static final String I18N_PREFIX = "Contact";
	
	public static final String UUID = "uuid";
	public static final String PERSON = "person";
	public static final String CONTACT_OFFICER = "contactOfficer";
	public static final String LAST_CONTACT_DATE = "lastContactDate";
	public static final String REPORT_DATE_TIME = "reportDateTime";
	public static final String FOLLOW_UP_UNTIL = "followUpUntil";
	public static final String DAY_1_RESULT = "day1Result";
	public static final String DAY_2_RESULT = "day2Result";
	public static final String DAY_3_RESULT = "day3Result";
	public static final String DAY_4_RESULT = "day4Result";
	public static final String DAY_5_RESULT = "day5Result";
	public static final String DAY_6_RESULT = "day6Result";
	public static final String DAY_7_RESULT = "day7Result";
	public static final String DAY_8_RESULT = "day8Result";
	
	private String uuid;
	private PersonReferenceDto person;
	private UserReferenceDto contactOfficer;
	private Date lastContactDate;
	private Date reportDateTime;
	private Date followUpUntil;
	private Disease disease;
	private VisitResult day1Result = VisitResult.NOT_PERFORMED;
	private VisitResult day2Result = VisitResult.NOT_PERFORMED;
	private VisitResult day3Result = VisitResult.NOT_PERFORMED;
	private VisitResult day4Result = VisitResult.NOT_PERFORMED;
	private VisitResult day5Result = VisitResult.NOT_PERFORMED;
	private VisitResult day6Result = VisitResult.NOT_PERFORMED;
	private VisitResult day7Result = VisitResult.NOT_PERFORMED;
	private VisitResult day8Result = VisitResult.NOT_PERFORMED;
	
	public ContactFollowUpDto(String uuid, String personUuid, String personFirstName, String personLastName,
			String contactOfficerUuid, String contactOfficerFirstName, String contactOfficerLastName,
			Date lastContactDate, Date reportDateTime, Date followUpUntil, Disease disease) {
		this.uuid = uuid;
		this.person = new PersonReferenceDto(personUuid, personFirstName, personLastName);
		this.contactOfficer = new UserReferenceDto(contactOfficerUuid, contactOfficerFirstName, contactOfficerLastName, null);
		this.lastContactDate = lastContactDate;
		this.reportDateTime = reportDateTime;
		this.followUpUntil = followUpUntil;
		this.disease = disease;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public PersonReferenceDto getPerson() {
		return person;
	}

	public void setPerson(PersonReferenceDto person) {
		this.person = person;
	}

	public UserReferenceDto getContactOfficer() {
		return contactOfficer;
	}

	public void setContactOfficer(UserReferenceDto contactOfficer) {
		this.contactOfficer = contactOfficer;
	}

	public Date getLastContactDate() {
		return lastContactDate;
	}

	public void setLastContactDate(Date lastContactDate) {
		this.lastContactDate = lastContactDate;
	}

	public Date getReportDateTime() {
		return reportDateTime;
	}

	public void setReportDateTime(Date reportDateTime) {
		this.reportDateTime = reportDateTime;
	}

	public Date getFollowUpUntil() {
		return followUpUntil;
	}

	public void setFollowUpUntil(Date followUpUntil) {
		this.followUpUntil = followUpUntil;
	}

	public Disease getDisease() {
		return disease;
	}

	public void setDisease(Disease disease) {
		this.disease = disease;
	}

	public VisitResult getDay1Result() {
		return day1Result;
	}

	public void setDay1Result(VisitResult day1Result) {
		this.day1Result = day1Result;
	}

	public VisitResult getDay2Result() {
		return day2Result;
	}

	public void setDay2Result(VisitResult day2Result) {
		this.day2Result = day2Result;
	}

	public VisitResult getDay3Result() {
		return day3Result;
	}

	public void setDay3Result(VisitResult day3Result) {
		this.day3Result = day3Result;
	}

	public VisitResult getDay4Result() {
		return day4Result;
	}

	public void setDay4Result(VisitResult day4Result) {
		this.day4Result = day4Result;
	}

	public VisitResult getDay5Result() {
		return day5Result;
	}

	public void setDay5Result(VisitResult day5Result) {
		this.day5Result = day5Result;
	}

	public VisitResult getDay6Result() {
		return day6Result;
	}

	public void setDay6Result(VisitResult day6Result) {
		this.day6Result = day6Result;
	}

	public VisitResult getDay7Result() {
		return day7Result;
	}

	public void setDay7Result(VisitResult day7Result) {
		this.day7Result = day7Result;
	}

	public VisitResult getDay8Result() {
		return day8Result;
	}

	public void setDay8Result(VisitResult day8Result) {
		this.day8Result = day8Result;
	}
	
}
