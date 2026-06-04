package teammates.ui.webapi;

import teammates.common.datatransfer.Provider;
import teammates.common.util.Const;
import teammates.storage.entity.Account;
import teammates.ui.exception.EntityNotFoundException;
import teammates.ui.output.AccountData;

/**
 * Gets account's information.
 */
public class GetAccountAction extends AdminOnlyAction {

    @Override
    public JsonResult execute() {
        String provider = getNonNullRequestParamValue(Const.ParamsNames.PROVIDER);
        String subject = getNonNullRequestParamValue(Const.ParamsNames.SUBJECT);
        String tenantId = getRequestParamValue(Const.ParamsNames.TENANT_ID);

        Provider providerEnum = getProviderFromRequest(provider);

        Account account = logic.getAccountByOidcClaims(providerEnum, subject, tenantId);

        if (account == null) {
            throw new EntityNotFoundException("Account does not exist.");
        }

        AccountData output = new AccountData(account);
        return new JsonResult(output);
    }

}
