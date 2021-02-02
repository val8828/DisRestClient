package er;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.HttpsURLConnection;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

class ExchangeControl {
    private static final Logger logger = LoggerFactory.getLogger(Class.class);

    private PGProperties properties;
    private final Map<Integer, AlarmEntity>  processingEntities;//ObjectId:AlarmEntity
    private final DesigoSQLDAO desigoSQLDAO;
    private final TaskTrackerService taskTrackerService;
    private final Map<Integer, Long> activeTasks;//ObjectId:TaskId

    ExchangeControl(PGProperties prop, DesigoSQLDAO desigoSQLDAO, TaskTrackerService taskTrackerService) {
        this.taskTrackerService = taskTrackerService;
        this.properties = prop;
        processingEntities = new HashMap<>();
        activeTasks = taskTrackerService.readOpenedTasks();
        this.desigoSQLDAO = desigoSQLDAO;
    }

    void execute() {
        //Read new entities and remove not active
        updateProcessingEntitiesList();

        //Creating task if condition valid and remove from processing map
        processingEntities.entrySet().removeIf(alarmEntity ->{
            Instant timeOfTaskCreation = alarmEntity.getValue().getAlarmRegistrationTime().plusMillis(properties.getFiltrationTimeMillis());
            if (Instant.now().isAfter(timeOfTaskCreation)) {
                Long taskId = taskTrackerService.createNewTask(alarmEntity.getValue());
                if(taskId!=null){
                    activeTasks.put(alarmEntity.getValue().getObjectId(), taskId);
                }
                return true;
            }
            return false;
        });
        //updating task map
        Map<Integer, Long> allTask = taskTrackerService.readOpenedTasks();
        activeTasks.entrySet().removeIf(alarmEntity -> !allTask.containsKey(alarmEntity.getKey()));
    }

    private void updateProcessingEntitiesList() {
        Map<Integer, AlarmEntity> activeAlarms = desigoSQLDAO.getAlarmEntities();

        //remove from list if alarm not active any more
        processingEntities.entrySet().removeIf(e -> !activeAlarms.containsKey(e.getKey()));

        //Extracting new alarms from active
        Map<Integer, AlarmEntity> newAlarms = new HashMap<>();
        int maxIdOfNewAlarms = 0;
        for (AlarmEntity activeAlarm : activeAlarms.values()) {
            if (activeAlarm.getAlarmEntryId() > properties.getLastAlarmId()) {
                newAlarms.put(activeAlarm.getObjectId(), activeAlarm);
                if(activeAlarm.getAlarmEntryId() > maxIdOfNewAlarms) {
                    maxIdOfNewAlarms = activeAlarm.getAlarmEntryId();
                }
            }
        }

        //remove from list if alarm already exist in task tracker
        newAlarms.entrySet().removeIf(alarmEntity -> activeTasks.containsKey(alarmEntity.getKey()));

        //Filtration by text
        filtrationByText("ротеч", newAlarms);
        filtrationByText("ет связ", newAlarms);

        // Add new alarms if count less than MaxNewAlarmPerCycle
        if (newAlarms.size() < properties.getMaxNewAlarmPerCycle()) {
            processingEntities.putAll(newAlarms);
        }
        if(maxIdOfNewAlarms > 0)  properties.setLastAlarmId(maxIdOfNewAlarms);
    }

    private void filtrationByText(String searchingText, Map<Integer, AlarmEntity> alarmEntities) {
        alarmEntities.entrySet().removeIf(alarmEntity -> {
            if (alarmEntity.getValue().getObjectDescription() != null &&
                    alarmEntity.getValue().getTechnicalDescription() != null &&
                    (containsIgnoreCase(alarmEntity.getValue().getObjectDescription(), searchingText) ||
                            containsIgnoreCase(alarmEntity.getValue().getTechnicalDescription(), searchingText)||
                    alarmEntity.getValue().getObjectDescription().equals("") ||
                    alarmEntity.getValue().getTechnicalDescription().equals(""))) {
                logger.info("Alarm Entry Filtered by empty text. Alarmid : " + alarmEntity.getValue().getAlarmEntryId());
                return true;
            }
            return false;
        });
    }

    private static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) return false;

        final int length = searchStr.length();
        if (length == 0)
            return true;

        for (int i = str.length() - length; i >= 0; i--) {
            if (str.regionMatches(true, i, searchStr, 0, length))
                return true;
        }
        return false;
    }
}