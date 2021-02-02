package er;

import java.util.prefs.Preferences;

class PGProperties {
    private String baseURL;
    private String baseUser;
    private String basePassword;
    private int baseExchangeTime;

    private String intraURL;
    private String intraUser;
    private String intraPassword;
    private int intraExchangeTime;

    private int maxNewAlarmPerCycle;
    private final Preferences userPrefs;

    private String emailLogin;
    private String emailPassword;
    private String subjectOfAlarmMessage;
    private String emailOfAlarmRecipient;

    private int filtrationTimeMillis;
    private long lastAlarmId;


    PGProperties() {
        userPrefs = Preferences.userRoot().node("dgw");
        baseURL = userPrefs.get("base_url", "jdbc:sqlserver://10.3.0.2\\DESIGO;DatabaseName=DIV23!PRJ=EXPO!DB=ISHTALM");
        baseUser = userPrefs.get("base_user", null);
        basePassword = userPrefs.get("base_password", null);
        baseExchangeTime = userPrefs.getInt("base_exchange_time", 10000);

        intraURL = userPrefs.get("intra_url", "https://intraservice.exporesource.ru/api/");
        intraUser = userPrefs.get("intra_user", null);
        intraPassword = userPrefs.get("intra_password", null);
        intraExchangeTime = userPrefs.getInt("intra_exchange_time", 10000);

        maxNewAlarmPerCycle = userPrefs.getInt("max_new_alarm_per_time", 3);

        emailLogin = userPrefs.get("email_login", null);
        emailPassword = userPrefs.get("email_password", null);

        subjectOfAlarmMessage = userPrefs.get("subject_of_alarm_message", null);
        emailOfAlarmRecipient = userPrefs.get("email_of_alarm_recipient", null);

        filtrationTimeMillis = userPrefs.getInt("filtration_time_millis", 5);
        lastAlarmId = userPrefs.getLong("last_alarm_id", 0);
    }

    String getBaseURL() {
        return baseURL;
    }

    void setBaseURL(String baseURL) {
        this.baseURL = baseURL;
    }

    String getBaseUser() {
        return baseUser;
    }

    void setBaseUser(String baseUser) {
        this.baseUser = baseUser;
    }

    String getBasePassword() {
        return basePassword;
    }

    void setBasePassword(String basePassword) {
        this.basePassword = basePassword;
    }

    int getBaseExchangeTime() {
        return baseExchangeTime;
    }

    void setBaseExchangeTime(int baseExchangeTime) {
        this.baseExchangeTime = baseExchangeTime;
    }

    void setIntraExchangeTime(int intraExchangeTime) {
        this.intraExchangeTime = intraExchangeTime;
    }

    String getIntraUser() {
        return intraUser;
    }

    void setIntraUser(String intraUser) {
        this.intraUser = intraUser;
    }

    String getIntraPassword() {
        return intraPassword;
    }

    void setIntraPassword(String intraPassword) {
        this.intraPassword = intraPassword;
    }

    String getIntraURL() {
        return intraURL;
    }

    void setIntraURL(String intraURL) {
        this.intraURL = intraURL;
    }

    int getMaxNewAlarmPerCycle() {
        return maxNewAlarmPerCycle;
    }

    void setMaxNewAlarmPerCycle(int maxNewAlarmPerCycle) {
        this.maxNewAlarmPerCycle = maxNewAlarmPerCycle;
    }

    String getEmailLogin() {
        return emailLogin;
    }

    void setEmailLogin(String emailLogin) {
        this.emailLogin = emailLogin;
    }

    String getEmailPassword() {
        return emailPassword;
    }

    void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    String getSubjectOfAlarmMessage() {
        return subjectOfAlarmMessage;
    }

    void setSubjectOfAlarmMessage(String subjectOfAlarmMessage) {
        this.subjectOfAlarmMessage = subjectOfAlarmMessage;
    }

    String getEmailOfAlarmRecipient() {
        return emailOfAlarmRecipient;
    }

    void setEmailOfAlarmRecipient(String emailOfAlarmRecipient) {
        this.emailOfAlarmRecipient = emailOfAlarmRecipient;
    }

    int getFiltrationTimeMillis() {
        return filtrationTimeMillis;
    }

    void setFiltrationTimeMillis(int filtrationTimeMillis) {
        this.filtrationTimeMillis = filtrationTimeMillis;
    }

    public long getLastAlarmId() {
        return lastAlarmId;
    }

    public void setLastAlarmId(long lastAlarmId) {
        this.lastAlarmId = lastAlarmId;
    }

    void updatePreferences() {
        if (baseURL != null) {
            userPrefs.put("base_url", baseURL);
        }
        if (baseUser != null) {
            userPrefs.put("base_user", baseUser);
        }
        if (basePassword != null) {
            userPrefs.put("base_password", basePassword);
        }
        userPrefs.putInt("base_exchange_time", baseExchangeTime);
        if (intraURL != null) {
            userPrefs.put("intra_url", intraURL);
        }
        if (intraUser != null) {
            userPrefs.put("intra_user", intraUser);
        }
        if (intraPassword != null) {
            userPrefs.put("intra_password", intraPassword);
        }
        if (emailLogin != null) {
            userPrefs.put("email_login", emailLogin);
        }
        if (emailPassword != null) {
            userPrefs.put("email_password", emailPassword);
        }
        if (subjectOfAlarmMessage != null) {
            userPrefs.put("subject_of_alarm_message", subjectOfAlarmMessage);
        }
        if (emailOfAlarmRecipient != null) {
            userPrefs.put("email_of_alarm_recipient", emailOfAlarmRecipient);
        }
        userPrefs.putInt("intra_exchange_time", intraExchangeTime);
        userPrefs.putInt("max_new_alarm_per_time", maxNewAlarmPerCycle);
        userPrefs.putInt("filtration_time_millis", filtrationTimeMillis);
        if(lastAlarmId >= 0){
            userPrefs.putLong("last_alarm_id", lastAlarmId);
        }
    }
}
