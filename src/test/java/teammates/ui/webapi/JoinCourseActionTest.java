package teammates.ui.webapi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import java.util.Map;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import teammates.common.exception.EntityAlreadyExistsException;
import teammates.common.exception.EntityDoesNotExistException;
import teammates.common.exception.InvalidParametersException;
import teammates.common.util.Const;
import teammates.common.util.EmailWrapper;
import teammates.common.util.JsonUtils;
import teammates.storage.entity.Course;
import teammates.storage.entity.Instructor;
import teammates.storage.entity.Student;
import teammates.ui.exception.InvalidOperationException;
import teammates.ui.output.MessageOutput;

/**
 * SUT: {@link JoinCourseAction}.
 */
public class JoinCourseActionTest extends BaseActionTest<JoinCourseAction> {
    private Student stubStudent;
    private Instructor stubInstructor;
    private Course stubCourse;
    private EmailWrapper stubEmailWrapper;

    @Override
    protected String getActionUri() {
        return Const.ResourceURIs.JOIN;
    }

    @Override
    protected String getRequestMethod() {
        return PUT;
    }

    private String regkeyBody(String key) {
        return JsonUtils.toCompactJson(Map.of(Const.ParamsNames.REGKEY, key));
    }

    @BeforeMethod
    void setUp() {
        stubCourse = getTypicalCourse();
        stubInstructor = getTypicalInstructor();
        stubStudent = getTypicalStudent();
        stubEmailWrapper = new EmailWrapper();
        stubEmailWrapper.setRecipient(stubStudent.getEmail());
        stubEmailWrapper.setSubject("You have been registered for " + stubCourse.getName());
        reset(mockLogic);
    }

    @Test
    void testExecute_invalidParams_throwsInvalidHttpParameterException() {
        String [] params1 = {};
        verifyHttpParameterFailure(params1);

        // regkey in body but no entity type
        verifyHttpParameterFailureWithBody(regkeyBody("regkey"));

        String[] params3 = {
                Const.ParamsNames.ENTITY_TYPE, Const.EntityType.STUDENT,
        };
        verifyHttpParameterFailure(params3);
    }

    @Test
    void testExecute_validStudentRegKey_success() throws EntityAlreadyExistsException, InvalidParametersException,
            EntityDoesNotExistException {
        loginAsUnregistered("unreg-student");

        when(mockLogic.getStudentByRegistrationKey("registered-key-student")).thenReturn(stubStudent);
        when(mockLogic.joinCourseForStudent("registered-key-student", "unreg-student")).thenReturn(stubStudent);
        when(mockLogic.getCourse(stubStudent.getCourseId())).thenReturn(stubCourse);
        when(mockEmailGenerator.generateUserCourseRegisteredEmail(stubStudent.getName(), stubStudent.getEmail(),
                "unreg-student", false, stubCourse)).thenReturn(stubEmailWrapper);
        String[] params = {
                Const.ParamsNames.ENTITY_TYPE, Const.EntityType.STUDENT,
        };
        JoinCourseAction action = getAction(regkeyBody("registered-key-student"), null, params);
        JsonResult jsonResult = getJsonResult(action);
        MessageOutput messageOutput = (MessageOutput) jsonResult.getOutput();
        assertEquals("Student successfully joined course", messageOutput.getMessage());
        verifyNumberOfEmailsSent(1);
    }

    @Test
    void testExecute_studentAlreadyExists_throwsInvalidOperationException() throws EntityAlreadyExistsException,
            InvalidParametersException, EntityDoesNotExistException {
        loginAsUnregistered("unreg-student");

        when(mockLogic.getStudentByRegistrationKey("registered-key-student")).thenReturn(stubStudent);
        when(mockLogic.joinCourseForStudent("registered-key-student", "unreg-student"))
                .thenThrow(EntityAlreadyExistsException.class);
        String[] params = {
                Const.ParamsNames.ENTITY_TYPE, Const.EntityType.STUDENT,
        };
        JoinCourseAction action = getAction(regkeyBody("registered-key-student"), null, params);
        assertThrows(InvalidOperationException.class, action::execute);
        verifyNoEmailsSent();
    }

    @Test
    void testExecute_studentDoesNotExist_throwsEntityNotFoundException() throws EntityAlreadyExistsException,
            InvalidParametersException, EntityDoesNotExistException {
        loginAsUnregistered("unreg-student");

        when(mockLogic.getStudentByRegistrationKey("invalid-reg-key")).thenReturn(stubStudent);
        when(mockLogic.joinCourseForStudent("invalid-reg-key", "unreg-student"))
                .thenThrow(EntityDoesNotExistException.class);
        String[] params = {
                Const.ParamsNames.ENTITY_TYPE, Const.EntityType.STUDENT,
        };
        verifyEntityNotFoundWithBody(regkeyBody("invalid-reg-key"), params);
        verifyNoEmailsSent();
    }

    @Test
    void testExecute_studentInvalidAccount_errorMessage() throws EntityAlreadyExistsException,
            InvalidParametersException, EntityDoesNotExistException {
        loginAsUnregistered("unreg-student");
        when(mockLogic.getStudentByRegistrationKey("invalid-reg-key")).thenReturn(stubStudent);
        when(mockLogic.joinCourseForStudent("invalid-reg-key", "unreg-student"))
                .thenThrow(InvalidParametersException.class);
        String[] params = {
                Const.ParamsNames.ENTITY_TYPE, Const.EntityType.STUDENT,
        };
        JoinCourseAction action = getAction(regkeyBody("invalid-reg-key"), null, params);
        JsonResult jsonResult = getJsonResult(action, 500);
        assertEquals(500, jsonResult.getStatusCode());
        verifyNoEmailsSent();
    }

    @Test
    void testExecute_validInstructorRegKey_success() throws EntityAlreadyExistsException,
            InvalidParametersException, EntityDoesNotExistException {
        loginAsUnregistered("unreg-instructor");

        when(mockLogic.getInstructorByRegistrationKey("registered-key-instructor")).thenReturn(stubInstructor);
        when(mockLogic.joinCourseForInstructor("registered-key-instructor", "unreg-instructor"))
                .thenReturn(stubInstructor);
        when(mockLogic.getCourse(stubInstructor.getCourseId())).thenReturn(stubCourse);
        when(mockEmailGenerator.generateUserCourseRegisteredEmail(stubInstructor.getName(), stubInstructor.getEmail(),
                "unreg-instructor", true, stubCourse)).thenReturn(stubEmailWrapper);
        String[] params = {
                Const.ParamsNames.ENTITY_TYPE, Const.EntityType.INSTRUCTOR,
        };
        JoinCourseAction action = getAction(regkeyBody("registered-key-instructor"), null, params);
        JsonResult jsonResult = getJsonResult(action);
        MessageOutput messageOutput = (MessageOutput) jsonResult.getOutput();
        assertEquals("Instructor successfully joined course", messageOutput.getMessage());
        verifyNumberOfEmailsSent(1);
    }

    @Test
    void testExecute_instructorAlreadyExists_throwsInvalidOperationException() throws EntityAlreadyExistsException,
            InvalidParametersException, EntityDoesNotExistException {
        loginAsUnregistered("unreg-instructor");

        when(mockLogic.getInstructorByRegistrationKey("registered-key-instructor")).thenReturn(stubInstructor);
        when(mockLogic.joinCourseForInstructor("registered-key-instructor", "unreg-instructor"))
                .thenThrow(EntityAlreadyExistsException.class);
        String[] params = {
                Const.ParamsNames.ENTITY_TYPE, Const.EntityType.INSTRUCTOR,
        };
        JoinCourseAction action = getAction(regkeyBody("registered-key-instructor"), null, params);
        assertThrows(InvalidOperationException.class, action::execute);
        verifyNoEmailsSent();
    }

    @Test
    void testExecute_instructorDoesNotExist_throwsEntityNotFoundException() throws EntityDoesNotExistException,
            EntityAlreadyExistsException, InvalidParametersException {
        loginAsUnregistered("unreg-instructor");

        when(mockLogic.getInstructorByRegistrationKey("invalid-reg-key")).thenReturn(stubInstructor);
        when(mockLogic.joinCourseForInstructor("invalid-reg-key", "unreg-instructor"))
                .thenThrow(EntityDoesNotExistException.class);
        String[] params = {
                Const.ParamsNames.ENTITY_TYPE, Const.EntityType.INSTRUCTOR,
        };
        verifyEntityNotFoundWithBody(regkeyBody("invalid-reg-key"), params);
        verifyNoEmailsSent();
    }

    @Test
    void testExecute_instructorInvalidAccount_errorMessage() throws EntityAlreadyExistsException,
            InvalidParametersException, EntityDoesNotExistException {
        loginAsUnregistered("unreg-instructor");
        when(mockLogic.getInstructorByRegistrationKey("invalid-reg-key")).thenReturn(stubInstructor);
        when(mockLogic.joinCourseForInstructor("invalid-reg-key", "unreg-instructor"))
                .thenThrow(InvalidParametersException.class);
        String[] params = {
                Const.ParamsNames.ENTITY_TYPE, Const.EntityType.INSTRUCTOR,
        };
        JoinCourseAction action = getAction(regkeyBody("invalid-reg-key"), null, params);
        JsonResult jsonResult = getJsonResult(action, 500);
        assertEquals(500, jsonResult.getStatusCode());
        verifyNoEmailsSent();
    }

    @Test
    void testExecute_invalidEntityType_throwsInvalidHttpParameterException() {
        loginAsUnregistered("unreg-user");

        String[] params1 = {
                Const.ParamsNames.ENTITY_TYPE, Const.EntityType.MAINTAINER,
        };
        verifyHttpParameterFailureWithBody(regkeyBody("registered-key-student"), params1);

        String[] params2 = {
                Const.ParamsNames.ENTITY_TYPE, "invalid-entity-type",
        };
        verifyHttpParameterFailureWithBody(regkeyBody("registered-key-student"), params2);

        String[] params3 = {
                Const.ParamsNames.ENTITY_TYPE, Const.EntityType.ADMIN,
        };
        verifyHttpParameterFailureWithBody(regkeyBody("registered-key-student"), params3);

        verifyNoEmailsSent();
    }

    @Test
    void testSpecificAccessControl_loggedIn_canAccess() {
        loginAsUnregistered("unreg-user");
        String[] params = {};
        verifyCanAccess(params);

        logoutUser();
        loginAsAdmin();
        verifyCanAccess(params);

        logoutUser();
        loginAsInstructor("instructor");
        verifyCanAccess(params);

        logoutUser();
        loginAsStudent("student");
        verifyCanAccess(params);
    }

    @Test
    void testSpecificAccessControl_loggedOut_cannotAccess() {
        logoutUser();
        String[] params = {};
        verifyCannotAccess(params);
    }
}
