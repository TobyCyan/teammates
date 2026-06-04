package teammates.logic.api;

import jakarta.servlet.http.HttpServletRequest;

import teammates.common.datatransfer.AuthContext;
import teammates.common.datatransfer.Provider;
import teammates.common.util.Const;
import teammates.storage.entity.Account;
import teammates.storage.entity.User;
import teammates.ui.exception.UnauthorizedAccessException;
import teammates.ui.webapi.AuthType;

/**
 * Allows mocking of the {@link UserProvision} API used in production.
 *
 * <p>Instead of getting user information from the authentication service,
 * the API will return pre-determined information instead.
 */
public class MockUserProvision extends UserProvision {
    private static final AuthContext PUBLIC_AUTH_CONTEXT = new AuthContext(AuthType.PUBLIC, null, null, false, false);
    private static final AuthContext AUTOMATED_SERVICE_AUTH_CONTEXT =
            new AuthContext(AuthType.AUTOMATED_SERVICE, null, null, false, false);

    private Logic logic = Logic.inst();
    private String loggedInSubject;
    private boolean createMissingAccounts;
    private boolean isLoggedIn;
    private boolean loggedInUserIsAdmin;
    private boolean isAutomatedServiceMode;
    private boolean isMaintainer;
    private boolean isAdmin;

    public void setLogic(Logic logic) {
        this.logic = logic;
    }

    public void setCreateMissingAccounts(boolean createMissingAccounts) {
        this.createMissingAccounts = createMissingAccounts;
    }

    // TODO: Login by subject instead of googleId. --- IGNORE ---
    private AuthContext loginUser(String subject, boolean isAdmin, boolean isMaintainer) {
        this.isLoggedIn = true;
        this.loggedInSubject = subject;
        this.loggedInUserIsAdmin = isAdmin;
        this.isAdmin = isAdmin;
        this.isMaintainer = isMaintainer;
        return createAccountAuthContext(AuthType.LOGGED_IN, subject, isAdmin, isMaintainer);
    }

    /**
     * Login as a user without admin rights.
     *
     * @return The auth context after login process
     */
    public AuthContext loginUser(String subject) {
        return loginUser(subject, false, false);
    }

    /**
     * Login as a user with admin rights.
     *
     * @return The auth context after login process
     */
    public AuthContext loginAsAdmin(String subject) {
        return loginUser(subject, true, false);
    }

    /**
     * Login as a user with maintainer rights.
     *
     * @return The auth context after login process
     */
    public AuthContext loginAsMaintainer(String subject) {
        return loginUser(subject, false, true);
    }

    /**
     * Logs in as an automated service (cron/worker).
     */
    public void loginAsAutomatedService() {
        isAutomatedServiceMode = true;
    }

    public boolean isAutomatedServiceMode() {
        return isAutomatedServiceMode;
    }

    /**
     * Removes the logged-in user information.
     */
    public void logoutUser() {
        isLoggedIn = false;
        loggedInUserIsAdmin = false;
        isAutomatedServiceMode = false;
        loggedInSubject = null;
    }

    @Override
    public AuthContext getAuthContextFromRequest(HttpServletRequest req) throws UnauthorizedAccessException {
        if (isAutomatedServiceMode) {
            return AUTOMATED_SERVICE_AUTH_CONTEXT;
        }

        if (!isLoggedIn) {
            String regKey = req.getParameter(Const.ParamsNames.REGKEY);
            if (regKey != null) {
                User regKeyUser = logic.getUserByRegistrationKey(regKey);
                return new AuthContext(AuthType.REG_KEY, null, regKeyUser, false, false);
            }
            return PUBLIC_AUTH_CONTEXT;
        }

        String masqueradeUserId = req.getParameter(Const.ParamsNames.USER);
        if (masqueradeUserId != null) {
            if (!loggedInUserIsAdmin) {
                throw new UnauthorizedAccessException(
                        String.format("Masquerade failed: user %s does not have admin privilege", loggedInSubject));
            }
            return createAccountAuthContext(AuthType.MASQUERADE, masqueradeUserId, isAdmin, isMaintainer);
        }

        return createAccountAuthContext(AuthType.LOGGED_IN, loggedInSubject, isAdmin, isMaintainer);
    }

    private AuthContext createAccountAuthContext(
            AuthType authType, String subject, boolean isAdmin, boolean isMaintainer) {
        Account account = createMissingAccounts
                ? new Account(
                        subject, Provider.TEAMMATES_DEV, "testUserSubject", null,
                        "Test User", subject + "@example.com")
                : logic.getAccountByOidcClaims(Provider.TEAMMATES_DEV, subject, null);
        return new AuthContext(authType, account, null, isAdmin, isMaintainer);
    }

}
