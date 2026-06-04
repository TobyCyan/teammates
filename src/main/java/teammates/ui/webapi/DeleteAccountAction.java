package teammates.ui.webapi;

import teammates.common.datatransfer.Provider;
import teammates.common.util.Const;

/**
 * Action: deletes an existing account (either student or instructor).
 */
public class DeleteAccountAction extends AdminOnlyAction {

    @Override
    public JsonResult execute() {
        String provider = getNonNullRequestParamValue(Const.ParamsNames.PROVIDER);
        String subject = getNonNullRequestParamValue(Const.ParamsNames.SUBJECT);
        String tenantId = getRequestParamValue(Const.ParamsNames.TENANT_ID);

        logic.deleteAccountCascade(Provider.valueOf(provider), subject, tenantId);

        return new JsonResult("Account is successfully deleted.");
    }

}
