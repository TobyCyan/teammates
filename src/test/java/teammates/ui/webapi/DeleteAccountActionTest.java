package teammates.ui.webapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.testng.annotations.Test;

import teammates.common.datatransfer.InstructorPrivileges;
import teammates.common.datatransfer.Provider;
import teammates.common.util.Const;
import teammates.storage.entity.Account;
import teammates.storage.entity.Course;
import teammates.storage.entity.Instructor;
import teammates.ui.output.MessageOutput;

/**
 * SUT: {@link DeleteAccountAction}.
 */
public class DeleteAccountActionTest extends BaseActionTest<DeleteAccountAction> {
    String googleId = "user-googleId";

    @Override
    protected String getActionUri() {
        return Const.ResourceURIs.ACCOUNT;
    }

    @Override
    protected String getRequestMethod() {
        return DELETE;
    }

    @Test
    void testAccessControl() {
        verifyOnlyAdminsCanAccess();
    }

    @Test
    protected void textExecute_nullParams_throwsInvalidHttpParameterException() {
        String[] params = {
                Const.ParamsNames.PROVIDER, null,
                Const.ParamsNames.SUBJECT, "validSubject",
        };
        verifyHttpParameterFailure(params);

        String[] params2 = {
                Const.ParamsNames.PROVIDER, "TEAMMATES_DEV",
                Const.ParamsNames.SUBJECT, null,
        };
        verifyHttpParameterFailure(params2);
    }

    @Test
    protected void testExecute_invalidProvider_throwsInvalidHttpParameterException() {
        String[] params = {
                Const.ParamsNames.PROVIDER, "INVALID_PROVIDER",
                Const.ParamsNames.SUBJECT, "validSubject",
        };
        verifyHttpParameterFailure(params);
    }

    @Test
    protected void testExecute_nonNullParams_success() {
        Course stubCourse = new Course("course-id", "name", Const.DEFAULT_TIME_ZONE, "institute");
        Account stubAccount = new Account(googleId, Provider.TEAMMATES_DEV, "validInstructorSubject",
                "validTenantId", "name", "instructoremail@tm.tmt");
        Instructor instructor = new Instructor(stubCourse, "name", "instructoremail@tm.tmt",
                false, "", null, new InstructorPrivileges());
        instructor.setAccount(stubAccount);
        String[] params = {
                Const.ParamsNames.PROVIDER, stubAccount.getProvider().name(),
                Const.ParamsNames.SUBJECT, stubAccount.getSubject(),
                Const.ParamsNames.TENANT_ID, stubAccount.getTenantId(),
        };
        DeleteAccountAction action = getAction(params);
        MessageOutput actionOutput = (MessageOutput) getJsonResult(action).getOutput();
        assertEquals("Account is successfully deleted.", actionOutput.getMessage());
        verify(mockLogic, times(1))
                .deleteAccountCascade(stubAccount.getProvider(), stubAccount.getSubject(), stubAccount.getTenantId());
    }
}
