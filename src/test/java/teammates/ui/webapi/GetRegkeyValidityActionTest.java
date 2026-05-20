package teammates.ui.webapi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import teammates.common.util.Const;
import teammates.common.util.JsonUtils;
import teammates.storage.entity.Instructor;
import teammates.storage.entity.Student;
import teammates.ui.output.RegkeyValidityData;
import teammates.ui.request.Intent;

/**
 * SUT: {@link GetRegkeyValidityAction}.
 */
public class GetRegkeyValidityActionTest extends BaseActionTest<GetRegkeyValidityAction> {
    private Student stubStudentWithAccount;
    private Instructor stubInstructorWithAccount;
    private Student stubStudentWithoutAccount;
    private Instructor stubInstructorWithoutAccount;
    private String stubRegkey = "key";

    @Override
    protected String getActionUri() {
        return Const.ResourceURIs.AUTH_REGKEY;
    }

    @Override
    protected String getRequestMethod() {
        return GET;
    }

    private String regkeyBody(String key) {
        return JsonUtils.toCompactJson(Map.of(Const.ParamsNames.REGKEY, key));
    }

    @BeforeMethod
    void setUp() {
        logoutUser();
        stubInstructorWithAccount = getTypicalInstructor();
        stubInstructorWithAccount.setAccount(getTypicalAccount());
        stubStudentWithAccount = getTypicalStudent();
        stubStudentWithAccount.setAccount(getTypicalAccount());

        stubInstructorWithoutAccount = getTypicalInstructor();
        stubStudentWithoutAccount = getTypicalStudent();
    }

    @Test
    void testExecute_invalidParams_throwsInvalidHttpParameterException() {
        String regBody = regkeyBody(stubRegkey);
        String[] params1 = {
                Const.ParamsNames.INTENT, Intent.STUDENT_SUBMISSION.name(),
        };
        // regkey in body but no intent
        verifyHttpParameterFailureWithBody(regBody);

        // intent in params but no regkey in body
        verifyHttpParameterFailure(params1);

        String[] params3 = {};
        verifyHttpParameterFailure(params3);

        loginAsStudent(stubStudentWithAccount.getGoogleId());
        verifyHttpParameterFailureWithBody(regBody);
        verifyHttpParameterFailure(params1);
        verifyHttpParameterFailure(params3);

        logoutUser();
        loginAsInstructor(stubInstructorWithAccount.getGoogleId());
        verifyHttpParameterFailureWithBody(regBody);
        verifyHttpParameterFailure(params1);
        verifyHttpParameterFailure(params3);
    }

    @Test
    void testExecute_studentIntentNotLoggedInUsedKey_validUsedDisallowed() {
        String body = regkeyBody(stubRegkey);
        String[] params = {
                Const.ParamsNames.INTENT, Intent.STUDENT_SUBMISSION.name(),
        };
        when(mockLogic.getStudentByRegistrationKey(stubRegkey)).thenReturn(stubStudentWithAccount);

        GetRegkeyValidityAction action = getAction(body, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertTrue(data.isValid());
        assertTrue(data.isUsed());
        assertFalse(data.isAllowedAccess());

        String[] params2 = {
                Const.ParamsNames.INTENT, Intent.STUDENT_RESULT.name(),
        };
        GetRegkeyValidityAction action2 = getAction(body, null, params2);
        JsonResult jsonResult2 = action2.execute();
        RegkeyValidityData data2 = (RegkeyValidityData) jsonResult2.getOutput();

        assertTrue(data2.isValid());
        assertTrue(data2.isUsed());
        assertFalse(data2.isAllowedAccess());
    }

    @Test
    void testExecute_instructorIntentNotLoggedInUsedKey_validUsedDisallowedKey() {
        String body = regkeyBody(stubRegkey);
        String[] params = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_SUBMISSION.name(),
        };
        when(mockLogic.getInstructorByRegistrationKey(stubRegkey)).thenReturn(stubInstructorWithAccount);

        GetRegkeyValidityAction action = getAction(body, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertTrue(data.isValid());
        assertTrue(data.isUsed());
        assertFalse(data.isAllowedAccess());

        String[] params2 = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_RESULT.name(),
        };
        GetRegkeyValidityAction action2 = getAction(body, null, params2);
        JsonResult jsonResult2 = action2.execute();
        RegkeyValidityData data2 = (RegkeyValidityData) jsonResult2.getOutput();

        assertTrue(data2.isValid());
        assertTrue(data2.isUsed());
        assertFalse(data2.isAllowedAccess());
    }

    @Test
    void testExecute_studentIntentLoggedInUsedKey_validUsedAllowedKey() {
        loginAsStudent(stubStudentWithAccount.getGoogleId());

        String body = regkeyBody(stubRegkey);
        String[] params = {
                Const.ParamsNames.INTENT, Intent.STUDENT_SUBMISSION.name(),
        };
        when(mockLogic.getStudentByRegistrationKey(stubRegkey)).thenReturn(stubStudentWithAccount);

        GetRegkeyValidityAction action = getAction(body, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertTrue(data.isValid());
        assertTrue(data.isUsed());
        assertTrue(data.isAllowedAccess());

        String[] params2 = {
                Const.ParamsNames.INTENT, Intent.STUDENT_RESULT.name(),
        };

        GetRegkeyValidityAction action2 = getAction(body, null, params2);
        JsonResult jsonResult2 = action2.execute();
        RegkeyValidityData data2 = (RegkeyValidityData) jsonResult2.getOutput();

        assertTrue(data2.isValid());
        assertTrue(data2.isUsed());
        assertTrue(data2.isAllowedAccess());
    }

    @Test
    void testExecute_instructorIntentLoggedInUsedKey_validUsedAllowedKey() {
        loginAsInstructor(stubInstructorWithAccount.getGoogleId());

        String body = regkeyBody(stubRegkey);
        String[] params = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_SUBMISSION.name(),
        };
        when(mockLogic.getInstructorByRegistrationKey(stubRegkey)).thenReturn(stubInstructorWithAccount);

        GetRegkeyValidityAction action = getAction(body, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertTrue(data.isValid());
        assertTrue(data.isUsed());
        assertTrue(data.isAllowedAccess());

        String[] params2 = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_RESULT.name(),
        };

        GetRegkeyValidityAction action2 = getAction(body, null, params2);
        JsonResult jsonResult2 = action2.execute();
        RegkeyValidityData data2 = (RegkeyValidityData) jsonResult2.getOutput();

        assertTrue(data2.isValid());
        assertTrue(data2.isUsed());
        assertTrue(data2.isAllowedAccess());
    }

    @Test
    void testExecute_studentIntentWrongUserLoggedInUsedKey_validUsedDisallowedKey() {
        loginAsStudent("another-id");

        String body = regkeyBody(stubRegkey);
        String[] params = {
                Const.ParamsNames.INTENT, Intent.STUDENT_SUBMISSION.name(),
        };
        when(mockLogic.getStudentByRegistrationKey(stubRegkey)).thenReturn(stubStudentWithAccount);

        GetRegkeyValidityAction action = getAction(body, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertTrue(data.isValid());
        assertTrue(data.isUsed());
        assertFalse(data.isAllowedAccess());

        String[] params2 = {
                Const.ParamsNames.INTENT, Intent.STUDENT_RESULT.name(),
        };

        GetRegkeyValidityAction action2 = getAction(body, null, params2);
        JsonResult jsonResult2 = action2.execute();
        RegkeyValidityData data2 = (RegkeyValidityData) jsonResult2.getOutput();

        assertTrue(data2.isValid());
        assertTrue(data2.isUsed());
        assertFalse(data2.isAllowedAccess());
    }

    @Test
    void testExecute_instructorIntentWrongUserLoggedInUsedKey_validUsedDisallowedKey() {
        loginAsInstructor("another-id");

        String body = regkeyBody(stubRegkey);
        String[] params = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_SUBMISSION.name(),
        };
        when(mockLogic.getInstructorByRegistrationKey(stubRegkey)).thenReturn(stubInstructorWithAccount);

        GetRegkeyValidityAction action = getAction(body, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertTrue(data.isValid());
        assertTrue(data.isUsed());
        assertFalse(data.isAllowedAccess());

        String[] params2 = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_RESULT.name(),
        };

        GetRegkeyValidityAction action2 = getAction(body, null, params2);
        JsonResult jsonResult2 = action2.execute();
        RegkeyValidityData data2 = (RegkeyValidityData) jsonResult2.getOutput();

        assertTrue(data2.isValid());
        assertTrue(data2.isUsed());
        assertFalse(data2.isAllowedAccess());
    }

    @Test
    void testExecute_studentIntentNotLoggedInUnusedKey_validUnusedAllowed() {
        String body = regkeyBody(stubRegkey);
        String[] params = {
                Const.ParamsNames.INTENT, Intent.STUDENT_SUBMISSION.name(),
        };
        when(mockLogic.getStudentByRegistrationKey(stubRegkey)).thenReturn(stubStudentWithoutAccount);

        GetRegkeyValidityAction action = getAction(body, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertTrue(data.isValid());
        assertFalse(data.isUsed());
        assertTrue(data.isAllowedAccess());

        String[] params2 = {
                Const.ParamsNames.INTENT, Intent.STUDENT_RESULT.name(),
        };

        GetRegkeyValidityAction action2 = getAction(body, null, params2);
        JsonResult jsonResult2 = action2.execute();
        RegkeyValidityData data2 = (RegkeyValidityData) jsonResult2.getOutput();

        assertTrue(data2.isValid());
        assertFalse(data2.isUsed());
        assertTrue(data2.isAllowedAccess());
    }

    @Test
    void testExecute_instructorIntentNotLoggedInUnusedKey_validUnusedAllowed() {
        String body = regkeyBody(stubRegkey);
        String[] params = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_SUBMISSION.name(),
        };
        when(mockLogic.getInstructorByRegistrationKey(stubRegkey)).thenReturn(stubInstructorWithoutAccount);

        GetRegkeyValidityAction action = getAction(body, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertTrue(data.isValid());
        assertFalse(data.isUsed());
        assertTrue(data.isAllowedAccess());

        String[] params2 = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_RESULT.name(),
        };

        GetRegkeyValidityAction action2 = getAction(body, null, params2);
        JsonResult jsonResult2 = action2.execute();
        RegkeyValidityData data2 = (RegkeyValidityData) jsonResult2.getOutput();

        assertTrue(data2.isValid());
        assertFalse(data2.isUsed());
        assertTrue(data2.isAllowedAccess());
    }

    @Test
    void testExecute_studentIntentLoggedInUnusedKey_validUnusedAllowed() {
        loginAsStudent(stubStudentWithAccount.getGoogleId());

        String body = regkeyBody(stubRegkey);
        String[] params = {
                Const.ParamsNames.INTENT, Intent.STUDENT_SUBMISSION.name(),
        };
        when(mockLogic.getStudentByRegistrationKey(stubRegkey)).thenReturn(stubStudentWithoutAccount);

        GetRegkeyValidityAction action = getAction(body, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertTrue(data.isValid());
        assertFalse(data.isUsed());
        assertTrue(data.isAllowedAccess());

        String[] params2 = {
                Const.ParamsNames.INTENT, Intent.STUDENT_RESULT.name(),
        };

        GetRegkeyValidityAction action2 = getAction(body, null, params2);
        JsonResult jsonResult2 = action2.execute();
        RegkeyValidityData data2 = (RegkeyValidityData) jsonResult2.getOutput();

        assertTrue(data2.isValid());
        assertFalse(data2.isUsed());
        assertTrue(data2.isAllowedAccess());
    }

    @Test
    void testExecute_instructorIntentLoggedInUnusedKey_validUnusedAllowed() {
        loginAsInstructor(stubInstructorWithAccount.getGoogleId());

        String body = regkeyBody(stubRegkey);
        String[] params = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_SUBMISSION.name(),
        };
        when(mockLogic.getInstructorByRegistrationKey(stubRegkey)).thenReturn(stubInstructorWithoutAccount);

        GetRegkeyValidityAction action = getAction(body, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertTrue(data.isValid());
        assertFalse(data.isUsed());
        assertTrue(data.isAllowedAccess());

        String[] params2 = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_RESULT.name(),
        };

        GetRegkeyValidityAction action2 = getAction(body, null, params2);
        JsonResult jsonResult2 = action2.execute();
        RegkeyValidityData data2 = (RegkeyValidityData) jsonResult2.getOutput();

        assertTrue(data2.isValid());
        assertFalse(data2.isUsed());
        assertTrue(data2.isAllowedAccess());
    }

    @Test
    void testExecute_invalidRegkey_invalidUnusedDisallowed() {
        String invalidBody = regkeyBody("invalid-regkey");
        String[] params = {
                Const.ParamsNames.INTENT, Intent.STUDENT_SUBMISSION.name(),
        };
        when(mockLogic.getStudentByRegistrationKey("invalid-regkey")).thenReturn(null);

        GetRegkeyValidityAction action = getAction(invalidBody, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertFalse(data.isValid());
        assertFalse(data.isUsed());
        assertFalse(data.isAllowedAccess());

        String[] params2 = {
                Const.ParamsNames.INTENT, Intent.STUDENT_RESULT.name(),
        };

        GetRegkeyValidityAction action2 = getAction(invalidBody, null, params2);
        JsonResult jsonResult2 = action2.execute();
        RegkeyValidityData data2 = (RegkeyValidityData) jsonResult2.getOutput();

        assertFalse(data2.isValid());
        assertFalse(data2.isUsed());
        assertFalse(data2.isAllowedAccess());

        String[] params3 = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_SUBMISSION.name(),
        };
        when(mockLogic.getInstructorByRegistrationKey("invalid-regkey")).thenReturn(null);

        GetRegkeyValidityAction action3 = getAction(invalidBody, null, params3);
        JsonResult jsonResult3 = action3.execute();
        RegkeyValidityData data3 = (RegkeyValidityData) jsonResult3.getOutput();

        assertFalse(data3.isValid());
        assertFalse(data3.isUsed());
        assertFalse(data3.isAllowedAccess());

        String[] params4 = {
                Const.ParamsNames.INTENT, Intent.INSTRUCTOR_RESULT.name(),
        };

        GetRegkeyValidityAction action4 = getAction(invalidBody, null, params4);
        JsonResult jsonResult4 = action4.execute();
        RegkeyValidityData data4 = (RegkeyValidityData) jsonResult4.getOutput();

        assertFalse(data4.isValid());
        assertFalse(data4.isUsed());
        assertFalse(data4.isAllowedAccess());
    }

    @Test
    void testExecute_invalidIntent_invalidUnusedDisallowed() {
        String body = regkeyBody(stubRegkey);
        String[] params = {
                Const.ParamsNames.INTENT, Intent.FULL_DETAIL.name(),
        };

        GetRegkeyValidityAction action = getAction(body, null, params);
        JsonResult jsonResult = action.execute();
        RegkeyValidityData data = (RegkeyValidityData) jsonResult.getOutput();

        assertFalse(data.isValid());
        assertFalse(data.isUsed());
        assertFalse(data.isAllowedAccess());
    }

    @Test
    void testSpecificAccessControl_anyUser_canAccess() {
        verifyCanAccess();

        loginAsAdmin();
        verifyCanAccess();

        logoutUser();
        loginAsStudent(stubStudentWithAccount.getGoogleId());
        verifyCanAccess();

        logoutUser();
        loginAsInstructor(stubInstructorWithAccount.getGoogleId());
        verifyCanAccess();

        logoutUser();
        loginAsMaintainer();
        verifyCanAccess();

        logoutUser();
        loginAsUnregistered(stubStudentWithAccount.getGoogleId());
        verifyCanAccess();

        logoutUser();
        loginAsStudentInstructor(stubStudentWithAccount.getGoogleId());
        verifyCanAccess();
    }
}
