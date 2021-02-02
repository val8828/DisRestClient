package er;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.Instant;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

class DesigoSQLDAO {
    private final PGProperties prop;
    private Connection connection;
    private static final Logger logger = LoggerFactory.getLogger(Class.class);

    DesigoSQLDAO(PGProperties prop) {
        this.prop = prop;
    }

    public Connection getConnection() {
        try {
            if(connection == null || connection.isClosed()){
                Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
                connection = DriverManager.getConnection(
                        prop.getBaseURL(),
                        prop.getBaseUser(),
                        prop.getBasePassword());
            }
            return connection;
        } catch (SQLException | ClassNotFoundException e) {
            logger.error(e.toString());
        }
        return null;
    }

    public Map<Integer, AlarmEntity> getAlarmEntities() {
        connection = getConnection();
        if(connection != null){
            Map <Integer, AlarmEntity> resultMapOfEntity = new HashMap<>();
            String queryString = "SELECT TOP (1000) [AlarmEntryId]     \n" +
                    "      ,[ObjectId]\n" +
                    "      ,[TechnicalDescription]\n" +
                    "      ,[ObjectDescription]            \n" +
                    "      ,[PriorityNumber]      \n" +
                    "      ,[StateText]      \n" +
                    "  FROM [dbo].[Alarm]";
            try (Statement statement = connection.createStatement();
                 ResultSet rs = statement.executeQuery(queryString)
            ) {
                while (rs != null && rs.next()) {
                    if(rs.getString("StateText").contains("ревога")) {
                        AlarmEntity alarmEntity = new AlarmEntity(
                                rs.getInt("AlarmEntryId"),
                                rs.getInt("ObjectId"),
                                rs.getString("TechnicalDescription"),
                                rs.getString("ObjectDescription"),
                                rs.getInt("PriorityNumber"),
                                Instant.now());
                        resultMapOfEntity.putIfAbsent(alarmEntity.getObjectId(), alarmEntity);
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return resultMapOfEntity;
        }
        return null;
    }
}
