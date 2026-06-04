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
        Provider provider = Provider.valueOf(getNonNullRequestParamValue(Const.ParamsNames.PROVIDER));
        String subject = getNonNullRequestParamValue(Const.ParamsNames.SUBJECT);
        String tenantId = getNonNullRequestParamValue(Const.ParamsNames.TENANT_ID);

        Account account = logic.getAccountByOidcClaims(provider, subject, tenantId);

        if (account == null) {
            throw new EntityNotFoundException("Account does not exist.");
        }

        AccountData output = new AccountData(account);
        return new JsonResult(output);
    }

}
