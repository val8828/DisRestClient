package er;

import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.*;
import java.util.Properties;

/**
 * DGW is a gateway between sql base and intraservice tasktracker.
 * SQL base located on "DESIGO" server(default ip: 10.3.0.2)
 * DGW contains some parameters which may be changed from commandline.
 * To know list of all parameters type in cmd: "java -jar [path of DGW.jar] -h" .
 * DGW reading alarm list from sql base, filtrating it and create tasks in intraservice.
 * To change parameters just call in cmd program with new arguments, old parameters will be replaced.
 * Program create log file in the same directory and write errors and info message to it.
 * When program using first time - need to call with all login and passwords(from sql base and intraservice).
 */
public class DGW {
    private static final Logger logger = LoggerFactory.getLogger(DGW.class);

    public static void main(String[] args) {

        PGProperties properties = readProperties(args);

        if (!checkCompletePreferences(properties)) {
            System.out.println("InComplete preferences");
            return;
        }
        DesigoSQLDAO desigoSQLDAO = new DesigoSQLDAO(properties);
        TaskTrackerService taskTrackerService = new TaskTrackerService(properties);
        ExchangeControl exchangeControl = new ExchangeControl(properties, desigoSQLDAO, taskTrackerService);

        //noinspection InfiniteLoopStatement
        while (true) {
            exchangeControl.execute();
            try {
                Thread.sleep(properties.getBaseExchangeTime());
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            sendEmail(properties);
        }
    }

    private static void sendEmail(PGProperties properties) {
        String username = properties.getEmailLogin();
        String password = properties.getEmailPassword();
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "false");
        props.put("mail.smtp.host", "mail.exporesource.ru");
        props.put("mail.smtp.port", "25");

        Session session = Session.getInstance(props, new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });

        try {
            Message message = new MimeMessage(session);
            //от кого
            message.setFrom(new InternetAddress(username));
            //кому
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(properties.getEmailOfAlarmRecipient()));
            //Заголовок письма
            message.setSubject(properties.getSubjectOfAlarmMessage());
            //Содержимое
            BufferedReader reader = new BufferedReader(new FileReader("./log/log.log"));
            String messageText = reader.readLine();
            reader.close();
            //Очищаем
            BufferedWriter writer = new BufferedWriter(new FileWriter("./log/log.log"));
            writer.write("");
            writer.close();

            if (messageText!=null) {
                if (messageText.equalsIgnoreCase("")) return;
                message.setText(messageText);
                //Отправляем сообщение
                Transport.send(message);
            }
        } catch (IOException | MessagingException e) {
            logger.error("Error sending email");
        }
    }

    /**
     * Read properties from register, parse command line arguments and replacing old parameters.
     *
     * @param argv cmd arguments (String[])
     * @return targetProperties object - actual parameters of program
     */
    private static PGProperties readProperties(String[] argv) {

        PGProperties targetProperties = new PGProperties();

        if (argv != null) {

            parsePropertiesFromCLI(targetProperties, argv);

            targetProperties.updatePreferences();
        }

        return targetProperties;
    }

    /**
     * parse command line arguments and replacing it in PGProperties object.
     *
     * @param targetProperties PGProperties object with old parameters or empty
     * @param argv             cli arguments
     */
    private static void parsePropertiesFromCLI(PGProperties targetProperties, String[] argv) {
        Options options = new Options();

        Option baseURLOption = Option.builder("bu")
                .argName("baseurl")
                .hasArg()
                .desc("sql base url")
                .build();
        options.addOption(baseURLOption);

        Option baseUserOption = Option.builder("bs")
                .argName("baseuser")
                .hasArg()
                .desc("sql base user login")
                .build();
        options.addOption(baseUserOption);

        Option basePasswordOption = Option.builder("bw")
                .argName("basepassword")
                .hasArg()
                .desc("sql base user password")
                .build();
        options.addOption(basePasswordOption);

        Option baseExchangeTimeOption = Option.builder("bt")
                .argName("baseexchangetime")
                .hasArg()
                .desc("sql base exchange cycle time")
                .build();
        options.addOption(baseExchangeTimeOption);

        Option intraURLOption = Option.builder("iu")
                .argName("intraurl")
                .hasArg()
                .desc("intraservice url")
                .build();
        options.addOption(intraURLOption);

        Option intraUserOption = Option.builder("is")
                .argName("intrauser")
                .hasArg()
                .desc("intraservice user login")
                .build();
        options.addOption(intraUserOption);

        Option intraPasswordOption = Option.builder("iw")
                .argName("intrapassword")
                .hasArg()
                .desc("intraservice user password")
                .build();
        options.addOption(intraPasswordOption);

        Option intraExchangeTimeOption = Option.builder("it")
                .argName("intraexchangetime")
                .hasArg()
                .desc("intraservice exchange cycle time")
                .build();
        options.addOption(intraExchangeTimeOption);

        Option maxNewAlarmPerCycleOption = Option.builder("mc")
                .argName("maxnewalarmpercycle")
                .hasArg()
                .desc("permissible quantity of alarm per cycle")
                .build();
        options.addOption(maxNewAlarmPerCycleOption);

        Option emailLoginOption = Option.builder("el")
                .argName("emaillogin")
                .hasArg()
                .desc("email login to send info message")
                .build();
        options.addOption(emailLoginOption);

        Option emailPasswordOption = Option.builder("ep")
                .argName("emailpassword")
                .hasArg()
                .desc("email password to send info message")
                .build();
        options.addOption(emailPasswordOption);

        Option subjectOfAlarmMessageOption = Option.builder("sm")
                .argName("subjectOfAlarmMessage")
                .hasArg()
                .desc("subject Of Alarm Message")
                .build();
        options.addOption(subjectOfAlarmMessageOption);

        Option emailOfAlarmRecipientOption = Option.builder("ea")
                .argName("emailOfAlarmRecipient")
                .hasArg()
                .desc("email Of Alarm Recipient")
                .build();
        options.addOption(emailOfAlarmRecipientOption);

        Option filtrationTimeMillisOption = Option.builder("ft")
                .argName("filtrationTimeMillis")
                .hasArg()
                .desc("time of filtration in millis")
                .build();
        options.addOption(filtrationTimeMillisOption);

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = new HelpFormatter();
        CommandLine cmd = null;

        try {
            cmd = parser.parse(options, argv);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            formatter.printHelp("utility-name", options);
            System.exit(1);
        }
        if (cmd.hasOption("bu")) {
            targetProperties.setBaseURL(cmd.getOptionValue("bu"));
        }
        if (cmd.hasOption("bs")) {
            targetProperties.setBaseUser(cmd.getOptionValue("bs"));
        }
        if (cmd.hasOption("bw")) {
            targetProperties.setBasePassword(cmd.getOptionValue("bw"));
        }
        if (cmd.hasOption("bt")) {
            targetProperties.setBaseExchangeTime(Integer.parseInt(cmd.getOptionValue("bt")));
        }
        if (cmd.hasOption("iu")) {
            targetProperties.setIntraURL(cmd.getOptionValue("iu"));
        }
        if (cmd.hasOption("is")) {
            targetProperties.setIntraUser(cmd.getOptionValue("is"));
        }
        if (cmd.hasOption("iw")) {
            targetProperties.setIntraPassword(cmd.getOptionValue("iw"));
        }
        if (cmd.hasOption("it")) {
            targetProperties.setIntraExchangeTime(Integer.parseInt(cmd.getOptionValue("it")));
        }
        if (cmd.hasOption("mc")) {
            targetProperties.setMaxNewAlarmPerCycle(Integer.parseInt(cmd.getOptionValue("mc")));
        }
        if (cmd.hasOption("el")) {
            targetProperties.setEmailLogin(cmd.getOptionValue("el"));
        }
        if (cmd.hasOption("ep")) {
            targetProperties.setEmailPassword(cmd.getOptionValue("ep"));
        }
        if (cmd.hasOption("sm")) {
            targetProperties.setSubjectOfAlarmMessage(cmd.getOptionValue("sm"));
        }
        if (cmd.hasOption("ea")) {
            targetProperties.setEmailOfAlarmRecipient(cmd.getOptionValue("ea"));
        }
        if (cmd.hasOption("ft")) {
            targetProperties.setFiltrationTimeMillis(Integer.parseInt(cmd.getOptionValue("ft")));
        }

    }

    /**
     * check not null parameters in PGProperties object
     * Return true if parameters is complete, false otherwise
     *
     * @param prop PGProperties object
     * @return boolean result
     */
    private static boolean checkCompletePreferences(PGProperties prop) {
        return prop.getBaseURL() != null &&
                prop.getBaseUser() != null &&
                prop.getBasePassword() != null &&
                prop.getIntraURL() != null &&
                prop.getIntraPassword() != null &&
                prop.getIntraUser() != null &&
                prop.getEmailLogin() != null &&
                prop.getEmailPassword() != null &&
                prop.getEmailOfAlarmRecipient() != null &&
                prop.getSubjectOfAlarmMessage() != null;
    }
}
