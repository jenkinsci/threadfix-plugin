package me.automationdomination.plugins.threadfix.util;

import java.util.ResourceBundle;

/**
 * Created with IntelliJ IDEA.
 * User: bspruth
 * Date: 5/22/14
 * Time: 5:00 PM
 * Class provides access to the Messages {@link ResourceBundle} and hides initialization of the
 * properties file.
 */

public enum LocalMessages {

    PROJECTACTION_DISPLAYNAME("ThreadfixProjectAction.DisplayName"),
    BUILDACTION_DISPLAYNAME("ThreadfixBuildAction.DisplayName"),
    PUBLISHER_DISPLAYNAME("ThreadfixResultsPublisher.DisplayName"),
    REPORT_DISPLAYNAME("ThreadfixReport.DisplayName");

    private final static ResourceBundle MESSAGES = ResourceBundle.getBundle("me.automationdomination.plugins.threadfix. Messages");
    private final String msgRef;


    private LocalMessages(final String msgReference) {
        msgRef = msgReference;
    }

    @Override
    public String toString() {
        return MESSAGES.getString(msgRef);
    }
}
