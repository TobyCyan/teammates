package teammates.logic.api;

import java.util.UUID;

import teammates.common.datatransfer.UserInfo;
import teammates.common.datatransfer.UserInfoCookie;
import teammates.common.util.Config;
import teammates.logic.core.AccountsLogic;
import teammates.logic.core.UsersLogic;
import teammates.storage.entity.Account;

/**
 * Handles logic related to username and user role provisioning.
 */
public class UserProvision {

    private static final UserProvision instance = new UserProvision();

    private final AccountsLogic accountsLogic = AccountsLogic.inst();

    UserProvision() {
        // prevent initialization
    }

    public static UserProvision inst() {
        return instance;
    }

    /**
     * Gets the information of the current logged in user.
     */
    public UserInfo getCurrentUser(UserInfoCookie uic) {
        UserInfo user = getCurrentLoggedInUser(uic);

        if (user == null) {
            return null;
        }

        UUID accountId = user.accountId;
        Account account = accountsLogic.getAccount(accountId);

        user.isAdmin = Config.getAppAdmins().contains(accountId.toString());
        user.isInstructor = !account.getInstructors().isEmpty();
        user.isStudent = !account.getStudents().isEmpty();
        user.isMaintainer = Config.getAppMaintainers().contains(accountId.toString());
        return user;
    }

    /**
     * Gets the current logged in user.
     */
    UserInfo getCurrentLoggedInUser(UserInfoCookie uic) {
        if (uic == null || !uic.isValid()) {
            return null;
        }

        return new UserInfo(uic.getAccountId());
    }

    /**
     * Gets the information of the current masqueraded user.
     */
    public UserInfo getMasqueradeUser(UUID accountId) {
        UserInfo userInfo = new UserInfo(accountId);
        Account account = accountsLogic.getAccount(accountId);
        userInfo.isAdmin = false;
        userInfo.isInstructor = !account.getInstructors().isEmpty();
        userInfo.isStudent = !account.getStudents().isEmpty();
        userInfo.isMaintainer = Config.getAppMaintainers().contains(accountId.toString());
        return userInfo;
    }

    /**
     * Gets the information of a user who has administrator role only.
     */
    public UserInfo getAdminOnlyUser(UUID accountId) {
        UserInfo userInfo = new UserInfo(accountId);
        userInfo.isAdmin = true;
        return userInfo;
    }

    /**
     * User principal for verified cron/worker requests: not a human app admin; {@link UserInfo#isAutomatedService} only.
     */
    public UserInfo getAutomatedServiceUser(String serviceId) {
        UserInfo userInfo = new UserInfo(serviceId);
        userInfo.isAutomatedService = true;
        return userInfo;
    }

}
