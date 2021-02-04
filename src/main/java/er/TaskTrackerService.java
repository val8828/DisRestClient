package er;

import org.apache.commons.codec.binary.Base64;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

class TaskTrackerService {
    private final PGProperties properties;
    private static final Logger logger = LoggerFactory.getLogger(Class.class);

    public TaskTrackerService(PGProperties properties) {
        this.properties = properties;
        HttpsURLConnection.setDefaultHostnameVerifier(new NullHostnameVerifier());
    }

    /**
     * Creating new task by alarm entry
     * before send new create task request - requesting default task parameters
     *
     * @param alarmEntity alarm entry
     */
    public Long createNewTask(AlarmEntity alarmEntity) {
        String WebURL = properties.getIntraURL() + "task";
        JSONObject jsonParams = prepareJSONParameters(alarmEntity);
        HttpsURLConnection con = openPostConnection(WebURL);
        if (jsonParams != null && con != null) {
            try (OutputStream os = con.getOutputStream()) {
                byte[] input = jsonParams.toJSONString().getBytes(StandardCharsets.UTF_8);
                os.write(input);
                int status = con.getResponseCode();
                if (status >= 200 && status < 206) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    con.disconnect();
                    JSONParser parser = new JSONParser();
                    JSONObject responseObject = (JSONObject) parser.parse(content.toString());
                    if (responseObject.containsKey("Task")) {
                        JSONObject responseTaskJSON = (JSONObject) responseObject.get("Task");
                        if (responseTaskJSON.containsKey("Id")) {
                            return (Long) responseTaskJSON.get("Id");
                        }else{
                            logger.error("Alarm Entry " + alarmEntity.getAlarmEntryId() + " not created. ");
                        }
                    }else{
                        logger.error("Alarm Entry " + alarmEntity.getAlarmEntryId() + " not created. ");
                    }
                } else {
                    logger.error("Alarm Entry " + alarmEntity.getAlarmEntryId() + " not created. " +
                            "server returned status: " + status);
                }
            } catch (IOException | ParseException e) {
                logger.error(e.toString());
            }
        }
        return null;
    }

    private static class NullHostnameVerifier implements HostnameVerifier {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    }

    public Map<Integer, Long> readOpenedTasks() {
        String WebURL = properties.getIntraURL() + "task?serviceid=53&tasktypeid=1028&pagesize=1000&StatusIds=27,31";
        HttpsURLConnection con = openGetConnection(WebURL);
        if (con != null) {
            try {
                int status = con.getResponseCode();
                if (status >= 200 && status < 206) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();
                    con.disconnect();

                    Map<Integer, Long> resultTaskMap = new HashMap<>();
                    JSONObject rootResponseObject;
                    rootResponseObject = (JSONObject) new JSONParser().parse(content.toString());
                    if (rootResponseObject.containsKey("Tasks")) {
                        JSONArray taskList = (JSONArray) rootResponseObject.get("Tasks");
                        for (Object currentTask : taskList) {
                            resultTaskMap.putIfAbsent(extractObjectId(currentTask), extractTaskId(currentTask));
                        }
                        return resultTaskMap;
                    } else {
                        logger.error("Error getting tasks from task tracker. No Task tag");
                    }
                } else {
                    logger.error("Error getting tasks from task tracker");
                }
            } catch (IOException | ParseException e) {
                logger.error(e.toString());
            }
        } else {
            logger.error("Error opening connection for url: " + WebURL);
        }
        return null;
    }

    private int extractObjectId(Object task) {
        if (((JSONObject) task).containsKey("Description")) {
            String description = ((JSONObject) task).get("Description").toString();
            String objectId = description.substring(description.indexOf("\n ") + 2);
            return Integer.parseInt(objectId);
        }
        return 0;
    }

    private Long extractTaskId(Object task) {
        if(((JSONObject)task).containsKey("Id")){
            String id = ((JSONObject)task).get("Id").toString();
            return Long.parseLong(id);
        }
        return 0L;
    }
    /*
    private Long extractTaskId(Object task) {
        if (((JSONObject) task).containsKey("Task")) {

                JSONObject responseTaskJSON = ((JSONObject) task).get("Task");
                if (responseTaskJSON.containsKey("Id")) {
                    return  (Long) responseTaskJSON.get("Id");
                } else {
                    logger.error("Wrong response when extracting taskId. Response did not contain tag ID");
                }
            } else {
                logger.error("rong response when extracting taskId. Response did not contain tag TASK");
            }
        } catch (ParseException e) {
            logger.error("rong response when extracting taskId.Wrong JSON format");
        }
        return null;
    }
*/
    private HttpsURLConnection openPostConnection(String WebURL) {
        try {
            URL url = new URL(WebURL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.addRequestProperty("Authorization", getAuthHeaderValue());
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            return con;
        } catch (MalformedURLException e) {
            logger.error("Wrong url for post connection " + WebURL);
        } catch (IOException e) {
            logger.error("Error opening connection for " + WebURL);
        }
        return null;
    }

    private HttpsURLConnection openGetConnection(String WebURL) {
        try {
            URL url = new URL(WebURL);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json; utf-8");
            con.setRequestProperty("Accept", "application/json");
            con.addRequestProperty("Authorization", getAuthHeaderValue());
            con.setDoOutput(true);
            con.setDoInput(true);
            con.setUseCaches(false);
            return con;
        } catch (MalformedURLException e) {
            logger.error("Wrong url for get connection " + WebURL);
        } catch (IOException e) {
            logger.error("Error opening get connection for " + WebURL);
        }
        return null;
    }

    private String getAuthHeaderValue() {
        String auth = properties.getIntraUser() + ":" + properties.getIntraPassword();
        byte[] encodedAuth = Base64.encodeBase64(auth.getBytes(StandardCharsets.UTF_8));
        return "Basic " + new String(encodedAuth);
    }

    private JSONObject prepareJSONParameters(AlarmEntity alarmEntity) {
        JSONObject currentJSONParams = readDefaultJSONData();//request default task parameters
        if (currentJSONParams != null) {
            if (alarmEntity.getPriorityNumber() == 0) {
                currentJSONParams.replace("PriorityId", 14);
                currentJSONParams.replace("PriorityName", "КРАСНЫЙ ( СРОЧНО ТРЕБУЕТ ИСПОЛНЕНИЯ) ");
            } else {
                currentJSONParams.replace("PriorityId", 16);
                currentJSONParams.replace("PriorityName", "Синий ( свыше 24 часов) ");
            }
            int countOfAdditionText = alarmEntity.getTechnicalDescription().indexOf("'");
            String resultDescriptionText;
            if (countOfAdditionText != -1) {
                resultDescriptionText = "Авария ДИС: " +
                        alarmEntity.getTechnicalDescription().substring(0, countOfAdditionText) + " : " +
                        alarmEntity.getObjectDescription();
            } else {
                resultDescriptionText = "Авария ДИС: " +
                        alarmEntity.getObjectDescription();
            }
            currentJSONParams.replace("Name", resultDescriptionText);
            currentJSONParams.replace("Description", alarmEntity.getTechnicalDescription() + " \n " + alarmEntity.getObjectId());
            currentJSONParams.put("Comment", alarmEntity.getObjectId());
            return currentJSONParams;
        } else {
            return null;
        }
    }

    private JSONObject readDefaultJSONData() {
        String WebURL = properties.getIntraURL() + "newtask?serviceid=53&tasktypeid=1028";
        HttpsURLConnection con = openGetConnection(WebURL);
        if (con != null) {
            try {
                int status = con.getResponseCode();
                if (status == HttpsURLConnection.HTTP_OK) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                    String inputLine;
                    StringBuilder content = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        content.append(inputLine);
                    }
                    in.close();

                    JSONObject rootResponseObject = (JSONObject) new JSONParser().parse(content.toString());
                    if (rootResponseObject.containsKey("Task")) {
                        return (JSONObject) rootResponseObject.get("Task");
                    } else {
                        logger.error("Error getting task template, template did not contain task tag");
                    }
                } else {
                    logger.error("Error getting task template");
                }
                con.disconnect();
            } catch (IOException | ParseException e) {
                logger.error(e.toString());
                logger.error("Error getting task template");
            }
        }
        logger.error("Error getting task template");
        return null;
    }
}
