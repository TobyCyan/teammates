package teammates.logic.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.mockito.MockedStatic;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import teammates.common.datatransfer.UserInfo;
import teammates.common.datatransfer.UserInfoCookie;
import teammates.common.util.Config;
import teammates.common.util.Const;
import teammates.logic.core.AccountsLogic;
import teammates.storage.entity.Account;
import teammates.storage.entity.Instructor;
import teammates.storage.entity.Student;
import teammates.test.BaseTestCase;

/**
 * SUT: {@link UserProvision}.
 */
public class UserProvisionTest extends BaseTestCase {

    private UserProvision userProvision;
    private AccountsLogic mockAccountsLogic;
    private Account mockAccount;
    private MockedStatic<Config> mockConfigStatic;
    private MockedStatic<AccountsLogic> mockAccountsLogicStatic;

    @BeforeClass
    public void setUpClass() {
        // We need to ensure the UserProvision class' static initialiser has run before setUpMethod() (below) runs.
        // This guarantees the singleton holds a reference to a real UsersLogic instance.
        // Otherwise, the singleton may initialize during setUpMethod() while UsersLogic is mocked, capturing
        // the mock instead. Since the mock is only active for the duration of each test, any other test class
        // that calls UserProvision.inst() directly would receive a singleton with a stale, uncontrolled mock.
        UserProvision.inst();
    }

    @BeforeMethod
    public void setUpMethod() {
        mockAccount = mock(Account.class);
        when(mockAccount.getInstructors()).thenReturn(Set.of());
        when(mockAccount.getStudents()).thenReturn(Set.of());
        mockAccountsLogic = mock(AccountsLogic.class);
        mockAccountsLogicStatic = mockStatic(AccountsLogic.class);
        mockAccountsLogicStatic.when(AccountsLogic::inst).thenReturn(mockAccountsLogic);
        when(mockAccountsLogic.getAccount(any(UUID.class))).thenReturn(mockAccount);
        userProvision = new UserProvision();

        mockConfigStatic = mockStatic(Config.class);
        mockConfigStatic.when(Config::getAppAdmins).thenReturn(List.of());
        mockConfigStatic.when(Config::getAppMaintainers).thenReturn(List.of());
    }

    @AfterMethod
    public void tearDownMethod() {
        mockConfigStatic.close();
        mockAccountsLogicStatic.close();
    }

    @Test
    public void testGetCurrentUser_nullUic_returnsNull() {
        assertNull(userProvision.getCurrentUser(null));
    }

    @Test
    public void testGetCurrentUser_invalidCookie_returnsNull() {
        assertNull(userProvision.getCurrentUser(createMockInvalidCookie()));
    }

    @Test
    public void testGetCurrentUser_instructor_returnsUserInfoWithInstructorRole() {
        UUID instructorAccountId = UUID.randomUUID();
        when(mockAccount.getInstructors()).thenReturn(Set.of(mock(Instructor.class)));

        UserInfo user = userProvision.getCurrentUser(createMockValidCookie(instructorAccountId));

        assertEquals(instructorAccountId, user.accountId);
        assertHasRoles(user, Role.INSTRUCTOR);
    }

    @Test
    public void testGetCurrentUser_student_returnsUserInfoWithStudentRole() {
        UUID studentAccountId = UUID.randomUUID();
        when(mockAccount.getStudents()).thenReturn(Set.of(mock(Student.class)));

        UserInfo user = userProvision.getCurrentUser(createMockValidCookie(studentAccountId));

        assertEquals(studentAccountId, user.accountId);
        assertHasRoles(user, Role.STUDENT);
    }

    @Test
    public void testGetCurrentUser_admin_returnsUserInfoWithAdminRole() {
        UUID adminAccountId = UUID.randomUUID();
        mockConfigStatic.when(Config::getAppAdmins).thenReturn(List.of(adminAccountId.toString()));

        UserInfo user = userProvision.getCurrentUser(createMockValidCookie(adminAccountId));

        assertEquals(adminAccountId, user.accountId);
        assertHasRoles(user, Role.ADMIN);
    }

    @Test
    public void testGetCurrentUser_maintainer_returnsUserInfoWithMaintainerRole() {
        UUID maintainerAccountId = UUID.randomUUID();
        mockConfigStatic.when(Config::getAppMaintainers).thenReturn(List.of(maintainerAccountId.toString()));

        UserInfo user = userProvision.getCurrentUser(createMockValidCookie(maintainerAccountId));

        assertEquals(maintainerAccountId, user.accountId);
        assertHasRoles(user, Role.MAINTAINER);
    }

    @Test
    public void testGetCurrentUser_unregistered_returnsUserInfoWithNoRoles() {
        UUID accountId = UUID.randomUUID();

        UserInfo user = userProvision.getCurrentUser(createMockValidCookie(accountId));

        assertEquals(accountId, user.accountId);
        assertHasNoRoles(user);
    }

    @Test
    public void testGetCurrentUser_instructorAndStudent_returnsBothRolesTrue() {
        UUID accountId = UUID.randomUUID();
        when(mockAccount.getInstructors()).thenReturn(Set.of(mock(Instructor.class)));
        when(mockAccount.getStudents()).thenReturn(Set.of(mock(Student.class)));

        UserInfo user = userProvision.getCurrentUser(createMockValidCookie(accountId));

        assertEquals(accountId, user.accountId);
        assertHasRoles(user, Role.INSTRUCTOR, Role.STUDENT);
    }

    @Test
    public void testGetCurrentUser_adminAndMaintainer_returnsBothRolesTrue() {
        UUID accountId = UUID.randomUUID();
        mockConfigStatic.when(Config::getAppAdmins).thenReturn(List.of(accountId.toString()));
        mockConfigStatic.when(Config::getAppMaintainers).thenReturn(List.of(accountId.toString()));

        UserInfo user = userProvision.getCurrentUser(createMockValidCookie(accountId));

        assertEquals(accountId, user.accountId);
        assertHasRoles(user, Role.ADMIN, Role.MAINTAINER);
    }

    @Test
    public void testGetCurrentUser_instructorStudentAndMaintainer_returnsThreeRolesTrue() {
        UUID accountId = UUID.randomUUID();
        when(mockAccount.getInstructors()).thenReturn(Set.of(mock(Instructor.class)));
        when(mockAccount.getStudents()).thenReturn(Set.of(mock(Student.class)));
        mockConfigStatic.when(Config::getAppMaintainers).thenReturn(List.of(accountId.toString()));

        UserInfo user = userProvision.getCurrentUser(createMockValidCookie(accountId));

        assertEquals(accountId, user.accountId);
        assertHasRoles(user, Role.INSTRUCTOR, Role.STUDENT, Role.MAINTAINER);
    }

    @Test
    public void testGetCurrentUser_allRoles_returnsAllRolesTrue() {
        UUID accountId = UUID.randomUUID();
        when(mockAccount.getInstructors()).thenReturn(Set.of(mock(Instructor.class)));
        when(mockAccount.getStudents()).thenReturn(Set.of(mock(Student.class)));
        mockConfigStatic.when(Config::getAppAdmins).thenReturn(List.of(accountId.toString()));
        mockConfigStatic.when(Config::getAppMaintainers).thenReturn(List.of(accountId.toString()));

        UserInfo user = userProvision.getCurrentUser(createMockValidCookie(accountId));

        assertEquals(accountId, user.accountId);
        assertHasRoles(user, Role.ADMIN, Role.INSTRUCTOR, Role.STUDENT, Role.MAINTAINER);
    }

    @Test
    public void testGetCurrentLoggedInUser_nullUic_returnsNull() {
        assertNull(userProvision.getCurrentLoggedInUser(null));
    }

    @Test
    public void testGetCurrentLoggedInUser_invalidCookie_returnsNull() {
        assertNull(userProvision.getCurrentLoggedInUser(createMockInvalidCookie()));
    }

    @Test
    public void testGetCurrentLoggedInUser_validCookie_returnsUserInfoWithCorrectIdAndNoRoles() {
        UUID accountId = UUID.randomUUID();

        UserInfo user = userProvision.getCurrentLoggedInUser(createMockValidCookie(accountId));

        assertEquals(accountId, user.accountId);
        assertHasNoRoles(user);
    }

    @Test
    public void testGetMasqueradeUser_instructor_returnsUserInfoWithInstructorRole() {
        UUID accountId = UUID.randomUUID();
        when(mockAccount.getInstructors()).thenReturn(Set.of(mock(Instructor.class)));

        UserInfo user = userProvision.getMasqueradeUser(accountId);

        assertEquals(accountId, user.accountId);
        assertHasRoles(user, Role.INSTRUCTOR);
    }

    @Test
    public void testGetMasqueradeUser_student_returnsUserInfoWithStudentRole() {
        UUID accountId = UUID.randomUUID();
        when(mockAccount.getStudents()).thenReturn(Set.of(mock(Student.class)));

        UserInfo user = userProvision.getMasqueradeUser(accountId);

        assertEquals(accountId, user.accountId);
        assertHasRoles(user, Role.STUDENT);
    }

    @Test
    public void testGetMasqueradeUser_maintainer_returnsUserInfoWithMaintainerRole() {
        UUID accountId = UUID.randomUUID();
        mockConfigStatic.when(Config::getAppMaintainers).thenReturn(List.of(accountId.toString()));

        UserInfo user = userProvision.getMasqueradeUser(accountId);

        assertEquals(accountId, user.accountId);
        assertHasRoles(user, Role.MAINTAINER);
    }

    @Test
    public void testGetMasqueradeUser_admin_returnsUserInfoWithNoAdminRole() {
        UUID accountId = UUID.randomUUID();
        mockConfigStatic.when(Config::getAppAdmins).thenReturn(List.of(accountId.toString()));

        UserInfo user = userProvision.getMasqueradeUser(accountId);

        assertEquals(accountId, user.accountId);
        assertHasNoRoles(user);
    }

    @Test
    public void testGetMasqueradeUser_unregistered_returnsUserInfoWithNoRoles() {
        UUID accountId = UUID.randomUUID();

        UserInfo user = userProvision.getMasqueradeUser(accountId);

        assertEquals(accountId, user.accountId);
        assertHasNoRoles(user);
    }

    @Test
    public void testGetMasqueradeUser_instructorAndStudent_returnsBothRolesTrue() {
        UUID accountId = UUID.randomUUID();
        when(mockAccount.getInstructors()).thenReturn(Set.of(mock(Instructor.class)));
        when(mockAccount.getStudents()).thenReturn(Set.of(mock(Student.class)));

        UserInfo user = userProvision.getMasqueradeUser(accountId);

        assertEquals(accountId, user.accountId);
        assertHasRoles(user, Role.INSTRUCTOR, Role.STUDENT);
    }

    @Test
    public void testGetMasqueradeUser_instructorStudentAndMaintainer_returnsThreeRolesTrue() {
        UUID accountId = UUID.randomUUID();
        when(mockAccount.getInstructors()).thenReturn(Set.of(mock(Instructor.class)));
        when(mockAccount.getStudents()).thenReturn(Set.of(mock(Student.class)));
        mockConfigStatic.when(Config::getAppMaintainers).thenReturn(List.of(accountId.toString()));

        UserInfo user = userProvision.getMasqueradeUser(accountId);

        assertEquals(accountId, user.accountId);
        assertHasRoles(user, Role.INSTRUCTOR, Role.STUDENT, Role.MAINTAINER);
    }

    @Test
    public void testGetAdminOnlyUser_returnsUserInfoWithOnlyIsAdminTrue() {
        UUID adminAccountId = UUID.randomUUID();

        UserInfo user = userProvision.getAdminOnlyUser(adminAccountId);

        assertEquals(adminAccountId, user.accountId);
        assertHasRoles(user, Role.ADMIN);
        verifyNoInteractions(mockAccountsLogic);
    }

    @Test
    public void testGetAutomatedServiceUser_returnsUserInfoWithOnlyIsAutomatedServiceTrue() {
        String serviceId = Const.AutomatedService.CRON_SERVICE_USER_ID;

        UserInfo user = userProvision.getAutomatedServiceUser(serviceId);

        assertEquals(serviceId, user.id);
        assertHasRoles(user, Role.AUTOMATED_SERVICE);
        verifyNoInteractions(mockAccountsLogic);
    }

    private static UserInfoCookie createMockValidCookie(UUID accountId) {
        UserInfoCookie cookie = mock(UserInfoCookie.class);
        when(cookie.isValid()).thenReturn(true);
        when(cookie.getAccountId()).thenReturn(accountId);
        return cookie;
    }

    private static UserInfoCookie createMockInvalidCookie() {
        UserInfoCookie cookie = mock(UserInfoCookie.class);
        when(cookie.isValid()).thenReturn(false);
        return cookie;
    }

    private static void assertHasNoRoles(UserInfo user) {
        // This method just calls assertHasRoles with an empty array to improve readability
        assertHasRoles(user);

    }

    private static void assertHasRoles(UserInfo user, Role... expectedRoles) {
        Set<Role> expected = Set.of(expectedRoles);
        assertEquals(expected.contains(Role.ADMIN), user.isAdmin);
        assertEquals(expected.contains(Role.INSTRUCTOR), user.isInstructor);
        assertEquals(expected.contains(Role.STUDENT), user.isStudent);
        assertEquals(expected.contains(Role.MAINTAINER), user.isMaintainer);
        assertEquals(expected.contains(Role.AUTOMATED_SERVICE), user.isAutomatedService);
    }

    private enum Role {
        ADMIN, INSTRUCTOR, STUDENT, MAINTAINER, AUTOMATED_SERVICE
    }

}
